package me.mats.common.game.waiting;

import me.mats.common.game.GameManager;
import me.mats.common.game.Team;
import me.mats.common.customInventory.CustomInventoryManager;
import me.mats.common.customInventory.TeamSelectorInventory;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

public class WaitingWorldListener implements Listener {

    private final GameManager<?> manager;

    public WaitingWorldListener(GameManager<?> manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onDiamondBreakEvent(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (manager.getPlayers().contains(p)) {
            if (e.getBlock().getType() == Material.DEEPSLATE_DIAMOND_ORE) {
                p.playSound(p, Sound.BLOCK_PISTON_CONTRACT, SoundCategory.MASTER, 1, 1);
                Location pos = new Location(p.getWorld(), 16, 101, 16);
                pos.getBlock().setType(Material.AIR);
                Bukkit.getScheduler().scheduleSyncDelayedTask(manager.getPlugin(), () -> {
                    pos.getBlock().setType(Material.DEEPSLATE);
                    p.playSound(p, Sound.BLOCK_PISTON_EXTEND, SoundCategory.MASTER, 1, 1);
                }, 200);
            }
            e.setCancelled(true);
            if (e.getBlock().getType().toString().contains("WOOL")) {
                Team team = manager.getTeam(p);

                if (team != null) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(manager.getPlugin(), () -> {
                        team.getWaitingTeam().updateLobbyBlocks(p);
                    }, 1);
                }
            }
        }
    }

    @EventHandler
    public void onSelectorClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (manager.getPlayers().contains(p)) {
            if (p.getInventory().getItemInMainHand().getType().toString().contains("_BED")) {
                CustomInventoryManager.openInventory(p, new TeamSelectorInventory(manager,p));

            } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block clickedBlock = e.getClickedBlock();
                if (clickedBlock != null && clickedBlock.getType() == Material.POLISHED_BLACKSTONE_BUTTON) {
                    p.playSound(p, Sound.BLOCK_PISTON_CONTRACT, SoundCategory.MASTER, 1, 1);
                    World w = p.getWorld();
                    Location pos1 = new Location(w, 5, 106, -26);
                    Location pos2 = new Location(w, 5, 107, -26);
                    Location pos3 = new Location(w, 5, 108, -26);
                    pos1.getBlock().setType(Material.AIR);
                    pos2.getBlock().setType(Material.AIR);
                    pos3.getBlock().setType(Material.AIR);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(manager.getPlugin(), () -> {
                        pos1.getBlock().setType(Material.NETHERRACK);
                        pos2.getBlock().setType(Material.NETHERRACK);
                        pos3.getBlock().setType(Material.NETHERRACK);
                        p.playSound(p, Sound.BLOCK_PISTON_EXTEND, SoundCategory.MASTER, 1, 1);
                    }, 70);
                }
            }

            if (e.getAction() != Action.LEFT_CLICK_BLOCK) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEndPortalUse(PlayerPortalEvent e) {
        Player p = e.getPlayer();
        if (manager.getPlayers().contains(p)) {
            if (e.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
                e.setCanCreatePortal(false);
                e.setCancelled(true);
                p.playSound(p, Sound.BLOCK_END_PORTAL_SPAWN, SoundCategory.MASTER, 1, 1);
                p.teleport(new Location(p.getWorld(), 0, 101, 21));
            }
        }
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
            if (e.getEntityType() == EntityType.ENDER_CRYSTAL) {
                p.playSound(p, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.MASTER, 1, 1);
                p.teleport(new Location(p.getWorld(), 1, 101, 0));
            }
            // Still get Damage sound
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onSwapHandInteract(PlayerSwapHandItemsEvent e) {
        if (manager.getPlayers().contains(e.getPlayer())) {
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
        if (e.getEntity() instanceof Player p && manager.getPlayers().contains(p)) {
            e.setCancelled(true);
        }
    }

}
