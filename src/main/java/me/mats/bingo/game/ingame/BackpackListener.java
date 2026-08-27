package me.mats.bingo.game.ingame;

import io.papermc.paper.event.player.PlayerPickItemEvent;
import me.mats.common.customInventory.CustomInventoryManager;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

public class BackpackListener implements Listener {

    private final IngameState state;

    public BackpackListener(IngameState state) {
        this.state = state;
    }



    @EventHandler
    public void onBackpackChangeItem(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (state.getManager().getPlayers().contains(p)) {
            if (e.getClick() == ClickType.RIGHT && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BUNDLE && !(e.getCursor().getType() == Material.AIR)) { // Add to Backpack
                //Bukkit.getLogger().info("Added to backpack");
                BundleMeta newBackpack = state.getManager().getTeam(p).addItemToBackPack(e.getCursor());
                if (newBackpack != null) {
                    e.getCurrentItem().setItemMeta(newBackpack);
                    e.getView().setCursor(new ItemStack(Material.AIR));
                }
                e.setCancelled(true);

            } else if (e.getClick() == ClickType.RIGHT && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BUNDLE && e.getCursor().getType() == Material.AIR) { // Remove from Backpack
                //Bukkit.getLogger().info("Removed from backpack");
                Pair<BundleMeta, ItemStack> pair = state.getManager().getTeam(p).removeItemFromBackPack();
                if (pair != null) {
                    e.getCurrentItem().setItemMeta(pair.getLeft());
                    e.getView().setCursor(pair.getRight());
                }
                e.setCancelled(true);
            } else if (e.getClick() == ClickType.LEFT && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BUNDLE) { // Update Backpack
                //Bukkit.getLogger().info("Updated backpack");
                e.getCurrentItem().setItemMeta(state.getManager().getTeam(p).updateBackPack());
            }

        }
    }


    @EventHandler
    public void onBackpackOpen(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (state.getManager().getPlayers().contains(p)) {
            if (p.getInventory().getItemInMainHand().getType() == Material.BUNDLE || p.getInventory().getItemInOffHand().getType() == Material.BUNDLE) {
                CustomInventoryManager.openInventory(p, state.getManager().getTeam(p).getBackpackInventory());
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBackpackDrop(PlayerDropItemEvent e) {
        if (e.getItemDrop().getItemStack().getType() == Material.BUNDLE && state.getManager().getPlayers().contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    // This only occurs if someone died or broke Inventory
    @EventHandler
    public void onBackpackPickup(EntityPickupItemEvent e) {
        if (e.getItem().getItemStack().getType() == Material.BUNDLE && e.getEntity() instanceof Player p && state.getManager().getPlayers().contains(p)) {
            e.getItem().getItemStack().setItemMeta(state.getManager().getTeam(p).updateBackPack());
        }
    }

}
