package codes.dreaming.discordloom.mixin.server;

import codes.dreaming.discordloom.PermissionHelper;
import codes.dreaming.discordloom.config.server.Config;
import codes.dreaming.discordloom.discord.ServerDiscordManager;
import codes.dreaming.discordloom.mixinInterfaces.LoginHelloC2SPacketAccessor;
import com.mojang.authlib.GameProfile;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static codes.dreaming.discordloom.DiscordLoom.*;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {

    @Shadow
    public abstract void disconnect(Text reason);

    @Shadow
    @Nullable GameProfile profile;

    @Shadow
    @Final
    public ClientConnection connection;

    @Inject(method = "onHello", at = @At("TAIL"))
    private void onHelloMixin(LoginHelloC2SPacket packet, CallbackInfo ci) {
        LoginHelloC2SPacketAccessor mixin = (LoginHelloC2SPacketAccessor) (Object) packet;

        //noinspection ConstantValue
        assert mixin != null;

        String code = mixin.discordloom$getCode();

        if (code == null) return;
        LOGGER.trace("Received code: " + code);
        String userId = DISCORD_MANAGER.doDicordLink(code);

        if (!Config.CONFIG.allowMultipleMinecraftAccountsPerDiscordAccount.get()) {
            Set<UUID> uuids = ServerDiscordManager.getPlayersFromDiscordId(userId);
            if (!uuids.isEmpty()) {
                UUID uuid = uuids.stream().findFirst().get();
                User user = LuckPermsProvider.get().getUserManager().getUser(uuids.stream().findFirst().get());

                String username;

                if (user != null) {
                    username = user.getUsername();
                } else {
                    username = uuid.toString() + " (unknown)";
                }

                Text text = Text.of("This Discord account is already linked to " + username + " Minecraft account!");

                this.connection.send(new LoginDisconnectS2CPacket(text));
                this.connection.disconnect(text);
                return;
            }
        }

        ServerDiscordManager.link(userId, packet.profileId().get());
    }

    @Inject(method = "acceptPlayer", at = @At(value = "RETURN", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", ordinal = 1), cancellable = true)
    private void checkCanJoin(CallbackInfo ci) {
        if (this.profile == null) {
            LOGGER.error("Profile is null!");
            this.disconnect(Text.of("There was an error while trying to fetch your profile, please try again later."));
            ci.cancel();
            return;
        }

        User luckUser = LuckPermsProvider.get().getUserManager().getUser(profile.getId());

        if (luckUser == null) {
            LOGGER.error("User not found in LuckPerms!");
            this.disconnect(Text.of("There was an error while trying to fetch your LuckPerms user, please try again later."));
            ci.cancel();
            return;
        }

        Optional<MetaNode> idNode = luckUser.getNodes(NodeType.META).stream().filter(node -> node.getMetaKey().equals(LuckPermsMetadataKey)).findAny();

        if (idNode.isEmpty()) {
            LOGGER.trace("A user without a discordloom.id node tried to join!");
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeString(DISCORD_MANAGER.generateDiscordOauthUri());
            NetworkManager.collectPackets(packet -> this.connection.send(packet), NetworkManager.serverToClient(), LINK_PACKET, buf);
            Text text = Text.of("If you're seeing this, it means that you haven't installed the DiscordLoom mod. Please install it and try again.");
            this.connection.send(new DisconnectS2CPacket(text));
            this.disconnect(text);
            ci.cancel();
            return;
        }

        net.dv8tion.jda.api.entities.User discordUser = DISCORD_MANAGER.getDiscordUserFromId(idNode.get().getMetaValue());

        if (discordUser == null) {
            LOGGER.error("Discord user not found!");
            Text text = Text.of("There was an error while trying to fetch your Discord user, please try again later.");
            this.connection.send(new DisconnectS2CPacket(text));
            this.disconnect(text);
            ci.cancel();
            return;
        }


        Optional<? extends String> nonMatchGuildOptional = Config.CONFIG.checkForGuildsOnJoin.get()
                .stream()
                .filter(guildId -> {
                    Guild guild = DISCORD_MANAGER.getJdaApi().getGuildById(guildId);
                    if (guild == null) return false;
                    return guild.retrieveMemberById(discordUser.getId()).complete() == null;
                })
                .findAny();

        if (nonMatchGuildOptional.isPresent()) {
            LOGGER.info("A user not in the required discord channel tried to join!");
            Text text = Text.of("You are not in the required discord channel to join this server.");
            this.connection.send(new DisconnectS2CPacket(text));
            this.disconnect(text);
            ci.cancel();
            return;
        }


        boolean hasMandatoryVCChannel = Config.CONFIG.mandatoryVCChannels.get().isEmpty() || PermissionHelper.hasPermission(luckUser, MOD_ID + ".bypass_vc");

        if (!hasMandatoryVCChannel) {

            hasMandatoryVCChannel = Config.CONFIG.mandatoryVCChannels.get()
                    .stream()
                    .anyMatch(mandatoryVCChannel ->
                    {
                        VoiceChannel voiceChannel = DISCORD_MANAGER.getJdaApi().getVoiceChannelById(mandatoryVCChannel);
                        if (voiceChannel == null) return false;
                        return voiceChannel.getMembers().stream().anyMatch(member -> member.getId().equals(discordUser.getId()));
                    });

            if (!hasMandatoryVCChannel) {
                LOGGER.info(String.format("User %s (%s) joined without being in a mandatory voice channel!", this.profile.getName(), this.profile.getId()));
                Text text = Text.of("You are not in a mandatory voice channel to join this server.");
                this.connection.send(new DisconnectS2CPacket(text));
                this.disconnect(text);
                ci.cancel();
                return;
            }
        }

        luckUser.getNodes().stream().filter(node -> node.getKey().startsWith("group." + MOD_ID + ":")).forEach(node -> luckUser.data().remove(node));

        Map<String, Guild> mutualGuildsMap = discordUser.getMutualGuilds().stream()
                .collect(Collectors.toMap(Guild::getId, Function.identity()));

        Config.CONFIG.syncDiscordRolesOnJoin.get().stream()
                .map(guildRole -> guildRole.split(":"))
                .filter(guildRoleSplit -> mutualGuildsMap.containsKey(guildRoleSplit[0]))
                .forEach(guildRoleSplit -> {
                    Guild guild = mutualGuildsMap.get(guildRoleSplit[0]);
                    Member member = guild.getMemberById(discordUser.getId());

                    if (member != null) {
                        member.getRoles().stream()
                                .filter(role -> role.getId().equals(guildRoleSplit[1]))
                                .findAny()
                                .ifPresent(role -> {
                                    LOGGER.info("User " + this.profile.getName() + " (" + this.profile.getId() + ") joined with " + guildRoleSplit[1] + " role!");
                                    luckUser.data().add(Node.builder("group." + MOD_ID + ":" + guildRoleSplit[1]).build());
                                });
                    }
                });

        LuckPermsProvider.get().getUserManager().saveUser(luckUser);

        LOGGER.info("User " + this.profile.getName() + " (" + this.profile.getId() + ") joined with a discordloom.id node! (" + idNode.get().getMetaValue() + ")");
    }
}
