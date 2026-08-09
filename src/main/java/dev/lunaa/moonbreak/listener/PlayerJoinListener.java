package dev.lunaa.moonbreak.listener;

import dev.lunaa.moonbreak.MoonBreak;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        MoonBreak.instance().previousBaseBlockBreakSpeed(e.getPlayer(), Objects.requireNonNull(e.getPlayer().getAttribute(Attribute.BLOCK_BREAK_SPEED)).getBaseValue());
    }

}
