package me.mats.common.game.ingame;

import me.mats.common.customInventory.CustomInventoryManager;
import me.mats.common.game.GameManager;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

public class BackpackListener implements Listener {

    private final GameManager<?> manager;

    public BackpackListener(GameManager<?> manager) {
        this.manager = manager;
    }



    @EventHandler
    public void onBackpackChangeItem(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (manager.getPlayers().contains(p)) {
            if (e.getClick() == ClickType.RIGHT && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BUNDLE && !(e.getCursor().getType() == Material.AIR)) { // Add to Backpack
                BundleMeta newBackpack = manager.getTeam(p).addItemToBackPack(e.getCursor());
                if (newBackpack != null) {
                    e.getCurrentItem().setItemMeta(newBackpack);
                    e.getView().setCursor(new ItemStack(Material.AIR));
                }
                e.setCancelled(true);

            } else if (e.getClick() == ClickType.RIGHT && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BUNDLE && e.getCursor().getType() == Material.AIR) { // Remove from Backpack
                Pair<BundleMeta, ItemStack> pair = manager.getTeam(p).removeItemFromBackPack();
                if (pair != null) {
                    e.getCurrentItem().setItemMeta(pair.getLeft());
                    e.getView().setCursor(pair.getRight());
                }
                e.setCancelled(true);
            } else if (e.getClick() == ClickType.LEFT && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BUNDLE) { // Update Backpack
                e.getCurrentItem().setItemMeta(manager.getTeam(p).updateBackPack());
            }

        }
    }


    @EventHandler
    public void onBackpackOpen(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (manager.getPlayers().contains(p)) {
            if (p.getInventory().getItemInMainHand().getType() == Material.BUNDLE || p.getInventory().getItemInOffHand().getType() == Material.BUNDLE) {
                CustomInventoryManager.openInventory(p, manager.getTeam(p).getBackpackInventory());
                e.setCancelled(true);
                // setCancelled only blocks the block-interaction half of the right-click; without
                // denying the item-use half too, vanilla leaves a stale "in-use" reference on this
                // bundle that can resurface as an extra copy in the held slot after a respawn.
                e.setUseItemInHand(Event.Result.DENY);
            }
        }
    }

    @EventHandler
    public void onBackpackDrop(PlayerDropItemEvent e) {
        if (e.getItemDrop().getItemStack().getType() == Material.BUNDLE && manager.getPlayers().contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    // This only occurs if someone died or broke Inventory
    @EventHandler
    public void onBackpackPickup(EntityPickupItemEvent e) {
        if (e.getItem().getItemStack().getType() == Material.BUNDLE && e.getEntity() instanceof Player p && manager.getPlayers().contains(p)) {
            e.getItem().getItemStack().setItemMeta(manager.getTeam(p).updateBackPack());
        }
    }

}
