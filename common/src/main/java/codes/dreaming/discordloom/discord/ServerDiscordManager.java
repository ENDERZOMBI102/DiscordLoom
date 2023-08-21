package codes.dreaming.discordloom.discord;

import codes.dreaming.discordloom.config.server.Config;
import discord4j.discordjson.json.AuthorizationCodeGrantRequest;
import discord4j.oauth2.DiscordOAuth2Client;
import discord4j.rest.RestClient;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.matcher.NodeMatcher;
import net.luckperms.api.node.types.MetaNode;
import reactor.util.annotation.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static codes.dreaming.discordloom.DiscordLoom.*;

@Environment(EnvType.SERVER)
public class ServerDiscordManager {
    private final RestClient restClient;
    private final JDA jdaApi;


    public ServerDiscordManager() {
        jdaApi = JDABuilder.createDefault(Config.CONFIG.discordBotToken.get(), GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS).build();

        jdaApi.addEventListener(new DiscordEventListener());

        restClient = RestClient.create(Config.CONFIG.discordBotToken.get());
    }

    public JDA getJdaApi() {
        return jdaApi;
    }

    public List<Guild> getMissingGuilds() {
        List<Guild> guilds = jdaApi.getGuilds();
        return Config.CONFIG.checkForGuildsOnJoin.get().stream().filter(id -> guilds.stream().noneMatch(guild -> guild.getId().equals(id))).map(jdaApi::getGuildById).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Nullable
    public User getDiscordUserFromId(String id) {
        return jdaApi.retrieveUserById(id).complete();
    }

    public String generateDiscordOauthUri() {
        return "https://discord.com/api/oauth2/authorize?client_id=" + Config.CONFIG.discordClientId.get() + "&redirect_uri=" + getDiscordRedirectUri() + "&response_type=code&scope=identify";
    }

    public String doDicordLink(String code) {
        DiscordOAuth2Client oAuth2Client = DiscordOAuth2Client.createFromCode(restClient, AuthorizationCodeGrantRequest.builder().code(code).clientId(Config.CONFIG.discordClientId.get()).clientSecret(Config.CONFIG.discordClientSecret.get()).redirectUri(getDiscordRedirectUri()).build());
        return oAuth2Client.getCurrentUser().block().id().toString();
    }

    public static Set<UUID> getPlayersFromDiscordId(String discordId) {
        Set<UUID> matches;

        try {
            matches = LuckPermsProvider.get().getUserManager().searchAll(NodeMatcher.metaKey(buildNodeMatcherWithDiscordId(discordId))).get().keySet();
        } catch (Exception e) {
            return Collections.emptySet();
        }

        return matches;
    }

    public static void link(String userId, UUID profileId) {
        LOGGER.info("Linking user " + userId + " to Minecraft account " + profileId.toString());
        LuckPermsProvider.get().getUserManager().modifyUser(profileId, user -> user.data().add(buildNodeMatcherWithDiscordId(userId)));
    }

    private static MetaNode buildNodeMatcherWithDiscordId(String discordId) {
        return MetaNode.builder()
                .key(LuckPermsMetadataKey)
                .value(discordId)
                .build();
    }

    private static String getDiscordRedirectUri() {
        return "http://localhost:" + Config.CONFIG.discordRedirectUriPort.get() + "/callback";
    }
}
