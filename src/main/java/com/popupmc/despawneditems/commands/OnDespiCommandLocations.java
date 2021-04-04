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

public class OnDespiCommandLocations extends AbstractDespiCommand {
    public OnDespiCommandLocations(@NotNull DespawnedItems plugin) {
        super(plugin, "locations", "Obtains all of your locations.");
    }

    // despi [locations, player, <player>] - All locations owned by a player
    // despi [locations, here] - All players owned by this location
    // despi [locations, count] - Count all server locations
    // despi [locations, solo-mode] - Make this location the only location on the server
    // despi [locations, normal-mode] - Restore normal locations
    // despi [locations, mine] - All of your locations
    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        // Get args to name
        String option = getArg(1, args);
        String playerName = getArg(2, args);

        if(option == null)
            return false;
        else if(option.equalsIgnoreCase("player"))
            return existsAnyLocationByName(sender, playerName);
        else if(option.equalsIgnoreCase("count"))
            return totalLocationCount(sender);
        else if(option.equalsIgnoreCase("solo-mode"))
            return soloLocation(sender);
        else if(option.equalsIgnoreCase("normal-mode"))
            return undoSolo(sender);
        else if(option.equalsIgnoreCase("here"))
            return existsAllOwnersByLocation(sender);
        else if(option.equalsIgnoreCase("mine"))
            return existsAnyLocationByName(sender, null);

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {

        boolean isElevated = canBeElevated(sender);
        ArrayList<String> list = new ArrayList<>();

        if(args.length == 2) {
            if(isElevated) {
                list.add("player");
                list.add("here");
                list.add("count");
                list.add("solo-mode");
                list.add("normal-mode");
            }

            list.add("mine");
        }

        if(args.length == 3 && isElevated && args[1].equalsIgnoreCase("player")) {
            for(Player player : Bukkit.getOnlinePlayers())
                list.add(player.getName());
        }

        return list;
    }

    @Override
    public void displayHelp(@NotNull CommandSender sender, @NotNull String[] args) {
        if(canBeElevated(sender)) {
            sender.sendMessage(ChatColor.GRAY + "/despi locations player <player> (All locations by a player)");
            sender.sendMessage(ChatColor.GRAY + "/despi locations count (All locations by everyone)");
            sender.sendMessage(ChatColor.GRAY + "/despi locations solo-mode (Singlular despawn target)");
            sender.sendMessage(ChatColor.GRAY + "/despi locations normal-mode (Normal despawn targets)");
            sender.sendMessage(ChatColor.GRAY + "/despi locations here (All owners by this location)");
            sender.sendMessage(ChatColor.GRAY + "/despi locations mine (All my locations)");
        }
        else {
            sender.sendMessage(ChatColor.GRAY + "/despi locations mine (All my locations)");
        }
    }

    @Override
    public boolean showDescription(@NotNull CommandSender sender, @NotNull String[] args) {
        return true;
    }

    public boolean totalLocationCount(@NotNull CommandSender sender) {
        if(!canBeElevated("You don't have permission to view location count", sender))
            return false;

        success("Locations: " + plugin.config.fileLocations.locationEntries.size(), sender);
        return true;
    }

    public boolean soloLocation(@NotNull CommandSender sender) {
        if(!canBeElevated("You don't have permission to create a solo location", sender))
            return false;

        Player player = isPlayer(sender);
        if(player == null)
            return false;

        Location location = getTargetLocation(player);
        if(location == null)
            return false;

        // Backup everything
        backupLocationEntries.addAll(plugin.config.fileLocations.locationEntries);

        plugin.config.fileLocations.locationEntries.clear();
        plugin.config.fileLocations.locationEntries.add(new LocationEntry(location, player.getUniqueId(), plugin));
        plugin.despawnIndexes.rebuildIndexes();

        success("Solo'd this location, /despi normal-mode to restore", sender);
        return true;
    }

    public boolean undoSolo(@NotNull CommandSender sender) {
        if(!canBeElevated("You don't have permission to undo a solo-mode", sender))
            return false;

        // Add backups back-in and save/load to clear duplicates
        plugin.config.fileLocations.locationEntries.addAll(backupLocationEntries);
        plugin.config.fileLocations.save();
        plugin.config.fileLocations.load();

        backupLocationEntries.clear();

        success("Undid solo", sender);
        return true;
    }

    public boolean existsAnyLocationByName(@NotNull CommandSender sender, @Nullable String ownerName) {
        if(ownerName != null && !canBeElevated("You don't have permission to check for existence for someone elses location", sender))
            return false;

        OfflinePlayer player;

        if(ownerName == null) {
            player = isPlayer(sender);
        }
        else {
            player = getPlayer(ownerName);
        }
        if(player == null)
            return false;

        ArrayList<LocationEntry> success = plugin.config.fileLocations.existsAll(player.getUniqueId());

        if(ownerName == null)
            ownerName = "you";

        if(success.size() > 0) {
            success(success.size() + " location(s) were found for " + ownerName, sender);

            for(LocationEntry locationEntry : success) {
                sender.sendMessage(ChatColor.GOLD + locationEntry.toString());
            }
        }
        else
            warning("No locations found for " + ownerName, sender);


        return true;
    }

    public boolean existsAllOwnersByLocation(@NotNull CommandSender sender) {
        if(!canBeElevated("You don't have permission to check for existence for someone elses location", sender))
            return false;

        Player player = isPlayer(sender);

        if(player == null)
            return false;

        Location location = getTargetLocation(player);
        if(location == null)
            return false;

        ArrayList<LocationEntry> success = plugin.config.fileLocations.existsAll(location);

        if(success.size() > 0) {
            success(success.size() + " owner(s) were found for this location", sender);

            for(LocationEntry locationEntry : success) {
                OfflinePlayer owner = Bukkit.getOfflinePlayer(locationEntry.owner);
                sender.sendMessage(ChatColor.GOLD + owner.getName());
            }
        }
        else
            warning("No owners found for this location.", sender);


        return true;
    }

    public ArrayList<LocationEntry> backupLocationEntries = new ArrayList<>();
}
