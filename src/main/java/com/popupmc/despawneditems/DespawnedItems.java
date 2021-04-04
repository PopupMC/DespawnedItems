package com.popupmc.despawneditems;

import com.popupmc.despawneditems.commands.OnDespiCommand;
import com.popupmc.despawneditems.commands.OnRecycleCommand;
import com.popupmc.despawneditems.config.Config;
import com.popupmc.despawneditems.despawn.DespawnEffect;
import com.popupmc.despawneditems.despawn.DespawnIndexes;
import com.popupmc.despawneditems.despawn.DespawnProcess;
import com.popupmc.despawneditems.events.OnItemDespawnEvent;
import com.popupmc.despawneditems.manage.RemoveMaterials;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class DespawnedItems extends JavaPlugin {
    @Override
    public void onEnable() {

        BlacklistedItems.setup();

        // Create classes
        this.config = new Config(this);
        this.despawnIndexes = new DespawnIndexes(this);

        // Create Listeners
        Bukkit.getPluginManager().registerEvents(new OnItemDespawnEvent(this), this);

        // Register /despi command executor
        PluginCommand command = this.getCommand("despi");
        if(command == null) {
            getLogger().warning("Command /despi is null");
            this.setEnabled(false);
            return;
        }

        OnDespiCommand cmd = new OnDespiCommand(this);
        command.setExecutor(cmd);
        command.setTabCompleter(cmd);

        Objects.requireNonNull(getCommand("recycle")).setExecutor(new OnRecycleCommand(this));

        getLogger().info("DespawnedItems is enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("DespawnedItems is disabled");
    }

    public Config config;
    public DespawnIndexes despawnIndexes;

    // Holds effects being played currently
    public ArrayList<DespawnEffect> effectsPlaying = new ArrayList<>();

    // Holds remove materials command being executed
    // This takes a long time to execute and so it must be spanned out over several
    // server ticks
    public Hashtable<UUID, RemoveMaterials> removeMaterialsInst = new Hashtable<>();

    // Holds instances to item despawns currently being processed
    public ArrayList<DespawnProcess> despawnProcesses = new ArrayList<>();
}
