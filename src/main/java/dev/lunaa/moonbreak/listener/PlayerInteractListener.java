package dev.lunaa.moonbreak.listener;

import dev.lunaa.moonbreak.MoonBreak;
import dev.lunaa.moonbreak.tool.CustomTool;
import dev.lunaa.moonbreak.tool.CustomToolType;
import dev.lunaa.moonbreak.tool.CustomToolTypeImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        MoonBreak.instance().breakingService().wasActive(e.getPlayer());

        ItemStack item = e.getItem();
        if (item == null) return;

        Optional<CustomTool> optionalTool = CustomTool.from(item);
        if (optionalTool.isEmpty()) return;
        CustomTool tool = optionalTool.get();
        ((CustomToolTypeImpl) tool.type()).executeHook(CustomToolType.EventHook.INTERACT, e, tool);
    }

}
