package codes.dreaming.discordloom;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.util.Identifier;

public class DiscordLoom
{
	public static final String MOD_ID = "discordloom";

	public static final LuckPerms LUCK_PERMS = LuckPermsProvider.get();

	public static final Identifier LINK_PACKET = new Identifier(MOD_ID, "link");

	@Environment(EnvType.CLIENT)
	public static void init() {
		NetworkManager.registerReceiver(NetworkManager.Side.S2C, LINK_PACKET, (buf, ctx) -> {
			System.out.println("Received link packet");
		});
	}
}