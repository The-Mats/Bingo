package me.mats.common.customInventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

public abstract class CustomInventory<T extends Inventory> {

    protected T inventory;

    protected InventoryView open(HumanEntity target) {
        return target.openInventory(inventory);
    }

    public abstract void onClick(InventoryClickEvent e);

    public T getInventory() {
        return inventory;
    }


}
