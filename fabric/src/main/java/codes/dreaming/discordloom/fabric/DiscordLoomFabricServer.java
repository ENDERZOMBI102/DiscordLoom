package codes.dreaming.discordloom.fabric;

import codes.dreaming.discordloom.DiscordLoom;
import net.fabricmc.api.DedicatedServerModInitializer;

public class DiscordLoomFabricServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        DiscordLoom.initServer();
    }
}
