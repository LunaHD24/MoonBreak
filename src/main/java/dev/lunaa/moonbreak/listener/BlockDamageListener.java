package dev.lunaa.moonbreak.listener;

import dev.lunaa.moonbreak.tool.CustomTool;
import dev.lunaa.moonbreak.tool.CustomToolType;
import dev.lunaa.moonbreak.tool.CustomToolTypeImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

import java.util.Optional;

public class BlockDamageListener implements Listener {

    @EventHandler
    public void onBlockDamage(BlockDamageEvent e) {
        Optional<CustomTool> optionalTool = CustomTool.from(e.getItemInHand());
        if (optionalTool.isEmpty()) return;
        CustomTool tool = optionalTool.get();
        ((CustomToolTypeImpl) tool.type()).executeHook(CustomToolType.EventHook.BLOCK_DAMAGE, e, tool);
    }

}
