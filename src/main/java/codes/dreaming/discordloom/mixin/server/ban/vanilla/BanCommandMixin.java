package codes.dreaming.discordloom.mixin.server.ban.vanilla;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.command.BanCommand;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(BanCommand.class)
public class BanCommandMixin {
    @Inject(method = "ban", at = @At(value = "RETURN", target = "Lnet/minecraft/server/BannedPlayerList;add(Lnet/minecraft/server/ServerConfigEntry;)V"))
    private static void banMixin(ServerCommandSource source, Collection<GameProfile> targets, @Nullable Text reason, CallbackInfoReturnable<Integer> cir) {
    }
}
