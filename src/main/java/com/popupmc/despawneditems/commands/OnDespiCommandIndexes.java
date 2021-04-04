package com.popupmc.despawneditems.commands;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.config.LocationEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OnDespiCommandIndexes extends AbstractDespiCommand{
    public OnDespiCommandIndexes(@NotNull DespawnedItems plugin) {
        super(plugin, "indexes", "Manages despawn indexes, often for testing");
    }

    // despi [indexes, count]
    // despi [indexes, rebuild]
    // despi [indexes, pull-one]
    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!canBeElevated(sender))
            return false;

        String option = getArg(1, args);

        if(option == null)
            return false;
        else if(option.equalsIgnoreCase("rebuild"))
            return rebuild(sender);
        else if(option.equalsIgnoreCase("pull-one"))
            return pullOne(sender);
        else if(option.equalsIgnoreCase("count"))
            return sendCount(sender);

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!canBeElevated(sender))
            return null;

        ArrayList<String> list = new ArrayList<>();

        if(args.length == 2) {
            list.add("count");
            list.add("rebuild");
            list.add("pull-one");
        }

        return list;
    }

    @Override
    public void displayHelp(@NotNull CommandSender sender, @NotNull String[] args) {
        if(canBeElevated(sender)) {
            sender.sendMessage(ChatColor.GRAY + "/despi indexes count (Count of all indexes)");
            sender.sendMessage(ChatColor.GRAY + "/despi indexes rebuild (Rebuild all indexes)");
            sender.sendMessage(ChatColor.GRAY + "/despi indexes pull-one (Pull an index from the list)");
        }
        else {
            sender.sendMessage(ChatColor.GRAY + "You don't have access to this command");
        }
    }

    @Override
    public boolean showDescription(@NotNull CommandSender sender, @NotNull String[] args) {
        return canBeElevated(sender);
    }

    public boolean sendCount(@NotNull CommandSender sender) {
        int size = plugin.despawnIndexes.locationEntryIndexes.size();
        success("Index Count: " + size, sender);

        return true;
    }

    public boolean rebuild(@NotNull CommandSender sender) {
        plugin.despawnIndexes.rebuildIndexes();
        success("Rebuilt", sender);

        return true;
    }

    public boolean pullOne(@NotNull CommandSender sender) {

        if(plugin.config.fileLocations.locationEntries.size() == 0) {
            warning("No despawn locations exist anywhere by anyone", sender);
        }

        LocationEntry locationEntry = plugin.despawnIndexes.randomChestCoord();

        success("Pulled " + locationEntry.toString() + " owned by " +
                Bukkit.getOfflinePlayer(locationEntry.owner).getName(), sender);

        return true;
    }
}
