package me.mats.common.game.waiting;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.ArrayList;
import java.util.List;

public class WaitingWorld {
    private static List<WaitingWorld> worlds = new ArrayList<>();
    private static int[] available = {1,2,3};


    private World world;
    private int num;

    public WaitingWorld() {
        // Load next available WaitingWorld
        num = available[0];
        available = ArrayUtils.remove(available, 0);
        WorldCreator wc = new WorldCreator("WaitingWorld"+num);
        Bukkit.getServer().getLogger().info("Loaded WaitingWorld"+num);
        world = wc.createWorld();
        world.setAutoSave(false);
        worlds.add(this);
    }

    public void free() {
        if (!Bukkit.getServer().isTickingWorlds()) {
            Bukkit.unloadWorld(world, false);
        } else {
            Bukkit.getLogger().warning("Couldn't unload WaitingWorld "+world.getName());
        }
        worlds.remove(this);
        available = ArrayUtils.add(available, num);
    }

    // Needed for onDisable since it iterates through list itself
    public void freeAll() {
        if (!Bukkit.getServer().isTickingWorlds()) {
            Bukkit.unloadWorld(world, false);
        }
        available = ArrayUtils.add(available, num);
    }

    public static void setWorlds(List<WaitingWorld> worlds) {
        WaitingWorld.worlds = worlds;
    }

    public static List<WaitingWorld> getWorlds() {
        return worlds;
    }

    public World getWorld() {
        return world;
    }

    public static int[] getAvailable() {
        return available;
    }
}
