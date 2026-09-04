package me.mats.common.world;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChunkPreloader {

    // Fires off an async load/generate for every chunk in a square radius around center.
    // One-shot: chunks are not force-loaded, they just get pulled into memory ahead of time
    // so players don't hit the generation/read cost when they actually arrive.
    public static CompletableFuture<Void> preload(World world, Location center, int radiusChunks) {
        int centerX = center.getBlockX() >> 4;
        int centerZ = center.getBlockZ() >> 4;

        List<CompletableFuture<Chunk>> futures = new ArrayList<>();
        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                futures.add(world.getChunkAtAsync(centerX + dx, centerZ + dz, true));
            }
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> Bukkit.getLogger().info("Preloaded " + futures.size() + " chunks around " + world.getName()));
    }
}
