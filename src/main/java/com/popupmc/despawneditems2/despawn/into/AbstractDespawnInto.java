package com.popupmc.despawneditems2.despawn.into;

import com.popupmc.despawneditems2.DespawnedItems2;
import com.popupmc.despawneditems2.despawn.DespawnProcess;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract public class AbstractDespawnInto {

    public AbstractDespawnInto(@NotNull DespawnedItems2 plugin) {
        this.plugin = plugin;
    }

    abstract public boolean doesApply(@NotNull Block targetBlock);
    abstract public DespawnIntoResult despawnInto(@NotNull DespawnProcess process, @NotNull Block targetBlock);
    abstract public boolean removeFrom(@NotNull Material material, @NotNull Block targetBlock);
    abstract public boolean removeFrom(@NotNull ItemStack material, @NotNull Block targetBlock);

    // Gets Inventory from block if it has inventory
    public @Nullable Inventory getInventory(@NotNull Block block) {
        BlockState state = block.getState();

        Container container = null;
        if(state instanceof Container)
            container = (Container)state;

        if(container == null)
            return null;

        return container.getInventory();
    }

    public final DespawnedItems2 plugin;
}
