package com.popupmc.despawneditems.commands;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.despawn.DespawnProcess;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OnDespiCommandDespawn extends AbstractDespiCommand {
    public OnDespiCommandDespawn(@NotNull DespawnedItems plugin) {
        super(plugin, "despawn", "Manages despawning often for testing");
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

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!canBeElevated(sender))
            return null;

        ArrayList<String> list = new ArrayList<>();

        if(args.length == 2) {
            list.add("count");
            list.add("create-hand");
            list.add("create-material");
            list.add("clear");
        }

        if(args.length == 3 && args[1].equalsIgnoreCase("create-material")) {
            for(Material material : Material.values()) {
                list.add(material.toString().toLowerCase());
            }
        }

        if(args.length == 4 && args[1].equalsIgnoreCase("create-material")) {

            try {
                Material material = Material.valueOf(args[2]);
                list.add(material.getMaxStackSize() + "");
            }
            catch (IllegalArgumentException ignored) {}
        }

        return list;
    }

    @Override
    public void displayHelp(@NotNull CommandSender sender, @NotNull String[] args) {
        if(canBeElevated(sender)) {
            sender.sendMessage(ChatColor.GRAY + "/despi despawn count|create-hand|create material <materials> <amt>|clear");
        }
        else {
            sender.sendMessage(ChatColor.GRAY + "You don't have access to this command");
        }
    }

    @Override
    public boolean showDescription(@NotNull CommandSender sender, @NotNull String[] args) {
        return canBeElevated(sender);
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

        // Get name(s)
        String[] names = material.split(",");
        ArrayList<Material> materials = new ArrayList<>();

        // Convert from string to Material
        for(String name : names) {
            // Attempt to get material, error out if invalid material
            try {
                Material matRes = Material.valueOf(name);
                materials.add(matRes);
            }
            catch (IllegalArgumentException ex) {
                warning("invalid material name " + name + " skipping...", sender);
            }
        }

        // If no materials names were useable then quit
        if(materials.size() <= 0) {
            error("No material names usable.", sender);
            return false;
        }

        for(Material materialEl : materials) {
            ItemStack item = new ItemStack(materialEl, amount);

            // Do a forced despawn
            new DespawnProcess(item, plugin);
        }

        success("Created forced despawn", sender);
        return true;
    }
}
