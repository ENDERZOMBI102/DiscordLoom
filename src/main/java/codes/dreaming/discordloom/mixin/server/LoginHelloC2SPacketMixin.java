package codes.dreaming.discordloom.mixin.server;

import codes.dreaming.discordloom.mixinInterfaces.LoginHelloC2SPacketAccessor;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoginHelloC2SPacket.class)
public class LoginHelloC2SPacketMixin implements LoginHelloC2SPacketAccessor {
    @Unique
    private String discordloom$code = null;

    @Unique
    public String discordloom$getCode() {
        return this.discordloom$code;
    }

    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("TAIL"))
    private void initMixin(PacketByteBuf buf, CallbackInfo ci) {
        this.discordloom$code = buf.readOptional(PacketByteBuf::readString).orElse(null);
    }

}
