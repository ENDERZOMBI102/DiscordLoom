package codes.dreaming.discordloom.mixin.client;

import codes.dreaming.discordloom.ClientLinkManager;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {
    @Shadow private MultilineText reasonFormatted;

    @Shadow private int reasonHeight;

    @Shadow @Final private Screen parent;

    protected DisconnectedScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("HEAD"), cancellable = true)
    private void init(CallbackInfo ci) {
        assert this.client != null;

        String uri;
        if((uri = ClientLinkManager.consumeUri()) != null) {
            this.client.keyboard.setClipboard(uri);

            Text message = Text.of("For playing on this server, you need to link your account. Please click on the button bellow to link your account, if the button doesn't work the link is in your clipboard, paste it in your browser. :)");
            this.reasonFormatted = MultilineText.create(this.textRenderer, message, this.width - 50);
            this.reasonHeight = this.reasonFormatted.count() * this.textRenderer.fontHeight;

            Text linkButtonMessage = Text.of("Click here to link your account");
            this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, Math.min(this.height / 2 + this.reasonHeight / 2 + this.textRenderer.fontHeight, this.height - 30), 200, 20, linkButtonMessage, button -> {
                Util.getOperatingSystem().open(uri);
            }));
            Text cancelButtonMessage = Text.of("Cancel");
            this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, Math.min(this.height / 2 + this.reasonHeight / 2 + this.textRenderer.fontHeight + 30, this.height - 30), 200, 20, cancelButtonMessage, button -> this.client.setScreen(this.parent)));

            ci.cancel();
            return;
        }
    }
}
