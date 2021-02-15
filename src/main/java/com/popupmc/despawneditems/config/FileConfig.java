package com.popupmc.despawneditems.config;

import com.popupmc.despawneditems.DespawnedItems;
import org.bukkit.Effect;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class FileConfig {
    public FileConfig(@NotNull DespawnedItems plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        // Load main config file
        FileConfiguration config = plugin.getConfig();

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
    }

    public boolean particlesEnabled = true;
    public Particle particleFX = Particle.VILLAGER_HAPPY; // Particle to use
    public int particleLengthSeconds = 3; // Particles last 3 seconds
    public int newParticlesEveryNthTick = 2; // Particles sent to client every 2 ticks
    public int particleCountEveryNthTick = 15; // 15 particles everytime sent
    public float particleRandomRadius = 0.5f;

    public boolean soundEnabled = true;
    public int soundRadius = 15; // Sound radius in blocks
    public Effect soundFX = Effect.EXTINGUISH; // Sound to play
    public int soundData = 0; // Sound data, usually 0 (Unused) depends on sound, some sounds require a data number

    public final DespawnedItems plugin;
}
