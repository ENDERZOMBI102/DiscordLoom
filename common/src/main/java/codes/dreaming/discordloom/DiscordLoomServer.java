package codes.dreaming.discordloom;

import com.google.common.base.Suppliers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

import java.util.function.Supplier;

@Environment(EnvType.SERVER)
public class DiscordLoomServer {
    public static Supplier<LuckPerms> LUCK_PERMS = Suppliers.memoize(LuckPermsProvider::get);
}
