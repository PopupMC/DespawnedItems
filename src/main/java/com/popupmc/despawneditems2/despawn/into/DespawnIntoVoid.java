package com.popupmc.despawneditems2.despawn.into;

import com.popupmc.despawneditems2.DespawnedItems2;
import com.popupmc.despawneditems2.despawn.DespawnProcess;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public class DespawnIntoVoid extends AbstractDespawnInto {
    public DespawnIntoVoid(@NotNull DespawnedItems2 plugin) {
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
    public boolean removeFrom(@NotNull Material material, @NotNull Block targetBlock) {
        return false;
    }
}
