package dev.lunaa.moonbreak.listener;

import dev.lunaa.moonbreak.tool.CustomTool;
import dev.lunaa.moonbreak.tool.CustomToolType;
import dev.lunaa.moonbreak.tool.CustomToolTypeImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageAbortEvent;

import java.util.Optional;

public class BlockDamageAbortListener implements Listener {

    @EventHandler
    public void onBlockDamageAbort(BlockDamageAbortEvent e) {
        Optional<CustomTool> optionalTool = CustomTool.from(e.getItemInHand());
        if (optionalTool.isEmpty()) return;
        CustomTool tool = optionalTool.get();
        ((CustomToolTypeImpl) tool.type()).executeHook(CustomToolType.EventHook.BLOCK_DAMAGE_ABORT, e, tool);
    }

}
