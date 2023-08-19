package codes.dreaming.discordloom.command;

import codes.dreaming.discordloom.PermissionHelper;
import codes.dreaming.discordloom.ServerDiscordManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.matcher.NodeMatcher;
import net.luckperms.api.node.types.MetaNode;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static codes.dreaming.discordloom.DiscordLoom.DISCORD_MANAGER;
import static codes.dreaming.discordloom.DiscordLoom.LuckPermsMetadataKey;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DiscordLoomCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> command() {
        return literal("discordloom")
                .requires(src -> PermissionHelper.hasPermission(src, "discordloom.discordloom", 2))
                .then(literal("whois")
                        .then(literal("player")
                                .then(argument("player", EntityArgumentType.player())
                                        .executes(DiscordLoomCommand::whoisPlayer))
                        )
                        .then(literal("discord")
                                .then(argument("id", StringArgumentType.word())
                                        .executes(DiscordLoomCommand::whoisDiscord)))
                );
    }

    public static int whoisPlayer(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        PlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");

        String discordId = LuckPermsProvider.get().getUserManager().getUser(player.getUuid()).getNodes(NodeType.META).stream().filter(node -> node.getMetaKey().equals(LuckPermsMetadataKey)).findAny().get().getMetaValue();

        String username = DISCORD_MANAGER.getUserName(discordId).block();

        ctx.getSource().sendFeedback(Text.of("§a" + player.getDisplayName().getString() + " is " + username + " on Discord!"), false);

        return 1;
    }

    public static int whoisDiscord(CommandContext<ServerCommandSource> ctx) {
        String id = StringArgumentType.getString(ctx, "id");

        Set<UUID> matches = ServerDiscordManager.getPlayersFromDiscordId(id);

        if (matches.isEmpty()) {
            ctx.getSource().sendFeedback(Text.of("§cNo matches found!"), false);
            return 0;
        }

        ArrayList<String> names = new ArrayList<>();

        for (UUID uuid : matches) {
            names.add(Objects.requireNonNull(LuckPermsProvider.get().getUserManager().getUser(uuid)).getFriendlyName());
        }

        ctx.getSource().sendFeedback(Text.of("§aFound " + names.size() + " matches: " + String.join(", ", names)), false);

        return 1;
    }
}
