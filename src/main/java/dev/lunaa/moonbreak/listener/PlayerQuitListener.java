package dev.lunaa.moonbreak.listener;

import dev.lunaa.moonbreak.MoonBreak;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        MoonBreak.instance().breakingService().removeTrackedPlayer(player);
    }

}
