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

public class OnDespiCommandAdd extends AbstractDespiCommand {

    public OnDespiCommandAdd(@NotNull DespawnedItems plugin) {
        super(plugin, "add", "Adds a location to receive despawn items");
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

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!canBeElevated(sender))
            return null;

        ArrayList<String> list = new ArrayList<>();

        if(args.length == 2) {
            for(Player player : Bukkit.getOnlinePlayers())
                list.add(player.getName());
        }

        return list;
    }

    @Override
    public void displayHelp(@NotNull CommandSender sender, @NotNull String[] args) {
        if(canBeElevated(sender)) {
            sender.sendMessage(ChatColor.GRAY + "/despi add <player>");
        }
        else {
            sender.sendMessage(ChatColor.GRAY + "/despi add");
        }
    }

    @Override
    public boolean showDescription(@NotNull CommandSender sender, @NotNull String[] args) {
        return true;
    }

    public boolean addLocationAsPlayer(@NotNull Player sender, @NotNull String otherPlayerName) {
        if(!canBeElevated("You don't have permission to add ownership of others", sender))
            return false;

        OfflinePlayer otherPlayer = getPlayer(otherPlayerName);

        return addLocation(sender, otherPlayer);
    }

    public boolean addLocation(@NotNull Player sender, @Nullable OfflinePlayer owner) {
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
