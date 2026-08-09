package dev.lunaa.moonbreak.listener;

import dev.lunaa.moonbreak.MoonBreak;
import dev.lunaa.moonbreak.block.CustomBlockManager;
import dev.lunaa.moonbreak.tool.CustomTool;
import dev.lunaa.moonbreak.tool.CustomToolImpl;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class PlayerBreakBlockListener implements Listener {

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent e) {
        if (e.isCancelled()) return;

        CustomBlockManager blockManager = MoonBreak.instance().blockManager();
        Location blockLocation = e.getBlock().getLocation();
        if (blockManager.isPlaced(blockLocation)) blockManager.remove(blockLocation, false);

        Player player = e.getPlayer();

        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem.getType() == Material.AIR) return;

        Optional<CustomTool> optionalTool = CustomToolImpl.from(handItem);
        if (optionalTool.isEmpty()) return;
        CustomTool tool = optionalTool.get();

        if (tool.unbreakable()) return;
        tool.damage(1);
        if (tool.broken()) {
            player.broadcastSlotBreak(EquipmentSlot.HAND);
            player.getInventory().setItemInMainHand(null);
            return;
        }

        player.getInventory().setItemInMainHand(tool.itemStack());
    }

}
