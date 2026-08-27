package me.mats.bingo.game.ingame;

import me.mats.bingo.game.BingoTeam;
import me.mats.bingo.customInventory.AbilitiesInventory;
import me.mats.common.customInventory.CustomInventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class AbilitiesMenuListener implements Listener {
    private final Inventory fInventory = Bukkit.createInventory(null, InventoryType.FURNACE);

    private final IngameState state;
    private final Map<BingoTeam, AbilitiesInventory> bingoTeamToGUI;

    public AbilitiesMenuListener(IngameState state) {
        this.state = state;
        bingoTeamToGUI = new HashMap<>(state.getManager().getTeams().size());
        for (BingoTeam bT : state.getManager().getTeams()) {
            bingoTeamToGUI.put(bT, new AbilitiesInventory(state, bT));
        }
    }

    @EventHandler
    public void onAbilitiesOpen(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (state.getManager().getPlayers().contains(p)) {
            if (p.getInventory().getItemInMainHand().getType() == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE) {
                CustomInventoryManager.openInventory(p, bingoTeamToGUI.get(state.getManager().getTeam(p)));
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
