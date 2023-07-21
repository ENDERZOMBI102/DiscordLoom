package codes.dreaming.discordloom.mixin.client;

import codes.dreaming.discordloom.ClientLinkManager;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import reactor.util.annotation.Nullable;

import static codes.dreaming.discordloom.DiscordLoom.*;

import java.net.InetSocketAddress;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {
    @Shadow
    private MultilineText reasonFormatted;

    @Shadow
    private int reasonHeight;

    @Shadow
    @Final
    private Screen parent;

    @Unique
    private ButtonWidget discordLoom$linkButton;

    @Unique
    @Nullable
    private HttpServer discordloom$server;


    protected DisconnectedScreenMixin(Text title) {
        super(title);
    }

    /**
     * Initializes the functionality for linking the account to the server.
     * This method is called at the beginning of the "init()" method in the containing class.
     * It sets up the necessary UI components and handles the link account process.
     */
    @Inject(method = "init()V", at = @At("HEAD"), cancellable = true)
    private void init(CallbackInfo ci) {
        assert this.client != null;

        String uri;
        if ((uri = ClientLinkManager.getUrl()) != null) {
            this.client.keyboard.setClipboard(uri);

            Text message = Text.of("For playing on this server, you need to link your account. Please click on the button bellow to link your account");
            this.reasonFormatted = MultilineText.create(this.textRenderer, message, this.width - 50);
            this.reasonHeight = this.reasonFormatted.count() * this.textRenderer.fontHeight;

            Text linkButtonMessage = Text.of("Click here to link your account");
            discordLoom$linkButton = new ButtonWidget(this.width / 2 - 100, Math.min(this.height / 2 + this.reasonHeight / 2 + this.textRenderer.fontHeight, this.height - 30), 200, 20, linkButtonMessage, button -> this.discordLoom$startLinkingProcess());
            this.addDrawableChild(discordLoom$linkButton);
            Text cancelButtonMessage = Text.of("Cancel");
            this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, Math.min(this.height / 2 + this.reasonHeight / 2 + this.textRenderer.fontHeight + 30, this.height - 30), 200, 20, cancelButtonMessage, button -> {
                if (this.discordloom$server != null) {
                    this.discordloom$server.stop(0);
                }
                this.client.setScreen(this.parent);
            }));

            ci.cancel();
        }
    }

    @Unique
    private void discordLoom$startLinkingProcess() {
        assert this.client != null;

        this.discordLoom$linkButton.active = false;
        this.discordLoom$linkButton.setMessage(Text.of("Linking..."));


        try {
            this.discordloom$server = HttpServer.create(new InetSocketAddress(ClientLinkManager.getPortFromOauthURL()), 0);
            this.discordloom$server.createContext("/callback", httpExchange -> {
                String response = "You can now close this tab and go back to the game";
                httpExchange.sendResponseHeaders(200, response.length());
                httpExchange.getResponseBody().write(response.getBytes());
                httpExchange.close();
                this.discordloom$server.stop(0);
                ClientLinkManager.setCode(httpExchange.getRequestURI().getQuery().split("=")[1]);

                MinecraftClient.getInstance().execute(() -> ConnectScreen.connect(this.parent, this.client, ClientLinkManager.getServerAddress(), null));
            });
            this.discordloom$server.setExecutor(null);
            this.discordloom$server.start();
            Util.getOperatingSystem().open(ClientLinkManager.getUrl());
            this.client.keyboard.setClipboard(ClientLinkManager.getUrl());

            this.discordLoom$linkButton.setMessage(Text.of("If your browser didn't open, paste the link in your clipboard in your browser"));
        } catch (Exception e) {
            this.discordLoom$linkButton.setMessage(Text.of("Error during linking process, manual linking is required contact the server owner"));
            LOGGER.error("Error creating server for linking process: " + e.getMessage());
        }

    }
}
