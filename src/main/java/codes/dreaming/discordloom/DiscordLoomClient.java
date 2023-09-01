package codes.dreaming.discordloom;

import codes.dreaming.discordloom.screen.DiscordLoginScreen;
import de.jcm.discordgamesdk.Core;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static codes.dreaming.discordloom.DiscordLoom.*;

public class DiscordLoomClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
		ClientLoginNetworking.registerGlobalReceiver(QUERY_PACKET_ID, DiscordLoomClient::onQueryRequest);
		ClientLoginNetworking.registerGlobalReceiver(RELAY_PACKET_ID, DiscordLoomClient::onRelayRequest);

		var name = switch ( Util.getOperatingSystem() ) {
			case WINDOWS -> "discord_game_sdk.dll";
			case LINUX -> "discord_game_sdk.so";
			case OSX -> "discord_game_sdk.dylib";
			default -> throw new IllegalStateException( "What os are you on??" );
		};
		var target = FabricLoader.getInstance().getGameDir().resolve( ".cache/" );

		if ( !( target.toFile().exists() || target.toFile().mkdir() ) )
			throw new IllegalStateException( "Failed to create `.cache` folder in game directory!" );

		target = target.resolve( name );
		if (! target.toFile().exists() ) {
			try ( var stream = DiscordLoomClient.class.getResourceAsStream( "/library/" + name ) ) {
				assert stream != null : "Why are we not reading a file we have..?";
				Files.copy( stream, target );
			} catch ( IOException e ) {
				throw new RuntimeException( e );
			}
		}
		Core.init( target.toFile() );
    }

	private static CompletableFuture<@Nullable PacketByteBuf> onQueryRequest(MinecraftClient client, ClientLoginNetworkHandler handler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> listenerAdder) {
		var clientId = buf.readLong();

		var future = new CompletableFuture<Long>();
		client.executeSync( () -> client.setScreen( new DiscordLoginScreen( client.currentScreen, future, clientId ) ) );

		return CompletableFuture.supplyAsync( () -> {
			var send = PacketByteBufs.create();

			var code = future.join();
			send.writeOptional(Optional.ofNullable(code), PacketByteBuf::writeLong);

			LOGGER.info("Sent user id: {}", code);

			return send;
		});
	}

	private static CompletableFuture<@Nullable PacketByteBuf> onRelayRequest(MinecraftClient client, ClientLoginNetworkHandler handler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> listenerAdder) {
		return CompletableFuture.completedFuture( PacketByteBufs.empty() );
	}
}
