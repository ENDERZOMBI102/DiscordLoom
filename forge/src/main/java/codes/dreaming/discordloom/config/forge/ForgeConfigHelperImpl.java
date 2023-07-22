package codes.dreaming.discordloom.config.forge;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ForgeConfigHelperImpl {
    public static void registerServerConfig(ForgeConfigSpec spec) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, spec);
    }

    public static void registerClientConfig(ForgeConfigSpec spec) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, spec);
    }

    public static void registerCommonConfig(ForgeConfigSpec spec) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, spec);
    }
}
