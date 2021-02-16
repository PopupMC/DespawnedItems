package com.popupmc.despawneditems.commands;

import com.popupmc.despawneditems.DespawnedItems;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnDespiCommand implements CommandExecutor, TabCompleter {

    public OnDespiCommand(DespawnedItems plugin) {
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
            showDescriptions(sender, args);
            return false;
        }

        // Get registered command
        AbstractDespiCommand despiCommand = despiCommands.getOrDefault(args[0].toLowerCase(), null);

        // Stop if no such registered command
        if(despiCommand == null) {
            showDescriptions(sender, args);
            return true;
        }

        if(args.length == 2 &&
            args[1].equalsIgnoreCase("help") &&
            despiCommand.showDescription(sender, args)) {
            despiCommand.displayHelp(sender, args);
            return true;
        }

        // Run command
        boolean result = despiCommand.runCommand(sender, args);

        if(!result)
            despiCommand.displayHelp(sender, args);

        return true;
    }

    public void showDescriptions(@NotNull CommandSender sender, @NotNull String[] args) {
        for(Map.Entry<String, AbstractDespiCommand> commandEntry : despiCommands.entrySet()) {
            if(commandEntry.getValue().showDescription(sender, args))
                sender.sendMessage(ChatColor.YELLOW + "/despi " + commandEntry.getKey() + " - " +
                        ChatColor.GOLD + commandEntry.getValue().description);
        }
    }

    public static void registerDespiCommands(DespawnedItems plugin) {
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

    public final DespawnedItems plugin;
    public static final HashMap<String, AbstractDespiCommand> despiCommands = new HashMap<>();

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {

        // Register despi commands one time
//        if(despiCommands.size() <= 0)
//            registerDespiCommands(plugin);
//
//        // Check for permission and block if no permission
//        if(!sender.hasPermission("despi.use"))
//            return null;
//
//        // Check for argument length, has to have at least 1 arg
//        // Block with error if not
//        if(args.length <= 1) {
//            ArrayList<String> list = new ArrayList<>();
//
//            for(Map.Entry<String, AbstractDespiCommand> commandEntry : despiCommands.entrySet()) {
//                if(commandEntry.getValue().showDescription(sender, args))
//                    list.add(commandEntry.getKey());
//            }
//
//            return list;
//        }
//        else {
//            AbstractDespiCommand despiCommand = despiCommands.getOrDefault(args[0].toLowerCase(), null);
//            if(despiCommand != null) {
//                List<String> list = despiCommand.onTabComplete(sender, args);
//                if(list == null)
//                    return null;
//
//                if(args.length == 2)
//                    list.add(0, "help");
//            }
//        }

        return null;
    }
}
