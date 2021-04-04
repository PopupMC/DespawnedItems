package com.popupmc.despawneditems.despawn.into;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.despawn.DespawnProcess;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DespawnItemIntoEntity extends AbstractDespawnInto {
    public DespawnItemIntoEntity(@NotNull DespawnedItems plugin) {
        super(plugin);
    }

    @Override
    public boolean doesApply(@NotNull Block targetBlock) {
        boolean isAir = targetBlock.getType().isAir();
        boolean hasProperEntity = false;
        Collection<Entity> entities = targetBlock.
                getLocation().
                toCenterLocation().
                getNearbyEntities(.5, .5, .5);

        for(Entity entity : entities) {
            if((entity instanceof ItemFrame) ||
                (entity instanceof LivingEntity) ||
                    (entity instanceof StorageMinecart)) {
                hasProperEntity = true;
                break;
            }
        }

        // Only if no block is there, has a correct entity, and contains only 1 entity.
        return isAir && hasProperEntity && entities.size() == 1;
    }

    @Override
    public DespawnIntoResult despawnInto(@NotNull DespawnProcess process, @NotNull Block targetBlock) {
        Collection<Entity> entities = targetBlock.
                getLocation().
                toCenterLocation().
                getNearbyEntities(.5, .5, .5);

        if(entities.size() != 1)
            return DespawnIntoResult.NONE;

        Entity entity = entities.iterator().next();

        if(entity instanceof ItemFrame) {
            ItemFrame tmp = (ItemFrame)entity;
            if(tmp.getItem().getType().isAir()) {
                tmp.setItem(process.item.asOne());
                return addedItem(process);
            }
        }
        else if(entity instanceof LivingEntity) {
            LivingEntity tmp = (LivingEntity)entity;
            EntityEquipment equipment = tmp.getEquipment();
            if(equipment == null)
                return DespawnIntoResult.NONE;

            if(equipment.getHelmet() == null || equipment.getHelmet().getType().isAir()) {
                equipment.setHelmet(process.item.asOne());
                return addedItem(process);
            }
            else if((equipment.getChestplate() == null ||
                    equipment.getChestplate().getType().isAir()) &&
                    process.item.getType().name().toLowerCase().contains("chestplate")) {
                equipment.setChestplate(process.item.asOne());
                return addedItem(process);
            }
            else if((equipment.getLeggings() == null ||
                    equipment.getLeggings().getType().isAir()) &&
                    process.item.getType().name().toLowerCase().contains("leggings")) {
                equipment.setLeggings(process.item.asOne());
                return addedItem(process);
            }
            else if((equipment.getBoots() == null ||
                    equipment.getBoots().getType().isAir()) &&
                    process.item.getType().name().toLowerCase().contains("boots")) {
                equipment.setBoots(process.item.asOne());
                return addedItem(process);
            }
            else if(equipment.getItemInMainHand().getType().isAir()) {
                if(entity instanceof ArmorStand) {
                    ArmorStand tmp2 = (ArmorStand)entity;
                    tmp2.setArms(true);
                }

                equipment.setItemInMainHand(process.item.asOne());
                return addedItem(process);
            }
            else if(equipment.getItemInOffHand().getType().isAir()) {
                if(entity instanceof ArmorStand) {
                    ArmorStand tmp2 = (ArmorStand)entity;
                    tmp2.setArms(true);
                }

                equipment.setItemInOffHand(process.item.asOne());
                return addedItem(process);
            }
        }
        else if(entity instanceof InventoryHolder) {
            InventoryHolder tmp = (InventoryHolder)entity;
            Inventory inventory = tmp.getInventory();

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
        else if(entity instanceof PoweredMinecart && process.item.getType().isFuel()) {
            PoweredMinecart tmp = (PoweredMinecart)entity;

            // According to the wiki
            // All items, no matter what, add 3600 ticks per item
            tmp.setFuel(tmp.getFuel() + 3600);
            return addedItem(process);
        }

        return DespawnIntoResult.NONE;
    }

    public DespawnIntoResult addedItem(@NotNull DespawnProcess process) {
        if(process.item.getAmount() == 1)
            return DespawnIntoResult.FULLY;

        process.item.setAmount(process.item.getAmount() - 1);
        return DespawnIntoResult.PARTIALLY;
    }

    @Override
    public void removeFrom(@NotNull Material material, @NotNull Block targetBlock) {
        removeFrom(new ItemStack(material), targetBlock);
    }

    @Override
    public void removeFrom(@NotNull ItemStack material, @NotNull Block targetBlock) {
        Collection<Entity> entities = targetBlock.
                getLocation().
                toCenterLocation().
                getNearbyEntities(.5, .5, .5);

        if(entities.size() != 1)
            return;

        Entity entity = entities.iterator().next();

        if(entity instanceof ItemFrame) {
            ItemFrame tmp = (ItemFrame)entity;
            if(tmp.getItem().isSimilar(material)) {
                tmp.setItem(null);
                return;
            }
        }
        else if(entity instanceof LivingEntity) {
            LivingEntity tmp = (LivingEntity)entity;
            EntityEquipment equipment = tmp.getEquipment();
            if(equipment == null)
                return;

            if(equipment.getHelmet() != null && equipment.getHelmet().isSimilar(material)) {
                equipment.setHelmet(null);
                return;
            }

            if(equipment.getChestplate() != null && equipment.getChestplate().isSimilar(material)) {
                equipment.setChestplate(null);
                return;
            }

            if(equipment.getLeggings() != null && equipment.getLeggings().isSimilar(material)) {
                equipment.setLeggings(null);
                return;
            }

            if(equipment.getBoots() != null && equipment.getBoots().isSimilar(material)) {
                equipment.setBoots(null);
                return;
            }

            if(equipment.getItemInMainHand().isSimilar(material)) {
                equipment.setItemInMainHand(null);
                return;
            }

            if(equipment.getItemInOffHand().isSimilar(material)) {
                equipment.setItemInOffHand(null);
                return;
            }
        }

        if(entity instanceof InventoryHolder) {
            InventoryHolder tmp = (InventoryHolder)entity;
            Inventory inventory = tmp.getInventory();

            inventory.remove(material);
        }

        if(entity instanceof PoweredMinecart && material.getType().isFuel()) {
            PoweredMinecart tmp = (PoweredMinecart)entity;
            tmp.setFuel(0);
        }
    }
}
