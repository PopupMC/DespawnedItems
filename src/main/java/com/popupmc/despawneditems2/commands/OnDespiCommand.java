package com.popupmc.despawneditems2.commands;

import com.popupmc.despawneditems2.DespawnedItems2;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class OnDespiCommand implements CommandExecutor {

    public OnDespiCommand(DespawnedItems2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        // Register despi commands one time
        if(despiCommands.size() <= 0)
            registerDespiCommands(plugin);

        // Check for permission and block if no permission
        if(!sender.hasPermission("despi.use")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
            return false;
        }

        // Check for argument length, has to have at least 1 arg
        // Block with error if not
        if(args.length < 1) {
            sender.sendMessage(ChatColor.GOLD + "Must specify an action");
            return false;
        }

        // Get registered command
        AbstractDespiCommand despiCommand = despiCommands.getOrDefault(args[0].toLowerCase(), null);

        // Stop if no such registered command
        if(despiCommand == null) {
            sender.sendMessage(ChatColor.GOLD + "Invalid action specified");
            return false;
        }

        // Run command
        return despiCommand.runCommand(sender, args);

//        switch (action) {
//            // Return number of locations in memory
//            case "locations":
//                return onCommandLocations(sender);
//            // Return number of indexes left to place items in
//            // All locations are evenly used to prevent items from piling up in a few
//            // locations but not others
//            case "indexes":
//                return onCommandIndexes(sender);
//            // Rebuild indexes
//            case "rebuild-index":
//                return onCommandRebuildIndex(sender);
//            // Number of ongoing active effects
//            case "active-effects":
//                return onCommandActiveEffects(sender);
//            // Number of ongoing despawns being processed
//            case "active-despawns":
//                return onCommandActiveDespawns(sender);
//            // Removes all locations, resets rnd-index, and adds a single location for debugging
//            // to undo use /despi load
//            case "solo-add":
//                return onCommandSoloAdd(sender);
//            // Remove all of a certain material from all chests
//            // Very handy for trash items or overly stocked items
//            case "remove-materials":
//                return onCommandRemoveMaterials(sender, args);
//        }
    }

    public static void registerDespiCommands(DespawnedItems2 plugin) {
        new OnDespiCommandAdd(plugin);
        new OnDespiCommandRemove(plugin);
        new OnDespiCommandExists(plugin);
        new OnDespiCommandReload(plugin);
        new OnDespiCommandSave(plugin);
    }

    public final DespawnedItems2 plugin;
    public static final HashMap<String, AbstractDespiCommand> despiCommands = new HashMap<>();
}
