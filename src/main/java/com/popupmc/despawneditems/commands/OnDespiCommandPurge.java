package com.popupmc.despawneditems.commands;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.manage.RemoveMaterials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public class OnDespiCommandPurge extends AbstractDespiCommand {
    public OnDespiCommandPurge(@NotNull DespawnedItems plugin) {
        super(plugin, "purge",
                "Removes a specific item in your hand or all items of a type.");
    }

    // despi [purge, <player>, materials, <names>] - All materials owned by player
    // despi [purge, <player>, item] - All of that item owned by player
    // despi [purge, all, materials, <names>] - All materials owned by everyone
    // despi [purge, all, item] - All of that item owned by everyone
    // despi [purge, materials, <names>] - All materials owned by you
    // despi [purge, item] - All of that item owned by you
    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        // They're too wide-scope to properly name
        String arg1 = getArg(1, args);
        String arg2 = getArg(2, args);
        String arg3 = getArg(3, args);

        if(arg1 == null) {
            error("you have to specify an argument", sender);
            return false;
        }

        if(arg1.equalsIgnoreCase("all"))
            return purgeForEveryone(sender, arg2, arg3);
        else if(arg1.equalsIgnoreCase("materials"))
            return purgeOwnMaterials(sender, arg2);
        else if(arg1.equalsIgnoreCase("item"))
            return purgeOwnItem(sender);

        return purgeForAnother(sender, arg1, arg2, arg3);
    }

    @Override
    public void displayHelp(@NotNull CommandSender sender, @NotNull String[] args) {
        if(canBeElevated(sender)) {
            sender.sendMessage(ChatColor.GRAY + "/despi purge [<player>|all [materials <materials>]|item]|materials <materials>|item");
        }
        else {
            sender.sendMessage(ChatColor.GRAY + "/despi purge materials <materials>|item");
        }
    }

    @Override
    public boolean showDescription(@NotNull CommandSender sender, @NotNull String[] args) {
        return true;
    }

    // despi [purge, all, materials, <names>] - All materials owned by everyone
    // despi [purge, all, item] - All of that item owned by everyone
    public boolean purgeForEveryone(@NotNull CommandSender sender, @Nullable String arg2, @Nullable String arg3) {
        if(!canBeElevated("You don't have permission to purge all items from everyone", sender))
            return false;

        if(arg2 == null) {
            error("Must provide an argument", sender);
            return false;
        }

        Player senderPlayer = isPlayer(sender);

        if(arg2.equalsIgnoreCase("materials") && arg3 != null)
            return purgeMaterials(arg3, sender, null, (senderPlayer != null)
                    ? senderPlayer.getUniqueId()
                    : null);
        else if(arg2.equalsIgnoreCase("materials")) {
            error("Missing materials list", sender);
            return false;
        }
        else if(arg2.equalsIgnoreCase("item") && arg3 != null) {
            if(senderPlayer == null) {
                error("You can't purge items from your hand if you don't have a hand", sender);
                return false;
            }

            return purgeItems(senderPlayer.getInventory().getItemInMainHand(), sender, null, senderPlayer.getUniqueId());
        }

        error("Wrong arguments", sender);
        return false;
    }

    // despi [purge, <player>, materials, <names>] - All materials owned by player
    // despi [purge, <player>, item] - All of that item owned by player
    public boolean purgeForAnother(@NotNull CommandSender sender, @Nullable String arg1, @Nullable String arg2, @Nullable String arg3) {
        if(!canBeElevated("You don't have permission to purge all items from someone else", sender))
            return false;

        if(arg1 == null || arg2 == null) {
            error("Must provide an argument", sender);
            return false;
        }

        Player senderPlayer = isPlayer(sender);
        OfflinePlayer player = Bukkit.getOfflinePlayer(arg1);

        if(arg2.equalsIgnoreCase("materials") && arg3 != null)
            return purgeMaterials(arg3, sender, player.getUniqueId(), (senderPlayer != null)
                    ? senderPlayer.getUniqueId()
                    : null);
        else if(arg2.equalsIgnoreCase("materials")) {
            error("Missing materials list", sender);
            return false;
        }
        else if(arg2.equalsIgnoreCase("item") && arg3 != null) {
            if(senderPlayer == null) {
                error("You can't purge items from your hand if you don't have a hand", sender);
                return false;
            }

            return purgeItems(senderPlayer.getInventory().getItemInMainHand(), sender, player.getUniqueId(), senderPlayer.getUniqueId());
        }

        error("Wrong arguments", sender);
        return false;
    }

    // despi [purge, materials, <names>] - All materials owned by you
    public boolean purgeOwnMaterials(@NotNull CommandSender sender, @Nullable String arg2) {
        Player player = isPlayer(sender, "You must be a player to perform this command");
        if(player == null)
            return false;

        if(arg2 == null) {
            error("Missing material list", sender);
            return false;
        }

        return purgeMaterials(arg2, sender, player.getUniqueId(), player.getUniqueId());
    }

    // despi [purge, item] - All of that item owned by you
    public boolean purgeOwnItem(@NotNull CommandSender sender) {
        Player player = isPlayer(sender, "You must be a player to perform this command");
        if(player == null)
            return false;

        return purgeItems(player.getInventory().getItemInMainHand(), sender, player.getUniqueId(), player.getUniqueId());
    }

    public boolean purgeMaterials(@NotNull String materialsStr, @NotNull CommandSender sender, @Nullable UUID owner, @Nullable UUID senderID) {
        // Get name(s)
        String[] names = materialsStr.split(",");
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

        // Make sure something isn't already in progress
        if(senderID != null && plugin.removeMaterialsInst.containsKey(senderID)) {
            error("Removal already in-progress for you", sender);
            return false;
        }

        // Initiate material removal
        new RemoveMaterials(sender, materials, null, plugin, owner, senderID);

        success("Begun removal of materials... This may take a while...", sender);

        return true;
    }

    public boolean purgeItems(@NotNull ItemStack item, @NotNull CommandSender sender, @Nullable UUID owner, @Nullable UUID senderID) {
        if(item.getType().isAir() || item.getAmount() <= 0) {
            error("Invalid Item", sender);
            return false;
        }

        // Make sure something isn't already in progress
        if(senderID != null && plugin.removeMaterialsInst.containsKey(senderID)) {
            error("Removal already in-progress for you", sender);
            return false;
        }

        // Initiate material removal
        new RemoveMaterials(sender, null, item, plugin, owner, senderID);

        success("Begun removal of items... This may take a while...", sender);

        return true;
    }
}
