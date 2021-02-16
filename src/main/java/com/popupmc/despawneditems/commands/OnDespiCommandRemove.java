package com.popupmc.despawneditems.commands;

import com.popupmc.despawneditems.DespawnedItems;
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

public class OnDespiCommandRemove extends AbstractDespiCommand {
    public OnDespiCommandRemove(@NotNull DespawnedItems plugin) {
        super(plugin, "remove", "Removes location your pointing at from despawn");
    }

    // despi [remove, player, <player>] - Removes the first location owned by the player
    // despi [remove, <player|any>] - Removes a location owned by a player, or the first of anybody
    // despi [remove] - Removes your location
    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {

        // Get args to name
        String anyLocationByName = getArg(2, args);
        String locationByName = getArg(1, args);

        // If 3rd arg, do any location by name
        if(anyLocationByName != null)
            return removeAnyLocationByName(sender, anyLocationByName);

        // Here after we need the sender to be a player
        Player player = isPlayer(sender, "Only players can remove locations");
        if(player == null)
            return false;

        // If 2nd arg, remove by name, otherwise, remove as self
        if(locationByName != null)
            return removeLocationByName((Player)sender, locationByName);

        return removeLocation((Player)sender, null);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!canBeElevated(sender))
            return null;

        ArrayList<String> list = new ArrayList<>();

        if(args.length == 2) {
            list.add("player");
            for(Player player : Bukkit.getOnlinePlayers())
                list.add(player.getName());
        }

        if(args.length == 3 && args[1].equals("player")) {
            for(Player player : Bukkit.getOnlinePlayers())
                list.add(player.getName());
        }

        return list;
    }

    @Override
    public void displayHelp(@NotNull CommandSender sender, @NotNull String[] args) {
        if(canBeElevated(sender)) {
            sender.sendMessage(ChatColor.GRAY + "/despi remove [[player <player>]|<player>|any]");
        }
        else {
            sender.sendMessage(ChatColor.GRAY + "/despi remove");
        }
    }

    @Override
    public boolean showDescription(@NotNull CommandSender sender, @NotNull String[] args) {
        return true;
    }

    public boolean removeAnyLocationByName(@NotNull CommandSender sender, @NotNull String ownerName) {
        if(!canBeElevated("You don't have permission to remove someone elses location", sender))
            return false;

        OfflinePlayer player = getPlayer(ownerName);

        boolean success = plugin.config.fileLocations.remove(player.getUniqueId());

        if(success)
            success("A location was deleted owned by " + ownerName, sender);
        else
            warning(ownerName + " doesn't own any locations!", sender);

        return true;
    }

    public boolean removeLocationByName(@NotNull Player sender, @NotNull String ownerName) {
        if(!canBeElevated("You don't have permission to remove someone elses location", sender))
            return false;

        if(ownerName.equalsIgnoreCase("any"))
            return removeLocation(sender, null, true);

        OfflinePlayer player = getPlayer(ownerName);

        return removeLocation(sender, player);
    }

    public boolean removeLocation(@NotNull Player sender, @Nullable OfflinePlayer owner) {
        return removeLocation(sender, owner, false);
    }

    public boolean removeLocation(@NotNull Player sender, @Nullable OfflinePlayer owner, boolean skipOwner) {
        if(owner == null)
            owner = sender;

        Location location = getTargetLocation(sender);
        if(location == null)
            return false;

        boolean success;

        if(skipOwner)
            success = plugin.config.fileLocations.remove(location);
        else
            success = plugin.config.fileLocations.remove(location, owner.getUniqueId());

        if(success)
            success("Location successfully removed", sender);
        else
            warning("Location did not exist to remove", sender);

        return true;
    }
}
