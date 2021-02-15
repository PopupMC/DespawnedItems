package com.popupmc.despawneditems.despawn.into;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.despawn.DespawnProcess;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DespawnIntoStorage extends AbstractDespawnInto {
    public DespawnIntoStorage(@NotNull DespawnedItems plugin) {
        super(plugin);
    }

    @Override
    public boolean doesApply(@NotNull Block targetBlock) {

        Material targetMaterial = targetBlock.getType();

        return targetMaterial == Material.BARREL ||
                targetMaterial == Material.CHEST ||
                targetMaterial == Material.DISPENSER ||
                targetMaterial == Material.DROPPER ||
                targetMaterial == Material.HOPPER ||
                targetMaterial == Material.SHULKER_BOX ||
                targetMaterial == Material.TRAPPED_CHEST;
    }

    @Override
    public DespawnIntoResult despawnInto(@NotNull DespawnProcess process, @NotNull Block targetBlock) {
        Inventory inventory = getInventory(targetBlock);
        if(inventory == null) {
            return DespawnIntoResult.NONE;
        }

        HashMap<Integer, ItemStack> leftover = inventory.addItem(process.item.clone());
        targetBlock.getState().update();

        // Check if any didnt get added and stop here if the whole stack could get added
        if(leftover.isEmpty()) {
            return DespawnIntoResult.FULLY;
        }

        // Clear out item stack
        process.item = null;

        // Add leftover items back in, this way also accounts for oversized stacks
        for(Map.Entry<Integer, ItemStack> leftOverEntry : leftover.entrySet()) {

            // On First Run it will be null from above, get the remainder item stack
            if(process.item == null)
                process.item = leftOverEntry.getValue();

                // Further loops means the stack was oversized, `addItem` breaks oversized stacks
                // into proper size stacks, lets make it oversized again to keep things simple
                // On further loops it won't be null from above, add to the item stack
            else
                process.item.add(leftOverEntry.getValue().getAmount());
        }

        // Now we startover with a new random location to try again
        return DespawnIntoResult.PARTIALLY;
    }

    @Override
    public void removeFrom(@NotNull Material material, @NotNull Block targetBlock) {
        Inventory inventory = getInventory(targetBlock);
        if(inventory == null) {
            return;
        }

        inventory.remove(material);
        targetBlock.getState().update();

    }

    @Override
    public void removeFrom(@NotNull ItemStack material, @NotNull Block targetBlock) {
        Inventory inventory = getInventory(targetBlock);
        if(inventory == null) {
            return;
        }

        inventory.remove(material);
        targetBlock.getState().update();

    }
}
