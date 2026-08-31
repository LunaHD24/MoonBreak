package dev.lunaa.moonbreak.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lunaa.moonbreak.MoonBreak;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class MoonBreakCommand {

    private static final LiteralArgumentBuilder<CommandSourceStack> CLEAN_UP = Commands.literal("cleanup")
            .requires(src -> !(src.getSender() instanceof ConsoleCommandSender))
            .executes(ctx -> {
                CommandSourceStack src = ctx.getSource();

                src.getSender().sendMessage(Component.text("Are you sure you want to delete all custom blocks from your server?", NamedTextColor.RED));
                src.getSender().sendMessage(Component.text("Confirm Deletion", NamedTextColor.DARK_RED, TextDecoration.BOLD)
                        .hoverEvent(Component.text("Delete all present custom blocks", NamedTextColor.RED))
                        .clickEvent(ClickEvent.callback(audience -> {
                            if (!(audience instanceof Player player) || !player.hasPermission(Permissions.MOONBREAK)) return;
                            MoonBreak.instance().blockLoader().wipeAllBlockFromExistence();
                            audience.sendMessage(Component.text("Successfully deleted all custom blocks", NamedTextColor.GREEN));
                        })));

                return Command.SINGLE_SUCCESS;
            });

    private static final LiteralArgumentBuilder<CommandSourceStack> SET_BREAK_SPEED = Commands.literal("breakspeed")
            .then(Commands.argument("players", ArgumentTypes.players())
                    .then(Commands.argument("speed", DoubleArgumentType.doubleArg(0))
                            .executes(ctx -> {
                                CommandSourceStack src = ctx.getSource();
                                PlayerSelectorArgumentResolver playersResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);

                                double speed = ctx.getArgument("speed", double.class);
                                List<Player> players = playersResolver.resolve(src);

                                players.forEach(p -> Objects.requireNonNull(p.getAttribute(Attribute.BLOCK_BREAK_SPEED)).setBaseValue(speed));
                                int playerCount = players.size();
                                src.getSender().sendMessage(Component.text("Set break speed " + speed + " for " + playerCount + " player" + (playerCount != 1 ? "s" : "")));

                                return Command.SINGLE_SUCCESS;
                            }))
                    .then(Commands.literal("reset")
                            .executes(ctx -> {
                                CommandSourceStack src = ctx.getSource();
                                PlayerSelectorArgumentResolver playersResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);

                                List<Player> players = playersResolver.resolve(src);
                                players.forEach(p -> {
                                    AttributeInstance attribute = Objects.requireNonNull(p.getAttribute(Attribute.BLOCK_BREAK_SPEED));
                                    attribute.setBaseValue(attribute.getDefaultValue());
                                });
                                int playerCount = players.size();
                                src.getSender().sendMessage(Component.text("Reset break speed for " + playerCount + " player" + (playerCount != 1 ? "s" : "")));

                                return Command.SINGLE_SUCCESS;
                            }))
            );

    public static final LiteralArgumentBuilder<CommandSourceStack> ROOT = Commands.literal("moonbreak")
            .requires(src -> src.getSender().hasPermission(Permissions.MOONBREAK))
            .then(CLEAN_UP)
            .then(SET_BREAK_SPEED);
}
