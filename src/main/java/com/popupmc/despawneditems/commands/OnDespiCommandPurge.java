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
import java.util.List;
import java.util.UUID;

public class OnDespiCommandPurge extends AbstractDespiCommand {
    public OnDespiCommandPurge(@NotNull DespawnedItems plugin) {
        super(plugin, "purge",
                "Removes a specific item in your hand or all items of a type.");
    }

    // despi [purge, owned-by-player, <player>, materials, <names>] - All materials owned by player
    // despi [purge, owned-by-player, <player>, in-hand] - All of that item owned by player
    // despi [purge, owned-by-everyone, materials, <names>] - All materials owned by everyone
    // despi [purge, owned-by-everyone, in-hand] - All of that item owned by everyone
    // despi [purge, owned-by-me, materials, <names>] - All materials owned by you
    // despi [purge, owned-by-me, in-hand] - All of that item owned by you
    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        // These are somewhat complicated
        // We do things a bit differently

        String ownedBy = getArg(1, args);
        String playerName = null;
        String materialOrHand;
        String materialNames;

        if(ownedBy == null)
            return false;

        if(ownedBy.equalsIgnoreCase("owned-by-player"))
            playerName = getArg(2, args);

        if(playerName == null)
            materialOrHand = getArg(2, args);
        else
            materialOrHand = getArg(3, args);

        if(playerName == null)
            materialNames = getArg(3, args);
        else
            materialNames = getArg(4, args);

        // We've now built up the args with proper names

        // Has to be a material or hand option specified
        if(materialOrHand == null)
            return false;

        if(ownedBy.equalsIgnoreCase("owned-by-player") && playerName != null)
            return purgeOtherPlayerStuff(sender, playerName, materialOrHand, materialNames);
        else if(ownedBy.equalsIgnoreCase("owned-by-everyone"))
            return purgeEveryonesStuff(sender, materialOrHand, materialNames);
        else if(ownedBy.equalsIgnoreCase("owned-by-me"))
            return purgeOwnStuff(sender, materialOrHand, materialNames);

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {

        boolean isElevated = canBeElevated(sender);
        ArrayList<String> list = new ArrayList<>();

        if(args.length == 2) {
            if(canBeElevated(sender)) {
                list.add("owned-by-player");
                list.add("owned-by-everyone");
            }

            list.add("owned-by-me");
        }

        else if(args.length == 3 && isElevated && args[1].equalsIgnoreCase("owned-by-player")) {
            for(Player player : Bukkit.getOnlinePlayers()) {
                list.add(player.getName());
            }
        }
        else if(args.length == 3 &&
                (args[1].equalsIgnoreCase("owned-by-everyone") ||
                        args[1].equalsIgnoreCase("owned-by-me"))) {
            list.add("materials");
            list.add("in-hand");
        }

        else if(args.length == 4 && isElevated && args[1].equalsIgnoreCase("owned-by-player")) {
            list.add("materials");
            list.add("in-hand");
        }
        else if(args.length == 4 &&
                (args[1].equalsIgnoreCase("owned-by-everyone") ||
                        args[1].equalsIgnoreCase("owned-by-me")) &&
                args[2].equalsIgnoreCase("materials")) {
            for(Material material : Material.values()) {
                list.add(material.name());
            }
        }

        else if(args.length == 5 &&
                isElevated &&
                args[1].equalsIgnoreCase("owned-by-player") &&
                args[3].equalsIgnoreCase("materials")) {
            for(Material material : Material.values()) {
                list.add(material.name());
            }
        }

        return list;
    }

    @Override
    public void displayHelp(@NotNull CommandSender sender, @NotNull String[] args) {
        if(canBeElevated(sender)) {
            sender.sendMessage(ChatColor.GRAY + "/despi purge owned-by-player <player> materials <name> (Purge all items of materials owned by player)");
            sender.sendMessage(ChatColor.GRAY + "/despi purge owned-by-player <player> in-hand (Purge all items like in-hand owned by player)");
            sender.sendMessage(ChatColor.GRAY + "/despi purge owned-by-everyone materials <name> (Purge all items of materials owned by all players)");
            sender.sendMessage(ChatColor.GRAY + "/despi purge owned-by-everyone in-hand (Purge all items like in-hand owned by all players)");
            sender.sendMessage(ChatColor.GRAY + "/despi purge owned-by-me materials <name> (Purge all items of materials owned by you)");
            sender.sendMessage(ChatColor.GRAY + "/despi purge owned-by-me in-hand (Purge all items like in-hand owned by you)");
        }
        else {
            sender.sendMessage(ChatColor.GRAY + "/despi purge owned-by-me materials <name> (Purge all items of materials owned by you)");
            sender.sendMessage(ChatColor.GRAY + "/despi purge owned-by-me in-hand (Purge all items like in-hand owned by you)");
        }
    }

    @Override
    public boolean showDescription(@NotNull CommandSender sender, @NotNull String[] args) {
        return true;
    }

    public boolean purgeOtherPlayerStuff(@NotNull CommandSender sender,
                               @NotNull String playerName,
                               @NotNull String materialOrHand,
                               @Nullable String materials) {

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);
        Player senderPlayer = isPlayer(sender);

        if(materialOrHand.equalsIgnoreCase("materials") && materials != null) {
            if(senderPlayer == null)
                return purgeMaterials(materials, sender, targetPlayer.getUniqueId(), null);
            else
                return purgeMaterials(materials, sender, targetPlayer.getUniqueId(), senderPlayer.getUniqueId());
        }
        else if(materialOrHand.equalsIgnoreCase("in-hand")) {
            if(senderPlayer == null) {
                error("You must be a player to purge item in your hand", sender);
                return false;
            }

            return purgeItems(senderPlayer.getInventory().getItemInMainHand(),
                    sender,
                    targetPlayer.getUniqueId(),
                    senderPlayer.getUniqueId());
        }

        return false;
    }

    public boolean purgeEveryonesStuff(@NotNull CommandSender sender,
                                         @NotNull String materialOrHand,
                                         @Nullable String materials) {

        Player senderPlayer = isPlayer(sender);

        if(materialOrHand.equalsIgnoreCase("materials") && materials != null) {
            if(senderPlayer == null)
                return purgeMaterials(materials, sender, null, null);
            else
                return purgeMaterials(materials, sender, null, senderPlayer.getUniqueId());
        }
        else if(materialOrHand.equalsIgnoreCase("in-hand")) {
            if(senderPlayer == null) {
                error("You must be a player to purge item in your hand", sender);
                return false;
            }

            return purgeItems(senderPlayer.getInventory().getItemInMainHand(),
                    sender,
                    null,
                    senderPlayer.getUniqueId());
        }

        return false;
    }

    public boolean purgeOwnStuff(@NotNull CommandSender sender,
                                       @NotNull String materialOrHand,
                                       @Nullable String materials) {

        Player senderPlayer = isPlayer(sender, "You must be a player to purge your own stuff");
        if(senderPlayer == null)
            return false;

        if(materialOrHand.equalsIgnoreCase("materials") && materials != null)
            return purgeMaterials(materials, sender, senderPlayer.getUniqueId(), senderPlayer.getUniqueId());
        else if(materialOrHand.equalsIgnoreCase("in-hand"))
            return purgeItems(senderPlayer.getInventory().getItemInMainHand(),
                    sender,
                    senderPlayer.getUniqueId(),
                    senderPlayer.getUniqueId());

        return false;
    }

    public boolean purgeMaterials(@NotNull String materialsStr, @NotNull CommandSender sender, @Nullable UUID owner, @Nullable UUID senderID) {
        // Get name(s)
        String[] names = materialsStr.split(",");
        ArrayList<Material> materials = new ArrayList<>();

        // Convert from string to Material
        for(String name : names) {
            // Attempt to get material, error out if invalid material
            try {
                Material matRes = Material.valueOf(name.toUpperCase());
                materials.add(matRes);
            }
            catch (IllegalArgumentException ex) {
                warning("Invalid material name " + name + " skipping...", sender);
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
