package com.popupmc.despawneditems;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DespawnedItems extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        // Save Plugin
        plugin = this;

        // Load Config Locations
        DespawnedItemsConfig.load();

        // Register Item DespawnIndexes Event
        Bukkit.getPluginManager().registerEvents(new OnItemDespawnEvent(), this);

        // Register /despi command executor
        Objects.requireNonNull(this.getCommand("despi")).setExecutor(new OnDespiCommand());

        // Log enabled status
        getLogger().info("DespawnedItems is enabled.");
    }

    // Log disabled status
    @Override
    public void onDisable() {
        getLogger().info("DespawnedItems is disabled");
    }

    // Pass tab completion to separate class for code cleanliness
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return OnTabComplete.onTabComplete(sender, command, alias, args);
    }

    // Static copy of the plugin instance
    static JavaPlugin plugin;

    // Holds effects being played currently
    static ArrayList<PlayEffect> effectsPlaying = new ArrayList<>();

    // Holds remove materials command being executed
    // This takes a long time to execute and so it must be spanned out over several
    // server ticks
    static RemoveMaterials removeMaterialsInst = null;

    // Holds instances to item despawns currently being processed
    public static ArrayList<ProcessItemDespawn> processItemDespawns = new ArrayList<>();
}
