package codes.dreaming.discordloom.mixin.server;

import codes.dreaming.discordloom.ServerDiscordManager;
import codes.dreaming.discordloom.config.server.Config;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

import static codes.dreaming.discordloom.DiscordLoom.*;

@Mixin(LoginHelloC2SPacket.class)
public abstract class LoginHelloC2SPacketMixin {
    @Shadow
    public abstract Optional<UUID> profileId();

    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("TAIL"))
    private void initMixin(PacketByteBuf buf, CallbackInfo ci) {
        String code = buf.readOptional(PacketByteBuf::readString).orElse(null);
        if(code == null) return;
        LOGGER.trace("Received code: " + code);
        String userId = DISCORD_MANAGER.doDicordLink(code);

        if (!Config.CONFIG.allowMultipleMinecraftAccountsPerDiscordAccount.get()) {
            if(!ServerDiscordManager.getPlayersFromDiscordId(userId).isEmpty()){
                LOGGER.trace("Discord account already linked to another user!");
                return;
            }
        }

        ServerDiscordManager.link(userId, this.profileId().get());
    }

}
