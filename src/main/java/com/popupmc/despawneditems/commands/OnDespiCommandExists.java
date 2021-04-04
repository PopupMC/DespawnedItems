package com.popupmc.despawneditems.commands;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.config.LocationEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OnDespiCommandExists extends AbstractDespiCommand {
    public OnDespiCommandExists(@NotNull DespawnedItems plugin) {
        super(plugin, "exists", "Checks despawn location and ownership information");
    }

    // despi [exists, [here|anywhere], owned-by, <player>] - Does this player own this location or location in general
    // despi [exists, [here], owned-by-anyone>] - Does anyone own this location
    // despi [exists, [here|anywhere], owned-by-me] - Do I own this location or location in general
    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        // Get arguments
        String hereOrAnywhere = getArg(1, args);
        String ownedBy = getArg(2, args);
        String playerName = getArg(3, args);

        // Get here or anywhere
        if(hereOrAnywhere == null ||
                (!hereOrAnywhere.equalsIgnoreCase("here") &&
                !hereOrAnywhere.equalsIgnoreCase("anywhere")))
            return false;

        // Is this
        // /despi exists anywhere owned-by <player>
        if(hereOrAnywhere.equalsIgnoreCase("anywhere") &&
                ownedBy != null &&
                ownedBy.equalsIgnoreCase("owned-by") &&
                playerName != null)
            return existsAnyLocationByName(sender, playerName);

        // Here after we need the sender to be a player
        Player player = isPlayer(sender, "Only players can check locations");
        if(player == null)
            return false;

        // Is this
        // /despi exists anywhere owned-by-me
        if(hereOrAnywhere.equalsIgnoreCase("anywhere") &&
                ownedBy != null &&
                ownedBy.equalsIgnoreCase("owned-by-me") &&
                playerName == null)
            return existsAnyLocationByName(player, player);

        // Is this
        // /despi exists here owned-by <player>
        if(hereOrAnywhere.equalsIgnoreCase("here") &&
                ownedBy != null &&
                ownedBy.equalsIgnoreCase("owned-by") &&
                playerName != null)
            return existsThisLocation(player, playerName);

        // Is this
        // /despi exists here owned-by-anyone
        if(hereOrAnywhere.equalsIgnoreCase("here") &&
                ownedBy != null &&
                ownedBy.equalsIgnoreCase("owned-by-anyone") &&
                playerName == null)
            return existsThisLocation(player, null, true);

        // Is this
        // /despi exists here owned-by-me
        if(hereOrAnywhere.equalsIgnoreCase("here") &&
                ownedBy != null &&
                ownedBy.equalsIgnoreCase("owned-by-me") &&
                playerName == null)
            return existsThisLocation(player, null, false);

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {

        boolean canElevate = canBeElevated(sender);
        ArrayList<String> list = new ArrayList<>();

        if(args.length == 2) {
            list.add("here");
            list.add("anywhere");
        }
        else if(args.length == 3) {

            if(canElevate)
                list.add("owned-by");

            if(!args[1].equalsIgnoreCase("anywhere") && canElevate)
                list.add("owned-by-anyone");

            list.add("owned-by-me");
        }
        else if(args.length == 4 && args[2].equalsIgnoreCase("owned-by") && canElevate) {
            for(Player player : Bukkit.getOnlinePlayers())
                list.add(player.getName());
        }

        return list;
    }

    @Override
    public void displayHelp(@NotNull CommandSender sender, @NotNull String[] args) {
        if(canBeElevated(sender)) {
            sender.sendMessage(ChatColor.GRAY + "/despi exists [here|anywhere] owned-by <player> (Does this player own this location or location in general)");
            sender.sendMessage(ChatColor.GRAY + "/despi exists [here] owned-by-anyone (Does any player own this location)");
            sender.sendMessage(ChatColor.GRAY + "/despi exists [here|anywhere] owned-by-me (Do I own this location or location in general)");
        }
        else {
            sender.sendMessage(ChatColor.GRAY + "/despi exists [here|anywhere] owned-by-me (Do I own this location or location in general)");
        }
    }

    @Override
    public boolean showDescription(@NotNull CommandSender sender, @NotNull String[] args) {
        return true;
    }

    public boolean existsAnyLocationByName(@NotNull CommandSender sender, @NotNull String ownerName) {
        if(!canBeElevated("You don't have permission to check for existence for someone elses location", sender))
            return false;

        OfflinePlayer player = getPlayer(ownerName);

        return existsAnyLocationByName(sender, player);
    }

    public boolean existsAnyLocationByName(@NotNull CommandSender sender, @NotNull OfflinePlayer ownerName) {
        LocationEntry success = plugin.config.fileLocations.exists(ownerName.getUniqueId());

        if(success != null) {
            success("A location was found!", sender);
            sender.sendMessage(ChatColor.GOLD + success.toString());
        }
        else
            warning("No location was found", sender);

        return true;
    }

    public boolean existsThisLocation(@NotNull Player sender, @NotNull String ownerName) {
        if(!canBeElevated("You don't have permission to check for existence for someone elses location", sender))
            return false;

        OfflinePlayer player = getPlayer(ownerName);

        return existsThisLocation(sender, player);
    }

    public boolean existsThisLocation(@NotNull Player sender, @Nullable OfflinePlayer owner) {
        return existsThisLocation(sender, owner, false);
    }

    public boolean existsThisLocation(@NotNull Player sender, @Nullable OfflinePlayer owner, boolean anyOwner) {
        if(owner == null)
            owner = sender;

        Location location = getTargetLocation(sender);
        if(location == null)
            return false;

        LocationEntry success;

        if(anyOwner)
            success = plugin.config.fileLocations.exists(location);
        else
            success = plugin.config.fileLocations.exists(location, owner.getUniqueId());

        if(success != null)
            success("Location does exist", sender);
        else
            warning("Location does not exist", sender);

        return true;
    }
}
