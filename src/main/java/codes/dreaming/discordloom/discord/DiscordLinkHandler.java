package codes.dreaming.discordloom.discord;

import de.jcm.discordgamesdk.*;

import static com.mojang.text2speech.Narrator.LOGGER;

public class DiscordLinkHandler extends DiscordEventAdapter implements AutoCloseable {
	private final Core core;
	private boolean running = false;
	private long userId = 0L;

	public DiscordLinkHandler( long clientId ) {
		var params = new CreateParams();
		params.setClientID( clientId );
		params.setFlags( CreateParams.Flags.NO_REQUIRE_DISCORD );
		params.registerEventHandler( this );

		this.core = new Core( params );
		this.core.setLogHook( LogLevel.DEBUG, ( level, message ) -> {
			switch ( level ) {
				case DEBUG -> LOGGER.debug( message );
				case INFO  -> LOGGER.info( message );
				case WARN  -> LOGGER.warn( message );
				case ERROR -> LOGGER.error( message );
			}
		});
	}

	/**
	 * Starts the game-sdk main loop and waits for the user to be received.
	 * @return the discord user id.
	 */
	public long start() {
		try {
			this.running = true;
			var thread = new Thread( null, this::run, "game-sdk-thread" );
			thread.start();
			thread.join();
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
