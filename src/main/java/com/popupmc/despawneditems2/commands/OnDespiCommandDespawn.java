package com.popupmc.despawneditems2.commands;

import com.popupmc.despawneditems2.DespawnedItems2;
import com.popupmc.despawneditems2.despawn.DespawnProcess;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class OnDespiCommandDespawn extends AbstractDespiCommand {
    public OnDespiCommandDespawn(@NotNull DespawnedItems2 plugin) {
        super(plugin, "despawn");
    }

    // despi [despawn, count]
    // despi [despawn, create-hand]
    // despi [despawn, create-material, <material>]
    // despi [despawn, create-material, <material>, <amount>]
    // despi [despawn, clear]
    // despi [despawn]
    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!canBeElevated(sender))
            return false;

        String option = getArg(1, args);
        String material = getArg(2, args);
        String amountStr = getArg(3, args);
        int amount = 1;

        if(option == null)
            return sendCount(sender);

        if(option.equalsIgnoreCase("create-material") && material == null) {
            error("You must specify a material", sender);
            return false;
        }

        if(amountStr != null) {
            try {
                amount = Integer.parseInt(amountStr);
            }
            catch (NumberFormatException ex) {
                error("Unable to parse amount", sender);
                return false;
            }
        }

        if(option.equalsIgnoreCase("create-hand"))
            return createFromHand(sender);
        else if(option.equalsIgnoreCase("create-material")) {
            assert material != null;
            return createFromMaterial(sender, material, amount);
        }
        else if(option.equalsIgnoreCase("clear"))
            return clear(sender);

        return sendCount(sender);
    }

    public boolean sendCount(@NotNull CommandSender sender) {
        int size = plugin.despawnProcesses.size();
        success("Despawn Count: " + size, sender);

        return true;
    }

    public boolean clear(@NotNull CommandSender sender) {

        int count = plugin.despawnProcesses.size();

        for(DespawnProcess process : new ArrayList<>(plugin.despawnProcesses)) {
            process.selfDestroy();
        }

        success("Cleared " + count + " on-going despawns", sender);

        return true;
    }

    public boolean createFromHand(@NotNull CommandSender sender) {
        Player player = isPlayer(sender);

        if(player == null)
            return false;

        ItemStack item = player.getInventory().getItemInMainHand();
        if(item.getAmount() == 0 || item.getType().isAir()) {
            error("You have no items in your hand", sender);
            return false;
        }

        // Do a forced despawn
        new DespawnProcess(item, plugin);

        success("Created forced despawn", sender);

        return true;
    }

    public boolean createFromMaterial(@NotNull CommandSender sender, @NotNull String material, int amount) {
        if(amount <= 0)
            amount = 1;

        ItemStack item;

        try {
            item = new ItemStack(Material.valueOf(material.toUpperCase()), amount);
        }
        catch (IllegalArgumentException ex) {
            error("Unable to parse material name", sender);
            return false;
        }

        // Do a forced despawn
        new DespawnProcess(item, plugin);

        success("Created forced despawn", sender);

        return true;
    }
}
