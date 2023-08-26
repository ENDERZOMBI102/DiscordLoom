package codes.dreaming.discordloom.discord;

import codes.dreaming.discordloom.PermissionHelper;
import codes.dreaming.discordloom.config.server.ConfigModel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import static codes.dreaming.discordloom.DiscordLoom.*;
import static codes.dreaming.discordloom.DiscordLoomServer.PLAYER_MANAGER;
import static codes.dreaming.discordloom.DiscordLoomServer.SERVER_CONFIG;

public class DiscordEventListener extends ListenerAdapter {
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if(SERVER_CONFIG.mandatoryVCChannels().isEmpty()){
            return;
        }

        if(event.getChannelJoined() != null && SERVER_CONFIG.mandatoryVCChannels().contains(event.getChannelJoined().getId())){
            return;
        }

        if(event.getChannelLeft() != null && SERVER_CONFIG.mandatoryVCChannels().contains(event.getChannelLeft().getId())){
            ServerDiscordManager.getPlayersFromDiscordId(event.getEntity().getId()).forEach(uuid -> {
                ServerPlayerEntity player = PLAYER_MANAGER.getPlayer(uuid);
                if(player == null) return;

                if(PermissionHelper.hasPermission(player.getUuid(), MOD_ID + ".bypass_vc")) return;

                LOGGER.info("Kicking player " + uuid + " from server since he left a mandatory VC channel");
                player.networkHandler.disconnect(Text.of("You left a mandatory VC channel"));
            });
        }
    }
}
