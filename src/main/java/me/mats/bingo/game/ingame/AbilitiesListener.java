package me.mats.bingo.game.ingame;


import me.mats.common.customInventory.CustomInventoryManager;
import me.mats.bingo.customInventory.TeleportInventory;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.*;

public class AbilitiesListener implements Listener {

    private final IngameState state;
    private final static List<Material> spawnEggs = Arrays.stream(Material.values()).filter(m -> m.toString().contains("_SPAWN_EGG")).toList();
    private final static List<Material> hostileSpawnEggs = Arrays.stream(EntityType.values()).filter(AbilitiesListener::isHostile).map(AbilitiesListener::toMaterial).filter(Objects::nonNull).toList();

    private final Map<Inventory, Triple<ItemStack[], LootTable, LootContext>> inventories = new HashMap<>();

    public static Material toMaterial(EntityType et) {
        try {
            return Material.valueOf(et.name()+"_SPAWN_EGG");
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean isHostile(EntityType et) {
        if (et.getEntityClass() != null) {
            return Enemy.class.isAssignableFrom(et.getEntityClass());
        }
        return false;
    }

    public AbilitiesListener(IngameState state) {
        this.state = state;
    }

    @EventHandler
    public void onMilkDrink(EntityPotionEffectEvent e) {
        if (e.getEntity() instanceof Player p && state.getManager().getPlayers().contains(p)) {
            if (e.getCause() == EntityPotionEffectEvent.Cause.MILK) {
                Bukkit.getScheduler().runTask(state.getManager().getPlugin(), () -> {state.getAbilities().setAbilities(p);});
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (state.getAbilities().getKeepInventoryAbilityList().contains(e.getPlayer())) {
            e.setKeepInventory(true);
            e.setKeepLevel(true);
        }

    }

    @EventHandler
    public void onCraftTool(PrepareItemCraftEvent e) {
        CraftingInventory ci = e.getInventory();
        Player p = (Player) e.getViewers().get(0);
        if (ci.getResult() != null && state.getManager().getPlayers().contains(p)) {
            ItemStack item = ci.getResult();
            ItemMeta meta = item.getItemMeta();
            if (item.getType().toString().contains("SWORD") && state.getAbilities().getLooterAbilityList().contains(p)) {
                meta.addEnchant(Enchantment.LOOT_BONUS_MOBS, 1, false);
                item.setItemMeta(meta);
                ci.setResult(item);
            } else if (item.getType().toString().contains("SWORD") && state.getAbilities().getLooterAbilityList2().contains(p)) {
                meta.addEnchant(Enchantment.LOOT_BONUS_MOBS, 3, false);
                item.setItemMeta(meta);
                ci.setResult(item);
            } else if (item.getType().toString().contains("PICKAXE") && state.getAbilities().getMinerAbilityList().contains(p)) {
                meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 1, false);
                meta.addEnchant(Enchantment.DIG_SPEED, 2, false);
                item.setItemMeta(meta);
                ci.setResult(item);
            } else if ((item.getType().toString().contains("AXE") || item.getType().toString().contains("SHOVEL")) && state.getAbilities().getMinerAbilityList().contains(p)) {
                meta.addEnchant(Enchantment.DIG_SPEED, 2, false);
                item.setItemMeta(meta);
                ci.setResult(item);
            } else if (item.getType().toString().contains("PICKAXE") && state.getAbilities().getMinerAbilityList2().contains(p)) {
                meta.addEnchant(Enchantment.LOOT_BONUS_BLOCKS, 3, false);
                meta.addEnchant(Enchantment.DIG_SPEED, 4, false);
                item.setItemMeta(meta);
                ci.setResult(item);
            } else if ((item.getType().toString().contains("AXE") || item.getType().toString().contains("SHOVEL")) && state.getAbilities().getMinerAbilityList2().contains(p)) {
                meta.addEnchant(Enchantment.DIG_SPEED, 4, false);
                item.setItemMeta(meta);
                ci.setResult(item);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof Chicken || e.getEntity() instanceof Rabbit) {
            if (e.getEntity().getKiller() != null && state.getAbilities().getEasterBunnyAbilityList().contains(e.getEntity().getKiller())) {
                Player p = e.getEntity().getKiller();

                // 1. Decide what List to use:
                List<Material> eggs = spawnEggs;
                if (!p.getWorld().isDayTime()) {
                    eggs = hostileSpawnEggs;
                }

                // 2. Apply Looting enchants if present
                ItemStack killItem = p.getInventory().getItemInMainHand();
                int lootingLevel = killItem.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
                int randomRange = switch (lootingLevel) {
                    case 1,2,3 -> 1;
                    default -> 2;
                };

                // Get a random mob spawn egg 33% of the time
                Random random = new Random();
                if (random.nextInt(randomRange) == 0) {
                    ItemStack spawnEgg = new ItemStack(eggs.get(random.nextInt(eggs.size())), random.nextInt(lootingLevel+1)+1);
                    e.getDrops().add(spawnEgg); // 3. Add to drops
                }
            }
        }
    }

    @EventHandler
    public void onDiamondBreak(BlockDropItemEvent e) {
        if ((e.getBlockState().getType() == Material.DIAMOND_ORE || e.getBlockState().getType() == Material.DEEPSLATE_DIAMOND_ORE) && state.getAbilities().getLuckyDiamondsAbilityList().contains(e.getPlayer())) {
            // Check if Diamonds drop or the Ore block
            if (e.getItems().get(0).getItemStack().getType() == Material.DIAMOND) {
                ItemStack diamonds = e.getItems().get(0).getItemStack();
                dropItems(diamonds.getAmount(), e.getBlock());
                e.getItems().removeIf(i -> i.getItemStack().getType()==Material.DIAMOND);
            }
        }
    }

    @EventHandler
    public void onOpenLootChest(LootGenerateEvent e) {
        if (e.getEntity() instanceof Player p && state.getAbilities().getTimeWizardAbilityList().contains(p) && e.getInventoryHolder() != null) {
            Inventory inv = e.getInventoryHolder().getInventory();
            if (inventories.get(inv) == null) {
                //Bukkit.getLogger().info("Loot generated");
                inventories.put(e.getInventoryHolder().getInventory(), new MutableTriple<>(e.getInventoryHolder().getInventory().getStorageContents(), e.getLootTable(), e.getLootContext()));
            } else {
                //Bukkit.getLogger().info("Loot regenerated");
                inventories.remove(inv);
            }
        }
    }


    @EventHandler
    public void onOpenChest(InventoryOpenEvent e) {

        if (inventories.get(e.getInventory()) != null && state.getAbilities().getTimeWizardAbilityList().contains((Player) e.getPlayer())) {

            Inventory inv = e.getInventory();
            ItemStack[] oldStorage = inventories.get(inv).getLeft();
            LootTable lootTable = inventories.get(inv).getMiddle();
            LootContext lootContext = inventories.get(inv).getRight();

            if (isEmpty(oldStorage)) {
                // First Open so we save the actual initial Contents
                //Bukkit.getLogger().info("First open");
                inventories.put(inv, new MutableTriple<>(inv.getStorageContents(), lootTable, lootContext));

            } else if (Arrays.equals(oldStorage, inv.getStorageContents())) {
                // This is the second Open
                inv.clear();
                lootTable.fillInventory(inv, new Random(), lootContext);
                //Bukkit.getLogger().info("Same one");
            }
        }
    }

    @EventHandler
    public void onLootChestClick(InventoryClickEvent e) {
        if (inventories.get(e.getClickedInventory()) != null) {
            inventories.remove(e.getClickedInventory());
            //Bukkit.getLogger().info("Removed due to click");
        }
    }

    @EventHandler
    public void onTeleporterOpen(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (state.getAbilities().getTeleporterAbilityList().contains(p)) {
            if ((p.getInventory().getItemInMainHand().getType() == Material.COMPASS && p.getInventory().getItemInMainHand().getItemMeta().isUnbreakable()) || (p.getInventory().getItemInOffHand().getType() == Material.COMPASS && p.getInventory().getItemInOffHand().getItemMeta().isUnbreakable())) {
                CustomInventoryManager.openInventory(p, new TeleportInventory(state, p));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTeleporterDrop(PlayerDropItemEvent e) {
        if (e.getItemDrop().getItemStack().getType() == Material.COMPASS && e.getItemDrop().getItemStack().getItemMeta().isUnbreakable() && state.getManager().getPlayers().contains(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onGappleConsume(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        if (e.getItem().getType() == Material.ENCHANTED_GOLDEN_APPLE && state.getAbilities().getGapperAbilityList().contains(p)) {
            state.getManager().getTeam(p).extendLongestBingoLine();
        }
    }


    private boolean isEmpty(ItemStack[] contents) {
        return Arrays.stream(contents).allMatch(Objects::isNull);
    }

    public void dropItems(int amount, Block block) {
        int i = 0;
        Random r = new Random();
        List<Material> list = BingoLists.getList(state.getSetting());
        while (i < amount) {
            Material material = list.get(r.nextInt(list.size()));
            ItemStack newItem = new ItemStack(material);
            if (material == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) newItem.getItemMeta();
                BingoLists.getRandomEnchant(meta);
                newItem.setItemMeta(meta);

            } else if (material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION || material == Material.TIPPED_ARROW) {
                PotionMeta meta = (PotionMeta) newItem.getItemMeta();
                BingoLists.getRandomPotion(state.getSetting(), meta);
                newItem.setItemMeta(meta);

            }
            block.getWorld().dropItemNaturally(block.getLocation(), newItem);
            i++;
        }

        // Otherwise do nothing
    }



}
