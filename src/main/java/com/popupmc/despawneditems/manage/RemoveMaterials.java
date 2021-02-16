package com.popupmc.despawneditems.manage;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.config.LocationEntry;
import com.popupmc.despawneditems.despawn.DespawnProcess;
import com.popupmc.despawneditems.despawn.into.AbstractDespawnInto;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public class RemoveMaterials {

    public RemoveMaterials(@NotNull CommandSender sender,
                           @Nullable ArrayList<Material> materials,
                           @Nullable ItemStack item,
                           @NotNull DespawnedItems plugin,
                           @Nullable UUID owner,
                           @Nullable UUID senderID) {
        this.plugin = plugin;
        this.sender = sender;
        this.materials = materials;
        this.owner = owner;
        this.item = item;
        this.senderID = senderID;

        if(plugin.config.fileLocations.locationEntries.size() == 0) {
            this.forceSelfDestroy();
            return;
        }

        if(materials == null && item == null) {
            sender.sendMessage(ChatColor.RED + "ERROR: Both material and item were null, refusing to start task...");
            invalid = true;
            return;
        }

        if(senderID != null) {
            if(plugin.removeMaterialsInst.containsKey(senderID)) {
                plugin.removeMaterialsInst.get(senderID).forceSelfDestroy();
                plugin.removeMaterialsInst.remove(senderID);
            }

            plugin.removeMaterialsInst.put(senderID, this);
        }

        // Grab list of locations
        if(owner != null) {
            this.senderLocationEntries = plugin.config.fileLocations.existsAll(owner);
        }
        else
            this.senderLocationEntries = new ArrayList<>(plugin.config.fileLocations.locationEntries);

        newLoop();
    }

    public void newLoop() {
        if(invalid)
            return;

        // Wait a tick
        new BukkitRunnable() {
            @Override
            public void run() {
                // Get location entry
                LocationEntry locationEntry = senderLocationEntries.get(locationIndex);

                // Load async
                loadWorld(locationEntry);
            }
        }.runTaskLater(plugin, 1);
    }

    public void forceSelfDestroy() {
        // Remove saved reference that prevents GC (thus allowing GC)
        plugin.removeMaterialsInst.remove(senderID);

        invalid = true;

        // Announce completion
        sender.sendMessage(ChatColor.GOLD + "Completed!");
    }

    // check if it needs to be self-destroyed or not
    public void checkSelfDestroy() {
        if(invalid)
            return;

        if (locationIndex >= senderLocationEntries.size()) {
            forceSelfDestroy();
        }
        else {
            newLoop();
        }
    }

    public void loadWorld(@NotNull LocationEntry locationEntry) {
        if(invalid)
            return;

        Location location = locationEntry.location;
        location.getWorld().getChunkAtAsync(location.getBlockX(), location.getBlockZ())
                .thenRun(() -> worldIsLoaded(locationEntry));
    }

    public void worldIsLoaded(LocationEntry locationEntry) {
        if(invalid)
            return;

        // Get Location
        Location location = locationEntry.location;

        // Get block and it's inventory, this ensures it's valid and useable
        Block block = location.getWorld()
                .getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        // Go through the despawnable intos list
        for(AbstractDespawnInto despawnInto : DespawnProcess.despawnIntos) {
            if(invalid)
                return;

            // Skip if doesn't apply to this into
            if(!despawnInto.doesApply(block))
                continue;

            // We've found the into for this location

            // Pass control to the into
            if(materials != null) {
                for (Material material : materials) {
                    if(invalid)
                        return;

                    despawnInto.removeFrom(material, block);
                }
            }

            if(item != null) {
                despawnInto.removeFrom(item, block);
            }

            // Announce progress every 20 inventory
            if((locationIndex % 20) == 0)
                sender.sendMessage(ChatColor.YELLOW + "Still processing... " +
                        locationIndex + " / " +
                        senderLocationEntries.size());

            // Onto the next location
            break;
        }

        // Try another location
        loopEnd();
    }

    // Mark end of loop
    public void loopEnd() {
        if(invalid)
            return;

        // Increment loop index and see if it needs to be self-destroyed
        locationIndex += 1;
        checkSelfDestroy();
    }

    // Instance Data
    int locationIndex = 0;
    public final CommandSender sender;
    public final ArrayList<Material> materials;
    public final ItemStack item;
    public final UUID owner;
    public final UUID senderID;
    public ArrayList<LocationEntry> senderLocationEntries;

    public boolean invalid = false;

    // Plugin
    public final DespawnedItems plugin;
}
