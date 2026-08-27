package me.mats.common.customInventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;

import java.util.HashMap;

public class CustomInventoryManager {
    private static final HashMap<InventoryView, CustomInventory<?>> viewToInventory = new HashMap<>();

    public static void openInventory(HumanEntity player, CustomInventory<?> inventory) {
        viewToInventory.put(inventory.open(player), inventory);
    }

    public static CustomInventory<?> getInventory(InventoryView view) {
        return viewToInventory.get(view);
    }

    public static CustomInventory<?> removeInventory(InventoryView view) {
        return viewToInventory.remove(view);
    }

}
