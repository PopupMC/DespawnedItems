package com.popupmc.despawneditems;

// Contains a single location entry
class LocationEntry {
    int x;
    int y;
    int z;
    String world;

    public LocationEntry(int x, int y, int z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }
}
