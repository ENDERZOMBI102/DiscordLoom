package codes.dreaming.discordloom;

import discord4j.discordjson.json.AuthorizationCodeGrantRequest;
import discord4j.oauth2.DiscordOAuth2Client;
import discord4j.rest.RestClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.luckperms.api.node.types.MetaNode;

import java.util.UUID;

import static codes.dreaming.discordloom.DiscordLoom.*;

@Environment(EnvType.SERVER)
public class ServerOauthManager {
    private static final String CLIENT_ID = "1131713124261703760";
    private static final String CLEINT_SECRET = "REDACTED";
    private static final String BOT_TOKEN = "REDACTED";
    private static final String REDIRECT_URI = "http://localhost:8000/callback";


    private static final RestClient restClient = RestClient.create(BOT_TOKEN);

    public static String generateDiscordOauthUri() {
        return "https://discord.com/api/oauth2/authorize?client_id=" + Long.parseLong(CLIENT_ID) + "&redirect_uri=" + REDIRECT_URI + "&response_type=code&scope=identify";
    }

    public static String gedDiscordId(String code) {
        DiscordOAuth2Client oAuth2Client = DiscordOAuth2Client.createFromCode(restClient, AuthorizationCodeGrantRequest.builder().code(code).clientId(Long.parseLong(CLIENT_ID)).clientSecret(CLEINT_SECRET).redirectUri(REDIRECT_URI).build());
        return oAuth2Client.getCurrentUser().block().id().toString();
    }

    public static void link(String userId, UUID profileId) {
        LOGGER.info("Linking user " + userId + " to Minecraft account " + profileId.toString());
        LUCK_PERMS.get().getUserManager().modifyUser(profileId, user -> user.data().add(MetaNode.builder(LuckPermsMetadataKey, userId).build()));
    }
}
