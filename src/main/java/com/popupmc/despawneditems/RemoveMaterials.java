package com.popupmc.despawneditems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

// Remove material command, this can take a long time to complete so it's spread out over several server ticks here
public class RemoveMaterials {
    void removeMaterials(@NotNull CommandSender sender, ArrayList<Material> materials) {
        // Save data to class instance
        this.sender = sender;
        this.materials = materials;

        // Begin loop
        newLoop();
    }

    public void newLoop() {
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

        // Ensure chunk is loaded, if not load async, otherwise work directly with chunk
        if(!world.isChunkLoaded(loc.x, loc.z))
            loadWorld(world, loc);
        else
            worldIsLoaded(world, loc);
    }

    public void forceSelfDestroy() {
        // Remove saved reference that prevents GC (thus allowing GC)
        DespawnedItems.removeMaterialsInst = null;

        // Announce completion
        sender.sendMessage(ChatColor.GOLD + "Completed!");
    }

    // check if it needs to be self-destroyed or not
    public void checkSelfDestroy() {
        if (locationIndex >= DespawnedItemsConfig.locs.size()) {
            forceSelfDestroy();
        }
        else {
            newLoop();
        }
    }

    public void loadWorld(World world, LocationEntry loc) {
        world.getChunkAtAsync(loc.x, loc.z).thenRun(() -> worldIsLoaded(world, loc));
    }

    public void worldIsLoaded(World world, LocationEntry loc) {
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

    // Mark end of loop
    public void loopEnd() {
        // Increment loop index and see if it needs to be self-destroyed
        locationIndex += 1;
        checkSelfDestroy();
    }

    // Instance Data
    int locationIndex = 0;
    CommandSender sender;
    ArrayList<Material> materials;
}
