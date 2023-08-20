package codes.dreaming.discordloom.mixinInterfaces;

import org.spongepowered.asm.mixin.Unique;
import reactor.util.annotation.Nullable;

public interface LoginHelloC2SPacketAccessor {
    @Unique
    @Nullable
    String discordloom$getCode();
}
