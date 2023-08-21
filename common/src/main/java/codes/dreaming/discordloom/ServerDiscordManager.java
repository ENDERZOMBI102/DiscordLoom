package codes.dreaming.discordloom;

import codes.dreaming.discordloom.config.server.Config;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.discordjson.json.AuthorizationCodeGrantRequest;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.oauth2.DiscordOAuth2Client;
import discord4j.rest.RestClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.matcher.NodeMatcher;
import net.luckperms.api.node.types.MetaNode;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static codes.dreaming.discordloom.DiscordLoom.*;

@Environment(EnvType.SERVER)
public class ServerDiscordManager {
    private final RestClient restClient;
    private final GatewayDiscordClient client;

    private final PlayerManager playerManager;


    public ServerDiscordManager(PlayerManager playerManager) {
        this.playerManager = playerManager;

        restClient = RestClient.create(Config.CONFIG.discordBotToken.get());
        client = DiscordClient.create(Config.CONFIG.discordBotToken.get())
                .gateway()
                .setEnabledIntents(IntentSet.of(Intent.GUILD_VOICE_STATES))
                .login()
                .block();

        assert client != null;
        client.on(VoiceStateUpdateEvent.class)
                .filter((event) -> event.isLeaveEvent() || event.isMoveEvent())
                .flatMap(this::processVoiceStateUpdateEvent)
                .subscribe();
    }

    private Mono<Void> processVoiceStateUpdateEvent(VoiceStateUpdateEvent voiceStateUpdateEvent) {
        return Mono.fromRunnable(() -> {
            User user = voiceStateUpdateEvent.getCurrent().getUser().block();
            if (user == null) {
                return;
            }
            ServerDiscordManager.getPlayersFromDiscordId(user.getId().asString()).forEach(profileId -> {
                LOGGER.info("User " + user.getUsername() + " left voice channel, kicking him");
                ServerPlayerEntity player = playerManager.getPlayer(profileId);
                if (player != null) {
                    player.networkHandler.disconnect(Text.of("You left the voice channel"));
                }
            });
        });
    }

    public List<String> getMissingGuilds() {
        return Config.CONFIG.checkForGuildsOnJoin.get().stream()
                .filter(guildId -> client.getGuilds()
                        .toStream()
                        .noneMatch(guild -> guild.getId().equals(Snowflake.of(guildId))))
                .collect(Collectors.toList());
    }

    public Mono<Member> getMember(String userId, String guildId) {
        return Mono.justOrEmpty(client.getGuildById(Snowflake.of(guildId)))
                .flatMap(guild -> guild.block().getMemberById(Snowflake.of(userId)))
                .doOnError(e -> {
                    // Handle your error here
                });
    }

    public boolean userHasRoles(Member member, String role) {
        boolean hasRole = false;

        try {
            hasRole = Boolean.TRUE.equals(member.getRoles().any(roleData -> roleData.getId().asString().equals(role)).block());
        } catch (Exception e) {
            // Do nothing
        }

        return hasRole;
    }

    public boolean isUserInVoiceChannel(Snowflake userId, String voiceChannelId) {

        try {
            return Boolean.TRUE.equals(client.getChannelById(Snowflake.of(voiceChannelId))
                    .cast(VoiceChannel.class)
                    .flatMapMany(VoiceChannel::getVoiceStates)
                    .any(voiceState -> voiceState.getUserId().equals(userId))
                    .block());
        } catch (Exception e) {
            // Handle exception
            return false;
        }
    }

    public Mono<String> getUserName(String userId) {
        return client.getUserById(Snowflake.of(userId)).map(User::getUsername);
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

    public static class DiscordMemberCache {
        private final Map<String, Member> cache = new HashMap<>();

        public Mono<Member> getMember(String userId, String guildId) {
            if (cache.containsKey(userId)) {
                return Mono.just(cache.get(userId));
            }

            return DISCORD_MANAGER.getMember(userId, guildId).doOnNext(member -> cache.put(userId, member));
        }
    }
}
