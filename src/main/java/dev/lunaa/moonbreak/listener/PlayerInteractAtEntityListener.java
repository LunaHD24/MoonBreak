package dev.lunaa.moonbreak.listener;

import dev.lunaa.moonbreak.tool.CustomTool;
import dev.lunaa.moonbreak.tool.CustomToolType;
import dev.lunaa.moonbreak.tool.CustomToolTypeImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.Optional;

public class PlayerInteractAtEntityListener implements Listener {

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        Optional<CustomTool> optionalTool = CustomTool.fromPlayer(e.getPlayer());
        if (optionalTool.isEmpty()) return;
        CustomTool tool = optionalTool.get();
        ((CustomToolTypeImpl) tool.type()).executeHook(CustomToolType.EventHook.INTERACT_AT_ENTITY, e, tool);
    }

}
