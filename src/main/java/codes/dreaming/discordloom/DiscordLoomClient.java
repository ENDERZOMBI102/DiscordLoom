package codes.dreaming.discordloom;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import static codes.dreaming.discordloom.DiscordLoom.*;

public class DiscordLoomClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(LINK_PACKET, (client, handler, buf, responSender) -> {
            LOGGER.info("Received link packet from server");
            ClientLinkManager.setUrl(buf.readString());
        });
    }
}
