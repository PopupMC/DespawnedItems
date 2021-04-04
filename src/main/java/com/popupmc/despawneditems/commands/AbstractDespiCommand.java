package com.popupmc.despawneditems.commands;

import com.popupmc.despawneditems.DespawnedItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

abstract public class AbstractDespiCommand {

    // Takes command action configuration and registers it
    public AbstractDespiCommand(@NotNull DespawnedItems plugin, @NotNull String action, @NotNull String description) {
        this.plugin = plugin;
        this.action = action.toLowerCase();
        this.description = description;

        OnDespiCommand.despiCommands.put(action.toLowerCase(), this);
    }

    // Code that runs the command
    abstract public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args);
    abstract public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args);
    abstract public void displayHelp(@NotNull CommandSender sender, @NotNull String[] args);
    abstract public boolean showDescription(@NotNull CommandSender sender, @NotNull String[] args);

    // Gets target block, first converting CommandSender to a player and gracefully failing if not a player
    public @Nullable Location getTargetLocation(@NotNull CommandSender sender) {
        // Get playey and block looking at
        Player player = isPlayer(sender, null);
        if(player == null)
            return null;

        return getTargetLocation(player);
    }

    // Gets block player is looking at (within 5 blocks)
    public @Nullable Location getTargetLocation(@NotNull Player sender) {
        Block block = sender.getTargetBlock(5);

        // Make sure it's within 5 blocks and obtainable, if not stop here with error
        if(block == null || block.getType().isAir()) {
            error("Unable to find block, are you within 5 blocks of something?", sender);
            return null;
        }

        // Return blocks location
        return block.getLocation();
    }

    // Checks to see if the sender is a player, returns null or Player accordingly
    // Sends an error message if not a player on behalf of you
    // The message can be type in or default
    public @Nullable Player isPlayer(@NotNull CommandSender sender) {
        return isPlayer(sender, null);
    }

    public @Nullable Player isPlayer(@NotNull CommandSender sender, @Nullable String msg) {
        if(msg == null)
            msg = "Player not found";

        if(!(sender instanceof Player)) {
            error(msg, sender);
            return null;
        }

        return (Player)sender;
    }

    // Gets a given argument, returning null if it doesn't exist
    public @Nullable String getArg(int index, @NotNull String[] args) {
        if(args.length < (index + 1))
            return null;

        return args[index];
    }

    // Gets a player by name returning null if doesn't exist
    // The sender is sent an error message on your behalf
    public @NotNull OfflinePlayer getPlayer(String name) {
        return Bukkit.getOfflinePlayer(name);
    }

    // Checks whether player has permission with optional custom error message, sends error on your behalf
    public boolean hasPermission(@NotNull String permission, CommandSender sender) {
        return hasPermission(permission, null, sender);
    }

    public boolean hasPermission(@NotNull String permission, @Nullable String msg, CommandSender sender) {
        if(msg == null)
            msg = "you don't have permission for that";

        if(!sender.hasPermission(permission)) {
            error(msg, sender);
            return false;
        }

        return true;
    }

    // Checks if the player can be elevated by having correct permission, sends error message if not
    public boolean canBeElevated(@Nullable String msg, @NotNull CommandSender sender) {
        return hasPermission(elevatedPermission, msg, sender);
    }

    public boolean canBeElevated(@NotNull CommandSender sender) {
        return hasPermission(elevatedPermission, sender);
    }

    // Sends Success, Warning, or Error Message
    public void error(String msg, CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "ERROR: " + msg);
    }

    public void success(String msg, CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + msg);
    }

    public void warning(String msg, CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "WARNING: " + msg);
    }

    public final DespawnedItems plugin;
    public final String action;
    public final String description;

    public static final String elevatedPermission = "despi.elevated";
}
