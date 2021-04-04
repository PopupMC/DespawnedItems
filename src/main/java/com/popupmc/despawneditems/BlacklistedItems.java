package com.popupmc.despawneditems;

import org.bukkit.Material;

import java.util.ArrayList;

public class BlacklistedItems {
    public static void setup() {
        for(Material type : Material.values()) {

            // Skip air
            if(type.isAir())
                continue;

            if(type == Material.JIGSAW ||
                    type == Material.SPAWNER ||
                    type == Material.DEBUG_STICK)
                continue;

            // get name
            String name = type.name().toLowerCase();

            // Skip any kind of blocks that have these names
            if(name.contains("command") ||
                    name.contains("spawn") ||
                    name.contains("legacy") ||
                    name.contains("structure") ||
                    name.contains("gold") ||
                    name.contains("golden") ||
                    name.contains("iron") ||
                    name.contains("diamond") ||
                    name.contains("emerald") ||
                    name.contains("netherite"))
                continue;

            itemList.add(type);
        }
    }

    public static final ArrayList<Material> itemList = new ArrayList<>();
}
