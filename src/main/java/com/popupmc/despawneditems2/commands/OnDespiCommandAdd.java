package com.popupmc.despawneditems2.commands;

import com.popupmc.despawneditems2.DespawnedItems2;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OnDespiCommandAdd extends AbstractDespiCommand {

    public OnDespiCommandAdd(@NotNull DespawnedItems2 plugin) {
        super(plugin, "add");
    }

    // despi [add, <player>] - Add location as yourself or someone else
    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        // Has to be a player for this one
        Player player = isPlayer(sender, "Only players can add locations.");
        if(player == null)
            return false;

        // Get optional player name
        String playerName = getArg(1, args);

        // If player name given check for elevated permission and get player
        // Otherwise proceed as self-ownership
        if(playerName != null)
            return addLocationAsPlayer((Player)sender, playerName);
        else
            return addLocation((Player)sender, null);
    }

    public boolean addLocationAsPlayer(@NotNull Player sender, @NotNull String otherPlayerName) {
        if(!canBeElevated("You don't have permission to add ownership of others", sender))
            return false;

        Player otherPlayer = getPlayer(otherPlayerName, sender);
        if(otherPlayer == null)
            return false;

        return addLocation(sender, otherPlayer);
    }

    public boolean addLocation(@NotNull Player sender, @Nullable Player owner) {
        if(owner == null)
            owner = sender;

        // Get valid useable block being looked at, will be null if not both valid
        // A message is already sent to the user
        Location location = getTargetLocation(sender);

        if(location == null)
            return false;

        if(plugin.config.fileLocations.add(location, owner.getUniqueId())) {
            success("Successfully added location!", sender);
            return true;
        }

        warning("Location already exists!", sender);
        return false;
    }
}