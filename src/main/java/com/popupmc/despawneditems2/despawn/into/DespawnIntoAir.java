package com.popupmc.despawneditems2.despawn.into;

import com.popupmc.despawneditems2.DespawnedItems2;
import com.popupmc.despawneditems2.despawn.DespawnProcess;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

// It's not possible for a #!!!@@!@##@@@ huyman on earth to do this
// There BlockStateMeta, BlockMeta, StateData or some sh#@@!, there's like a bazillion classes, sub-classes
// interfaces, linked classes, linked data, parent classes, sub-classes
// In order to place a block with it's data I would have to write probably several thousand lineso f code
// to cover every single possible shred of data
// Why the f##!@ is there not a `placeItem(Location location)` function.
// Minecraft truly has some of the most garbage code I've ever seen in the 20 years I've been programming
// Additioanlly this marks the 6th or 7th thing I've had to entierley abandon because the code is so badly trash

// This guy explain about 15% of what I'd have to write, just a tiny fraction
// https://www.spigotmc.org/threads/copying-saving-block-information-to-be-placed-later.157015/
// I agree with him, the level of absolute absurdity is unfathomable
public class DespawnIntoAir extends AbstractDespawnInto {
    public DespawnIntoAir(@NotNull DespawnedItems2 plugin) {
        super(plugin);
    }

    @Override
    public boolean doesApply(@NotNull Block targetBlock) {
        return targetBlock.getType().isAir();
    }

    @Override
    public DespawnIntoResult despawnInto(@NotNull DespawnProcess process, @NotNull Block targetBlock) {
        return DespawnIntoResult.NONE;
    }

    @Override
    public boolean removeFrom(@NotNull Material material, @NotNull Block targetBlock) {
        return false;
    }
}
