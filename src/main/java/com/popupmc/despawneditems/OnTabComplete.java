package com.popupmc.despawneditems;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class OnTabComplete {
    static @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // Ensure this is a player
        if(!(sender instanceof Player)) {
            return null;
        }

        // Get player
        Player player = (Player)sender;

        // Check for permission and block if no permission
        if(!player.hasPermission("despi.use")) {
            return null;
        }

        // Theres only 1 argument, offer no tab completion above 1 argument
        if(args.length  != 0) {
            return null;
        }

        return new ArrayList<String>(Arrays.asList(
                "add",
                "rem",
                "exists",
                "reload",
                "force-save",
                "locs-count",
                "rnd-ind-count",
                "rnd-ind-reset",
                "active-effects-count"
        ));
    }
}
