package dev.lunaa.moonbreak.listener;

import dev.lunaa.moonbreak.MoonBreak;
import dev.lunaa.moonbreak.block.CustomBlockManagerImpl;
import dev.lunaa.moonbreak.tool.CustomTool;
import dev.lunaa.moonbreak.tool.CustomToolImpl;
import dev.lunaa.moonbreak.tool.CustomToolType;
import dev.lunaa.moonbreak.tool.CustomToolTypeImpl;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.function.BiConsumer;

public class PlayerBreakBlockListener implements Listener {

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent e) {
        executeHook(e, true);
        if (e.isCancelled()) return;

        CustomBlockManagerImpl blockManager = MoonBreak.instance().blockManager();
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
        } else {
            player.getInventory().setItemInMainHand(tool.itemStack());
        }

        executeHook(e, false);
    }

    private void executeHook(BlockBreakEvent e, boolean isPre) {
        Optional<CustomTool> optionalTool = CustomTool.fromPlayer(e.getPlayer());
        if (optionalTool.isEmpty()) return;
        CustomTool tool = optionalTool.get();
        ((CustomToolTypeImpl) tool.type()).executeHook(
                isPre ? CustomToolType.EventHook.PRE_BLOCK_BREAK : CustomToolType.EventHook.POST_BLOCK_BREAK, e, tool
        );
    }

}
