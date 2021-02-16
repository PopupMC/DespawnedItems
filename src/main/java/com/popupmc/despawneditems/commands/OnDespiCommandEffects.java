package com.popupmc.despawneditems.commands;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.config.LocationEntry;
import com.popupmc.despawneditems.despawn.DespawnEffect;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OnDespiCommandEffects extends AbstractDespiCommand {
    public OnDespiCommandEffects(@NotNull DespawnedItems plugin) {
        super(plugin, "effects", "Manages effects, often for testing");
    }

    // despi [effects, count]
    // despi [effects, create]
    // despi [effects, clear]
    // despi [effects]
    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!canBeElevated(sender))
            return false;

        String option = getArg(1, args);

        if(option == null)
            return sendCount(sender);
        else if(option.equalsIgnoreCase("create"))
            return create(sender);
        else if(option.equalsIgnoreCase("clear"))
            return clear(sender);

        return sendCount(sender);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!canBeElevated(sender))
            return null;

        ArrayList<String> list = new ArrayList<>();

        if(args.length == 2) {
            list.add("count");
            list.add("create");
            list.add("clear");
        }

        return list;
    }

    @Override
    public void displayHelp(@NotNull CommandSender sender, @NotNull String[] args) {
        if(canBeElevated(sender)) {
            sender.sendMessage(ChatColor.GRAY + "/despi effects count|create|clear");
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
        int size = plugin.effectsPlaying.size();
        success("Effects Count: " + size, sender);

        return true;
    }

    public boolean clear(@NotNull CommandSender sender) {

        int count = plugin.effectsPlaying.size();

        for(DespawnEffect effect : new ArrayList<>(plugin.effectsPlaying)) {
            effect.forceSelfDestroy();
        }

        success("Cleared " + count + " on-going effects", sender);

        return true;
    }

    public boolean create(@NotNull CommandSender sender) {
        Player player = isPlayer(sender);

        if(player == null)
            return false;

        Location location = getTargetLocation(sender);
        if(location == null)
            return false;

        LocationEntry fakeLocationEntry = new LocationEntry(location, player.getUniqueId(), plugin);
        new DespawnEffect(fakeLocationEntry, plugin);

        success("Created fake effect at location", sender);

        return true;
    }
}
