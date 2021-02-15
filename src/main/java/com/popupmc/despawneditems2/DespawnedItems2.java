package com.popupmc.despawneditems2;

import com.popupmc.despawneditems2.config.Config;
import com.popupmc.despawneditems2.despawn.DespawnEffect;
import com.popupmc.despawneditems2.despawn.DespawnIndexes;
import com.popupmc.despawneditems2.despawn.DespawnProcess;
import com.popupmc.despawneditems2.events.OnItemDespawnEvent;
import com.popupmc.despawneditems2.manage.RemoveMaterials;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class DespawnedItems2 extends JavaPlugin {
    @Override
    public void onEnable() {

        // Create classes
        this.config = new Config(this);
        this.despawnIndexes = new DespawnIndexes(this);

        // Create Listeners
        Bukkit.getPluginManager().registerEvents(new OnItemDespawnEvent(this), this);

        getLogger().info("DespawnedItems2 is enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("DespawnedItems2 is disabled");
    }

    public Config config;
    public DespawnIndexes despawnIndexes;

    // Holds effects being played currently
    public ArrayList<DespawnEffect> effectsPlaying = new ArrayList<>();

    // Holds remove materials command being executed
    // This takes a long time to execute and so it must be spanned out over several
    // server ticks
    public RemoveMaterials removeMaterialsInst = null;

    // Holds instances to item despawns currently being processed
    public ArrayList<DespawnProcess> despawnProcesses = new ArrayList<>();
}
