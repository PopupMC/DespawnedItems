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

    // despi [locations, <player>] - All locations owned by a player
    // despi [locations, here] - All players owned by this location
    // despi [locations, total] - Total server locations
    // despi [locations, solo] - Total server locations
    // despi [locations, undo-solo] - Total server locations
    // despi [locations] - All of your locations
    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        // Get args to name
        String playerNameOrAny = getArg(1, args);

        if(playerNameOrAny == null)
            return existsAnyLocationByName(sender, null);
        else if(playerNameOrAny.equalsIgnoreCase("total"))
            return totalLocationCount(sender);
        else if(playerNameOrAny.equalsIgnoreCase("solo"))
            return soloLocation(sender);
        else if(playerNameOrAny.equalsIgnoreCase("undo-solo"))
            return undoSolo(sender);
        else if(!playerNameOrAny.equalsIgnoreCase("here"))
            return existsAnyLocationByName(sender, playerNameOrAny);

        return existsAllOwnersByLocation(sender);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!canBeElevated(sender))
            return null;

        ArrayList<String> list = new ArrayList<>();

        if(args.length == 2) {
            list.add("here");
            list.add("total");
            list.add("solo");
            list.add("undo-solo");

            for(Player player : Bukkit.getOnlinePlayers())
                list.add(player.getName());
        }

        return list;
    }

    @Override
    public void displayHelp(@NotNull CommandSender sender, @NotNull String[] args) {
        if(canBeElevated(sender)) {
            sender.sendMessage(ChatColor.GRAY + "/despi locations [<player>|here|total|solo|undo-solo]");
        }
        else {
            sender.sendMessage(ChatColor.GRAY + "/despi locations");
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

        success("Solo'd this location, /despi undo-solo to restore", sender);
        return true;
    }

    public boolean undoSolo(@NotNull CommandSender sender) {
        if(!canBeElevated("You don't have permission to undo a solo", sender))
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
