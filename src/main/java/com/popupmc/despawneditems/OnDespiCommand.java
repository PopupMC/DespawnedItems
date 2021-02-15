package com.popupmc.despawneditems;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

// Handles the /despi command
public class OnDespiCommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Get that argument and process command accordignly
        String action = args[0];

        switch (action) {
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
                // Number of ongoing despawns being processed
            case "active-despawns-count":
                return onCommandActiveDespawnsCount(sender);
                // Removes all locations, resets rnd-index, and adds a single location for debugging
                // to undo use /despi load
            case "solo-add":
                return onCommandSoloAdd(sender);
                // Remove all of a certain material from all chests
                // Very handy for trash items or overly stocked items
            case "remove-all-material":
                return onCommandRemoveAllMaterial(sender, args);
        }

        // If we've gotten this far it means the player issued a wrong command
        // Notify and quit
        sender.sendMessage(ChatColor.GOLD + "Invalid action specified");
        return false;
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

    private boolean onCommandActiveDespawnsCount(@NotNull CommandSender sender) {
        // Return number of ongoing active effects
        sender.sendMessage(ChatColor.GOLD + "Active Despawns Counts: " + ChatColor.YELLOW + DespawnedItems.processItemDespawns.size());
        return true;
    }

    private boolean onCommandSoloAdd(@NotNull CommandSender sender) {
        // Get valid useable block being looked at, will be null if not both valid and useable
        // A message is already sent to the user
        Location loc = getTargLoc(sender);

        if(loc == null)
            return false;

        // Clear locations
        DespawnedItemsConfig.locs.clear();

        // Add location to list directly
        DespawnedItemsConfig.locs.add(new LocationEntry(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName()));

        // Announce success
        sender.sendMessage(ChatColor.GOLD + "Solo Location Setup");
        return true;
    }

    // Removes all of a certain material from all chests
    private boolean onCommandRemoveAllMaterial(@NotNull CommandSender sender, String[] args) {

        // Ensure args is correct
        if(args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Error: requires name of material to remove");
            return false;
        }

        // Get name(s)
        String[] names = args[1].split(",");
        ArrayList<Material> materials = new ArrayList<>();

        // Convert from string to Material
        for(String name : names) {
            // Attempt to get material, error out if invalid material
            try {
                Material matRes = Material.valueOf(name);
                materials.add(matRes);
            }
            catch (IllegalArgumentException ex) {
                sender.sendMessage(ChatColor.GOLD + "Warning: invalid material name " + name + " skipping...");
            }
        }

        // If no materials names were useable then quit
        if(materials.size() <= 0) {
            sender.sendMessage(ChatColor.RED + "Error: No material names usable.");
            return false;
        }

        // Make sure something isn't already in progress
        if(DespawnedItems.removeMaterialsInst != null) {
            sender.sendMessage(ChatColor.RED + "Error: Removal already in-progress");
            return false;
        }

        // Initiate material removal
        DespawnedItems.removeMaterialsInst = new RemoveMaterials();
        DespawnedItems.removeMaterialsInst.removeMaterials(sender, materials);

        sender.sendMessage(ChatColor.GREEN + "Begun removal of materials... This may take a while...");

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
