package codes.dreaming.discordloom.discord;

import codes.dreaming.discordloom.config.server.Config;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import static codes.dreaming.discordloom.DiscordLoom.*;

public class DiscordEventListener extends ListenerAdapter {
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if(Config.CONFIG.mandatoryVCChannels.get().isEmpty()){
            return;
        }

        if(event.getChannelJoined() != null && Config.CONFIG.mandatoryVCChannels.get().contains(event.getChannelJoined().getId())){
            return;
        }

        if(event.getChannelLeft() != null && Config.CONFIG.mandatoryVCChannels.get().contains(event.getChannelLeft().getId())){
            ServerDiscordManager.getPlayersFromDiscordId(event.getEntity().getId()).forEach(uuid -> {
                LOGGER.info("Kicking player " + uuid + " from server since he left a mandatory VC channel");
                ServerPlayerEntity player = PLAYER_MANAGER.getPlayer(uuid);
                if(player == null) return;

                player.networkHandler.disconnect(Text.of("You left a mandatory VC channel"));
            });
        }
    }
}
