package codes.dreaming.discordloom;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DiscordLoom
{
	public static final String MOD_ID = "discordloom";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier LINK_PACKET = new Identifier(MOD_ID, "link");

	public static void init() {

	}

	public static void initClient() {
		LOGGER.info("Initializing DiscordLoom");
		NetworkManager.registerReceiver(NetworkManager.serverToClient(), LINK_PACKET, (buf, ctx) -> {
			LOGGER.info("Received link packet");
		});
	}
}