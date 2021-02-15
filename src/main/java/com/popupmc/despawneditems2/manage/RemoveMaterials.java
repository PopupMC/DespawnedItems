package com.popupmc.despawneditems2.manage;

import com.popupmc.despawneditems2.DespawnedItems2;
import com.popupmc.despawneditems2.config.LocationEntry;
import com.popupmc.despawneditems2.despawn.DespawnProcess;
import com.popupmc.despawneditems2.despawn.into.AbstractDespawnInto;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public class RemoveMaterials {

    public RemoveMaterials(@NotNull CommandSender sender,
                           @NotNull ArrayList<Material> materials,
                           @NotNull DespawnedItems2 plugin,
                           @Nullable UUID owner) {
        this.plugin = plugin;
        this.sender = sender;
        this.materials = materials;
        this.owner = owner;

        newLoop();
    }

    public void newLoop() {
        // Wait a tick
        new BukkitRunnable() {
            @Override
            public void run() {
                // Get location entry
                LocationEntry locationEntry = plugin.config.fileLocations.locationEntries.get(locationIndex);

                // Skip if owner is provided but doesn't match location
                if(owner != null && !locationEntry.equals(owner)) {
                    loopEnd();
                    return;
                }

                // Load async
                loadWorld(locationEntry);
            }
        }.runTaskLater(plugin, 1);
    }

    public void forceSelfDestroy() {
        // Remove saved reference that prevents GC (thus allowing GC)
        plugin.removeMaterialsInst = null;

        // Announce completion
        sender.sendMessage(ChatColor.GOLD + "Completed!");
    }

    // check if it needs to be self-destroyed or not
    public void checkSelfDestroy() {
        if (locationIndex >= plugin.config.fileLocations.locationEntries.size()) {
            forceSelfDestroy();
        }
        else {
            newLoop();
        }
    }

    public void loadWorld(@NotNull LocationEntry locationEntry) {
        Location location = locationEntry.location;
        location.getWorld().getChunkAtAsync(location.getBlockX(), location.getBlockZ())
                .thenRun(() -> worldIsLoaded(locationEntry));
    }

    public void worldIsLoaded(LocationEntry locationEntry) {

        // Get Location
        Location location = locationEntry.location;

        // Get block and it's inventory, this ensures it's valid and useable
        Block block = location.getWorld()
                .getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        // Go through the despawnable intos list
        for(AbstractDespawnInto despawnInto : DespawnProcess.despawnIntos) {

            // Skip if doesn't apply to this into
            if(!despawnInto.doesApply(block))
                continue;

            // We've found the into for this location

            // Pass control to the into
            for(Material material : materials) {
                despawnInto.removeFrom(material, block);
            }

            // Announce progress every 20 inventory
            if((locationIndex % 20) == 0)
                sender.sendMessage(ChatColor.YELLOW + "Still processing... " +
                        locationIndex + " / " +
                        plugin.config.fileLocations.locationEntries.size());

            // Onto the next location
            break;
        }

        // Try another location
        loopEnd();
    }

    // Mark end of loop
    public void loopEnd() {
        // Increment loop index and see if it needs to be self-destroyed
        locationIndex += 1;
        checkSelfDestroy();
    }

    // Instance Data
    int locationIndex = 0;
    public final CommandSender sender;
    public final ArrayList<Material> materials;
    public final UUID owner;

    // Plugin
    public final DespawnedItems2 plugin;
}
