package codes.dreaming.discordloom;

import net.fabricmc.api.*;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DiscordLoom implements ModInitializer {
    public static final String MOD_ID = "discordloom";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier LINK_PACKET = new Identifier(MOD_ID, "link");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing DiscordLoom");
    }
}