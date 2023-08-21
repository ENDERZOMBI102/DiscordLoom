package codes.dreaming.discordloom;

import codes.dreaming.discordloom.command.DiscordLoomCommand;
import codes.dreaming.discordloom.config.ForgeConfigHelper;
import codes.dreaming.discordloom.config.server.Config;
import codes.dreaming.discordloom.discord.ServerDiscordManager;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.networking.NetworkManager;
import net.dv8tion.jda.api.entities.Guild;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class DiscordLoom {
    public static final String MOD_ID = "discordloom";

    public static final String LuckPermsMetadataKey = MOD_ID + ":discordid";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier LINK_PACKET = new Identifier(MOD_ID, "link");

    public static ServerDiscordManager DISCORD_MANAGER;

    public static PlayerManager PLAYER_MANAGER;

    public static void init() {
        LOGGER.info("Initializing DiscordLoom");
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        NetworkManager.registerReceiver(NetworkManager.serverToClient(), LINK_PACKET, (buf, ctx) -> {
            LOGGER.info("Received link packet from server");
            ClientLinkManager.setUrl(buf.readString());
        });
    }

    @Environment(EnvType.SERVER)
    public static void initServer() {
        ForgeConfigHelper.registerServerConfig(Config.SPEC);

        CommandRegistrationEvent.EVENT.register(((dispatcher, registry, selection) -> dispatcher.register(DiscordLoomCommand.command())));
    }

    @Environment(EnvType.SERVER)
    public static void serverStarted(MinecraftServer server) {
        if(server.isDedicated()) {
            PLAYER_MANAGER = server.getPlayerManager();
            DISCORD_MANAGER = new ServerDiscordManager();
            List<Guild> missingGuilds =  DISCORD_MANAGER.getMissingGuilds();
            if(!missingGuilds.isEmpty()) {
                missingGuilds.forEach(guild -> LOGGER.error("Bot is not in required guild: " + guild.getName()));
                throw new CrashException(new CrashReport("Bot is not in all required guilds", new Exception()));
            }
        }
    }
}