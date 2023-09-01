package codes.dreaming.discordloom.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.node.matcher.NodeMatcher;
import net.luckperms.api.node.types.MetaNode;
import reactor.util.annotation.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static codes.dreaming.discordloom.DiscordLoomServer.LuckPermsMetadataKey;
import static codes.dreaming.discordloom.DiscordLoomServer.SERVER_CONFIG;

@Environment(EnvType.SERVER)
public class ServerDiscordManager {
    private final JDA jdaApi;


    public ServerDiscordManager() {
        jdaApi = JDABuilder
			.createDefault( SERVER_CONFIG.discordBotToken(), GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS )
			.build();

        jdaApi.addEventListener(new DiscordEventListener());

        jdaApi.updateCommands()
			.addCommands(Commands.context(Command.Type.USER, "Get user minecraft info"))
			.queue();
    }

    public JDA getJdaApi() {
        return jdaApi;
    }

    public List<Guild> getMissingGuilds() {
        List<Guild> guilds = jdaApi.getGuilds();
        return SERVER_CONFIG.checkForGuildsOnJoin()
			.stream()
			.filter(id -> guilds.stream().noneMatch(guild -> guild.getId().equals(id)))
			.map(jdaApi::getGuildById)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
    }

    @Nullable
    public User getDiscordUserFromId(String id) {
        return jdaApi.retrieveUserById(id).complete();
    }

    @Nullable
    public Guild getDiscordGuildFromId(Long guildId) {
        return jdaApi.getGuildById(guildId);
    }

    public static Set<UUID> getPlayersFromDiscordId(String discordId) {
        Set<UUID> matches;

        try {
            MetaNode discordIdNode = buildNodeMatcherWithDiscordId(discordId);
            matches = LuckPermsProvider.get()
				.getUserManager()
				.searchAll(NodeMatcher.equals(discordIdNode, NodeEqualityPredicate.EXACT))
				.get()
				.keySet();
        } catch (Exception e) {
            return Collections.emptySet();
        }

        return matches;
    }

    public static MetaNode buildNodeMatcherWithDiscordId(String discordId) {
        return MetaNode.builder()
			.key(LuckPermsMetadataKey)
			.value(discordId)
			.build();
    }
}
