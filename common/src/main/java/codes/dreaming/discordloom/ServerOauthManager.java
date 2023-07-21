package codes.dreaming.discordloom;

import discord4j.discordjson.json.AuthorizationCodeGrantRequest;
import discord4j.oauth2.DiscordOAuth2Client;
import discord4j.rest.RestClient;

public class ServerOauthManager {
    private static final String CLIENT_ID = "1131713124261703760";
    private static final String BOT_TOKEN = "REDACTED";
    private static final String REDIRECT_URI = "http://localhost:8000/callback";


    private static RestClient restClient = RestClient.create(BOT_TOKEN);

    public static String generateDiscordOauthUri() {
        return "https://discord.com/api/oauth2/authorize?client_id=" + Long.parseLong(CLIENT_ID) + "&redirect_uri=" + REDIRECT_URI + "&response_type=code&scope=identify";
    }

    public static String gedDiscordId(String code) {
        DiscordOAuth2Client oAuth2Client = DiscordOAuth2Client.createFromCode(restClient, AuthorizationCodeGrantRequest.builder().code(code).clientId(1131713124261703760L).clientSecret("oWvA01xPkIu9TqjRPBy8GGqQ_cz6lsYp").redirectUri("http://localhost:8000/callback").build());
        return oAuth2Client.getCurrentUser().block().id().toString();
    }
}
