package com.popupmc.despawneditems;

import com.popupmc.despawneditems.commands.OnDespiCommand;
import com.popupmc.despawneditems.config.Config;
import com.popupmc.despawneditems.despawn.DespawnEffect;
import com.popupmc.despawneditems.despawn.DespawnIndexes;
import com.popupmc.despawneditems.despawn.DespawnProcess;
import com.popupmc.despawneditems.events.OnItemDespawnEvent;
import com.popupmc.despawneditems.manage.RemoveMaterials;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DespawnedItems extends JavaPlugin {
    @Override
    public void onEnable() {

        // Create classes
        this.config = new Config(this);
        this.despawnIndexes = new DespawnIndexes(this);

        // Create Listeners
        Bukkit.getPluginManager().registerEvents(new OnItemDespawnEvent(this), this);

        // Register /despi command executor
        Objects.requireNonNull(this.getCommand("despi")).setExecutor(new OnDespiCommand(this));

        getLogger().info("DespawnedItems is enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("DespawnedItems is disabled");
    }

    // Pass tab completion to separate class for code cleanliness
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return super.onTabComplete(sender, command, alias, args);
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
