package net.pixeldreamstudios.perplayerquests.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.level.ServerPlayer;
import net.pixeldreamstudios.perplayerquests.progression.PerPlayerCommands;

import java.util.Collection;

public final class PPQCommands {
    private PPQCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("perplayerquests")
                .requires(s -> s.hasPermission(2))
                .then(buildPerPlayerCommand())
                .then(buildUniqueCommand())
                .then(buildGlobalCommand()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildUniqueCommand() {
        return Commands.literal("unique")
                .then(Commands.literal("release")
                        .then(Commands.argument("quest", StringArgumentType.string())
                                .executes(ctx -> PerPlayerCommands.releaseUnique(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "quest")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildGlobalCommand() {
        return Commands.literal("global")
                .then(Commands.literal("reset")
                        .then(Commands.argument("quest", StringArgumentType.string())
                                .executes(ctx -> PerPlayerCommands.resetGlobal(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "quest")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildPerPlayerCommand() {
        return Commands.literal("per_player")
                .then(Commands.literal("reset")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("quest_object", StringArgumentType.string())
                                        .executes(ctx -> PerPlayerCommands.reset(
                                                ctx.getSource(),
                                                EntityArgument.getPlayers(ctx, "players"),
                                                StringArgumentType.getString(ctx, "quest_object"))))))
                .then(Commands.literal("complete")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("quest_object", StringArgumentType.string())
                                        .executes(ctx -> PerPlayerCommands.complete(
                                                ctx.getSource(),
                                                EntityArgument.getPlayers(ctx, "players"),
                                                StringArgumentType.getString(ctx, "quest_object"))))))
                .then(Commands.literal("resync")
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(ctx -> PerPlayerCommands.resync(
                                        ctx.getSource(),
                                        EntityArgument.getPlayers(ctx, "players")))))
                .then(Commands.literal("reset_chapter")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("chapter", StringArgumentType.string())
                                        .executes(ctx -> PerPlayerCommands.resetChapter(
                                                ctx.getSource(),
                                                EntityArgument.getPlayers(ctx, "players"),
                                                StringArgumentType.getString(ctx, "chapter"))))))
                .then(Commands.literal("reset_all")
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(ctx -> PerPlayerCommands.resetAll(
                                        ctx.getSource(),
                                        EntityArgument.getPlayers(ctx, "players")))));
    }
}
