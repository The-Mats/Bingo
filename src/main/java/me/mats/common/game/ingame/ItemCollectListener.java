package me.mats.common.game.ingame;

import me.mats.common.game.GameManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;

// Generic "did the player just get their hands on one of the game's target items" detection,
// shared by every game that draws from an ItemLists pool: watches pickups and in-inventory
// moves, and for materials that need more than a Material match (enchanted books, potions)
// compares the exact rolled meta against the target's. What's currently a target, and what
// "collected" means, are per-game hooks.
public abstract class ItemCollectListener implements Listener {

    protected final GameManager<?> manager;

    protected ItemCollectListener(GameManager<?> manager) {
        this.manager = manager;
    }

    // Hook: is `material` currently something this game wants collected, and if so the
    // GameItem describing it (so its rolled meta can be compared)? Null if it isn't currently
    // a target.
    protected abstract GameItem getTargetItem(Material material);

    // Hook: `item` (Material + matching meta, where relevant) was just collected by `p`.
    protected abstract void onItemCollected(Player p, Material material, GameItem item);

    public void checkItem(ItemStack item, Player p) {
        Material mat = item.getType();
        GameItem target = getTargetItem(mat);
        if (target == null) {
            return;
        }

        // Check for special Items
        if (mat == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta itemMeta = (EnchantmentStorageMeta) item.getItemMeta();
            EnchantmentStorageMeta targetMeta = (EnchantmentStorageMeta) target.getMeta();

            for (Enchantment ench : targetMeta.getStoredEnchants().keySet()) {
                if (itemMeta.getStoredEnchants().containsKey(ench) && itemMeta.getStoredEnchants().get(ench) == targetMeta.getStoredEnchantLevel(ench)) {
                    onItemCollected(p, mat, target);
                }
            }
        } else if (mat == Material.POTION || mat == Material.SPLASH_POTION || mat == Material.LINGERING_POTION || mat == Material.TIPPED_ARROW) {
            PotionMeta itemMeta = (PotionMeta) item.getItemMeta();
            PotionMeta targetMeta = (PotionMeta) target.getMeta();

            PotionData itemData = itemMeta.getBasePotionData();
            PotionData targetData = targetMeta.getBasePotionData();

            if (itemData.getType() == targetData.getType() && itemData.isExtended() == targetData.isExtended() && itemData.isUpgraded() == targetData.isUpgraded()) {
                onItemCollected(p, mat, target);
            }
        } else {
            onItemCollected(p, mat, target);
        }
    }



    @EventHandler
    public void onInventoryPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player p && manager.getPlayers().contains(p)) {
            ItemStack item = e.getItem().getItemStack();
            if (item.getItemMeta() == null || (item.getItemMeta() != null && !item.getItemMeta().isUnbreakable())) {
                checkItem(item, p);
            }
        }
    }

    @EventHandler
    public void onInventoryMove(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        InventoryAction action = e.getAction();
        if (manager.getPlayers().contains(p)) {
            if (e.getCurrentItem() != null && e.getClickedInventory() == p.getOpenInventory().getTopInventory() && (action == InventoryAction.MOVE_TO_OTHER_INVENTORY || action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.PICKUP_ALL || action == InventoryAction.PICKUP_HALF || action == InventoryAction.SWAP_WITH_CURSOR || action == InventoryAction.PICKUP_ONE || action == InventoryAction.PICKUP_SOME)) {
                ItemStack item = e.getCurrentItem();
                if (item.getItemMeta() == null || (item.getItemMeta() != null && !item.getItemMeta().isUnbreakable())) {
                    checkItem(item, p);
                }
            }
        }
    }
}
