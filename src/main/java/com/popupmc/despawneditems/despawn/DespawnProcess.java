package com.popupmc.despawneditems.despawn;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.config.LocationEntry;
import com.popupmc.despawneditems.despawn.into.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DespawnProcess {
    public DespawnProcess(@NotNull ItemStack item, @NotNull DespawnedItems plugin) {
        this.plugin = plugin;
        this.item = item;
        this.loopsLeft = plugin.config.fileLocations.locationEntries.size();

        plugin.despawnProcesses.add(this);

        // These are the functionality that attempts to despawnIndexes a given item into something
        // the order matters
        if(despawnIntos.isEmpty()) {
            // Always try to delete first entirely if it's illegal
            despawnIntos.add(new DespawnIntoVoid(plugin));

            // Then move into cooker
            despawnIntos.add(new DespawnIntoCooker(plugin));

            // Then place in the air
            despawnIntos.add(new DespawnIntoAir(plugin));

            // Then place into storage
            despawnIntos.add(new DespawnIntoStorage(plugin));
        }

        newLoop();
    }

    // Begins a new loop
    public void newLoop() {
        if(invalid)
            return;

        // Delay by 1 tick
        new BukkitRunnable() {
            @Override
            public void run() {
                // Get random location of random chest
                LocationEntry locationEntry = plugin.despawnIndexes.randomChestCoord();

                // Load the chunk
                loadWorld(locationEntry);
            }
        }.runTaskLater(plugin, 1);
    }

    // Async loads the world first
    public void loadWorld(@NotNull LocationEntry locationEntry) {
        if(invalid)
            return;

        Location location = locationEntry.location;
        location.getWorld().getChunkAtAsync(location.getBlockX(), location.getBlockZ())
                .thenRun(() -> worldIsLoaded(locationEntry));
    }

    // When a loop has ended and is ready to self-destroy or enter a new loop
    public void endLoop() {
        if(invalid)
            return;

        // Decrement loops left (To prevent infinite loops)
        loopsLeft--;

        // If no more loops left then stop here and self-destroy
        // to prevent infinite loops
        if(loopsLeft <= 0) {
            plugin.getLogger().warning("WARNING: Despawn ended without being able to find a location");
            plugin.getLogger().warning("WARNING: Is everything full?");
            selfDestroy();
            return;
        }

        // Begin a new loop
        newLoop();
    }

    // Simply remove this instance and call no more new loops
    public void selfDestroy() {
        plugin.despawnProcesses.remove(this);
        invalid = true;
    }

    // With the chunk loaded we can go ahead and directly work with it
    public void worldIsLoaded(@NotNull LocationEntry locationEntry) {
        if(invalid)
            return;

        // Get Location
        Location targetLocation = locationEntry.location;

        // Get targetBlock and it's inventory, this ensures it's valid and useable
        Block targetBlock = targetLocation.getWorld()
                .getBlockAt(targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());

        // Go through the despawnable intos list
        for(AbstractDespawnInto despawnInto : despawnIntos) {
            if(invalid)
                return;

            // Skip if doesn't apply to this into
            if(!despawnInto.doesApply(targetBlock))
                continue;

            // We've found the into for this location

            // Pass control to the into
            DespawnIntoResult result = despawnInto.despawnInto(this, targetBlock);

            // If result was a partial or full transfer then play effect
            if(result == DespawnIntoResult.PARTIALLY || result == DespawnIntoResult.FULLY)
                playEffect(locationEntry);

            // If a full transfer or contraband transfer end here
            if(result == DespawnIntoResult.FULLY || result == DespawnIntoResult.CONTRABAND) {
                selfDestroy();
                return;
            }

            // Onto the next location
            break;
        }

        // Try another location
        endLoop();
    }

    public void playEffect(@NotNull LocationEntry locationEntry) {
        if(invalid)
            return;

        if(plugin.config.fileConfig.soundEnabled || plugin.config.fileConfig.particlesEnabled) {
            // Play particles and sound for a short time
            new DespawnEffect(locationEntry, plugin);
        }
    }

    public final DespawnedItems plugin;
    public ItemStack item;
    public int loopsLeft;

    public boolean invalid = false;

    public final static ArrayList<AbstractDespawnInto> despawnIntos = new ArrayList<>();
}
