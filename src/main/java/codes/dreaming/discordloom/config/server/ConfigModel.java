package codes.dreaming.discordloom.config.server;

import io.wispforest.owo.config.annotation.Config;

import java.util.List;

import static codes.dreaming.discordloom.DiscordLoom.*;

@Config(name = MOD_ID + "-server", wrapperName = "ServerConfig")
public class ConfigModel {
    public Long discordClientId = 1111111111111111111L;
    public String discordBotToken = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    public List<String> checkForGuildsOnJoin = List.of();
    public List<String> syncDiscordRolesOnJoin = List.of();

    public Boolean allowMultipleMinecraftAccountsPerDiscordAccount = false;

    public List<String> mandatoryVCChannels = List.of();

    public Boolean banDiscordAccount = true;
}
