package com.popupmc.despawneditems.commands;

import com.popupmc.despawneditems.DespawnedItems;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OnDespiCommandClear extends AbstractDespiCommand {
    public OnDespiCommandClear(@NotNull DespawnedItems plugin) {
        super(plugin, "clear");
    }

    // despi [clear, <player>] - All locations owned by a player
    // despi [clear, here] - All players owned by this location
    // despi [clear, total] - All players owned by this location
    // despi [clear] - All of your locations
    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        // Get args to name
        String playerNameOrAny = getArg(1, args);

        if(playerNameOrAny == null)
            return removeAllLocationsByOwner(sender, null);
        else if(playerNameOrAny.equalsIgnoreCase("total"))
            return removeAllLocations(sender);
        else if(!playerNameOrAny.equalsIgnoreCase("here"))
            return removeAllLocationsByOwner(sender, playerNameOrAny);

        return removeAllOwnersByLocation(sender);
    }

    public boolean removeAllLocations(@NotNull CommandSender sender) {
        if(!canBeElevated("You don't have permission to clear all locations", sender))
            return false;

        success("Locations Removed: " + plugin.config.fileLocations.removeAll(), sender);
        return true;
    }

    public boolean removeAllLocationsByOwner(@NotNull CommandSender sender, @Nullable String ownerName) {
        if(ownerName != null && !canBeElevated("You don't have permission to remove all locations of someone else", sender))
            return false;

        Player player;

        if(ownerName == null) {
            player = isPlayer(sender);
        }
        else {
            player = getPlayer(ownerName, sender);
        }
        if(player == null)
            return false;

        int success = plugin.config.fileLocations.removeAll(player.getUniqueId());

        if(ownerName == null)
            ownerName = "you";

        if(success > 0) {
            success(success + " location(s) were removed for " + ownerName, sender);
        }
        else
            warning("No locations found to remove for " + ownerName, sender);


        return true;
    }

    public boolean removeAllOwnersByLocation(@NotNull CommandSender sender) {
        if(!canBeElevated("You don't have permission to remove all owners of this location", sender))
            return false;

        Player player = isPlayer(sender);

        if(player == null)
            return false;

        Location location = getTargetLocation(player);
        if(location == null)
            return false;

        int success = plugin.config.fileLocations.removeAll(location);

        if(success > 0) {
            success(success + " owner(s) were removed for this location", sender);
        }
        else
            warning("No owners found to remove for this location.", sender);


        return true;
    }
}
