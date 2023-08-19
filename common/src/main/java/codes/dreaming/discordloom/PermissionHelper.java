package codes.dreaming.discordloom;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PermissionHelper {
    public static boolean hasPermission(UUID player, String permission) {
        User user = LuckPermsProvider.get().getUserManager().getUser(player);
        if (user == null) {
            return false;
        }

        return user.getNodes().stream()
                .filter(iNode -> iNode.getKey().equals(permission))
                .map(Node::getValue)
                .findFirst()
                .orElse(false);
    }


    public static boolean hasPermission(ServerCommandSource src, String permission) {
        if(!src.isExecutedByPlayer()){
            return true;
        }

        return hasPermission(Objects.requireNonNull(src.getPlayer()).getUuid(), permission);
    }

    public static boolean hasPermission(ServerCommandSource src, String permission, Integer fallbackLevel) {
        if(src.hasPermissionLevel(fallbackLevel)){
            return true;
        }

        return hasPermission(src, permission);
    }
}
