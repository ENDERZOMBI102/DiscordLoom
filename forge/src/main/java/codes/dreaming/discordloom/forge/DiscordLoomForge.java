package codes.dreaming.discordloom.forge;

import dev.architectury.platform.forge.EventBuses;
import codes.dreaming.discordloom.DiscordLoom;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DiscordLoom.MOD_ID)
public class DiscordLoomForge {
    public DiscordLoomForge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(DiscordLoom.MOD_ID, bus);
        bus.register(this);
        DiscordLoom.init();
    }

    @SubscribeEvent
    public void onInitializeClient(final FMLClientSetupEvent event) {
        DiscordLoom.initClient();
    }

    @SubscribeEvent
    public void onInitializeServer(final FMLDedicatedServerSetupEvent event) {
        DiscordLoom.initServer();
    }
}