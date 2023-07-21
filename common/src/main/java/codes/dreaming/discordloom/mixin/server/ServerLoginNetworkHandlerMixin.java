package codes.dreaming.discordloom.mixin.server;

import codes.dreaming.discordloom.ServerOauthManager;
import com.mojang.authlib.GameProfile;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static codes.dreaming.discordloom.DiscordLoom.*;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {

    @Shadow public abstract void disconnect(Text reason);

    @Shadow @Nullable GameProfile profile;

    @Shadow @Final public ClientConnection connection;

    @Shadow public abstract String getConnectionInfo();

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "acceptPlayer", at = @At(value = "RETURN", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V"), cancellable = true)
    private void checkCanJoin(CallbackInfo ci) {
        //TODO: Use a shared LuckPerms instance
        LuckPerms LUCK_PERMS = LuckPermsProvider.get();
        if(this.profile == null) {
            LOGGER.error("Profile is null!");
            return;
        }

        User LuckUser = LUCK_PERMS.getUserManager().getUser(profile.getId());

        if(LuckUser == null) {
            LOGGER.error("User not found in LuckPerms!");
            this.disconnect(Text.of("There was an error while trying to fetch your LuckPerms user, please try again later."));
            ci.cancel();
            return;
        }

        Optional<Node> idNode = LuckUser.getNodes().stream().filter(node -> node.getKey().equals("discordloom.id")).findAny();

        if(idNode.isEmpty()) {
            LOGGER.trace("A user without a discordloom.id node tried to join!");
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeString(ServerOauthManager.generateDiscordOauthUri());
            NetworkManager.collectPackets(packet -> this.connection.send(packet), NetworkManager.serverToClient(), LINK_PACKET, buf);
            Text text = Text.of("If you're seeing this, it means that you haven't installed the DiscordLoom mod. Please install it and try again.");
            this.connection.send(new DisconnectS2CPacket(text));
            this.disconnect(text);
            ci.cancel();
            return;
        }

        LOGGER.info("User " + this.profile.getName() + " (" + this.profile.getId() + ") joined with a discordloom.id node! (" + idNode.get().getContexts().getAnyValue("discordId").get() + ")");
    }
}
