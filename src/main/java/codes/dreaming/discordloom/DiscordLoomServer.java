package codes.dreaming.discordloom;

import codes.dreaming.discordloom.command.DiscordLoomCommand;
import codes.dreaming.discordloom.config.server.ServerConfig;
import codes.dreaming.discordloom.discord.ServerDiscordManager;
import codes.dreaming.discordloom.impl.BanImpl;
import com.mojang.authlib.GameProfile;
import eu.pb4.banhammer.api.PunishmentType;
import eu.pb4.banhammer.impl.BanHammerImpl;
import net.dv8tion.jda.api.entities.Guild;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;

import java.util.List;

import static codes.dreaming.discordloom.DiscordLoom.*;

public class DiscordLoomServer implements DedicatedServerModInitializer {
    public static final ServerConfig SERVER_CONFIG = ServerConfig.createAndLoad();
    public static final String LuckPermsMetadataKey = MOD_ID + ":discordid";

    public static ServerDiscordManager DISCORD_MANAGER;

    public static PlayerManager PLAYER_MANAGER;

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register(DiscordLoomServer::serverStarted);

        CommandRegistrationCallback.EVENT.register(((dispatcher, registry, selection) -> dispatcher.register(DiscordLoomCommand.command())));
    }

    public static void serverStarted(MinecraftServer server) {
        if (server.isDedicated()) {
            PLAYER_MANAGER = server.getPlayerManager();
            DISCORD_MANAGER = new ServerDiscordManager();
            List<Guild> missingGuilds = DISCORD_MANAGER.getMissingGuilds();
            if (!missingGuilds.isEmpty()) {
                missingGuilds.forEach(guild -> LOGGER.error("Bot is not in required guild: " + guild.getName()));
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
}
