package me.mats.common.customInventory;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class CustomInventoryListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null)
            return;

        CustomInventory<?> inventory = CustomInventoryManager.getInventory(e.getView());

        if (inventory != null) {
            inventory.onClick(e);
        }

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent e) {
        CustomInventory<?> inventory = CustomInventoryManager.removeInventory(e.getView());
        // Could do stuff with Inventory here
    }

}
