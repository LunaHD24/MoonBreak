package dev.lunaa.moonbreak.listener;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import dev.lunaa.moonbreak.MoonBreak;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ServerTickStartListener implements Listener {

    private int tickCount = 0;

    @EventHandler
    public void onTickEnd(ServerTickStartEvent e) {
        MoonBreak.instance().breakingService().updateBreakSpeeds();

        tickCount++;
        if (tickCount >= 5 * 60 * 20) {
            if (!MoonBreak.instance().getServer().getPluginManager().isPluginEnabled(MoonBreak.instance())) {
                return;
            }
            MoonBreak.instance().blockLoader().saveAllBlocks(true);
            tickCount = 0;
        }
    }

}
