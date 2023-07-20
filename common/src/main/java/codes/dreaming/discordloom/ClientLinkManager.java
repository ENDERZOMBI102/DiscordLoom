package codes.dreaming.discordloom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientLinkManager {
    private static String uri = null;

    public static String consumeUri() {
        String temp = uri;
        uri = null;
        return temp;
    }

    public static boolean hasUri() {
        return uri != null;
    }

    public static void setUri(String uri) {
        ClientLinkManager.uri = uri;
    }

}
