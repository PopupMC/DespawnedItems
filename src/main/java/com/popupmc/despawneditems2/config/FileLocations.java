package com.popupmc.despawneditems2.config;

import com.popupmc.despawneditems2.DespawnedItems2;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileLocations {
    public FileLocations(@NotNull DespawnedItems2 plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        // Get locations file
        File fileLocations = new File(plugin.getDataFolder(), "locations.yml");
        FileConfiguration fileLocationsConfig = new YamlConfiguration();

        // Save only if doesn't exist to prevent annoying warning, this is literally the dumbest functionality
        // By definition if I say don't replace then warning me that your not replacing is pretty silly and stupid
        // and causes me to have to write extra code to prevent that warning defeating the whole purpose of the option
        if(!fileLocations.exists()) {
            plugin.saveResource("locations.yml", false);
        }

        // Load locations file, prepare for errors
        try {
            fileLocationsConfig.load(fileLocations);
        }
        catch (IOException | InvalidConfigurationException ex) {
            plugin.getLogger().warning("Unable to load locations.yml file");
            ex.printStackTrace();
            return;
        }

        // Load list of locations
        List<String> flattenedLocationEntries = fileLocationsConfig.getStringList("locations");

        // Loop through each location
        for(String flattenedLocationEntry : flattenedLocationEntries) {
            LocationEntry locationEntry = LocationEntry.fromString(flattenedLocationEntry, plugin);
            if(locationEntry == null) {
                plugin.getLogger().warning("WARNING: Unable to load location entry, skipping " + flattenedLocationEntry);
                continue;
            }

            if(exists(locationEntry) == null) {
                locationEntries.add(locationEntry);
            }
            else {
                plugin.getLogger().warning("WARNING: Duplicate location, skipping " + flattenedLocationEntry);
            }
        }
    }

    public void save() {
        // Create new empty list
        ArrayList<String> flattenedLocationEntries = new ArrayList<>();

        // Add in all the locations flattened
        for(LocationEntry locationEntry : locationEntries) {
            flattenedLocationEntries.add(locationEntry.toString());
        }

        // Get locations file
        File fileLocations = new File(plugin.getDataFolder(), "locations.yml");
        FileConfiguration fileLocationsConfig = new YamlConfiguration();

        if(!fileLocations.exists()) {
            plugin.saveResource("locations.yml", false);
        }

        // Save list into YML file
        fileLocationsConfig.set("locations", flattenedLocationEntries);

        // Save YML file
        try {
            fileLocationsConfig.save(fileLocations);
        } catch (IOException ex) {
            plugin.getLogger().warning("Unable to save locations.yml file");
            ex.printStackTrace();
        }
    }

    public boolean add(@NotNull Location location, @NotNull UUID owner) {
        // Check if the location exists, if it does a location is returned
        LocationEntry locationEntry = exists(location, owner);

        // If there's no location returned then it's safe to add it meaning it doesn't
        // already exist
        if(locationEntry == null) {
            // Add location to list
            locationEntries.add(new LocationEntry(location, owner, plugin));

            // Do a save
            save();
        }

        // Return whether or not it already existed by checking to see if a location was returned
        return locationEntry == null;
    }

    public boolean remove(@NotNull Location location, @NotNull UUID owner) {
        // Check if the location exists, if it does a location is returned
        LocationEntry locationEntry = exists(location, owner);

        // Only remove if it exists
        if(locationEntry != null) {
            // Remove using returned location
            locationEntries.remove(locationEntry);

            // Save updated list
            save();
        }

        // Return if successful or not
        return locationEntry != null;
    }

    public boolean remove(@NotNull Location location) {
        // Check if the location exists, if it does a location is returned
        LocationEntry locationEntry = exists(location);

        // Only remove if it exists
        if(locationEntry != null) {
            // Remove using returned location
            locationEntries.remove(locationEntry);

            // Save updated list
            save();
        }

        // Return if successful or not
        return locationEntry != null;
    }

    public boolean remove(@NotNull UUID owner) {
        // Check if the location exists, if it does a location is returned
        LocationEntry locationEntry = exists(owner);

        // Only remove if it exists
        if(locationEntry != null) {
            // Remove using returned location
            locationEntries.remove(locationEntry);

            // Save updated list
            save();
        }

        // Return if successful or not
        return locationEntry != null;
    }

    public int removeAll(@NotNull Location location) {
        boolean result;
        int count = 0;

        do {
            result = remove(location);
            if(result)
                count++;
        }while (result);

        return count;
    }

    public int removeAll(@NotNull Location location, @NotNull UUID owner) {
        boolean result;
        int count = 0;

        do {
            result = remove(location, owner);
            if(result)
                count++;
        }while (result);

        return count;
    }

    public int removeAll(@NotNull UUID owner) {
        boolean result;
        int count = 0;

        do {
            result = remove(owner);
            if(result)
                count++;
        }while (result);

        return count;
    }

    public @Nullable LocationEntry exists(@NotNull LocationEntry entry) {

        // Set return value to be null (Doesn't exist)
        LocationEntry ret = null;

        // Attempt to find it in the locations
        for(LocationEntry entry2 : locationEntries) {

            // Look for a precise match
            if(entry.equals(entry2)) {
                // When found save reference to precise match and stop looking
                ret = entry2;
                break;
            }
        }

        // Return location if found or null
        return ret;
    }

    public @Nullable LocationEntry exists(@NotNull UUID owner) {

        // Set return value to be null (Doesn't exist)
        LocationEntry ret = null;

        // Attempt to find it in the locations
        for(LocationEntry entry2 : locationEntries) {
            // Look for a precise match
            if(entry2.equals(owner)) {
                // When found save reference to precise match and stop looking
                ret = entry2;
                break;
            }
        }

        // Return location if found or null
        return ret;
    }

    public @Nullable LocationEntry exists(@NotNull Location location, @NotNull UUID owner) {

        // Set return value to be null (Doesn't exist)
        LocationEntry ret = null;

        // Attempt to find it in the locations
        for(LocationEntry entry2 : locationEntries) {

            // Look for a precise match
            if(entry2.equals(location, owner)) {
                // When found save reference to precise match and stop looking
                ret = entry2;
                break;
            }
        }

        // Return location if found or null
        return ret;
    }

    public @Nullable LocationEntry exists(@NotNull Location location) {

        // Set return value to be null (Doesn't exist)
        LocationEntry ret = null;

        // Attempt to find it in the locations
        for(LocationEntry entry2 : locationEntries) {

            // Look for a precise match
            if(entry2.equals(location)) {
                // When found save reference to precise match and stop looking
                ret = entry2;
                break;
            }
        }

        // Return location if found or null
        return ret;
    }

    public @NotNull ArrayList<LocationEntry> existsAll(@NotNull UUID owner) {
        ArrayList<LocationEntry> foundLocationEntries = new ArrayList<>();

        LocationEntry locationEntry;
        do {
            locationEntry = exists(owner);
            if(locationEntry != null)
                foundLocationEntries.add(locationEntry);
        }while (locationEntry != null);

        return foundLocationEntries;
    }

    public @NotNull ArrayList<LocationEntry> existsAll(@NotNull Location location, @NotNull UUID owner) {
        ArrayList<LocationEntry> foundLocationEntries = new ArrayList<>();

        LocationEntry locationEntry;
        do {
            locationEntry = exists(location, owner);
            if(locationEntry != null)
                foundLocationEntries.add(locationEntry);
        }while (locationEntry != null);

        return foundLocationEntries;
    }

    public @NotNull ArrayList<LocationEntry> existsAll(@NotNull Location location) {
        ArrayList<LocationEntry> foundLocationEntries = new ArrayList<>();

        LocationEntry locationEntry;
        do {
            locationEntry = exists(location);
            if(locationEntry != null)
                foundLocationEntries.add(locationEntry);
        }while (locationEntry != null);

        return foundLocationEntries;
    }

    // List of locations
    public ArrayList<LocationEntry> locationEntries = new ArrayList<>();

    // Plugin
    public final DespawnedItems2 plugin;
}
