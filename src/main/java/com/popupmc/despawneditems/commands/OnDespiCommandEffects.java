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

    // despi [effects, count-ongoing]
    // despi [effects, create-here]
    // despi [effects, clear-ongoing]
    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!canBeElevated(sender))
            return false;

        String option = getArg(1, args);

        if(option == null)
            return false;

        if(option.equalsIgnoreCase("create-here"))
            return create(sender);
        else if(option.equalsIgnoreCase("clear-ongoing"))
            return clear(sender);
        else if(option.equalsIgnoreCase("count-ongoing"))
            return sendCount(sender);

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if(!canBeElevated(sender))
            return null;

        ArrayList<String> list = new ArrayList<>();

        if(args.length == 2) {
            list.add("count-ongoing");
            list.add("create-here");
            list.add("clear-ongoing");
        }

        return list;
    }

    @Override
    public void displayHelp(@NotNull CommandSender sender, @NotNull String[] args) {
        if(canBeElevated(sender)) {
            sender.sendMessage(ChatColor.GRAY + "/despi effects count-ongoing (Count ongoing effects)");
            sender.sendMessage(ChatColor.GRAY + "/despi effects create-here (Make effects here)");
            sender.sendMessage(ChatColor.GRAY + "/despi effects clear-ongoing (Remove ongoing effects)");
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

        Location location = getTargetLocation(player);
        if(location == null)
            return false;

        LocationEntry fakeLocationEntry = new LocationEntry(location, player.getUniqueId(), plugin);
        new DespawnEffect(fakeLocationEntry, plugin);

        success("Created fake effect at location", sender);

        return true;
    }
}
