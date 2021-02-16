package com.popupmc.despawneditems.commands;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.despawn.DespawnProcess;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OnRecycleCommand implements CommandExecutor {
    public OnRecycleCommand(DespawnedItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command");
            return false;
        }

        Player player = (Player)sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if(!player.hasPermission("recycle.use")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use that command");
            return false;
        }

        if(item.getType().isAir() || item.getAmount() == 0) {
            player.sendMessage(ChatColor.GOLD + "There's nothing in your hand to recycle.");
            return false;
        }

        new DespawnProcess(item.clone(), plugin);
        player.getInventory().setItemInMainHand(null);

        return false;
    }

    public final DespawnedItems plugin;
}
