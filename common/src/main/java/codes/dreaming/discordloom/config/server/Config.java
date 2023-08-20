package codes.dreaming.discordloom.config.server;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class Config {
    public final ForgeConfigSpec.ConfigValue<Long> discordClientId;
    public final ForgeConfigSpec.ConfigValue<String> discordClientSecret;
    public final ForgeConfigSpec.ConfigValue<String> discordBotToken;
    public final ForgeConfigSpec.ConfigValue<Integer> discordRedirectUriPort;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> checkForGuildsOnJoin;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> syncDiscordRolesOnJoin;

    public final ForgeConfigSpec.ConfigValue<Boolean> allowMultipleMinecraftAccountsPerDiscordAccount;


    private Config(ForgeConfigSpec.Builder builder) {
        builder.push("serverConfig");
        discordClientId = builder.comment("The client ID of your Discord application").define("client_id", 1111111111111111111L);
        discordClientSecret = builder.comment("The client secret of your Discord application").define("client_secret", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        discordBotToken = builder.comment("The bot token of your Discord application").define("bot_token", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        discordRedirectUriPort = builder.comment("The port to use for the redirect URI").define("redirect_uri_port", 8000);
        checkForGuildsOnJoin = builder.comment("The guilds to check for on join, obviously the bot need to be in those").defineList("check_for_guilds", List.of(), entry -> true);
        syncDiscordRolesOnJoin = builder.comment("The roles to sync on join.").comment("Format: [\"guildId:roleId\"]").defineList("sync_discord_roles", List.of(), entry -> true);
        allowMultipleMinecraftAccountsPerDiscordAccount = builder.comment("Allow multiple Minecraft accounts per Discord account").define("allow_multiple_minecraft_accounts_per_discord_account", false);
        builder.pop();
    }

    public static final ForgeConfigSpec SPEC;
    public static final Config CONFIG;
    static {
        final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
        SPEC = specPair.getRight();
        CONFIG = specPair.getLeft();
    }
}
