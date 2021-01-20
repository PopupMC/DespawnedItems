package com.popupmc.despawneditems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

// Remove material command, this can take a long time to complete so it's spread out over several server ticks here
public class RemoveMaterials {
    void removeMaterials(@NotNull CommandSender sender, ArrayList<Material> materials) {

        this.sender = sender;

        inst = new BukkitRunnable() {
            @Override
            public void run() {
                // Extra precaution before getting location entry
                if(locationIndex >= DespawnedItemsConfig.locs.size()) {
                    sender.sendMessage(ChatColor.RED + "Index is out of bounds terminating early...");
                    forceSelfDestroy();
                    return;
                }

                // Get location entry
                LocationEntry loc = DespawnedItemsConfig.locs.get(locationIndex);

                // Get the world it's in
                World world = Bukkit.getWorld(loc.world);

                // Skip if the world is invalid (Such as being renamed)
                if(world == null) {
                    sender.sendMessage(ChatColor.GOLD + "World " + loc.world + " is null, skipping...");
                    loopEnd();
                    return;
                }

                // Ensure chunk is loaded
                if(!world.isChunkLoaded(loc.x, loc.z))
                    world.loadChunk(loc.x, loc.z);

                // Get block and it's inventory, this ensures it's valid and useable
                Block block = world.getBlockAt(loc.x, loc.y, loc.z);
                Inventory inv = OnItemDespawnEvent.getInventory(block);
                if(inv == null) {
                    sender.sendMessage(ChatColor.GOLD + "Block at coords " + loc.x + ", " + loc.y + ", " + loc.z + " isnt a chest or barrel. Skipping...");
                    loopEnd();
                    return;
                }

                // Remove all item stacks of that material in the inventory
                for(Material material : materials) {
                    inv.remove(material);
                }

                // Announce progress every 20 inventory
                if((locationIndex % 20) == 0)
                    sender.sendMessage(ChatColor.YELLOW + "Still processing... " + locationIndex + " / " + DespawnedItemsConfig.locs.size());

                // End of loop
                loopEnd();
            }

            // Run on the next tick at given interval
        }.runTaskTimer(DespawnedItems.plugin, 1, 2);
    }

    public void forceSelfDestroy() {
        // Remove saved reference that prevents GC (thus allowing GC)
        DespawnedItems.removeMaterialsInst = null;

        // Tell Bukkit to stop the task
        inst.cancel();

        // Announce completion
        sender.sendMessage(ChatColor.GOLD + "Completed!");
    }

    // check if it needs to be self-destroyed or not
    public void checkSelfDestroy() {
        if (locationIndex >= DespawnedItemsConfig.locs.size()) {
            forceSelfDestroy();
        }
    }

    // Mark end of loop
    public void loopEnd() {
        // Decrement loops left and see if it needs to be self-destroyed
        locationIndex += 1;
        checkSelfDestroy();
    }

    // Instance Data
    int locationIndex = 0;
    CommandSender sender;
    BukkitTask inst = null;  // Holds inst so it's not GC
}
