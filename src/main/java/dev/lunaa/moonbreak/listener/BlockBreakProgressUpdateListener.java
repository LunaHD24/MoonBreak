package dev.lunaa.moonbreak.listener;

import dev.lunaa.moonbreak.tool.CustomTool;
import dev.lunaa.moonbreak.tool.CustomToolType;
import dev.lunaa.moonbreak.tool.CustomToolTypeImpl;
import io.papermc.paper.event.block.BlockBreakProgressUpdateEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

public class BlockBreakProgressUpdateListener implements Listener {

    @EventHandler
    public void onBlockDamageUpdate(BlockBreakProgressUpdateEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        Optional<CustomTool> optionalTool = CustomTool.fromPlayer(player);
        if (optionalTool.isEmpty()) return;
        CustomTool tool = optionalTool.get();
        ((CustomToolTypeImpl) tool.type()).executeHook(CustomToolType.EventHook.BLOCK_DAMAGE_UPDATE, e, tool);
    }

}
