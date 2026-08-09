package dev.lunaa.moonbreak.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lunaa.moonbreak.MoonBreak;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.ConsoleCommandSender;

public class MoonBreakCommand {

    private static final LiteralArgumentBuilder<CommandSourceStack> CLEAN_UP = Commands.literal("cleanup")
            .executes(ctx -> {
                CommandSourceStack src = ctx.getSource();

                src.getSender().sendMessage(Component.text("Are you sure you want to delete all custom blocks from your server?", NamedTextColor.RED)
                );
                src.getSender().sendMessage(Component.text("Confirm Deletion", NamedTextColor.DARK_RED, TextDecoration.BOLD)
                        .hoverEvent(Component.text("Delete all present custom blocks", NamedTextColor.RED))
                        .clickEvent(ClickEvent.callback(audience -> {
                            MoonBreak.instance().blockLoader().wipeAllBlockFromExistence();
                            audience.sendMessage(Component.text("Successfully deleted all custom blocks", NamedTextColor.GREEN));
                        })));

                return Command.SINGLE_SUCCESS;
            });

    public static final LiteralArgumentBuilder<CommandSourceStack> ROOT = Commands.literal("moonbreak")
            .requires(src -> src.getSender().isOp() && !(src.getSender() instanceof ConsoleCommandSender))
            .then(CLEAN_UP);
}
