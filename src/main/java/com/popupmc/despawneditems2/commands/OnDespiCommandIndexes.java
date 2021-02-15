package com.popupmc.despawneditems2.commands;

import com.popupmc.despawneditems2.DespawnedItems2;
import com.popupmc.despawneditems2.config.LocationEntry;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class OnDespiCommandIndexes extends AbstractDespiCommand{
    public OnDespiCommandIndexes(@NotNull DespawnedItems2 plugin) {
        super(plugin, "indexes");
    }

    // despi [indexes, count]
    // despi [indexes, rebuild]
    // despi [indexes, pull-one]
    // despi [indexes]
    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!canBeElevated(sender))
            return false;

        String option = getArg(1, args);

        if(option == null)
            return sendCount(sender);
        else if(option.equalsIgnoreCase("rebuild"))
            return rebuild(sender);
        else if(option.equalsIgnoreCase("pull-one"))
            return pullOne(sender);

        return sendCount(sender);
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
        LocationEntry locationEntry = plugin.despawnIndexes.randomChestCoord();

        success("Pulled " + locationEntry.toString() + " owned by " +
                Bukkit.getOfflinePlayer(locationEntry.owner).getName(), sender);

        return true;
    }
}
