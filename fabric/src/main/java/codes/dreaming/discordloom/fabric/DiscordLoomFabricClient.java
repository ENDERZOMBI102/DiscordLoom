package codes.dreaming.discordloom.fabric;

import codes.dreaming.discordloom.DiscordLoom;
import net.fabricmc.api.ClientModInitializer;

public class DiscordLoomFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DiscordLoom.initClient();
    }
}
