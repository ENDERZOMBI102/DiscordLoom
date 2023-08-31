package codes.dreaming.discordloom;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static codes.dreaming.discordloom.DiscordLoom.*;

public class DiscordLoomClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
		ClientLoginNetworking.registerGlobalReceiver(QUERY_PACKET_ID, DiscordLoomClient::onQueryRequest);
        ClientPlayNetworking.registerGlobalReceiver(LINK_PACKET, (client, handler, buf, responSender) -> {
            LOGGER.info("Received link packet from server");
            ClientLinkManager.setUrl(buf.readString());
        });
    }

	private static CompletableFuture<@Nullable PacketByteBuf> onQueryRequest(MinecraftClient client, ClientLoginNetworkHandler handler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> listenerAdder) {
		var send = PacketByteBufs.create();

		var code = ClientLinkManager.getCode();
		send.writeOptional(Optional.ofNullable(code), PacketByteBuf::writeString);

		LOGGER.info("Sent code: {}", code);
		ClientLinkManager.setCode(null);

		return CompletableFuture.completedFuture(send);
	}
}
