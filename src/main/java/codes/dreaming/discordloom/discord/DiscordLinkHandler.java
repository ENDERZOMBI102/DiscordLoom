package codes.dreaming.discordloom.discord;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.DiscordEventAdapter;

public class DiscordLinkHandler extends DiscordEventAdapter implements AutoCloseable {
	private final Core core;
	private boolean running = false;
	private long userId = 0L;

	public DiscordLinkHandler( long clientId ) {
		var params = new CreateParams();
		params.setClientID( clientId );
		params.setFlags(CreateParams.getDefaultFlags());

		this.core = new Core( params );
	}

	/**
	 * Starts the game-sdk main loop and waits for the user to be received.
	 * @return the discord user id.
	 */
	public long start() {
		try {
			this.running = true;
			new Thread( this::run ).join();
			return this.userId;
		} catch ( InterruptedException e ) {
			throw new RuntimeException( e );
		}
	}

	private void run() {
		while ( this.running )
			this.core.runCallbacks();
	}

	@Override
	public void onCurrentUserUpdate() {
		this.userId = this.core.userManager().getCurrentUser().getUserId();
		this.running = false;
	}

	@Override
	public void close() {
		this.core.close();
	}
}
