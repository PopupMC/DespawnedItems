package com.popupmc.despawneditems.config;

import com.popupmc.despawneditems.DespawnedItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class LocationEntry {
    public Location location;
    public UUID owner;

    public LocationEntry(@NotNull Location location, @NotNull UUID owner, @NotNull DespawnedItems plugin) {
        this.location = location;
        this.owner = owner;
        this.plugin = plugin;
    }

    public LocationEntry(@NotNull DespawnedItems plugin) {
        this.plugin = plugin;
    }

    public @NotNull String toString() {

        return location.getBlockX() + ";" +
                location.getBlockY() + ";" +
                location.getBlockZ() + ";" +
                location.getWorld().getName();
    }

    public boolean equals(@NotNull LocationEntry entry2) {
        return this.location.getBlockX() == entry2.location.getBlockX() &&
                this.location.getBlockY() == entry2.location.getBlockY() &&
                this.location.getBlockZ() == entry2.location.getBlockZ() &&
                this.location.getWorld() == entry2.location.getWorld() &&
                this.owner == entry2.owner;
    }

    public boolean equals(@NotNull Location location) {
        return this.location.getBlockX() == location.getBlockX() &&
                this.location.getBlockY() == location.getBlockY() &&
                this.location.getBlockZ() == location.getBlockZ() &&
                this.location.getWorld() == location.getWorld();
    }

    public boolean equals(@NotNull Location location, @NotNull UUID owner) {
        return this.location.getBlockX() == location.getBlockX() &&
                this.location.getBlockY() == location.getBlockY() &&
                this.location.getBlockZ() == location.getBlockZ() &&
                this.location.getWorld() == location.getWorld() &&
                this.owner.equals(owner);
    }

    public boolean equals(@NotNull UUID owner) {
        return this.owner.equals(owner);
    }

    public boolean loadFromString(@NotNull String str, UUID owner) {
        String[] parts = str.split(";");
        if(parts.length < 4) {
            plugin.getLogger().warning("ERROR: Location entry, wrong number of args " + str);
            return false;
        }

        try {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            World world = Bukkit.getWorld(parts[3]);

            if(world == null) {
                plugin.getLogger().warning("ERROR: Location entry world is null " + str);
                return false;
            }

            this.location = new Location(world, x, y, z);
            this.owner = owner;
        }
        catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("ERROR: Unable to parse location entry " + str);
            return false;
        }

        return true;
    }

    public static @Nullable LocationEntry fromString(String str, UUID owner, @NotNull DespawnedItems plugin) {
        LocationEntry entry = new LocationEntry(plugin);
        boolean result = entry.loadFromString(str, owner);

        if(!result)
            return null;
        else
            return entry;
    }

    public final DespawnedItems plugin;
}
