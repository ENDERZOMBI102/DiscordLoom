package codes.dreaming.discordloom;

import codes.dreaming.discordloom.command.DiscordLoomCommand;
import codes.dreaming.discordloom.config.server.ServerConfig;
import codes.dreaming.discordloom.discord.ServerDiscordManager;
import codes.dreaming.discordloom.impl.BanImpl;
import codes.dreaming.discordloom.mixin.ServerLoginNetworkHandlerAccessor;
import com.mojang.authlib.GameProfile;
import eu.pb4.banhammer.api.PunishmentType;
import eu.pb4.banhammer.impl.BanHammerImpl;
import net.dv8tion.jda.api.entities.Guild;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static codes.dreaming.discordloom.DiscordLoom.*;
import static codes.dreaming.discordloom.discord.ServerDiscordManager.buildNodeMatcherWithDiscordId;

public class DiscordLoomServer implements DedicatedServerModInitializer {
    public static final ServerConfig SERVER_CONFIG = ServerConfig.createAndLoad();
    public static final String LuckPermsMetadataKey = MOD_ID + ":discordid";

    public static ServerDiscordManager DISCORD_MANAGER;

    public static PlayerManager PLAYER_MANAGER;

	@Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register(DiscordLoomServer::serverStarted);

		ServerLoginConnectionEvents.QUERY_START.register( DiscordLoomServer::onLoginStart );
		ServerLoginNetworking.registerGlobalReceiver(QUERY_PACKET_ID, DiscordLoomServer::onQueryResponse);

        CommandRegistrationCallback.EVENT.register(((dispatcher, registry, selection) -> dispatcher.register(DiscordLoomCommand.command())));
    }

	public static void serverStarted(MinecraftServer server) {
        if (server.isDedicated()) {
            PLAYER_MANAGER = server.getPlayerManager();
            DISCORD_MANAGER = new ServerDiscordManager();
            List<Guild> missingGuilds = DISCORD_MANAGER.getMissingGuilds();
            if (!missingGuilds.isEmpty()) {
                missingGuilds.forEach(guild -> LOGGER.error("Bot is not in required guild: {}", guild.getName()));
                throw new CrashException(new CrashReport("Bot is not in all required guilds", new Exception()));
            }

            if (SERVER_CONFIG.banDiscordAccount() && FabricLoader.getInstance().isModLoaded("banhammer")) {
                LOGGER.info("BanHammer detected");
                BanHammerImpl.PUNISHMENT_EVENT.register(((punishmentData, b, b1) -> {
                    if (punishmentData.type != PunishmentType.BAN && punishmentData.type != PunishmentType.IP_BAN) {
                        return;
                    }
                    BanImpl.ban(List.of(new GameProfile(punishmentData.playerUUID, punishmentData.playerName)), punishmentData.reason, punishmentData.adminDisplayName.getString());
                }));
            }
        }
    }

	private static void onLoginStart( ServerLoginNetworkHandler handler, MinecraftServer server, PacketSender sender, ServerLoginNetworking.LoginSynchronizer synchronizer ) {
		sender.sendPacket(QUERY_PACKET_ID, PacketByteBufs.empty());
	}

	private static void onQueryResponse(MinecraftServer server, ServerLoginNetworkHandler handler, boolean understood, PacketByteBuf buf, ServerLoginNetworking.LoginSynchronizer synchronizer, PacketSender responseSender) {
		if (!understood)
			return;

		var profile = ((ServerLoginNetworkHandlerAccessor) handler).getProfile();

		if (profile == null) {
			LOGGER.error("Profile is null!");
			var text = Text.of("There was an error while trying to fetch your profile, please try again later.");

			handler.disconnect(text);
			return;
		}

		var luckUser = LuckPermsProvider.get().getUserManager().getUser(profile.getId());


		if (luckUser == null) {
			LOGGER.error("User not found in LuckPerms!");
			var text = Text.of("There was an error while trying to fetch your LuckPerms user, please try again later.");

			handler.disconnect( text );
			return;
		}

		var idNode = luckUser.getNodes(NodeType.META)
			.stream()
			.filter( node -> node.getMetaKey().equals(LuckPermsMetadataKey))
			.findAny()
			.orElse(null);

		var code = buf.readOptional(PacketByteBuf::readString).orElse(null);

		if (idNode == null) {
			if (code == null) {
				LOGGER.trace("A user without a discordloom.id node tried to join!");
				var outBuf = PacketByteBufs.create().writeString(DISCORD_MANAGER.generateDiscordOauthUri());
				handler.getConnection().send(ServerPlayNetworking.createS2CPacket(LINK_PACKET, outBuf));
				var text = Text.of("If you're seeing this, it means that you haven't installed the DiscordLoom mod. Please install it and try again.");
				handler.disconnect(text);
				return;
			} else {
				LOGGER.trace("Received code: {}", code);
				var userId = DISCORD_MANAGER.doDiscordLink(code);

				if (!SERVER_CONFIG.allowMultipleMinecraftAccountsPerDiscordAccount()) {
					var uuids = ServerDiscordManager.getPlayersFromDiscordId(userId);
					if (!uuids.isEmpty()) {
						var uuid = uuids.stream().findFirst().get();
						var user = LuckPermsProvider.get().getUserManager().getUser(uuids.stream().findFirst().orElseThrow());

						String username;

						if (user != null) {
							username = user.getUsername();
						} else {
							username = uuid + " (unknown)";
						}

						var text = Text.of("This Discord account is already linked to " + username + " Minecraft account!");

						handler.disconnect(text);
						return;
					}
				}

				idNode = buildNodeMatcherWithDiscordId(userId);
				luckUser.data().add(idNode);
			}
		}

		var discordUser = DISCORD_MANAGER.getDiscordUserFromId(idNode.getMetaValue());

		if (discordUser == null) {
			LOGGER.error("Discord user not found!");
			var text = Text.of("There was an error while trying to fetch your Discord user, please try again later.");
			handler.disconnect(text);
			return;
		}

		Optional<? extends String> nonMatchGuildOptional = SERVER_CONFIG.checkForGuildsOnJoin()
			.stream()
			.filter(guildId -> {
				var guild = DISCORD_MANAGER.getJdaApi().getGuildById(guildId);
				if (guild == null)
					return false;

				return guild
				   .retrieveMemberById(discordUser.getId())
				   .onErrorMap((throwable)->null)
				   .complete() == null;
			})
			.findAny();

		if (nonMatchGuildOptional.isPresent()) {
			LOGGER.info("A user not in the required discord channel tried to join!");
			var text = Text.of("You are not in the required discord channel to join this server.");
			handler.disconnect(text);
			return;
		}

		var hasMandatoryVCChannel = SERVER_CONFIG.mandatoryVCChannels().isEmpty() || PermissionHelper.hasPermission(luckUser, MOD_ID + ".bypass_vc");

		if (!hasMandatoryVCChannel) {
			hasMandatoryVCChannel = SERVER_CONFIG
				.mandatoryVCChannels()
				.stream()
				.anyMatch(mandatoryVCChannel -> {
					var voiceChannel = DISCORD_MANAGER.getJdaApi().getVoiceChannelById(mandatoryVCChannel);
					if (voiceChannel == null)
						return false;

					return voiceChannel.getMembers()
						.stream()
						.anyMatch(member -> member.getId().equals(discordUser.getId()));
				});

			if (!hasMandatoryVCChannel) {
				LOGGER.info("User {} ({}) joined without being in a mandatory voice channel!", profile.getName(), profile.getId());
				var text = Text.of("You are not in a mandatory voice channel to join this server.");
				handler.disconnect(text);
				return;
			}
		}


		luckUser.getNodes()
			.stream()
			.filter(node -> node.getKey().startsWith("group." + MOD_ID + ":"))
			.forEach(node -> luckUser.data().remove(node));

		var mutualGuildsMap = discordUser.getMutualGuilds()
			.stream()
			.collect(Collectors.toMap(Guild::getId, Function.identity()));

		SERVER_CONFIG.syncDiscordRolesOnJoin()
			.stream()
			.map(guildRole -> guildRole.split(":"))
			.filter(guildRoleSplit -> mutualGuildsMap.containsKey(guildRoleSplit[0]))
			.forEach(guildRoleSplit -> {
				var guild = mutualGuildsMap.get(guildRoleSplit[0]);
				var member = guild.getMemberById(discordUser.getId());

				if (member != null) {
					member.getRoles()
						.stream()
						.filter(role -> role.getId().equals(guildRoleSplit[1]))
						.findAny()
						.ifPresent(role -> {
							LOGGER.info("User {} ({}) joined with {} role!", profile.getName(), profile.getId(), guildRoleSplit[1]);
							luckUser.data().add( Node.builder( "group." + MOD_ID + ":" + guildRoleSplit[1]).build());
						});
				}
			});

		LuckPermsProvider.get().getUserManager().saveUser(luckUser);

		LOGGER.info("User {} ({}) joined with a discordloom.id node! ({})", profile.getName(), profile.getId(), idNode.getMetaValue());
	}
}
