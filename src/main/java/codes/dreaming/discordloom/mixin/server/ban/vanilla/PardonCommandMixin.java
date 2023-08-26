package codes.dreaming.discordloom.mixin.server.ban.vanilla;

import codes.dreaming.discordloom.impl.BanImpl;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.command.PardonCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(PardonCommand.class)
public class PardonCommandMixin {
    @Inject(method = "pardon", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;sendFeedback(Lnet/minecraft/text/Text;Z)V"))
    private static void pardonMixin(ServerCommandSource source, Collection<GameProfile> targets, CallbackInfoReturnable<Integer> cir) {
        BanImpl.pardon(targets);
    }
}
