package codes.dreaming.discordloom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientLinkManager {
    private static String uri = null;

    public static String getUri() {
        return uri;
    }

    public static void setUri(String uri) {
        ClientLinkManager.uri = uri;
    }

}
