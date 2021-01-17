package com.popupmc.despawneditems;

import org.bukkit.*;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

public class OnItemDespawnEvent implements Listener {
    @EventHandler
    public void onItemDespawn(final ItemDespawnEvent event) {

        // Get item stack
        ItemStack item = event.getEntity().getItemStack();

        // Find a chest that works
        // We're just randomly searching for a location that works
        // However, to prevent a possible infinite loop, use the number of locations as a stop measure
        for(LocationEntry ignored : DespawnedItemsConfig.locs) {

            // Get random location of random chest
            LocationEntry loc = randomChestCoord();

            // Get the world it's in
            World world = Bukkit.getWorld(loc.world);

            // Skip if the world is invalid (Such as being renamed)
            if(world == null) {
                DespawnedItems.plugin.getLogger().log(Level.WARNING, "World " + loc.world + " is null, skipping...");
                continue;
            }

            // Ensure chunk is loaded
            if(!world.isChunkLoaded(loc.x, loc.z))
                world.loadChunk(loc.x, loc.z);

            // Get block and it's inventory, this ensures it's valid and useable
            Block block = world.getBlockAt(loc.x, loc.y, loc.z);
            Inventory inv = getInventory(block);
            if(inv == null) {
                DespawnedItems.plugin.getLogger().log(Level.WARNING, "Block at coords " + loc.x + ", " + loc.y + ", " + loc.z + " isnt a chest or barrel. Skipping...");
                continue;
            }

            // For efficiency, just add the whole stack, don't check for available space
            // Bukkit already checks for that, leftover is how much was added if anything
            HashMap<Integer, ItemStack> leftover = inv.addItem(item.clone());

            // Play effect only if sound and/or particles enabled
            if(DespawnedItemsConfig.soundEnabled || DespawnedItemsConfig.particlesEnabled) {
                // Play particles and sound for a short time
                PlayEffect play = new PlayEffect();
                play.play(block.getLocation());

                // Save reference so it's not GC
                DespawnedItems.effectsPlaying.add(play);
            }

            // Check if any didnt get added and stop here if the whole stack could get added
            if(leftover.isEmpty())
                break;

            // Clear out item stack
            item = null;

            // Add leftover items back in, this way also accounts for oversized stacks
            for(Map.Entry<Integer, ItemStack> leftOverEntry : leftover.entrySet()) {

                // On First Run it will be null from above, get the remainder item stack
                if(item == null)
                    item = leftOverEntry.getValue();

                // Further loops means the stack was oversized, `addItem` breaks oversized stacks
                // into proper size stacks, lets make it oversized again to keep things simple
                // On further loops it won't be null from above, add to the item stack
                else
                    item.add(leftOverEntry.getValue().getAmount());
            }

            // Now we startover with a new random location to try again
        }
    }

    // Returns if the block is a valid useable block this plugin can work with
    // by returning it's inventory or null if it's invalid
    static Inventory getInventory(Block block) {
        if(block.getType() == Material.CHEST)
            return getChestInventory(block);
        else if(block.getType() == Material.BARREL)
            return getBarrelInventory(block);

        return null;
    }

    static Inventory getChestInventory(Block block) {
        Chest blockState = (Chest)block.getState();
        return blockState.getInventory();
    }

    static Inventory getBarrelInventory(Block block) {
        Barrel blockState = (Barrel) block.getState();
        return blockState.getInventory();
    }

    // Reset random indexes
    static void newRndIndexes() {
        // Clear list
        rndIndexes.clear();

        // Add in all the location indexes
        int counter = 0;
        for(LocationEntry ignored : DespawnedItemsConfig.locs) {
            rndIndexes.add(counter);
            counter++;
        }
    }

    // Obtain a random coord
    static LocationEntry randomChestCoord() {
        // Regen if empty
        if(rndIndexes.size() <= 0)
            newRndIndexes();

        // Get random index of random indexes
        int rndInd = rand.nextInt(rndIndexes.size());

        // Get location index from random index
        int locInd = rndIndexes.get(rndInd);

        // Get location with location index
        LocationEntry ret = DespawnedItemsConfig.locs.get(locInd);

        // Remove used random index
        rndIndexes.remove(rndInd);

        return ret;
    }

    static ArrayList<Integer> rndIndexes = new ArrayList<>();
    static Random rand = new Random();
}
