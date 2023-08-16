package codes.dreaming.discordloom.fabric;

import codes.dreaming.discordloom.DiscordLoom;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class DiscordLoomFabricServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        DiscordLoom.initServer();
        ServerLifecycleEvents.SERVER_STARTED.register(DiscordLoom::serverStarted);
    }
}
