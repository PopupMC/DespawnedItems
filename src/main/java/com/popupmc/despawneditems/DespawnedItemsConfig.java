package com.popupmc.despawneditems;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class DespawnedItemsConfig {
    static void load() {
        // Create if doesnt exist
        DespawnedItems.plugin.saveDefaultConfig();

        // Load main config file
        FileConfiguration config = DespawnedItems.plugin.getConfig();

        particlesEnabled = config.getBoolean("particles.enabled", true);
        particleFX = Particle.valueOf(config.getString("particles.particle", "VILLAGER_HAPPY"));
        particleLengthSeconds = config.getInt("particles.length-seconds", 3);
        newParticlesEveryNthTick = config.getInt("particles.new-every-nth-tick", 2);
        particleCountEveryNthTick = config.getInt("particles.count-every-nth-tick", 15);
        particleRandomRadius = (float)config.getDouble("particles.radius", 0.5);

        soundEnabled = config.getBoolean("sound.enabled", true);
        soundFX = Effect.valueOf(config.getString("sound.sound", "EXTINGUISH"));
        soundData = config.getInt("sound.data", 0);
        soundRadius = config.getInt("sound.radius", 15);

        // Get locations file
        locationsConfigFile = new File(DespawnedItems.plugin.getDataFolder(), "locations.yml");
        locationsConfig = new YamlConfiguration();

        // Save only if doesn't exist to prevent annoying warning, this is literally the dumbest functionality
        // By definition if I say don't replace then warning me that your not replacing is pretty silly and stupid
        // and causes me to have to write extra code to prevent that warning defeating the whole purpose of the option
        if(!locationsConfigFile.exists()) {
            DespawnedItems.plugin.saveResource("locations.yml", false);
        }

        // Load locations file, prepare for errors
        try {
            locationsConfig.load(locationsConfigFile);
        }
        catch (IOException | InvalidConfigurationException ex) {
            DespawnedItems.plugin.getLogger().warning("Unable to load locations.yml file");
            ex.printStackTrace();
            return;
        }

        // Clear cached old locations
        // Useful for a reload, Otherwise a reload would add even more locations (doubling the list)
        locs.clear();

        // Load list of locations
        List<String> locsStrList = locationsConfig.getStringList("locations");

        // Loop through each location
        for(String locStr : locsStrList) {

            // A location has this format
            // "world_name;x;y;z"
            // Split up the string into pieces
            String[] locStrPieces = locStr.split(";");

            // There have to be 4 pieces to each list item, if not then it was hand-edited incorrectly or a saving
            // error happened and we must notify of error and skip this entry
            if(locStrPieces.length < 4) {
                DespawnedItems.plugin.getLogger().warning("Location: " + locStr + " is missing 4 pieces, skipping entry");
                continue;
            }

            // Prepare to hold a single location entry
            LocationEntry loc;

            // Parsing from string is error prone, prepare to encounter errors
            try {
                // Parse the location from the string
                // Start with x, y, and z first (#1, #2, & #3)
                // Then the world name #0
                loc = new LocationEntry(Integer.parseInt(locStrPieces[1]),
                        Integer.parseInt(locStrPieces[2]),
                        Integer.parseInt(locStrPieces[3]),
                        locStrPieces[0]);
            }
            // The location string is wrong which means it was badly hand edited or saved wrong
            // Notify of warning and skip
            catch (NumberFormatException ex) {
                DespawnedItems.plugin.getLogger().warning("Location: " + locStr + " coords can't be parsed, skipping entry.");
                continue;
            }

            // Add it to the list, if it already exists then warn and skip (Prevents duplication)
            if(!locs.add(loc))
                DespawnedItems.plugin.getLogger().warning("Location: " + locStr + " already exists, skipping...");
        }
    }

    static void save() {
        // Create new empty list
        ArrayList<String> locsStrList = new ArrayList<>();

        // Add in all the locations as strings
        for(LocationEntry loc : locs) {
            locsStrList.add(loc.world + ";" + loc.x + ";" + loc.y + ";" + loc.z);
        }

        // Save list into YML file
        locationsConfig.set("locations", locsStrList);

        // Delete file, if it doesnt exist wont throw error
        boolean res = locationsConfigFile.delete();

        // Save YML file
        try {
            locationsConfig.save(locationsConfigFile);
        } catch (IOException ex) {
            DespawnedItems.plugin.getLogger().warning("Unable to save locations.yml file");
            ex.printStackTrace();
        }
    }

    static boolean add(Location loc) {
        // Check if the location exists, if it does a location is returned
        LocationEntry ret = exists(loc);

        // If there's no location returned then it's safe to add it meaning it doesn't
        // already exist
        if(ret == null) {
            // Add location to list
            locs.add(new LocationEntry(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName()));

            // Do a save
            save();
        }

        // Return whether or not it already existed by checking to see if a location was returned
        return ret == null;
    }

    static boolean rem(Location loc) {
        // Check to see if exists, if it does a location is returned
        LocationEntry ret = exists(loc);

        // Only remove if it exists
        if(ret != null) {
            // Remove using returned location
            locs.remove(ret);

            // Save updated list
            save();
        }

        // Return if successful or not
        return ret != null;
    }

    static LocationEntry exists(Location loc2) {

        // Set return value to be null (Doesn't exist)
        LocationEntry ret = null;

        // Attempt to find it in the locations
        for(LocationEntry loc : DespawnedItemsConfig.locs) {

            // Look for a precise match
            if(loc.x == loc2.getBlockX() &&
                    loc.y == loc2.getBlockY() &&
                    loc.z == loc2.getBlockZ() &&
                    loc.world.equals(loc2.getWorld().getName())) {

                // When found save reference to precise match and stop looking
                ret = loc;
                break;
            }
        }

        // Return location if found or null
        return ret;
    }

    // List of locations
    static ArrayList<LocationEntry> locs = new ArrayList<>();

    // Other config
    static boolean particlesEnabled = true;
    static Particle particleFX = Particle.VILLAGER_HAPPY; // Particle to use
    static int particleLengthSeconds = 3; // Particles last 3 seconds
    static int newParticlesEveryNthTick = 2; // Particles sent to client every 2 ticks
    static int particleCountEveryNthTick = 15; // 15 particles everytime sent
    static float particleRandomRadius = 0.5f;

    static boolean soundEnabled = true;
    static int soundRadius = 15; // Sound radius in blocks
    static Effect soundFX = Effect.EXTINGUISH; // Sound to play
    static int soundData = 0; // Sound data, usually 0 (Unused) depends on sound, some sounds require a data number

    // Location File
    private static File locationsConfigFile;
    private static FileConfiguration locationsConfig;
}
