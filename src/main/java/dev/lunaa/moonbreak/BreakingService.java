package dev.lunaa.moonbreak;

import dev.lunaa.moonbreak.block.CustomBlockType;
import dev.lunaa.moonbreak.tool.CustomTool;
import dev.lunaa.moonbreak.tool.CustomToolImpl;
import dev.lunaa.moonbreak.tool.CustomToolType;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class BreakingService {

    private static final int INACTIVE_DELAY_TICKS = 5 * 20;
    private static final HashMap<UUID, Integer> activePlayers = new HashMap<>();
    private static final HashMap<UUID, LastState> lastStates = new HashMap<>();

    public void updateBreakSpeeds() {
        updateActivePlayers();
        if (activePlayers.isEmpty()) return;

        for (UUID uuid : activePlayers.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                lastStates.remove(uuid);
                resetToInitialValues(player);
                continue;
            }

            double range = Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)).getValue();
            Block block = player.getTargetBlockExact((int) Math.ceil(range), FluidCollisionMode.NEVER);
            if (block == null) {
                lastStates.remove(uuid);
                resetToInitialValues(player);
                continue;
            }

            Location blockLocation = block.getLocation();
            ItemStack handItem = player.getInventory().getItemInMainHand();
            boolean onGround = player.isOnGround();
            boolean underWater = player.isUnderWater();
            if (!lastStateChanged(uuid, blockLocation, handItem, onGround, underWater)) continue;
            updateLastState(uuid,  blockLocation, handItem, onGround, underWater);

            CustomTool tool = null;
            if (handItem.getType() != Material.AIR) {
                tool = CustomToolImpl.from(handItem).orElse(null);
            }

            Optional<CustomBlockType> optionalType = MoonBreak.instance().blockManager().get(blockLocation);
            calcBlockBreakSpeed(player, tool, block, optionalType.orElse(null), onGround, underWater);
        }
    }

    public void wasActive(Player player) {
        activePlayers.put(player.getUniqueId(), 0);
    }

    public void removeTrackedPlayer(Player player) {
        activePlayers.remove(player.getUniqueId());
        lastStates.remove(player.getUniqueId());
        resetToInitialValues(player);
        MoonBreak.instance().removePreviousBaseBlockBreakSpeed(player);
    }

    private void updateActivePlayers() {
        if (activePlayers.isEmpty()) return;

        Iterator<Map.Entry<UUID, Integer>> it = activePlayers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            if (entry.getValue() > INACTIVE_DELAY_TICKS) {
                lastStates.remove(entry.getKey());
                resetToInitialValues(entry.getKey());
                it.remove();
                continue;
            }
            entry.setValue(entry.getValue() + 1);
        }
    }

    private void resetToInitialValues(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        resetToInitialValues(player);
    }

    private void resetToInitialValues(Player player) {
        Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_BREAK_SPEED)).setBaseValue(MoonBreak.instance().previousBaseBlockBreakSpeed(player));
    }

    private void calcBlockBreakSpeed(Player player, @Nullable CustomTool tool, Block block, @Nullable CustomBlockType blockType, boolean onGround, boolean underWater) {
        float vanillaBlockHardness = block.getType().getHardness();
        double breakSpeed = blockType != null ? 1f/(blockType.hardness()/vanillaBlockHardness) : 1;

        if (tool != null) {
            CustomToolType type = tool.type();
            if (!type.affectedByFloating() && !onGround) {
                breakSpeed *= 5;
            }
            if (!type.affectedByUnderwater() && underWater) {
                breakSpeed *= 5;
            }
        }

        Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_BREAK_SPEED)).setBaseValue(breakSpeed);
    }

    private boolean lastStateChanged(UUID uuid, Location lookingAt, ItemStack handItem, boolean onGround, boolean underwater) {
        if (!lastStates.containsKey(uuid)) return true;
        LastState state = lastStates.get(uuid);
        return !state.lastLookedAt().equals(lookingAt)
                || !state.lastHeldItem().isSimilar(handItem)
                || !(state.lastOnGround() == onGround)
                || !(state.lastUnderwater() == underwater);
    }

    private void updateLastState(UUID uuid, Location lookingAt, ItemStack handItem, boolean onGround, boolean underwater) {
        if (lastStates.containsKey(uuid)) {
            LastState state = lastStates.get(uuid);
            state.lastLookedAt(lookingAt);
            state.lastHeldItem(handItem);
            state.lastOnGround(onGround);
            state.lastUnderwater(underwater);
        } else {
            lastStates.put(uuid, new LastState(lookingAt, handItem, onGround, underwater));
        }
    }

    private static class LastState {

        private Location lastLookedAt;
        private ItemStack lastHeldItem;
        private boolean lastOnGround;
        private boolean lastUnderwater;

        public LastState(Location lastLookedAt, ItemStack lastHeldItem, boolean lastOnGround, boolean lastUnderwater) {
            this.lastLookedAt = lastLookedAt.clone();
            this.lastHeldItem = lastHeldItem.clone();
            this.lastOnGround = lastOnGround;
            this.lastUnderwater = lastUnderwater;
        }

        public Location lastLookedAt() {
            return lastLookedAt;
        }

        public void lastLookedAt(Location lastLookedAt) {
            this.lastLookedAt = lastLookedAt.clone();
        }

        public ItemStack lastHeldItem() {
            return lastHeldItem;
        }

        public void lastHeldItem(ItemStack lastHeldItem) {
            this.lastHeldItem = lastHeldItem.clone();
        }

        public boolean lastOnGround() {
            return lastOnGround;
        }

        public void lastOnGround(boolean lastOnGround) {
            this.lastOnGround = lastOnGround;
        }

        public boolean lastUnderwater() {
            return lastUnderwater;
        }

        public void lastUnderwater(boolean lastUnderwater) {
            this.lastUnderwater = lastUnderwater;
        }
    }

}
