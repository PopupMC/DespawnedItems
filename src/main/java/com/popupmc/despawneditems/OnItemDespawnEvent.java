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
        // Clone to prevent potential errors from using a removed item stack
        ItemStack item = event.getEntity().getItemStack().clone();

        // Begin to process it async
        DespawnedItems.processItemDespawns.add(new ProcessItemDespawn(item));
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
