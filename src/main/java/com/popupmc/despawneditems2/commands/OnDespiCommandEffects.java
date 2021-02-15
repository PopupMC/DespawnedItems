package com.popupmc.despawneditems2.commands;

import com.popupmc.despawneditems2.DespawnedItems2;
import com.popupmc.despawneditems2.config.LocationEntry;
import com.popupmc.despawneditems2.despawn.DespawnEffect;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class OnDespiCommandEffects extends AbstractDespiCommand {
    public OnDespiCommandEffects(@NotNull DespawnedItems2 plugin) {
        super(plugin, "effects");
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
