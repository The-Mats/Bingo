package me.mats.bingo.game.ingame.spawn;

import me.mats.bingo.game.ingame.BingoIngameState;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

// Protects the exact spawn platform baked into bingospawn.nbt (a 31x31 area around spawn).
// Tied to that structure's specific dimensions, so kept separate from the generic SpawnListener.
public class SpawnProtectionListener implements Listener {

    private final BingoIngameState state;

    public SpawnProtectionListener(BingoIngameState state) {
        this.state = state;
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();
        if (state.getManager().getPlayers().contains(p)) {
            if (b.getX() <= 15 && b.getX() >= -15 && b.getZ() <= 15 && b.getZ() >= -15 && b.getY() >= state.getManager().getWorld().getSpawnLocation().getY()-5) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlockPlaced();
        if (state.getManager().getPlayers().contains(p)) {
            if (b.getX() <= 15 && b.getX() >= -15 && b.getZ() <= 15 && b.getZ() >= -15 && b.getY() >= state.getManager().getWorld().getSpawnLocation().getY() - 5) {
                e.setCancelled(true);
            }
        }
    }
}
