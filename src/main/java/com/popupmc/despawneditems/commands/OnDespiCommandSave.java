package com.popupmc.despawneditems.commands;

import com.popupmc.despawneditems.DespawnedItems;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class OnDespiCommandSave extends AbstractDespiCommand {
    public OnDespiCommandSave(@NotNull DespawnedItems plugin) {
        super(plugin, "save");
    }

    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!canBeElevated(sender))
            return false;

        plugin.config.fileLocations.save();
        success("Locations have been saved", sender);

        return true;
    }
}
