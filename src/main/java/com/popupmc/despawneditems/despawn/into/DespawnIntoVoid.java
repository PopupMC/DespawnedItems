package com.popupmc.despawneditems.despawn.into;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.despawn.DespawnProcess;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DespawnIntoVoid extends AbstractDespawnInto {
    public DespawnIntoVoid(@NotNull DespawnedItems plugin) {
        super(plugin);
    }

    @Override
    public boolean doesApply(@NotNull Block targetBlock) {
        return true;
    }

    @Override
    public DespawnIntoResult despawnInto(@NotNull DespawnProcess process, @NotNull Block targetBlock) {

        Material itemType = process.item.getType();

        if(itemType == Material.COMMAND_BLOCK ||
                itemType == Material.COMMAND_BLOCK_MINECART ||
                itemType == Material.CHAIN_COMMAND_BLOCK ||
                itemType == Material.REPEATING_COMMAND_BLOCK ||
                itemType == Material.DEBUG_STICK ||
                itemType == Material.JIGSAW ||
                itemType == Material.STRUCTURE_BLOCK ||
                itemType == Material.STRUCTURE_VOID ||
                itemType == Material.NETHERITE_AXE ||
                itemType == Material.NETHERITE_BLOCK ||
                itemType == Material.NETHERITE_BOOTS ||
                itemType == Material.NETHERITE_CHESTPLATE ||
                itemType == Material.NETHERITE_HELMET ||
                itemType == Material.NETHERITE_HOE ||
                itemType == Material.NETHERITE_INGOT ||
                itemType == Material.NETHERITE_LEGGINGS ||
                itemType == Material.NETHERITE_PICKAXE ||
                itemType == Material.NETHERITE_SCRAP ||
                itemType == Material.NETHERITE_SHOVEL ||
                itemType == Material.NETHERITE_SWORD ||
                itemType == Material.ANCIENT_DEBRIS) {
            process.item.setAmount(0);
            process.item.setType(Material.AIR);
            return DespawnIntoResult.CONTRABAND;
        }

        return DespawnIntoResult.NONE;
    }

    @Override
    public void removeFrom(@NotNull Material material, @NotNull Block targetBlock) {
    }

    @Override
    public void removeFrom(@NotNull ItemStack material, @NotNull Block targetBlock) {
    }
}
