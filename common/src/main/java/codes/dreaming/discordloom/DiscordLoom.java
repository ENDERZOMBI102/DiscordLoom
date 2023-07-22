package codes.dreaming.discordloom;

import codes.dreaming.discordloom.config.ForgeConfigHelper;
import codes.dreaming.discordloom.config.server.Config;
import com.google.common.base.Suppliers;
import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;


public class DiscordLoom {
    public static final String MOD_ID = "discordloom";

    public static final String LuckPermsMetadataKey = MOD_ID + ":discordid";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier LINK_PACKET = new Identifier(MOD_ID, "link");

    @Environment(EnvType.SERVER)
    public static Supplier<LuckPerms> LUCK_PERMS = Suppliers.memoize(LuckPermsProvider::get);

    public static ServerDiscordManager DISCORD_MANAGER;

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

        //Spawn a new thread to wait for config to be loaded
        new Thread(() -> {
            while (!Config.SPEC.isLoaded()) {
                try {
                    LOGGER.info("Waiting for config to be loaded");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            LOGGER.info("Config loaded");
            DISCORD_MANAGER = new ServerDiscordManager();
        }).start();
    }
}