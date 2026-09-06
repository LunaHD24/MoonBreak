package dev.lunaa.moonbreak.tool;

import dev.lunaa.moonbreak.MoonBreakApi;
import dev.lunaa.moonbreak.registry.Registrable;
import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Represents a ToolType which holds information about how a tool of this type should behave
 * when turned into an actual item.
 */
@NullMarked
public interface CustomToolType extends Registrable {

    /**
     * Returns the ToolType builder
     * @return the builder
     */
    static Builder builder() {
        return MoonBreakApi.provider().toolTypeBuilder();
    }

    /**
     * Returns the name of this ToolType
     * @return the name
     */
    Component name();

    /**
     * Returns the lore of this ToolType
     * @return the lore
     */
    List<Component> lore();

    /**
     * Returns the material of this ToolType
     * @return the material
     */
    Material material();

    /**
     * Returns the maximum durability of this ToolType
     * @return the maximum durability
     */
    int maxDurability();

    /**
     * Returns the mining level of this ToolType.
     * If the mining level of the tool is lower than the mining level of the block being mined,
     * the block will not drop anything.
     * 0-4 represent the vanilla values, which are as follows:<br>
     * - 0: Wood, Gold<br>
     * - 1: Stone, Copper<br>
     * - 2: Iron<br>
     * - 3: Diamond<br>
     * - 4: Netherite
     * @return the mining level
     */
    int miningLevel();

    /**
     * Returns the tool speed of this ToolType
     * @return the tool speed
     */
    float speed();

    /**
     * Returns a collection of all materials that are considered correct for this ToolType,
     * regardless of the actual correct materials for the ToolType's material
     * @return the materials considered correct for this ToolType
     */
    Collection<Material> correctToolFor();

    /**
     * Returns if this ToolType includes the vanilla "mineable/" tags whose materials will be considered as correct for this ToolType.
     * E.g., if the material of this ToolType is a pickaxe, it will include the tag "mineable/pickaxe", meaning
     * all íncluded entries in that tag, will now be considered correct for this ToolType.
     * Will be overwritten with the default speed set for this ToolType if not disabled, see {@link CustomToolType#overwriteVanillaMineables()}.
     * @return if vanilla "mineable/" tag materials are considered correct for this ToolType
     */
    boolean includeVanillaMineables();

    /**
     * Returns if the tags set by {@link CustomToolType#includeVanillaMineables()} will be
     * overwritten by the default speed for this ToolType.
     * @return if they will be overwritten
     */
    boolean overwriteVanillaMineables();

    /**
     * Returns if this ToolType decreases its tool speed when the ToolType is not correct for the block
     * @return if the tool speed is affected
     */
    boolean affectedByWrongTool();

    /**
     * Returns if this ToolType decreases its tool speed when the user is underwater
     * @return if the tool speed is affected
     */
    boolean affectedByUnderwater();

    /**
     * Returns if this ToolType decreases its tool speed when the user is not touching the ground
     * @return if the tool speed is affected
     */
    boolean affectedByFloating();

    /**
     * Returns a map of all event hooks, if any are set or an empty one.
     * @return the map of event hooks or an empty one if none are set
     */
    Map<EventHook<? extends Event>, BiConsumer<? extends Event, CustomTool>> eventHooks();

    /**
     * Returns the event action for the specific hook.
     * @param eventHook the hook
     * @return the event action
     * @param <T> the event
     */
    <T extends Event> Optional<BiConsumer<T, CustomTool>> hook(EventHook<T> eventHook);

    /**
     * Represents a builder used to create new ToolTypes
     */
    interface Builder {

        /**
         * Copies all parameters from an existing ToolType
         * @param type the ToolType to copy from
         * @return the builder
         */
        Builder copyFrom(CustomToolType type);

        /**
         * Sets the displayed item name of this ToolType. Default formatting applies.
         *
         * @param name the displayed item name
         * @return the builder
         */
        Builder name(Component name);

        /**
         * Sets the displayed lore of this ToolType. Default formatting applies.<br>
         * @param lore the displayed lore
         * @return the builder
         */
        Builder lore(List<Component> lore);

        /**
         * Sets the material used for the {@link ItemStack} of this ToolType
         *
         * @param material the material used
         * @return the builder
         */
        Builder material(Material material);

        /**
         * Sets the maximum durability this ToolType can have.<br>
         * Defaults to the materials maximum durability or throws if non-existent.
         * @param maxDurability the maximum durability
         * @return the builder
         */
        Builder maxDurability(int maxDurability);

        /**
         * Sets the mining level for this ToolType. See {@link CustomToolType#miningLevel()}.<br>
         * Defaults to 0 (wooden).
         * @param miningLevel the mining level
         * @return the builder
         */
        Builder miningLevel(int miningLevel);

        /**
         * Sets the mining level for this ToolType based on a vanilla material.<br>
         * Defaults to 0 (wooden).
         * @param miningLevel the mining level
         * @return the builder
         */
        Builder miningLevel(MiningLevel miningLevel);

        /**
         * Sets the mining speed for this ToolType. See {@link CustomToolType#speed()}.<br>
         * Defaults to the materials default mining speed or throws if non-existent.
         * @param speed the mining speed
         * @return the builder
         */
        Builder speed(float speed);

        /**
         * Sets the mining speed for this ToolType based on a vanilla material.<br>
         * Defaults to the materials default mining speed or throws if non-existent.
         * @param speed the mining speed
         * @return the builder
         */
        Builder speed(MiningSpeed speed);

        /**
         * Sets the materials which are considered correct by this ToolType. See {@link CustomToolType#correctToolFor()}
         *
         * @param materials the materials
         * @return the builder
         */
        Builder correctToolFor(Collection<Material> materials);

        /**
         * Sets if the vanilla "mineable/" tags are considered correct by this ToolType. See {@link #includeVanillaMineables()}<br>
         * Defaults to {@code true}. Currently only affects pickaxes. Vanilla functionality of other tools is not guaranteed.
         * @param includeVanillaMineables if the "mineable/" tags should be considered correct
         * @return the builder
         */
        Builder includeVanillaMineables(boolean includeVanillaMineables);

        /** Sets if the tags potentially set by {@link #includeVanillaMineables()} should be
         * overwritten by the default speed set for this ToolType. See {@link #overwriteVanillaMineables()}.<br>
         * Defaults to {@code true}. Currently only affects pickaxes. Vanilla functionality of other tools is not guaranteed.
         * @param overwriteVanillaMineables if the tags should be overwritten
         * @return the builder
         */
        Builder overwriteVanillaMineables(boolean overwriteVanillaMineables);

        /**
         * Sets if this ToolType is affected by using the wrong tool for a block. See {@link CustomToolType#affectedByWrongTool()}.<br>
         * Defaults to {@code true}.
         * @param affected if this ToolType is affected
         * @return the builder
         */
        Builder affectedByWrongTool(boolean affected);

        /**
         * Sets if this ToolType is affected by the user being underwater. See {@link CustomToolType#affectedByUnderwater()}.<br>
         * Defaults to {@code true}.
         * @param affected if this ToolType is affected
         * @return the builder
         */
        Builder affectedUnderwater(boolean affected);

        /**
         * Sets if this ToolType is affected by the user floating. See {@link CustomToolType#affectedByFloating()}.<br>
         * Defaults to {@code true}.
         * @param affected if this ToolType is affected
         * @return the builder
         */
        Builder affectedByFloating(boolean affected);

        /**
         * Adds an action which will be executed accordingly to the specified {@link EventHook}.
         * @param eventHook the event hook
         * @param hookAction the action to be executed
         * @return the builder
         * @param <T> the event type
         */
        <T extends Event> Builder onHook(EventHook<T> eventHook, BiConsumer<T, CustomTool> hookAction);

        /**
         * Builds the ToolType
         *
         * @return the ToolType instance
         */
        CustomToolType build();
    }

    /**
     * Represents the vanilla mining levels of the respective tool material
     */
    enum MiningLevel {
        WOOD(0),
        GOLD(0),
        STONE(1),
        COPPER(1),
        IRON(2),
        DIAMOND(3),
        NETHERITE(4);

        private final int miningLevel;

        MiningLevel(int miningLevel) {
            this.miningLevel = miningLevel;
        }

        /**
         * Returns the mining level of the vanilla material
         * @return the mining level
         */
        public int miningLevel() {
            return miningLevel;
        }
    }

    /**
     * Represents the vanilla mining speeds of the respective tool material
     */
    enum MiningSpeed {
        WOOD(2),
        STONE(4),
        COPPER(5),
        IRON(6),
        DIAMOND(8),
        NETHERITE(9),
        GOLD(12);

        private final float miningSpeed;

        MiningSpeed(float miningSpeed) {
            this.miningSpeed = miningSpeed;
        }

        /**
         * Returns the mining speed of the vanilla material
         * @return the mining speed
         */
        public float miningSpeed() {
            return miningSpeed;
        }
    }

    /**
     * Represents specific hooks which can be used to attach a callback action for this tool to a given event.
     * @param <T> the event type
     */
    final class EventHook<T extends Event> {

        public static final EventHook<BlockDamageEvent> BLOCK_DAMAGE = new EventHook<>(BlockDamageEvent.class);
        public static final EventHook<BlockBreakProgressUpdateEvent> BLOCK_DAMAGE_UPDATE = new EventHook<>(BlockBreakProgressUpdateEvent.class);
        public static final EventHook<BlockDamageAbortEvent> BLOCK_DAMAGE_ABORT = new EventHook<>(BlockDamageAbortEvent.class);
        public static final EventHook<BlockBreakEvent> PRE_BLOCK_BREAK = new EventHook<>(BlockBreakEvent.class);

        /**
         * This hook is called during an event with {@link EventPriority#MONITOR} and should NEVER perform any modifications to the event.
         */
        public static final EventHook<BlockBreakEvent> POST_BLOCK_BREAK = new EventHook<>(BlockBreakEvent.class);
        public static final EventHook<PlayerInteractEvent> INTERACT = new EventHook<>(PlayerInteractEvent.class);
        public static final EventHook<PlayerInteractAtEntityEvent> INTERACT_AT_ENTITY = new EventHook<>(PlayerInteractAtEntityEvent.class);
        public static final EventHook<EntityDamageByEntityEvent> ENTITY_DAMAGE_BY_ENTITY = new EventHook<>(EntityDamageByEntityEvent.class);

        private final Class<T> eventClass;

        private EventHook(Class<T> eventClass) {
            this.eventClass = eventClass;
        }
    }
}
