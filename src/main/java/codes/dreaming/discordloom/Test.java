package codes.dreaming.discordloom;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.DiscordEventAdapter;

import java.nio.file.Path;

public class Test {
	public static void main( String[] argv ) {
		Core.init( Path.of( "./.cache/discord_game_sdk.dll" ).toFile() );

		final long[] userId = { 0L };
		try ( var params = new CreateParams() ) {
			params.setClientID( 1146820282070356009L );
			params.setFlags(CreateParams.getDefaultFlags());

			var cores = new Core[1];
			params.registerEventHandler( new DiscordEventAdapter() {
				@Override
				public void onCurrentUserUpdate() {
					var core = cores[0];
					userId[0] = core.userManager().getCurrentUser().getUserId();
				}
			} );

			try ( var core = new Core( params ) ) {
				cores[0] = core;
				while ( userId[0] == 0L )
					core.runCallbacks();
			}
		}
		System.out.printf( "Current user's id: %s\n", userId[0] );
	}
}
