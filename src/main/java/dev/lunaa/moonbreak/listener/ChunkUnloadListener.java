package dev.lunaa.moonbreak.listener;

import dev.lunaa.moonbreak.MoonBreak;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkUnloadListener implements Listener {

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        if (!e.isSaveChunk()) return;
        MoonBreak.instance().blockLoader().unloadChunk(e.getChunk());
    }

}
