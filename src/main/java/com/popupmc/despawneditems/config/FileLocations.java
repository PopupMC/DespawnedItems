package com.popupmc.despawneditems.config;

import com.popupmc.despawneditems.DespawnedItems;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class FileLocations {
    public FileLocations(@NotNull DespawnedItems plugin) {
        this.plugin = plugin;
        load();
    }

    public @NotNull Path getplayerDataDir() {
        return Paths.get(plugin.getDataFolder().toPath().toString(), "userdata");
    }

    public boolean ensurePlayerDataDirExists() {
        try {
            Files.createDirectories(getplayerDataDir());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void load() {

        // Ensure player data exists
        if (!ensurePlayerDataDirExists())
            return;

        File folder = new File(getplayerDataDir().toString());
        File[] listOfFiles = folder.listFiles();

        for(File file : listOfFiles) {
            if(!file.getName().endsWith(".yml"))
                continue;

            String uuidStr = file.getName().substring(0, file.getName().length() - 4);
            UUID owner;

            try {
                plugin.getLogger().info("Attempting to convert file name to UUID: " + uuidStr);
                owner = UUID.fromString(uuidStr);
            }
            catch (IllegalArgumentException ex) {
                plugin.getLogger().info("Failed");
                continue;
            }

            loadFile(owner, file);
        }

        // Rebuild indexes
        if(plugin.despawnIndexes != null)
            plugin.despawnIndexes.rebuildIndexes();
    }

    public void loadFile(@NotNull UUID owner, @NotNull File file) {
        // Get locations file
        FileConfiguration fileLocationsConfig = new YamlConfiguration();

        // Create from template if doesn't exist
        if(!file.exists()) {
            try {
                Files.copy(
                        Objects.requireNonNull(plugin.getResource("locations.yml")),
                        file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        // Load locations file, prepare for errors
        try {
            fileLocationsConfig.load(file);
        }
        catch (IOException | InvalidConfigurationException ex) {
            plugin.getLogger().warning("Unable to load file " + file.getName());
            ex.printStackTrace();
            return;
        }

        // Load list of locations
        List<String> flattenedLocationEntries = fileLocationsConfig.getStringList("locations");

        // Loop through each location
        for(String flattenedLocationEntry : flattenedLocationEntries) {
            LocationEntry locationEntry = LocationEntry.fromString(flattenedLocationEntry, owner, plugin);
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
        // Ensure player data exists
        if (!ensurePlayerDataDirExists())
            return;

        // Split location entries up into groups by owner
        Hashtable<UUID, ArrayList<LocationEntry>> splitFiles = new Hashtable<>();

        for(LocationEntry locationEntry : new ArrayList<>(locationEntries)) {

            UUID owner = locationEntry.owner;

            if(!splitFiles.containsKey(owner))
                splitFiles.put(owner, new ArrayList<>());

            ArrayList<LocationEntry> splitLocationEntries = splitFiles.get(owner);
            splitLocationEntries.add(locationEntry);

            splitFiles.put(owner, splitLocationEntries);
        }

        for(Map.Entry<UUID, ArrayList<LocationEntry>> entry : splitFiles.entrySet()) {
            // Get file to user data
            Path path = Paths.get(getplayerDataDir().toString(), entry.getKey().toString() + ".yml");
            File file = new File(path.toString());

            // Save it
            saveFile(file, entry.getValue());
        }
    }

    public void saveFile(@NotNull File file, @NotNull ArrayList<LocationEntry> splitLocationEntries) {
        // Create new empty list
        ArrayList<String> flattenedLocationEntries = new ArrayList<>();

        // Add in all the locations flattened
        for(LocationEntry locationEntry : splitLocationEntries) {
            flattenedLocationEntries.add(locationEntry.toString());
        }

        // Get locations file
        FileConfiguration fileLocationsConfig = new YamlConfiguration();

        // Save list into YML file
        fileLocationsConfig.set("locations", flattenedLocationEntries);

        // Save YML file
        try {
            fileLocationsConfig.save(file);
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

    public int removeAll() {
        int count = locationEntries.size();
        locationEntries.clear();
        return count;
    }

    public @Nullable LocationEntry exists(@NotNull LocationEntry entry) {

        // Set return value to be null (Doesn't exist)
        LocationEntry ret = null;

        // Attempt to find it in the locations
        for(LocationEntry entry2 : new ArrayList<>(locationEntries)) {

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
        for(LocationEntry entry2 : new ArrayList<>(locationEntries)) {
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
        for(LocationEntry entry2 : new ArrayList<>(locationEntries)) {

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
        for(LocationEntry entry2 : new ArrayList<>(locationEntries)) {

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

        for(LocationEntry locationEntry : new ArrayList<>(this.locationEntries)) {
            if(locationEntry.equals(owner))
                foundLocationEntries.add(locationEntry);
        }

        return foundLocationEntries;
    }

    public @NotNull ArrayList<LocationEntry> existsAll(@NotNull Location location) {
        ArrayList<LocationEntry> foundLocationEntries = new ArrayList<>();

        for(LocationEntry locationEntry : new ArrayList<>(this.locationEntries)) {
            if(locationEntry.equals(location))
                foundLocationEntries.add(locationEntry);
        }

        return foundLocationEntries;
    }

    // List of locations
    public ArrayList<LocationEntry> locationEntries = new ArrayList<>();

    // Plugin
    public final DespawnedItems plugin;
}
