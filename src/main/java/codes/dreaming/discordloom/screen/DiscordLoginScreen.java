package codes.dreaming.discordloom.screen;

import codes.dreaming.discordloom.discord.DiscordLinkHandler;
import codes.dreaming.discordloom.mixin.client.ConnectScreenAccessor;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

import static codes.dreaming.discordloom.DiscordLoom.LOGGER;

public class DiscordLoginScreen extends Screen {
	private final CompletableFuture<Long> future;
	private final Screen parent;
	private final long clientId;
	private int reasonHeight;
	private ButtonWidget linkButton;
	private MultilineText reasonFormatted;


	public DiscordLoginScreen( Screen parent, CompletableFuture<Long> future, long clientId ) {
        super( Text.translatable( "screen.discordloom.login.title" ) );
		this.parent = parent;
		this.future = future;
		this.clientId = clientId;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	/**
     * Initializes the functionality for linking the account to the server.
     * This sets up the necessary UI components and handles the link account process.
     */
	@Override
    public void init() {
        assert this.client != null;

		this.reasonFormatted = MultilineText.create(
			this.textRenderer,
			Text.translatable( "text.discordloom.connect.message" ),
			this.width - 50
		);
		this.reasonHeight = reasonFormatted.count() * this.textRenderer.fontHeight;

		this.linkButton = new ButtonWidget(
			this.width / 2 - 100,
			Math.min(this.height / 2 + reasonHeight / 2 + this.textRenderer.fontHeight, this.height - 30),
			200, 20,
			Text.translatable( "text.discordloom.connect.link_button" ),
			button -> this.startLinkingProcess()
		);
		this.addDrawableChild(this.linkButton);
		var cancelButton = new ButtonWidget(
			this.width / 2 - 100,
			Math.min( this.height / 2 + reasonHeight / 2 + this.textRenderer.fontHeight + 30, this.height - 30 ),
			200, 20,
			Text.of( "Cancel" ),
			this::onCancel
		);
		this.addDrawableChild( cancelButton );
    }

	public void render( MatrixStack matrices, int mouseX, int mouseY, float delta ) {
		this.renderBackground(matrices);
		int yPosition = this.height / 2 - this.reasonHeight / 2;

		drawCenteredText(
			matrices,
			this.textRenderer,
			this.title,
			this.width / 2,
			yPosition - 9 * 2,
			0xAAAAAA
		);
		this.reasonFormatted.drawCenterWithShadow(
			matrices,
			this.width / 2,
			yPosition
		);
		super.render(matrices, mouseX, mouseY, delta);
	}

    private void startLinkingProcess() {
        assert this.client != null;

        this.linkButton.active = false;
        this.linkButton.setMessage(Text.of("Linking..."));

		try ( var linker = new DiscordLinkHandler( this.clientId ) ) {
			this.client.submit( () -> this.client.setScreen( this.parent ) );
			this.future.complete( linker.start() );
		} catch (Exception e) {
            this.linkButton.setMessage(Text.of("text.discordloom.connect.failed" ));
            LOGGER.error("Error creating server for linking process: {}", e.getMessage());
        }
    }

	private void onCancel( ButtonWidget btn ) {
		assert this.client != null;

		// first set back to connection screen
		this.client.setScreen( this.parent );

		// then close the connection
		( (ConnectScreenAccessor) this.parent ).cancel( null );
	}
}
