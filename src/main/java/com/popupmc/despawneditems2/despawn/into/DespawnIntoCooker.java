package com.popupmc.despawneditems2.despawn.into;

import com.popupmc.despawneditems2.DespawnedItems2;
import com.popupmc.despawneditems2.despawn.DespawnProcess;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class DespawnIntoCooker extends AbstractDespawnInto {

    public DespawnIntoCooker(@NotNull DespawnedItems2 plugin) {
        super(plugin);
    }

    @Override
    public boolean doesApply(@NotNull Block targetBlock) {
        Material targetMaterial = targetBlock.getType();

        return targetMaterial == Material.BLAST_FURNACE ||
                targetMaterial == Material.FURNACE ||
                targetMaterial == Material.SMOKER;
    }

    @Override
    public DespawnIntoResult despawnInto(@NotNull DespawnProcess process, @NotNull Block targetBlock) {
        // Get Inventory
        Inventory inventory = getInventory(targetBlock);
        if(inventory == null)
            return DespawnIntoResult.NONE;

        // Get smelt & fuel
        ItemStack smelt = inventory.getItem(0);
        ItemStack fuel = inventory.getItem(1);

        // Auto-add to existing items if able to
        if(smelt != null && smelt.getType() == process.item.getType())
            return addToStack(process, targetBlock, inventory, smelt, 0);
        else if(fuel != null && fuel.getType() == process.item.getType())
            return addToStack(process, targetBlock, inventory, fuel, 1);

        // By this point nothing could be added to existing slots
        // Proceed no further if both slots are filled
        if(smelt != null && fuel != null)
            return DespawnIntoResult.NONE;

        // Drop fuel in if empty and is fuel
        if(process.item.getType().isFuel() && fuel == null) {
            inventory.setItem(1, process.item);
            targetBlock.getState().update();
            return DespawnIntoResult.FULLY;
        }
        // Drop in item to smelt/cook if smeltable/cookable and cooking slot is empty
        else if(smelt == null && targetBlock.getType() == Material.BLAST_FURNACE && isInBlastingRecipe(process.item)) {
            inventory.setItem(0, process.item);
            targetBlock.getState().update();
            return DespawnIntoResult.FULLY;
        }
        else if(smelt == null && targetBlock.getType() == Material.FURNACE && isInFurnaceRecipe(process.item)) {
            inventory.setItem(0, process.item);
            targetBlock.getState().update();
            return DespawnIntoResult.FULLY;
        }
        else if(smelt == null && targetBlock.getType() == Material.SMOKER && isInSmokerRecipe(process.item)) {
            inventory.setItem(0, process.item);
            targetBlock.getState().update();
            return DespawnIntoResult.FULLY;
        }

        return DespawnIntoResult.NONE;
    }

    @Override
    public boolean removeFrom(@NotNull Material material, @NotNull Block targetBlock) {
        // Get Inventory
        Inventory inventory = getInventory(targetBlock);
        if(inventory == null)
            return false;

        inventory.remove(material);
        targetBlock.getState().update();
        return true;
    }

    @Override
    public boolean removeFrom(@NotNull ItemStack material, @NotNull Block targetBlock) {
        // Get Inventory
        Inventory inventory = getInventory(targetBlock);
        if(inventory == null)
            return false;

        inventory.remove(material);
        targetBlock.getState().update();
        return true;
    }

    // Simply adds the despawned item amount to a stack without going over
    public DespawnIntoResult addToStack(@NotNull DespawnProcess process,
                              @NotNull Block targetBlock,
                              @NotNull Inventory inventory,
                              @NotNull ItemStack toItem,
                              int slot) {
        // Add all at once
        toItem.setAmount(toItem.getAmount() + process.item.getAmount());

        // Get new amount
        int newAmount = toItem.getAmount();

        // If oversized
        if(newAmount > toItem.getType().getMaxStackSize()) {

            // Correct stack size to max
            toItem.setAmount(toItem.getType().getMaxStackSize());

            // Get difference
            newAmount = newAmount - toItem.getType().getMaxStackSize();

            // Apply leftover to item
            process.item.setAmount(newAmount);

            // Update inventory
            inventory.setItem(slot, toItem);
            targetBlock.getState().update();

            // End Loop to begin next loop
            return DespawnIntoResult.PARTIALLY;
        }
        else {
            // Update inventory and stop as we're done
            inventory.setItem(slot, toItem);
            targetBlock.getState().update();
            return DespawnIntoResult.FULLY;
        }
    }

    public boolean isInBlastingRecipe(ItemStack item) {
        Iterator<Recipe> recipes = Bukkit.getServer().recipeIterator();

        while (recipes.hasNext()) {
            Recipe recipe = recipes.next();

            if (!(recipe instanceof BlastingRecipe))
                continue;

            BlastingRecipe blastingRecipe = (BlastingRecipe)recipe;
            if(blastingRecipe.getInput().getType() != item.getType())
                continue;

            return true;
        }

        return false;
    }

    public boolean isInFurnaceRecipe(ItemStack item) {
        Iterator<Recipe> recipes = Bukkit.getServer().recipeIterator();

        while (recipes.hasNext()) {
            Recipe recipe = recipes.next();

            if (!(recipe instanceof FurnaceRecipe))
                continue;

            FurnaceRecipe furnaceRecipe = (FurnaceRecipe)recipe;
            if(furnaceRecipe.getInput().getType() != item.getType())
                continue;

            return true;
        }

        return false;
    }

    public boolean isInSmokerRecipe(ItemStack item) {
        Iterator<Recipe> recipes = Bukkit.getServer().recipeIterator();

        while (recipes.hasNext()) {
            Recipe recipe = recipes.next();

            if (!(recipe instanceof SmokingRecipe))
                continue;

            SmokingRecipe smokingRecipe = (SmokingRecipe)recipe;
            if(smokingRecipe.getInput().getType() != item.getType())
                continue;

            return true;
        }

        return false;
    }
}
