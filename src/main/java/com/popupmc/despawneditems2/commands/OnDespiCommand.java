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
        new OnDespiCommandLocations(plugin);
        new OnDespiCommandClear(plugin);
        new OnDespiCommandIndexes(plugin);
        new OnDespiCommandEffects(plugin);
        new OnDespiCommandDespawn(plugin);
        new OnDespiCommandPurge(plugin);
    }

    public final DespawnedItems2 plugin;
    public static final HashMap<String, AbstractDespiCommand> despiCommands = new HashMap<>();
}
