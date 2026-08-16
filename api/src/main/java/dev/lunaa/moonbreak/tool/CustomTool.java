package dev.lunaa.moonbreak.tool;

import dev.lunaa.moonbreak.MoonBreakApi;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Represents an instance of a CustomTool, holding information about its current state,
 * such as durability and unbreakable status.
 * Based on a {@link CustomToolType} it can be used to create the actual {@link ItemStack} of a tool.
 */
public interface CustomTool {

    /**
     * Creates a new CustomTool instance based on a given ToolType
     * @param type the type
     * @return the CustomTool
     */
    static CustomTool of(CustomToolType type) {
        return MoonBreakApi.provider().toolOfType(type);
    }

    /**
     * Creates a new CustomTool instance from an {@link ItemStack} if the item is a tool
     * @param item the itemstack
     * @return the CustomTool if it is one
     */
    static Optional<CustomTool> from(ItemStack item) {
        return MoonBreakApi.provider().toolFromItem(item);
    }

    /**
     * Generates an {@link ItemStack} which this CustomTool represents.<br>
     * If this CustomTool is broken (see {@link CustomTool#broken()}), this method will always return {@link ItemStack#empty()}.
     * @return the itemstack
     */
    ItemStack itemStack();

    /**
     * Returns the ToolType of this CustomTool
     * @return the ToolType
     */
    CustomToolType type();

    /**
     * Returns the durability of this CustomTool
     * @return the durability
     */
    int durability();

    /**
     * Sets the durability of this CustomTool
     * @param durability the durability
     */
    void durability(int durability);

    /**
     * Decreases the durability of this CustomTool by a given amount, if the item is not unbreakable.
     * For decreasing the durability of an unbreakable item, use {@link CustomTool#durability(int)} instead.
     * @throws IllegalArgumentException if the amount is negative
     * @param amount the amount
     */
    default void damage(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be positive");
        if (unbreakable()) return;
        durability(durability() - amount);
    }

    /**
     * Completely repairs this CustomTool to it's maximum durability
     */
    default void repair() {
        durability(type().maxDurability());
    }

    /**
     * Increases the durability of this CustomTool by a given amount
     * @throws IllegalArgumentException if the amount is negative
     * @param amount the amount
     */
    default void repair(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be positive");
        durability(durability() + amount);
    }

    /**
     * Returns if this tool's durability reached 0 or lower
     * @return if the tool is broken
     */
    default boolean broken() {
        return durability() <= 0;
    }

    /**
     * Returns if this CustomTool is unbreakable
     * @return if it is unbreakable
     */
    boolean unbreakable();

    /**
     * Sets if this CustomTool is unbreakable
     * @param unbreakable if it is unbreakable
     */
    void unbreakable(boolean unbreakable);

}