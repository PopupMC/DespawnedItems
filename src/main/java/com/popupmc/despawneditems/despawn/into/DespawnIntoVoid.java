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
        Material targetMaterial = targetBlock.getType();

        return targetMaterial == Material.COMMAND_BLOCK ||
                targetMaterial == Material.COMMAND_BLOCK_MINECART ||
                targetMaterial == Material.CHAIN_COMMAND_BLOCK ||
                targetMaterial == Material.REPEATING_COMMAND_BLOCK ||
                targetMaterial == Material.DEBUG_STICK ||
                targetMaterial == Material.JIGSAW ||
                targetMaterial == Material.STRUCTURE_BLOCK ||
                targetMaterial == Material.STRUCTURE_VOID ||
                targetMaterial == Material.NETHERITE_AXE ||
                targetMaterial == Material.NETHERITE_BLOCK ||
                targetMaterial == Material.NETHERITE_BOOTS ||
                targetMaterial == Material.NETHERITE_CHESTPLATE ||
                targetMaterial == Material.NETHERITE_HELMET ||
                targetMaterial == Material.NETHERITE_HOE ||
                targetMaterial == Material.NETHERITE_INGOT ||
                targetMaterial == Material.NETHERITE_LEGGINGS ||
                targetMaterial == Material.NETHERITE_PICKAXE ||
                targetMaterial == Material.NETHERITE_SCRAP ||
                targetMaterial == Material.NETHERITE_SHOVEL ||
                targetMaterial == Material.NETHERITE_SWORD ||
                targetMaterial == Material.ANCIENT_DEBRIS;
    }

    @Override
    public DespawnIntoResult despawnInto(@NotNull DespawnProcess process, @NotNull Block targetBlock) {
        process.item.setAmount(0);
        process.item.setType(Material.AIR);
        return DespawnIntoResult.CONTRABAND;
    }

    @Override
    public void removeFrom(@NotNull Material material, @NotNull Block targetBlock) {
    }

    @Override
    public void removeFrom(@NotNull ItemStack material, @NotNull Block targetBlock) {
    }
}
