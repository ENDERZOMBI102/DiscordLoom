package codes.dreaming.discordloom.mixin.server.ban.vanilla;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.command.BanIpCommand;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BanIpCommand.class)
public class BanIpCommandMixin {
    @Inject(method = "banIp", at = @At(value = "RETURN", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;disconnect(Lnet/minecraft/text/Text;)V"))
    private static void banIpMixin(ServerCommandSource source, String targetIp, Text reason, CallbackInfoReturnable<Integer> cir) {

    }
}
