package me.mats.common.game.ingame.abilities;

import me.mats.common.game.ingame.IngameState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;

// Generic "which quick-access item did the player right-click" routing shared during the
// pre-spawn-countdown menu phase: the fixed crafting-table/furnace shortcuts, plus a hook for
// whatever "abilities" GUI a concrete game opens off the smithing-template item.
public class AbilitiesMenuListener implements Listener {
    private final Inventory fInventory = Bukkit.createInventory(null, InventoryType.FURNACE);

    protected final IngameState<?> state;

    public AbilitiesMenuListener(IngameState<?> state) {
        this.state = state;
    }

    // Hook: open whatever "abilities" GUI this game has for the given player.
    protected void openAbilitiesGui(Player p) {
    }

    @EventHandler
    public void onAbilitiesOpen(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (state.getManager().getPlayers().contains(p)) {
            if (p.getInventory().getItemInMainHand().getType() == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE) {
                openAbilitiesGui(p);
                e.setCancelled(true);
            } else if (p.getInventory().getItemInMainHand().getType() == Material.CRAFTING_TABLE) {
                p.openWorkbench(null, true);
                e.setCancelled(true);
            } else if (p.getInventory().getItemInMainHand().getType() == Material.FURNACE) {
                p.openInventory(fInventory);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onOffhandSwitch(PlayerSwapHandItemsEvent e) {
        if (state.getManager().getPlayers().contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHungerLose(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player p && state.getManager().getPlayers().contains(p)) {
            e.setCancelled(true);
        }
    }


}
