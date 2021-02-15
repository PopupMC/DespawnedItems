package com.popupmc.despawneditems2.config;

import com.popupmc.despawneditems2.DespawnedItems2;
import org.jetbrains.annotations.NotNull;

public class Config {
    public Config(@NotNull DespawnedItems2 plugin) {
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
    public final DespawnedItems2 plugin;
}
