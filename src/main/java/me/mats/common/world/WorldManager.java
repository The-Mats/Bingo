package me.mats.common.world;


import org.bukkit.*;
import org.bukkit.plugin.Plugin;
import org.codehaus.plexus.util.FileUtils;

import java.io.IOException;

public class WorldManager {

    private Plugin plugin;

    public WorldManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public static void deleteWorld(World world) {
        if (!Bukkit.getServer().isTickingWorlds()) {
            Bukkit.unloadWorld(world, false);
        } else {
            Bukkit.getLogger().warning("Couldn't unload World "+world.getName());
        }
        try {
            FileUtils.deleteDirectory(world.getWorldFolder());
        } catch (IOException e) {
            Bukkit.getLogger().warning("Couldn't delete World "+world.getName());
        }

    }
}
  