package com.popupmc.despawneditems.commands;

import com.popupmc.despawneditems.DespawnedItems;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class OnDespiCommandSave extends AbstractDespiCommand {
    public OnDespiCommandSave(@NotNull DespawnedItems plugin) {
        super(plugin, "save", "Saves locations");
    }

    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!canBeElevated(sender))
            return false;

        plugin.config.fileLocations.save();
        success("Locations have been saved", sender);

        return true;
    }

    @Override
    public void displayHelp(@NotNull CommandSender sender, @NotNull String[] args) {
        if(canBeElevated(sender)) {
            sender.sendMessage(ChatColor.GRAY + "/despi save");
        }
        else {
            sender.sendMessage(ChatColor.GRAY + "You don't have access to this command");
        }
    }

    @Override
    public boolean showDescription(@NotNull CommandSender sender, @NotNull String[] args) {
        return canBeElevated(sender);
    }
}
