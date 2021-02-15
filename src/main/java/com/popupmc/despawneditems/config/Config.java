package com.popupmc.despawneditems.config;

import com.popupmc.despawneditems.DespawnedItems;
import org.jetbrains.annotations.NotNull;

public class Config {
    public Config(@NotNull DespawnedItems plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        // Start a new instance of the config files
        this.fileConfig = new FileConfig(plugin);
        this.fileLocations = new FileLocations(plugin);
    }

    public FileConfig fileConfig;
    public FileLocations fileLocations;
    public final DespawnedItems plugin;
}
