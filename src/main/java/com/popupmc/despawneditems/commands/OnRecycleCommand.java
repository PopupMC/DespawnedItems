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
import org.bukkit.scoreboard.Objective;
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

        player.sendMessage(ChatColor.GREEN + "Done!");

        // Get partial objective
        // A partial is a partial stack of 64, not a full stack
        Objective recycleCountPartObj = player.getScoreboard().getObjective("recycleCountPart");
        if(recycleCountPartObj == null) {
            plugin.getLogger().warning("Objective recycleCountPartObj is null");
            return true;
        }

        int recycleCountPart = recycleCountPartObj.getScore(player.getName()).getScore();

        recycleCountPart++;

        // If it's less than a full stack then increment and stop here
        if(recycleCountPart < 64) {
            recycleCountPartObj.getScore(player.getName()).setScore(recycleCountPart);
            return true;
        }
        // Otherwise reset value and keep going
        else {
            recycleCountPart = 0;
            recycleCountPartObj.getScore(player.getName()).setScore(recycleCountPart);
        }

        // Get objective
        Objective recycleCountObj = player.getScoreboard().getObjective("recycleCount");
        if(recycleCountObj == null) {
            plugin.getLogger().warning("Objective recycleCountObj is null");
            return true;
        }

        int recycleCount = recycleCountObj.getScore(player.getName()).getScore();

        // Update it
        recycleCount++;
        recycleCountObj.getScore(player.getName()).setScore(recycleCount);

        // Get paid objective
        Objective recycleCountPaidObj = player.getScoreboard().getObjective("recycleCountPaid");
        if(recycleCountPaidObj == null) {
            plugin.getLogger().warning("Objective recycleCountPaidObj is null");
            return true;
        }

        int recycleCountPaid = recycleCountPaidObj.getScore(player.getName()).getScore();

        // Paid should always be less than or equal to unpaid
        // If greater than then stop here
        if(recycleCountPaid > recycleCount)
            return true;

        // Get difference between paid and unpaid
        int difference = recycleCount - recycleCountPaid;

        // If not enough difference then stop here
        if(difference <= 0)
            return true;

        // Pay the player
        ItemStack itemStack = new ItemStack(Material.GOLD_NUGGET);
        itemStack.setAmount(difference);
        player.getWorld().dropItem(player.getLocation(), itemStack);

        // Update score
        recycleCountPaidObj.getScore(player.getName()).setScore(recycleCount);

        return true;
    }

    public final DespawnedItems plugin;
}
