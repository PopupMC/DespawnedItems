package com.popupmc.despawneditems.despawn.into;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.despawn.DespawnProcess;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    public DespawnIntoAir(@NotNull DespawnedItems plugin) {
        super(plugin);
    }

    @Override
    public boolean doesApply(@NotNull Block targetBlock) {
        Material targetMaterial = targetBlock.getType();
        return targetMaterial.isAir();
    }

    @Override
    public DespawnIntoResult despawnInto(@NotNull DespawnProcess process, @NotNull Block targetBlock) {

        Material itemType = process.item.getType();

        // Blacklisted Items
        // These often cause destruction with explosives, heat, liquids, redstone, or gravity
        if(itemType == Material.TNT ||
                itemType == Material.SPAWNER ||
                itemType == Material.BEE_NEST ||
                itemType == Material.BEEHIVE ||
                itemType == Material.CACTUS ||
                itemType == Material.CAMPFIRE ||
                itemType == Material.SOUL_CAMPFIRE ||
                itemType == Material.DAMAGED_ANVIL ||
                itemType == Material.CHIPPED_ANVIL ||
                itemType == Material.ANVIL ||
                itemType.hasGravity() ||
                itemType.name().toLowerCase().contains("redstone") ||
                itemType.name().toLowerCase().contains("infested") ||
                itemType == Material.DAYLIGHT_DETECTOR ||
                itemType == Material.LECTERN ||
                itemType == Material.TARGET ||
                itemType == Material.TRIPWIRE ||
                itemType == Material.TRIPWIRE_HOOK ||
                itemType == Material.OBSERVER ||
                itemType == Material.LEVER ||
                itemType.name().toLowerCase().contains("button") ||
                itemType.name().toLowerCase().contains("pressure_plate") ||
                itemType == Material.DETECTOR_RAIL ||
                itemType == Material.END_CRYSTAL ||
                itemType == Material.FLETCHING_TABLE ||
                itemType == Material.ICE ||
                itemType == Material.SPONGE ||
                itemType == Material.WET_SPONGE ||
                itemType == Material.GLOWSTONE ||
                itemType == Material.SNOW ||
                itemType == Material.SNOW_BLOCK ||
                itemType == Material.PACKED_ICE ||
                itemType == Material.BLUE_ICE ||
                itemType == Material.FROSTED_ICE ||
                itemType == Material.JACK_O_LANTERN ||
                itemType == Material.LANTERN ||
                itemType == Material.TORCH ||
                itemType == Material.MAGMA_BLOCK ||
                itemType == Material.SEA_LANTERN ||
                itemType == Material.SHROOMLIGHT ||
                itemType == Material.SOUL_LANTERN ||
                itemType == Material.TNT_MINECART ||
                itemType == Material.TRAPPED_CHEST ||
                itemType == Material.LAVA ||
                itemType == Material.LAVA_BUCKET ||
                itemType == Material.WATER ||
                itemType == Material.WATER_BUCKET ||
                itemType == Material.CONDUIT
        )
            return DespawnIntoResult.NONE;

        // Has to be a block
        if(!itemType.isBlock())
            return DespawnIntoResult.NONE;

        // Copy data over
        copyBlockToLocation(process.item, targetBlock);

        // Decrement if more than 1, otherwise consider a full transfer
        if(process.item.getAmount() > 1) {
            process.item.setAmount(process.item.getAmount() - 1);
            return DespawnIntoResult.PARTIALLY;
        }

        return DespawnIntoResult.FULLY;
    }

    // Theres just no way these can be cleanly used
    // Once down it can't be picked back up
    @Override
    public void removeFrom(@NotNull Material material, @NotNull Block targetBlock) {
    }

    @Override
    public void removeFrom(@NotNull ItemStack material, @NotNull Block targetBlock) {
    }

    public static void copyBlockToLocation(ItemStack sourceBlock, Block targetBlock) {

        // First, just set the type
        targetBlock.setType(sourceBlock.getType());

        // Stop here if theres no further data
        if(!sourceBlock.hasItemMeta())
            return;

        // Get extra data
        ItemMeta meta = sourceBlock.getItemMeta();

        // Copy Block Data if it exists
        if(meta instanceof BlockDataMeta) {
            BlockDataMeta blockDataMeta = (BlockDataMeta)meta;
            targetBlock.setBlockData(blockDataMeta.getBlockData(sourceBlock.getType()));
        }

        // Copy Meta if it exists
        if(meta instanceof BannerMeta) {
            BannerMeta sourceMeta = (BannerMeta)meta;
            Banner targetBanner = (Banner)targetBlock.getState();

            for(Pattern pattern : sourceMeta.getPatterns()) {
                targetBanner.addPattern(pattern);
            }
        }

        if(meta instanceof SkullMeta) {
            SkullMeta sourceMeta = (SkullMeta)meta;
            Skull targetSkull = (Skull) targetBlock.getState();

            if(sourceMeta.getOwningPlayer() != null)
                targetSkull.setOwningPlayer(sourceMeta.getOwningPlayer());

            if(sourceMeta.getPlayerProfile() != null)
                targetSkull.setPlayerProfile(sourceMeta.getPlayerProfile());
        }

        // If there's no block state then stop here
        if(!(meta instanceof BlockStateMeta))
            return;

        // Get the block state
        BlockStateMeta stateMeta = (BlockStateMeta)meta;

        // Stop if no state received
        if(!stateMeta.hasBlockState())
            return;

        // Now go through the trouble of setting the state
        BlockState sourceState = stateMeta.getBlockState();
        BlockState targetState = targetBlock.getState();
        targetState.setType(sourceState.getType());
        targetState.setData(sourceState.getData());

        if(sourceState instanceof InventoryHolder) {
            InventoryHolder sourceHolder = (InventoryHolder) sourceState;
            InventoryHolder targetHolder = (InventoryHolder) targetState;
            targetHolder.getInventory().setContents(sourceHolder.getInventory().getContents());
        }

        if(sourceState instanceof Banner) {
            Banner sourceBanner = (Banner) sourceState;
            Banner targetBanner = (Banner) targetState;
            targetBanner.setBaseColor(sourceBanner.getBaseColor());
            targetBanner.setPatterns(sourceBanner.getPatterns());

        }

        if(sourceState instanceof BrewingStand) {
            BrewingStand sourceStand = (BrewingStand) sourceState;
            BrewingStand targetStand = (BrewingStand) targetState;
            targetStand.setBrewingTime(sourceStand.getBrewingTime());

        }

        if(sourceState instanceof Beacon) {
            Beacon sourceStateB = (Beacon) sourceState;
            Beacon targetStateB = (Beacon) targetState;
            targetStateB.setEffectRange(sourceStateB.getEffectRange());

            if(sourceStateB.getPrimaryEffect() != null)
                targetStateB.setPrimaryEffect(sourceStateB.getPrimaryEffect().getType());
            if(sourceStateB.getSecondaryEffect() != null)
                targetStateB.setSecondaryEffect(sourceStateB.getSecondaryEffect().getType());

        }

        if(sourceState instanceof Beehive) {
            Beehive sourceStateB = (Beehive) sourceState;
            Beehive targetStateB = (Beehive) targetState;
            targetStateB.setFlower(sourceStateB.getFlower());

        }

        if(sourceState instanceof Chest) {
            Chest sourceChest = (Chest) sourceState;
            Chest targetChest = (Chest) targetState;
            targetChest.getBlockInventory().setContents(sourceChest.getBlockInventory().getContents());

        }

        if(sourceState instanceof CommandBlock) {
            CommandBlock sourceCommandBlock = (CommandBlock) sourceState;
            CommandBlock targetCommandBlock = (CommandBlock) targetState;
            targetCommandBlock.setName(sourceCommandBlock.getName());
            targetCommandBlock.setCommand(sourceCommandBlock.getCommand());

        }

        if(sourceState instanceof CreatureSpawner) {
            CreatureSpawner sourceSpawner = (CreatureSpawner) sourceState;
            CreatureSpawner targetSpawner = (CreatureSpawner) targetState;
            targetSpawner.setSpawnedType(sourceSpawner.getSpawnedType());
            targetSpawner.setDelay(sourceSpawner.getDelay());

        }

        if(sourceState instanceof Furnace) {
            Furnace sourceFurnace = (Furnace) sourceState;
            Furnace targetFurnace = (Furnace) targetState;
            targetFurnace.setBurnTime(sourceFurnace.getBurnTime());
            targetFurnace.setCookTime(sourceFurnace.getCookTime());

        }

        if(sourceState instanceof Jukebox) {
            Jukebox sourceJukebox = (Jukebox) sourceState;
            Jukebox targetJukebox = (Jukebox) targetState;
            targetJukebox.setPlaying(sourceJukebox.getPlaying());

        }

        if(sourceState instanceof NoteBlock) {
            NoteBlock sourceNoteBlock = (NoteBlock) sourceState;
            NoteBlock targetNoteBlock = (NoteBlock) targetState;
            targetNoteBlock.setNote(sourceNoteBlock.getNote());

        }

        if(sourceState instanceof Sign) {
            Sign sourceSign = (Sign) sourceState;
            Sign targetSign = (Sign) targetState;
            List<Component> lines = sourceSign.lines();
            for (int i = 0; i < lines.size(); i++) {
                targetSign.line(i, lines.get(i));
            }

        }

        if(sourceState instanceof Skull) {
            Skull sourceSkull = (Skull) sourceState;
            Skull targetSkull = (Skull) targetState;

            OfflinePlayer sourceOwner = sourceSkull.getOwningPlayer();

            if(sourceOwner != null)
                targetSkull.setOwningPlayer(sourceOwner);
        }

        targetState.update();
    }
}
