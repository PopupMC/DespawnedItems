package com.popupmc.despawneditems;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

// Handles the /despi command
public class OnDespiCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        // Ensure this is a player
        if(!(sender instanceof Player)) {
            sender.sendMessage("Only a player can execute this command");
            return false;
        }

        // Get player
        Player player = (Player)sender;

        // Check for permission and block if no permission
        if(!player.hasPermission("despi.use")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
            return false;
        }

        // Check for argument length, has to have at least 1 arg
        // Block with error if not
        if(args.length < 1) {
            sender.sendMessage(ChatColor.GOLD + "Must specify an action");
            return false;
        }

        // Get that argument and process command accordignly
        String action = args[0];

        switch (action) {
                // Adds the location to list
            case "add":
                return onCommandAdd(sender);
                // Removes the location from list
            case "rem":
                return onCommandRem(sender);
                // Check if location exists
            case "exists":
                return onCommandExists(sender);
                // Reload config
            case "reload":
                return onCommandReload(sender);
                // Save locations
            case "force-save":
                return onCommandForceSave(sender);
                // Return number of locations in memory
            case "locs-count":
                return onCommandLocsCount(sender);
                // Return number of locations left to place items in
                // All locations are evenly used to prevent items from piling up in a few
                // locations but not others
            case "rnd-ind-count":
                return onCommandRndIndCount(sender);
                // Reset random locations
            case "rnd-ind-reset":
                return onCommandRndIndReset(sender);
                // Number of ongoing active effects
            case "active-effects-count":
                return onCommandActiveEffectsCount(sender);
                // Removes all locations, resets rnd-index, and adds a single location for debugging
                // to undo use /despi load
            case "solo-add":
                return onCommandSoloAdd(sender);
        }

        // If we've gotten this far it means the player issued a wrong command
        // Notify and quit
        sender.sendMessage(ChatColor.GOLD + "Invalid action specified");
        return false;
    }

    private boolean onCommandAdd(@NotNull CommandSender sender) {
        // Get valid useable block being looked at, will be null if not both valid and useable
        // A message is already sent to the user
        Location blockLoc = getTargLoc(sender);

        if(blockLoc == null)
            return false;

        // Attempt to add the location
        if(DespawnedItemsConfig.add(blockLoc)) {
            sender.sendMessage(ChatColor.GREEN + "Successfully added location " +
                    blockLoc.getBlockX() + ", " +
                    blockLoc.getBlockY() + ", " +
                    blockLoc.getBlockZ() + " in world " +
                    blockLoc.getWorld().getName());
            return true;
        }
        else {
            sender.sendMessage(ChatColor.GOLD + "Location " +
                    blockLoc.getBlockX() + ", " +
                    blockLoc.getBlockY() + ", " +
                    blockLoc.getBlockZ() + " in world " +
                    blockLoc.getWorld().getName() + " " +
                    "Already exists!");
            return false;
        }
    }

    private boolean onCommandRem(@NotNull CommandSender sender) {
        // Get valid useable block being looked at, will be null if not both valid and useable
        // A message is already sent to the user
        Location blockLoc = getTargLoc(sender);

        if(blockLoc == null)
            return false;

        // Attempt to remove location
        if(DespawnedItemsConfig.rem(blockLoc)) {
            sender.sendMessage(ChatColor.GREEN + "Successfully removed location " +
                    blockLoc.getBlockX() + ", " +
                    blockLoc.getBlockY() + ", " +
                    blockLoc.getBlockZ() + " in world " +
                    blockLoc.getWorld().getName());
            return true;
        }
        else {
            sender.sendMessage(ChatColor.GOLD + "Location " +
                    blockLoc.getBlockX() + ", " +
                    blockLoc.getBlockY() + ", " +
                    blockLoc.getBlockZ() + " in world " +
                    blockLoc.getWorld().getName() + " " +
                    "Doesn't exist!");
            return false;
        }
    }

    private boolean onCommandExists(@NotNull CommandSender sender) {
        // Get valid useable block being looked at, will be null if not both valid and useable
        // A message is already sent to the user
        Location blockLoc = getTargLoc(sender);

        if(blockLoc == null)
            return false;

        // Check if null which determines it's existence
        if(DespawnedItemsConfig.exists(blockLoc) != null) {
            sender.sendMessage(ChatColor.GREEN + "Location " +
                    blockLoc.getBlockX() + ", " +
                    blockLoc.getBlockY() + ", " +
                    blockLoc.getBlockZ() + " in world " +
                    blockLoc.getWorld().getName() +
                    "Does exist!");
            return true;
        }
        else {
            sender.sendMessage(ChatColor.GOLD + "Location " +
                    blockLoc.getBlockX() + ", " +
                    blockLoc.getBlockY() + ", " +
                    blockLoc.getBlockZ() + " in world " +
                    blockLoc.getWorld().getName() + " " +
                    "Doesn't exist!");
            return false;
        }
    }

    private boolean onCommandReload(@NotNull CommandSender sender) {
        // Do a simple reload
        DespawnedItemsConfig.load();
        sender.sendMessage(ChatColor.GREEN + "Reloaded");
        return true;
    }

    private boolean onCommandForceSave(@NotNull CommandSender sender) {
        // Do a simple save
        DespawnedItemsConfig.save();
        sender.sendMessage(ChatColor.GREEN + "Saved");
        return true;
    }

    private boolean onCommandLocsCount(@NotNull CommandSender sender) {
        // Return number of locations
        sender.sendMessage(ChatColor.GOLD + "Location Counts: " + ChatColor.YELLOW + DespawnedItemsConfig.locs.size());
        return true;
    }

    private boolean onCommandRndIndCount(@NotNull CommandSender sender) {
        // Return number of locations left before resetting (So that items are spread evenly)
        sender.sendMessage(ChatColor.GOLD + "Random Index Counts: " + ChatColor.YELLOW + OnItemDespawnEvent.rndIndexes.size());
        return true;
    }

    private boolean onCommandRndIndReset(@NotNull CommandSender sender) {
        // Reset number of locations left
        OnItemDespawnEvent.newRndIndexes();

        // Announce reset
        sender.sendMessage(ChatColor.GREEN + "Random index reset to " + OnItemDespawnEvent.rndIndexes.size());
        return true;
    }

    private boolean onCommandActiveEffectsCount(@NotNull CommandSender sender) {
        // Return number of ongoing active effects
        sender.sendMessage(ChatColor.GOLD + "Active Effects Counts: " + ChatColor.YELLOW + DespawnedItems.effectsPlaying.size());
        return true;
    }

    private boolean onCommandSoloAdd(@NotNull CommandSender sender) {
        // Get valid useable block being looked at, will be null if not both valid and useable
        // A message is already sent to the user
        Location loc = getTargLoc(sender);

        if(loc == null)
            return false;

        // Add location to list directly
        DespawnedItemsConfig.locs.add(new LocationEntry(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName()));

        // Announce success
        sender.sendMessage(ChatColor.GOLD + "Solo Location Setup");
        return true;
    }

    // Gets block player is looking at (within 5 blocks) and ensures it's a valid block
    private Location getTargLoc(CommandSender sender) {
        // Get playey and block looking at
        Player player = (Player)sender;
        Block block = player.getTargetBlock(5);

        // Maake sure it's within 5 blocks and obtainable, if not stop here with error
        if(block == null) {
            sender.sendMessage(ChatColor.GOLD + "Unable to find block, are you within 5 blocks of a chest or barrel?");
            return null;
        }

        // Get the blocks inventory, if this fails then it's of the wrong type
        // Has to be a valid block that this plugin is programmed to use
        Inventory inv = OnItemDespawnEvent.getInventory(block);
        if(inv == null) {
            sender.sendMessage(ChatColor.GOLD + "Block is invalid, is it a chest or barrel?");
            return null;
        }

        // Return blocks location
        return block.getLocation();
    }
}
