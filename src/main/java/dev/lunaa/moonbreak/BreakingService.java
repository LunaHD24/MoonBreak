package dev.lunaa.moonbreak;

import dev.lunaa.moonbreak.block.CustomBlockType;
import dev.lunaa.moonbreak.tool.CustomTool;
import dev.lunaa.moonbreak.tool.CustomToolImpl;
import dev.lunaa.moonbreak.tool.CustomToolType;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.*;

public class BreakingService {

    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey(MoonBreak.instance(), "breakspeed");

    private static final int INACTIVE_DELAY_SECONDS = 2 * 60;
    private static final HashMap<UUID, Long> activePlayers = new HashMap<>();
    private static final HashMap<UUID, LastState> lastStates = new HashMap<>();

    public static void removeBreakSpeedModifier(Player player) {
        Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_BREAK_SPEED)).removeModifier(MODIFIER_KEY);
    }

    public static void removeBreakSpeedModifier(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        removeBreakSpeedModifier(player);
    }

    public void updateBreakSpeeds() {
        updateActivePlayers();
        if (activePlayers.isEmpty()) return;

        for (UUID uuid : activePlayers.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                lastStates.remove(uuid);
                removeBreakSpeedModifier(player);
                continue;
            }

            double range = Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)).getValue();
            Block block = player.getTargetBlockExact((int) Math.ceil(range), FluidCollisionMode.NEVER);
            if (block == null) {
                lastStates.remove(uuid);
                removeBreakSpeedModifier(player);
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
        activePlayers.put(player.getUniqueId(), Instant.now().getEpochSecond());
    }

    public void removeTrackedPlayer(Player player) {
        activePlayers.remove(player.getUniqueId());
        lastStates.remove(player.getUniqueId());
        removeBreakSpeedModifier(player);
    }

    private void updateActivePlayers() {
        if (activePlayers.isEmpty()) return;

        Iterator<Map.Entry<UUID, Long>> it = activePlayers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            UUID uuid = entry.getKey();

            long difference = Instant.now().getEpochSecond() - entry.getValue();
            if (difference > INACTIVE_DELAY_SECONDS) {
                lastStates.remove(uuid);
                removeBreakSpeedModifier(uuid);
                it.remove();
            }
        }
    }

    private void calcBlockBreakSpeed(Player player, @Nullable CustomTool tool, Block block, @Nullable CustomBlockType blockType, boolean onGround, boolean underWater) {
        double factor = 1;
        float vanillaBlockHardness = block.getType().getHardness();

        if (blockType != null && vanillaBlockHardness > 0) {
            factor *= (vanillaBlockHardness/blockType.hardness());
        }

        if (tool != null) {
            CustomToolType type = tool.type();
            if (!type.affectedByFloating() && !onGround) {
                factor *= 5;
            }
            if (!type.affectedByUnderwater() && underWater) {
                ItemStack helmet = player.getEquipment().getHelmet();
                boolean hasAquaAffinity = helmet != null
                        && !helmet.isEmpty()
                        && helmet.containsEnchantment(Enchantment.AQUA_AFFINITY);
                if (!hasAquaAffinity) factor *= 5;
            }
        }

        AttributeInstance attribute = Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_BREAK_SPEED));
        attribute.removeModifier(MODIFIER_KEY);
        attribute.addTransientModifier(new AttributeModifier(MODIFIER_KEY, factor-1, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
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
