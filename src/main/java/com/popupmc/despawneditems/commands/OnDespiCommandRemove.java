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

    // despi [remove, [here|anywhere], owned-by, <player>] - Remove this players location or location in general
    // despi [remove, [here], owned-by-anyone>] - Remove this location
    // despi [remove, [here|anywhere], owned-by-me] - Remove own location or location in general
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
            return removeAnyLocationByName(sender, playerName);

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
            return removeAnyLocationByName(player, player);

        // Is this
        // /despi exists here owned-by <player>
        if(hereOrAnywhere.equalsIgnoreCase("here") &&
                ownedBy != null &&
                ownedBy.equalsIgnoreCase("owned-by") &&
                playerName != null)
            return removeThisLocation(player, playerName);

        // Is this
        // /despi exists here owned-by-anyone
        if(hereOrAnywhere.equalsIgnoreCase("here") &&
                ownedBy != null &&
                ownedBy.equalsIgnoreCase("owned-by-anyone") &&
                playerName == null)
            return removeThisLocation(player, null, true);

        // Is this
        // /despi exists here owned-by-me
        if(hereOrAnywhere.equalsIgnoreCase("here") &&
                ownedBy != null &&
                ownedBy.equalsIgnoreCase("owned-by-me") &&
                playerName == null)
            return removeThisLocation(player, null, false);

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
            sender.sendMessage(ChatColor.GRAY + "/despi remove [here|anywhere] owned-by <player> (Remove this player owned location here or first location in general)");
            sender.sendMessage(ChatColor.GRAY + "/despi remove [here] owned-by-anyone (Remove any player owned location here)");
            sender.sendMessage(ChatColor.GRAY + "/despi remove [here|anywhere] owned-by-me (Remove your location here or location in general)");
        }
        else {
            sender.sendMessage(ChatColor.GRAY + "/despi remove [here|anywhere] owned-by-me (Remove your location here or first location in general)");
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

        return removeAnyLocationByName(sender, player);
    }

    public boolean removeAnyLocationByName(@NotNull CommandSender sender, @NotNull OfflinePlayer ownerName) {
        boolean success = plugin.config.fileLocations.remove(ownerName.getUniqueId());

        if(success) {
            success("A location was removed!", sender);
        }
        else
            warning("No location was removed (Did the player have locations?)", sender);

        return true;
    }

    public boolean removeThisLocation(@NotNull Player sender, @NotNull String ownerName) {
        if(!canBeElevated("You don't have permission to remove someone elses location", sender))
            return false;

        OfflinePlayer player = getPlayer(ownerName);

        return removeThisLocation(sender, player);
    }

    public boolean removeThisLocation(@NotNull Player sender, @Nullable OfflinePlayer owner) {
        return removeThisLocation(sender, owner, false);
    }

    public boolean removeThisLocation(@NotNull Player sender, @Nullable OfflinePlayer owner, boolean anyOwner) {
        if(owner == null)
            owner = sender;

        Location location = getTargetLocation(sender);
        if(location == null)
            return false;

        boolean success;

        if(anyOwner)
            success = plugin.config.fileLocations.remove(location);
        else
            success = plugin.config.fileLocations.remove(location, owner.getUniqueId());

        if(success)
            success("Location removed", sender);
        else
            warning("Location wasn't removed (Did it ever exist?)", sender);

        return true;
    }
}
