package com.popupmc.despawneditems.despawn;

import com.popupmc.despawneditems.DespawnedItems;
import com.popupmc.despawneditems.config.LocationEntry;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class DespawnEffect {
    public DespawnEffect(LocationEntry locationEntry, DespawnedItems plugin) {
        this.plugin = plugin;
        this.locationEntry = locationEntry;

        // Here we calculate how long to let the timer go
        // We convert the configured time in seconds (3) to ticks (60)
        // Then to get the number of loops we divide how spaced apart the loops are
        // Every loop is 2 ticks so to stretch it out over 3 seconds that would be 10 loops every 2 ticks
        loopsLeft = (plugin.config.fileConfig.particleLengthSeconds * second) /
                plugin.config.fileConfig.newParticlesEveryNthTick;

        // Save reference so it's not GC
        plugin.effectsPlaying.add(this);

        play();
    }

    public void play() {
        // Get world & center location
        World world = locationEntry.location.getWorld();
        Location center = locationEntry.location.toCenterLocation();

        // Play sound 1 time only if chunk is loaded and if sound enabled
        if(plugin.config.fileConfig.soundEnabled && world.isChunkLoaded(center.getBlockX(), center.getBlockZ()))
            world.playEffect(center,
                    plugin.config.fileConfig.soundFX,
                    plugin.config.fileConfig.soundData,
                    plugin.config.fileConfig.soundRadius);

        // If particles disabled then stop here and remove class inst
        if(!plugin.config.fileConfig.particlesEnabled) {
            plugin.effectsPlaying.remove(this);
            return;
        }

        // So the inner class can access the outer class easily and without worry for conflict or shadowing
        DespawnEffect self = this;

        // Save instance so it's not gc
        inst = new BukkitRunnable() {
            @Override
            public void run() {
                // If the chunk is not loaded do nothing, there is no need to load an empty chunk to play
                // an effect that nobody is going to hear or see
                if (!world.isChunkLoaded(center.getBlockX(), center.getBlockZ())) {
                    // End of this loop
                    self.loopEnd();
                    return;
                }

                // Play 2 sets of particles at the center of the block
                // Going out from center to positive and negative space on the X & Z axis
                // We divide particle count by 2 because we're issuing 2 particle effects

                world.spawnParticle(plugin.config.fileConfig.particleFX,
                        center,
                        plugin.config.fileConfig.particleCountEveryNthTick / 2,
                        plugin.config.fileConfig.particleRandomRadius,
                        0,
                        plugin.config.fileConfig.particleRandomRadius);

                world.spawnParticle(plugin.config.fileConfig.particleFX,
                        center,
                        plugin.config.fileConfig.particleCountEveryNthTick / 2,
                        -plugin.config.fileConfig.particleRandomRadius,
                        0,
                        -plugin.config.fileConfig.particleRandomRadius);

                // End of this loop
                self.loopEnd();
            }
        }.runTaskTimer(plugin, 1, plugin.config.fileConfig.newParticlesEveryNthTick);
    }

    // check if it needs to be self-destroyed or not
    public void checkSelfDestroy() {
        if (loopsLeft <= 0) {
            // Remove saved reference that prevents GC (thus allowing GC)
            plugin.effectsPlaying.remove(this);

            // Tell Bukkit to stop the task
            inst.cancel();
        }
    }

    public void forceSelfDestroy() {
        // Remove saved reference that prevents GC (thus allowing GC)
        plugin.effectsPlaying.remove(this);

        // Tell Bukkit to stop the task
        inst.cancel();
    }

    // Mark end of loop
    public void loopEnd() {
        // Decrement loops left and see if it needs to be self-destroyed
        loopsLeft -= 1;
        checkSelfDestroy();
    }

    // Instance Data
    int loopsLeft;
    BukkitTask inst = null; // Holds inst so it's not GC

    public final DespawnedItems plugin;
    public final LocationEntry locationEntry;

    // Just something easy to reference for code cleanliness
    final static int second = 20; // 1 second = 20 ticks
}
