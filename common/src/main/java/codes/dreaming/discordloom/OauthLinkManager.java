package codes.dreaming.discordloom;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

import static codes.dreaming.discordloom.DiscordLoom.*;

public class OauthLinkManager {
    public static void link(String userId, UUID profileId) {
        //TODO: Use a shared LuckPerms instance
        LuckPerms LUCK_PERMS = LuckPermsProvider.get();

        LOGGER.info("Linking user " + userId + " to Minecraft account " + profileId.toString());
        LUCK_PERMS.getUserManager().modifyUser(profileId, user -> user.data().add(Node.builder("discordloom.id").value(true).withContext("discordId", userId).build()));
    }
}
