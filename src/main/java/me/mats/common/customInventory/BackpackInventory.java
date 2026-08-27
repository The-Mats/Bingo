package me.mats.common.customInventory;

import me.mats.common.game.GameManager;
import me.mats.common.game.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class BackpackInventory extends CustomInventory<Inventory> {

    private final GameManager<?> manager;
    private final Team team;

    public BackpackInventory(GameManager<?> manager, Team team) {
        this.team = team;
        this.manager = manager;
        inventory =  Bukkit.createInventory(null, 27, Component.text("Backpack", NamedTextColor.DARK_GRAY).decoration(TextDecoration.UNDERLINED, true));
    }


    @Override
    public void onClick(InventoryClickEvent e) {
        // Here we want to always update the Backpack item and forbid any interaction with it
        if ((e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BUNDLE) || (e.getClick() == ClickType.NUMBER_KEY && e.getView().getBottomInventory().getItem(e.getHotbarButton()) != null && e.getView().getBottomInventory().getItem(e.getHotbarButton()).getType() == Material.BUNDLE)) {
            e.setCancelled(true);
        } else {
            // The event still goes through the other handlers so removing and adding to backpack is still possible
            Bukkit.getScheduler().runTask(manager.getPlugin(), team::updateBackPack);
        }

    }
}
