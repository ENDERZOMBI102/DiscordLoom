package codes.dreaming.discordloom.mixin.client;

import codes.dreaming.discordloom.ClientLinkManager;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static codes.dreaming.discordloom.DiscordLoom.*;

@Mixin(LoginHelloC2SPacket.class)
public class LoginHelloC2SPacketMixin {
    @Inject(method = "write", at = @At("TAIL"))
    private void writeMixin(PacketByteBuf buf, CallbackInfo ci) {
        buf.writeOptional(Optional.ofNullable(ClientLinkManager.getCode()), PacketByteBuf::writeString);

        LOGGER.info("Sent code: " + ClientLinkManager.getCode());
        ClientLinkManager.setCode(null);
    }
}
