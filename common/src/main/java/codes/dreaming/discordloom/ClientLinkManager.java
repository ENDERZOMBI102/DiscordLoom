package codes.dreaming.discordloom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ServerAddress;

import java.net.InetSocketAddress;

public class ClientLinkManager {
    private static String url = null;
    private static ServerAddress serverAddress = null;

    private static String code = null;

    public static String getCode() {
        return code;
    }

    public static void setCode(String code) {
        ClientLinkManager.code = code;
    }

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String url) {
        ClientLinkManager.url = url;
    }

    public static ServerAddress getServerAddress() {
        return serverAddress;
    }

    public static void setServerAddress(ServerAddress serverAddress) {
        if(ClientLinkManager.serverAddress != null && !ClientLinkManager.serverAddress.equals(serverAddress)) {
            ClientLinkManager.setCode(null);
        }
        ClientLinkManager.serverAddress = serverAddress;
        ClientLinkManager.url = null;
    }
}
