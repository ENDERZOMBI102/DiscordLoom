package codes.dreaming.discordloom;

import codes.dreaming.discordloom.config.server.Config;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.discordjson.json.AuthorizationCodeGrantRequest;
import discord4j.oauth2.DiscordOAuth2Client;
import discord4j.rest.RestClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.luckperms.api.node.types.MetaNode;

import java.util.UUID;

import static codes.dreaming.discordloom.DiscordLoom.*;
import static codes.dreaming.discordloom.DiscordLoomServer.LUCK_PERMS;

@Environment(EnvType.SERVER)
public class ServerDiscordManager {
    private final RestClient restClient;
    private final DiscordClient client;

    public ServerDiscordManager() {
        restClient = RestClient.create(Config.CONFIG.discordBotToken.get());
        client = DiscordClient.create(Config.CONFIG.discordBotToken.get());
        client.login().block();
    }

    public boolean isUserInGuild(String userId, long guildId) {
        return client.getGuildById(Snowflake.of(guildId)).getMember(Snowflake.of(userId)).blockOptional().isPresent();
    }

    public String generateDiscordOauthUri() {
        return "https://discord.com/api/oauth2/authorize?client_id=" + Config.CONFIG.discordClientId.get() + "&redirect_uri=" + this.getDiscordRedirectUri() + "&response_type=code&scope=identify";
    }

    public String doDicordLink(String code) {
        DiscordOAuth2Client oAuth2Client = DiscordOAuth2Client.createFromCode(restClient, AuthorizationCodeGrantRequest.builder().code(code).clientId(Config.CONFIG.discordClientId.get()).clientSecret(Config.CONFIG.discordClientSecret.get()).redirectUri(this.getDiscordRedirectUri()).build());
        return oAuth2Client.getCurrentUser().block().id().toString();
    }

    public static void link(String userId, UUID profileId) {
        LOGGER.info("Linking user " + userId + " to Minecraft account " + profileId.toString());
        LUCK_PERMS.get().getUserManager().modifyUser(profileId, user -> user.data().add(MetaNode.builder(LuckPermsMetadataKey, userId).build()));
    }

    private static String getDiscordRedirectUri() {
        return "http://localhost:" + Config.CONFIG.discordRedirectUriPort.get() + "/callback";
    }
}
