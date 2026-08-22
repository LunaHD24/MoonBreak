package dev.lunaa.moonbreak;

import dev.lunaa.moonbreak.block.CustomBlock;
import dev.lunaa.moonbreak.tool.CustomTool;
import dev.lunaa.moonbreak.tool.CustomToolImpl;
import dev.lunaa.moonbreak.tool.CustomToolType;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class BreakingService {

    private static final int INACTIVE_DELAY_TICKS = 5 * 20;
    private static final HashMap<UUID, Integer> activePlayers = new HashMap<>();
    private static final HashMap<UUID, Location> lastLookedAt = new HashMap<>();

    public void updateBreakSpeeds() {
        updateActivePlayers();
        if (activePlayers.isEmpty()) return;

        for (UUID uuid : activePlayers.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            double range = Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)).getValue();
            Block block = player.getTargetBlockExact((int) Math.ceil(range), FluidCollisionMode.NEVER);
            if (block == null) {
                resetToInitialValues(player);
                continue;
            }

            if (lastLookedAt.containsKey(player.getUniqueId()) && lastLookedAt.get(player.getUniqueId()).equals(block.getLocation())) {
                continue;
            }
            lastLookedAt.put(player.getUniqueId(), block.getLocation());

            Optional<CustomBlock> optionalCustomBlock = MoonBreak.instance().blockManager().get(block.getLocation());

            CustomTool tool = null;
            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (handItem.getType() != Material.AIR) {
                tool = CustomToolImpl.from(handItem).orElse(null);
            }

            CustomBlock customBlock = optionalCustomBlock.orElse(null);
            calcBlockBreakSpeed(player, tool, block, customBlock);
        }
    }

    public void wasActive(Player player) {
        activePlayers.put(player.getUniqueId(), 0);
    }

    public void removeTrackedPlayer(Player player) {
        activePlayers.remove(player.getUniqueId());
        lastLookedAt.remove(player.getUniqueId());
        resetToInitialValues(player);
    }

    private void updateActivePlayers() {
        if (activePlayers.isEmpty()) return;

        Iterator<Map.Entry<UUID, Integer>> it = activePlayers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            if (entry.getValue() > INACTIVE_DELAY_TICKS) {
                it.remove();
                continue;
            }
            entry.setValue(entry.getValue() + 1);
        }
    }

    private void resetToInitialValues(Player player) {
        Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_BREAK_SPEED)).setBaseValue(MoonBreak.instance().previousBaseBlockBreakSpeed(player));
    }

    private void calcBlockBreakSpeed(Player player, @Nullable CustomTool tool, Block block, @Nullable CustomBlock customBlock) {
        float vanillaBlockHardness = block.getType().getHardness();
        double breakSpeed = customBlock != null ? 1f/(customBlock.type().hardness()/vanillaBlockHardness) : 1;

        if (tool != null) {
            CustomToolType type = tool.type();
            if (!type.affectedByFloating() && !player.isOnGround()) {
                breakSpeed *= 5;
            }
            if (!type.affectedByUnderwater() && player.isUnderWater()) {
                breakSpeed *= 5;
            }
        }

        Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_BREAK_SPEED)).setBaseValue(breakSpeed);
    }

}
