package dev.lunaa.moonbreak;

import dev.lunaa.moonbreak.command.MoonBreakCommand;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

@SuppressWarnings("UnstableApiUsage")
public class MoonBreakBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, cmds -> {
            Commands reg = cmds.registrar();
            reg.register(MoonBreakCommand.ROOT.build());
        });
    }
}
