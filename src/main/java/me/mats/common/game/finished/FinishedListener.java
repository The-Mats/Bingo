package me.mats.common.game.finished;

import me.mats.common.game.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class FinishedListener implements Listener {

    private final GameManager<?> manager;

    public FinishedListener(GameManager<?> manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (manager.getPlayers().contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityInteract(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p && manager.getPlayers().contains(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent e) {
        if (manager.getPlayers().contains((Player) e.getWhoClicked())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && (manager.getPlayers().contains(p))) {
            e.setCancelled(true);

        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (manager.getPlayers().contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

}
