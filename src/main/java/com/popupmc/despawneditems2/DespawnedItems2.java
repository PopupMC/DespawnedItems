package com.popupmc.despawneditems2;

import com.popupmc.despawneditems2.commands.OnDespiCommand;
import com.popupmc.despawneditems2.config.Config;
import com.popupmc.despawneditems2.despawn.DespawnEffect;
import com.popupmc.despawneditems2.despawn.DespawnIndexes;
import com.popupmc.despawneditems2.despawn.DespawnProcess;
import com.popupmc.despawneditems2.events.OnItemDespawnEvent;
import com.popupmc.despawneditems2.manage.RemoveMaterials;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DespawnedItems2 extends JavaPlugin {
    @Override
    public void onEnable() {

        // Create classes
        this.config = new Config(this);
        this.despawnIndexes = new DespawnIndexes(this);

        // Create Listeners
        Bukkit.getPluginManager().registerEvents(new OnItemDespawnEvent(this), this);

        // Register /despi command executor
        Objects.requireNonNull(this.getCommand("despi")).setExecutor(new OnDespiCommand(this));

        getLogger().info("DespawnedItems2 is enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("DespawnedItems2 is disabled");
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
