package com.popupmc.despawneditems;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static com.popupmc.despawneditems.OnItemDespawnEvent.getInventory;
import static com.popupmc.despawneditems.OnItemDespawnEvent.randomChestCoord;

public class ProcessItemDespawn {
    public ProcessItemDespawn(ItemStack item) {
        // Save item
        this.item = item;

        // Start loop
        newLoop();
    }

    // Begins a new loop
    public void newLoop() {
        // Get random location of random chest
        LocationEntry loc = randomChestCoord();

        // Get the world it's in
        World world = Bukkit.getWorld(loc.world);

        // Skip if the world is invalid (Such as being renamed)
        if(world == null) {
            DespawnedItems.plugin.getLogger().log(Level.WARNING, "World " + loc.world + " is null, skipping...");
            endLoop();
            return;
        }

        // Ensure chunk is loaded, if not async load it first
        if(!world.isChunkLoaded(loc.x, loc.z))
            loadWorld(world, loc, item);

            // Otherwise proceed to directly work with the chunk
        else
            worldIsLoaded(world, loc, item);
    }

    // When a loop has ended and is ready to self-destroy or enter a new loop
    public void endLoop() {
        // Decrement loops left (To prevent infinite loops)
        loopsLeft--;

        // If no more loops left then stop here and self-destroy
        // to prevent infinite loops
        if(loopsLeft <= 0) {
            selfDestroy();
            return;
        }

        // Begin a new loop
        newLoop();
    }

    // Simply remove this instance and call no more new loops
    public void selfDestroy() {
        DespawnedItems.processItemDespawns.remove(this);
    }

    // Async loads the world first
    public void loadWorld(World world, LocationEntry loc, ItemStack item) {
        world.getChunkAtAsync(loc.x, loc.z).thenRun(() -> worldIsLoaded(world, loc, item));
    }

    // With the chunk loaded we can go ahead and directly work with it
    public void worldIsLoaded(World world, LocationEntry loc, ItemStack item) {
        // Get block and it's inventory, this ensures it's valid and useable
        Block block = world.getBlockAt(loc.x, loc.y, loc.z);
        Inventory inv = getInventory(block);
        if(inv == null) {
            DespawnedItems.plugin.getLogger().log(Level.WARNING, "Block at coords " + loc.x + ", " + loc.y + ", " + loc.z + " isnt a chest or barrel. Skipping...");
            endLoop();
            return;
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
        if(leftover.isEmpty()) {
            selfDestroy();
            return;
        }

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
        endLoop();
    }

    public ItemStack item;
    public int loopsLeft = DespawnedItemsConfig.locs.size();
}
