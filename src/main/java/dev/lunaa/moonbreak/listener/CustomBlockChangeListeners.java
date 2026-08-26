package dev.lunaa.moonbreak.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import dev.lunaa.moonbreak.MoonBreak;
import dev.lunaa.moonbreak.block.CustomBlock;
import dev.lunaa.moonbreak.block.CustomBlockManagerImpl;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public class CustomBlockChangeListeners implements Listener {

    public static @MonotonicNonNull CustomBlockManagerImpl blockManager;

    public static void init(CustomBlockManagerImpl blockManager) {
        CustomBlockChangeListeners.blockManager = blockManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakBlock(BlockBreakBlockEvent e) {
        blockManager.remove(e.getBlock().getLocation(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        blockManager.remove(e.getBlock().getLocation(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        e.blockList().forEach(block -> blockManager.remove(block.getLocation(), false));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent e) {
        blockManager.remove(e.getBlock().getLocation(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDestroy(BlockDestroyEvent e) {
        blockManager.remove(e.getBlock().getLocation(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent e) {
        blockManager.remove(e.getBlock().getLocation(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTNTPrime(TNTPrimeEvent e) {
        blockManager.remove(e.getBlock().getLocation(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        blockManager.remove(e.getBlock().getLocation(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        e.blockList().forEach(block -> blockManager.remove(block.getLocation(), false));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Location location = e.getBlock().getLocation();
        if (!blockManager.isPlaced(location)) return;

        MoonBreak.logger().warning("Tried placing block at a location where a custom block is present - cancelled");
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMultiBlockPlace(BlockMultiPlaceEvent e) {
        e.getReplacedBlockStates().forEach(
                state -> {
                    Location location = state.getLocation();
                    if (!blockManager.isPlaced(location)) return;

                    MoonBreak.logger().warning("Tried placing block at a location where a custom block is present (x:"
                            + location.getBlockX()
                            + ", y:" + location.getBlockY()
                            + ", z:" + location.getBlockZ()
                            + ") - cancelled"
                    );
                    e.setCancelled(true);
                }
        );
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        e.getBlocks().forEach(block -> {
            Location location = block.getLocation().clone();
            if (!blockManager.isPlaced(location)) return;

            CustomBlock customBlock = blockManager.get(location).orElseThrow();
            customBlock.location(location.add(e.getDirection().getDirection()));
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        e.getBlocks().forEach(block -> {
            Location location = block.getLocation().clone();
            if (!blockManager.isPlaced(location)) return;

            CustomBlock customBlock = blockManager.get(location).orElseThrow();
            customBlock.location(location.add(e.getDirection().getOppositeFace().getDirection()));
        });
    }
}