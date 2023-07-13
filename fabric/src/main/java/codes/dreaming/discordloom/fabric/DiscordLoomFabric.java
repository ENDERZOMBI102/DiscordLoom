package codes.dreaming.discordloom.fabric;

import codes.dreaming.discordloom.DiscordLoom;
import net.fabricmc.api.ModInitializer;

public class DiscordLoomFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        DiscordLoom.init();
    }
}