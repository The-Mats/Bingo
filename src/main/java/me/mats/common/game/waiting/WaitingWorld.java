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

    private final World world;
    private final int num;

    private WaitingWorld(int num, World world) {
        this.num = num;
        this.world = world;
        worlds.add(this);
    }

    // Checks out the next available pre-built waiting world, trying every remaining pool
    // number in turn. Returns null if the pool is empty, or every remaining number failed to
    // actually load (e.g. still loaded from a checkout whose free() had to skip the unload -
    // see free() below). Never throws: a bad pool entry means a graceful "no world available"
    // to the caller, not a crash.
    public static WaitingWorld tryAcquire() {
        while (available.length >= 1) {
            int num = available[0];
            available = ArrayUtils.remove(available, 0);

            WorldCreator wc = new WorldCreator("WaitingWorld"+num);
            World world = wc.createWorld();
            if (world != null) {
                Bukkit.getServer().getLogger().info("Loaded WaitingWorld"+num);
                world.setAutoSave(false);
                return new WaitingWorld(num, world);
            }

            // Deliberately not returned to `available` - its Bukkit world is presumably still
            // loaded somewhere, so handing this number out again would just repeat the failure.
            Bukkit.getLogger().warning("Failed to load WaitingWorld"+num+" (still loaded from a skipped unload?) - trying the next one");
        }
        return null;
    }

    public void free() {
        if (!Bukkit.getServer().isTickingWorlds()) {
            Bukkit.unloadWorld(world, false);
            // Only hand this number back out once we know it's actually unloaded - otherwise
            // the next checkout would try to load a world Bukkit still considers in use.
            available = ArrayUtils.add(available, num);
        } else {
            Bukkit.getLogger().warning("Couldn't unload WaitingWorld "+world.getName()+" - it will stay checked out for the rest of this server session");
        }
        worlds.remove(this);
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
