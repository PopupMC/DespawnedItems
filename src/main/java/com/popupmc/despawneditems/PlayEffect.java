package com.popupmc.despawneditems;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

class PlayEffect {
    // Plays an effect
    void play(Location loc) {

        // Get world
        World world = loc.getWorld();

        // Get center location
        Location centerLoc = loc.toCenterLocation();

        // Play sound 1 time only if chunk is loaded and if sound enabled
        if(DespawnedItemsConfig.soundEnabled && world.isChunkLoaded(loc.getBlockX(), loc.getBlockZ()))
            world.playEffect(centerLoc, DespawnedItemsConfig.soundFX, DespawnedItemsConfig.soundData, DespawnedItemsConfig.soundRadius);

        // If particles disabled then stop here and remove class inst
        if(!DespawnedItemsConfig.particlesEnabled) {
            DespawnedItems.effectsPlaying.remove(this);
        }

        // So the inner class can access the outer class easily and without worry for conflict or shadowing
        PlayEffect self = this;

        // Save instance so it's not gc
        inst = new BukkitRunnable() {
            @Override
            public void run() {
                // If the chunk is not loaded do nothing, there is no need to load an empty chunk to play
                // an effect that nobody is going to hear or see
                if(!world.isChunkLoaded(loc.getBlockX(), loc.getBlockZ())) {
                    // End of this loop
                    self.loopEnd();
                    return;
                }

                // Play 2 sets of particles at the center of the block
                // Going out from center to positive and negative space on the X & Z axis
                world.spawnParticle(DespawnedItemsConfig.particleFX, centerLoc, DespawnedItemsConfig.particleCountEveryNthTick / 2, DespawnedItemsConfig.particleRandomRadius, 0, DespawnedItemsConfig.particleRandomRadius);
                world.spawnParticle(DespawnedItemsConfig.particleFX, centerLoc, DespawnedItemsConfig.particleCountEveryNthTick / 2, -DespawnedItemsConfig.particleRandomRadius, 0, -DespawnedItemsConfig.particleRandomRadius);

                // End of this loop
                self.loopEnd();
            }

            // Run on the next tick at given interval
        }.runTaskTimer(DespawnedItems.plugin, 1, DespawnedItemsConfig.newParticlesEveryNthTick);
    }

    // check if it needs to be self-destroyed or not
    public void checkSelfDestroy() {
        if (loopsLeft <= 0) {
            // Remove saved reference that prevents GC (thus allowing GC)
            DespawnedItems.effectsPlaying.remove(this);

            // Tell Bukkit to stop the task
            inst.cancel();
        }
    }

    // Mark end of loop
    public void loopEnd() {
        // Decrement loops left and see if it needs to be self-destroyed
        loopsLeft -= 1;
        checkSelfDestroy();
    }

    // Instance Data
    int loopsLeft = (DespawnedItemsConfig.particleLengthSeconds * second) / DespawnedItemsConfig.newParticlesEveryNthTick;
    BukkitTask inst = null; // Holds inst so it's not GC

    // Just something easy to reference for code cleanliness
    final static int second = 20; // 1 second = 20 ticks
}
