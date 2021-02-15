package com.popupmc.despawneditems2.commands;

import com.popupmc.despawneditems2.DespawnedItems2;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OnDespiCommandRemove extends AbstractDespiCommand {
    public OnDespiCommandRemove(@NotNull DespawnedItems2 plugin) {
        super(plugin, "remove");
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

    public boolean removeAnyLocationByName(@NotNull CommandSender sender, @NotNull String ownerName) {
        if(!canBeElevated("You don't have permission to remove someone elses location", sender))
            return false;

        Player player = getPlayer(ownerName, sender);
        if(player == null)
            return false;

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

        Player player = getPlayer(ownerName, sender);
        if(player == null)
            return false;

        return removeLocation(sender, player);
    }

    public boolean removeLocation(@NotNull Player sender, @Nullable Player owner) {
        return removeLocation(sender, owner, false);
    }

    public boolean removeLocation(@NotNull Player sender, @Nullable Player owner, boolean skipOwner) {
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
