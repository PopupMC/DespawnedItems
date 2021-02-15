package com.popupmc.despawneditems.events;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.despawn.DespawnProcess;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OnItemDespawnEvent implements Listener {
    public OnItemDespawnEvent(@NotNull DespawnedItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEvent(final ItemDespawnEvent event) {

        // Get item stack
        // Clone to prevent potential errors from using a removed item stack
        ItemStack item = event.getEntity().getItemStack().clone();

        // Begin to process it async
        new DespawnProcess(item, plugin);
    }

    public final DespawnedItems plugin;
}
