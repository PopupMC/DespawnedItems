package com.popupmc.despawneditems.commands;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.config.LocationEntry;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OnDespiCommandExists extends AbstractDespiCommand {
    public OnDespiCommandExists(@NotNull DespawnedItems plugin) {
        super(plugin, "exists");
    }

    // despi [exists, player, <player>] - Checks for existence the first location owned by the player
    // despi [exists, <player|any>] - Checks for existence a location owned by a player, or the first of anybody
    // despi [exists] - Checks for existence your location
    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        // Get args to name
        String anyLocationByName = getArg(2, args);
        String locationByName = getArg(1, args);

        // If 3rd arg, do any location by name
        if(anyLocationByName != null)
            return existsAnyLocationByName(sender, anyLocationByName);

        // Here after we need the sender to be a player
        Player player = isPlayer(sender, "Only players can remove locations");
        if(player == null)
            return false;

        // If 2nd arg, exists by name, otherwise, exists as self
        if(locationByName != null)
            return existsLocationByName((Player)sender, locationByName);

        return existsLocation((Player)sender, null);
    }

    public boolean existsAnyLocationByName(@NotNull CommandSender sender, @NotNull String ownerName) {
        if(!canBeElevated("You don't have permission to check for existence for someone elses location", sender))
            return false;

        Player player = getPlayer(ownerName, sender);
        if(player == null)
            return false;

        LocationEntry success = plugin.config.fileLocations.exists(player.getUniqueId());

        if(success != null) {
            success("A location was found by " + ownerName, sender);
            sender.sendMessage(ChatColor.GOLD + success.toString());
        }
        else
            warning(ownerName + " doesn't own any locations!", sender);

        return true;
    }

    public boolean existsLocationByName(@NotNull Player sender, @NotNull String ownerName) {
        if(!canBeElevated("You don't have permission to check for existence for someone elses location", sender))
            return false;

        if(ownerName.equalsIgnoreCase("any"))
            return existsLocation(sender, null, true);

        Player player = getPlayer(ownerName, sender);
        if(player == null)
            return false;

        return existsLocation(sender, player);
    }

    public boolean existsLocation(@NotNull Player sender, @Nullable Player owner) {
        return existsLocation(sender, owner, false);
    }

    public boolean existsLocation(@NotNull Player sender, @Nullable Player owner, boolean skipOwner) {
        if(owner == null)
            owner = sender;

        Location location = getTargetLocation(sender);
        if(location == null)
            return false;

        LocationEntry success;

        if(skipOwner)
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
