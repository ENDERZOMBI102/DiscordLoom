package codes.dreaming.discordloom.config.fabric;

import codes.dreaming.discordloom.DiscordLoom;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class ForgeConfigHelperImpl {
    public static void registerServerConfig(ForgeConfigSpec spec) {
        ModLoadingContext.registerConfig(DiscordLoom.MOD_ID, ModConfig.Type.SERVER, spec);
    }

    public static void registerClientConfig(ForgeConfigSpec spec) {
        ModLoadingContext.registerConfig(DiscordLoom.MOD_ID, ModConfig.Type.CLIENT, spec);
    }

    public static void registerCommonConfig(ForgeConfigSpec spec) {
        ModLoadingContext.registerConfig(DiscordLoom.MOD_ID, ModConfig.Type.COMMON, spec);
    }
}
