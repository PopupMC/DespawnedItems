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
                targetMaterial == Material.REPEATING_COMMAND_BLOCK;
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
