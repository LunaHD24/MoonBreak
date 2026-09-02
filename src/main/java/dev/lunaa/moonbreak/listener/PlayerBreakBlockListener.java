package dev.lunaa.moonbreak.listener;

import dev.lunaa.moonbreak.MoonBreak;
import dev.lunaa.moonbreak.block.CustomBlockManagerImpl;
import dev.lunaa.moonbreak.tool.CustomTool;
import dev.lunaa.moonbreak.tool.CustomToolType;
import dev.lunaa.moonbreak.tool.CustomToolTypeImpl;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.PlayerInventory;

import java.util.Optional;

public class PlayerBreakBlockListener implements Listener {

    @EventHandler
    public void onPlayerBreakBlockPre(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Optional<CustomTool> optionalTool = CustomTool.fromPlayer(player);
        optionalTool.ifPresent(customTool -> executeHook(e, true, customTool));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBreakBlockPost(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Optional<CustomTool> optionalTool = CustomTool.fromPlayer(player);

        CustomBlockManagerImpl blockManager = MoonBreak.instance().blockManager();
        Location blockLocation = e.getBlock().getLocation();
        if (blockManager.isPlaced(blockLocation)) blockManager.remove(blockLocation, false);

        if (optionalTool.isEmpty()) return;
        CustomTool tool = optionalTool.get();

        PlayerInventory inventory = player.getInventory();
        if (!tool.unbreakable()) tool.damage(1);
        if (tool.broken()) {
            player.broadcastSlotBreak(EquipmentSlot.HAND);
            inventory.setItemInMainHand(null);
        } else {
            inventory.setItemInMainHand(tool.itemStack(inventory.getItemInMainHand().getItemMeta()));
        }

        executeHook(e, false, tool);
    }

    private void executeHook(BlockBreakEvent e, boolean isPre, CustomTool tool) {
        ((CustomToolTypeImpl) tool.type()).executeHook(
                isPre ? CustomToolType.EventHook.PRE_BLOCK_BREAK : CustomToolType.EventHook.POST_BLOCK_BREAK, e, tool
        );
    }

}
