package codes.dreaming.discordloom.mixin;

import com.mojang.authlib.GameProfile;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static codes.dreaming.discordloom.DiscordLoom.LINK_PACKET;
import static codes.dreaming.discordloom.DiscordLoom.LUCK_PERMS;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {

    @Shadow public abstract void disconnect(Text reason);

    @Shadow @Nullable GameProfile profile;

    @Inject(method = "acceptPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;"), cancellable = true)
    private void checkCanJoin(CallbackInfo ci) {
        if(this.profile == null) {
            System.err.println("Profile is null!");
            return;
        }

        User LuckUser =  LUCK_PERMS.getUserManager().getUser(profile.getId());

        if(LuckUser == null) {
            System.err.println("User not found in LuckPerms!");
            this.disconnect(Text.of("There was an error while trying to fetch your LuckPerms user, please try again later."));
            ci.cancel();
            return;
        }

        Optional<Node> idNode = LuckUser.getNodes().stream().filter(node -> node.getKey().equals("discordloom.id")).findAny();

        if(idNode.isEmpty()) {
            System.out.println("A user without a discordloom.id node tried to join!");
            this.disconnect(Text.of("If you're seeing this, it means that you haven't installed the DiscordLoom mod. Please install it and try again."));
            ci.cancel();
        }
    }
}
