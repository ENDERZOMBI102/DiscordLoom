package codes.dreaming.discordloom.mixinInterfaces;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

public interface LoginHelloC2SPacketAccessor {
    @Unique
    @Nullable
    String discordloom$getCode();
}
