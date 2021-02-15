package com.popupmc.despawneditems.commands;

import com.popupmc.despawneditems.DespawnedItems;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class OnDespiCommandReload extends AbstractDespiCommand {
    public OnDespiCommandReload(@NotNull DespawnedItems plugin) {
        super(plugin, "reload");
    }

    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!canBeElevated(sender))
            return false;

        plugin.config.load();
        success("Config has been reloaded", sender);

        return true;
    }
}
