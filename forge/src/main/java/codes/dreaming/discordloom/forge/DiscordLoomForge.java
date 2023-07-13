package codes.dreaming.discordloom.forge;

import dev.architectury.platform.forge.EventBuses;
import codes.dreaming.discordloom.DiscordLoom;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DiscordLoom.MOD_ID)
public class DiscordLoomForge {
    public DiscordLoomForge() {
		// Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(DiscordLoom.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        DiscordLoom.init();
    }
}