package com.popupmc.despawneditems2.commands;

import com.popupmc.despawneditems2.DespawnedItems2;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class OnDespiCommandReload extends AbstractDespiCommand {
    public OnDespiCommandReload(@NotNull DespawnedItems2 plugin) {
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
