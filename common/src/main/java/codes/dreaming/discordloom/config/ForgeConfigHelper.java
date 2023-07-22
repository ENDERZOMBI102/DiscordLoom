package codes.dreaming.discordloom.config;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraftforge.common.ForgeConfigSpec;

public class ForgeConfigHelper {
    @ExpectPlatform
    public static void registerServerConfig(ForgeConfigSpec spec) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerClientConfig(ForgeConfigSpec spec) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerCommonConfig(ForgeConfigSpec spec) {
        throw new AssertionError();
    }
}
