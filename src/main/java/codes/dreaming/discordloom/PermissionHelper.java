package codes.dreaming.discordloom;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Objects;
import java.util.UUID;

public class PermissionHelper {
    public static boolean hasPermission(User user, String permission) {
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    public static boolean hasPermission(UUID player, String permission) {
        User user = LuckPermsProvider.get().getUserManager().getUser(player);
        if (user == null) {
            return false;
        }

        return hasPermission(user, permission);
    }


    public static boolean hasPermission(ServerCommandSource src, String permission) {
        if(!src.isExecutedByPlayer()){
            return true;
        }

        return hasPermission(Objects.requireNonNull(src.getPlayer()).getUuid(), permission);
    }
}
