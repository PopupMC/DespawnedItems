package com.popupmc.despawneditems.despawn;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.config.LocationEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Random;

public class DespawnIndexes {
    public DespawnIndexes(@NotNull DespawnedItems plugin) {
        this.plugin = plugin;
        rebuildIndexes();
    }

    public void rebuildIndexes() {
        // Clear list
        locationEntryIndexes.clear();

        // Add in all the location indexes
        int counter = 0;
        for(LocationEntry ignored : new ArrayList<>(plugin.config.fileLocations.locationEntries)) {
            locationEntryIndexes.add(counter);
            counter++;
        }
    }

    // Obtain a random coord
    public @NotNull LocationEntry randomChestCoord() {
        // Regen if empty
        if(locationEntryIndexes.size() <= 0)
            rebuildIndexes();

        // Get random index of random indexes
        int randomLocationEntryIndex = random.nextInt(locationEntryIndexes.size());

        // Get location index from random index
        int locationEntryIndex = locationEntryIndexes.get(randomLocationEntryIndex);

        // Get location with location index
        LocationEntry locationEntry = plugin.config.fileLocations.locationEntries.get(locationEntryIndex);

        // Remove used random index
        locationEntryIndexes.remove(randomLocationEntryIndex);

        return locationEntry;
    }

    public final DespawnedItems plugin;
    public final ArrayList<Integer> locationEntryIndexes = new ArrayList<>();
    public final Random random = new Random();
}
