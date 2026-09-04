package me.mats.common.game.ingame.abilities;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import me.mats.common.game.ingame.IngameState;
import me.mats.common.game.ingame.ItemLists;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.*;

// Generic ability-effect wiring shared by every game: Movement/Looter/Miner enchant &
// potion boosts, Keep Inventory, Lucky Diamonds, Easter Bunny, Time Wizard, and Teleporter.
// A concrete game adds its own exclusive abilities' handlers as extra @EventHandler methods
// on a subclass (see BingoAbilitiesListener's Gapper).
public class AbilitiesListener implements Listener {

    protected final IngameState<?> state;
    private final static List<Material> spawnEggs = Arrays.stream(Material.values()).filter(m -> m.toString().contains("_SPAWN_EGG")).toList();
    private final static List<Material> hostileSpawnEggs = Arrays.stream(EntityType.values()).filter(AbilitiesListener::isHostile).map(AbilitiesListener::toMaterial).filter(Objects::nonNull).toList();

    private final Map<Inventory, Triple<ItemStack[], LootTable, LootContext>> inventories = new HashMap<>();

    private final Set<UUID> pendingPearlDamage = new HashSet<>(); // Track if an ender pearl was used to stop the damage

    private final static Enchantment[] enchantments = Arrays.stream(Enchantment.values()).filter(ench -> !ench.isCursed()).toArray(Enchantment[]::new);
    private final Random random = new Random();

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

    public AbilitiesListener(IngameState<?> state) {
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
            // setKeepInventory alone doesn't stop these from also being spawned as ground drops -
            // without clearing this, an item can end up both kept in inventory AND dropped, then
            // picked back up, duplicating it.
            e.getDrops().clear();
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

            if (item.getType().toString().contains("PICKAXE") && item.getType() != Material.WOODEN_PICKAXE && state.getAbilities().getPyroAbilityList().contains(p)) {
                if (meta.getEnchantLevel(Enchantment.DURABILITY) < 1) {
                    meta.addEnchant(Enchantment.DURABILITY, 1, false);
                }
                List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                lore.add(AutoSmeltCache.AUTO_SMELT_LORE);
                meta.lore(lore);
                item.setItemMeta(meta);
                ci.setResult(item);
            }

            if (item.getType().toString().contains("SWORD") && state.getAbilities().getPyroAbilityList().contains(p)) {
                meta.addEnchant(Enchantment.FIRE_ASPECT, 2, false);
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
                inventories.put(e.getInventoryHolder().getInventory(), new MutableTriple<>(e.getInventoryHolder().getInventory().getStorageContents(), e.getLootTable(), e.getLootContext()));
            } else {
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
                inventories.put(inv, new MutableTriple<>(inv.getStorageContents(), lootTable, lootContext));

            } else if (Arrays.equals(oldStorage, inv.getStorageContents())) {
                // This is the second Open
                inv.clear();
                lootTable.fillInventory(inv, new Random(), lootContext);
            }
        }
    }

    @EventHandler
    public void onLootChestClick(InventoryClickEvent e) {
        if (inventories.get(e.getClickedInventory()) != null) {
            inventories.remove(e.getClickedInventory());
        }
    }

    @EventHandler
    public void onTeleporterOpen(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (state.getAbilities().getTeleporterAbilityList().contains(p)) {
            if ((p.getInventory().getItemInMainHand().getType() == Material.COMPASS && p.getInventory().getItemInMainHand().getItemMeta().isUnbreakable()) || (p.getInventory().getItemInOffHand().getType() == Material.COMPASS && p.getInventory().getItemInOffHand().getItemMeta().isUnbreakable())) {
                openTeleporter(p);
                e.setCancelled(true);
                // setCancelled only blocks the block-interaction half of the right-click; without
                // denying the item-use half too, vanilla leaves a stale "in-use" reference on this
                // compass that can resurface as an extra copy in the held slot after a respawn.
                e.setUseItemInHand(Event.Result.DENY);
            }
        }
    }

    // Hook: open whatever "teleport to teammates" GUI this game uses.
    protected void openTeleporter(Player p) {
    }

    @EventHandler
    public void onAnvilOpen(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (state.getAbilities().getBookAbilityList().contains(p)) {
            if ((p.getInventory().getItemInMainHand().getType() == Material.ANVIL && p.getInventory().getItemInMainHand().getItemMeta().isUnbreakable()) || (p.getInventory().getItemInOffHand().getType() == Material.ANVIL && p.getInventory().getItemInOffHand().getItemMeta().isUnbreakable())) {
                // openAnvil(loc, true) opens a real vanilla anvil menu (fires PrepareAnvilEvent,
                // returns unconsumed items to the player on close) without needing an actual
                // anvil block at that location - unlike Bukkit.createInventory(ANVIL), which is
                // a known-broken CraftInventoryCustom that doesn't behave like a real anvil.
                p.openAnvil(p.getLocation(), true);
                e.setCancelled(true);
                // setCancelled only blocks the block-interaction half of the right-click; without
                // denying the item-use half too, vanilla leaves a stale "in-use" reference on this
                // compass that can resurface as an extra copy in the held slot after a respawn.
                e.setUseItemInHand(Event.Result.DENY);
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
    public void onPearlTeleport(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL && state.getAbilities().getEndermanAbilityList().contains(e.getPlayer())) {
            Player p = e.getPlayer();
            pendingPearlDamage.add(p.getUniqueId());
            World world = p.getWorld();
            int x_change = e.getTo().getBlockX() - e.getFrom().getBlockX();
            int z_change = e.getTo().getBlockZ() - e.getFrom().getBlockZ();

            if (Math.random() < 0.50) {
                Location teleport_to;
                if (Math.abs(x_change) >= Math.abs(z_change)) {
                    teleport_to = e.getFrom().add(Math.signum(x_change) * 1000, 0, 0);

                } else {
                    teleport_to = e.getFrom().add(0, 0, Math.signum(z_change) * 1000);
                }
                teleport_to.setY(world.getHighestBlockAt(teleport_to).getY()+1);
                p.teleport(teleport_to);
                p.playSound(p, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.MASTER, 1, 1);

                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockMine(BlockDropItemEvent e) {
        ItemStack tool = e.getPlayer().getInventory().getItemInMainHand();
        if (tool.getType().toString().contains("PICKAXE") && AutoSmeltCache.hasAutoSmelt(tool)) {
            for (Item itemEntity : e.getItems()) {
                ItemStack itemStack = itemEntity.getItemStack();
                ItemStack cooked = AutoSmeltCache.get(itemStack.getType());
                if (cooked != null) {
                    ItemStack result = cooked.clone();
                    result.setAmount(itemStack.getAmount());
                    itemEntity.setItemStack(result);
                }
            }
        }
        if (e.getBlockState().getType() == Material.BOOKSHELF && state.getAbilities().getBookAbilityList().contains(e.getPlayer())) {
            Item itemEntity = e.getItems().get(0); // Count should always be 3
            int amount = itemEntity.getItemStack().getAmount();
            for (int i = 0; i < amount; i++) {
                ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();

                int enchantCount = random.nextInt(4) + 1;
                for (int j = 0; j < enchantCount; j++) {
                    Enchantment enchant = enchantments[random.nextInt(enchantments.length)];
                    meta.addStoredEnchant(enchant, random.nextInt(enchant.getMaxLevel()) + 1, false);
                }
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                book.setItemMeta(meta);
                if (i == 0) {
                    itemEntity.setItemStack(book);
                } else {
                    itemEntity.getWorld().dropItemNaturally(itemEntity.getLocation(), book);
                }
            }
        }
    }

    @EventHandler
    public void onGrindstoneUse(PrepareGrindstoneEvent e) {
        Player p = (Player) e.getViewers().get(0);
        if (state.getAbilities().getBookAbilityList().contains(p)) {
            // e.getInventory().
        }
    }

    // Only the Rocketman ability's own unbreakable fireworks may boost an elytra - a regular
    // firework picked up elsewhere (loot, bookshelf enchant table, etc.) must not.
    @EventHandler
    public void onElytraBoost(PlayerElytraBoostEvent e) {
        Player p = e.getPlayer();
        if (state.getManager().getPlayers().contains(p)) {
            ItemStack usedFirework = e.getItemStack();
            ItemMeta meta = usedFirework == null ? null : usedFirework.getItemMeta();
            if (meta == null || !meta.isUnbreakable()) {
                e.setCancelled(true);
                e.setShouldConsume(false);
            }
        }
    }

    @EventHandler
    public void onPearlFallDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (pendingPearlDamage.remove(p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onUseAnvil(PrepareAnvilEvent e) {
        Player p = (Player) e.getViewers().get(0);
        if (state.getAbilities().getBookAbilityList().contains(p)) {
            e.getInventory().setRepairCost(0);

            // setRepairCost(0) only zeroes the level cost shown/charged for this use - the
            // resulting item still carries its own RepairCost tag (the "prior work penalty"),
            // which keeps climbing and would eventually hit "too expensive!" even at 0 XP cost.
            ItemStack result = e.getResult();
            if (result != null) {
                ItemMeta meta = result.getItemMeta();
                boolean changed = false;
                if (meta instanceof Repairable repairable) {
                    repairable.setRepairCost(0);
                    changed = true;
                }
                // Vanilla's anvil merge doesn't reliably carry the Unbreakable tag through - if
                // either input was one of our books, force it back onto the result so a merge
                // with a normal (non-unbreakable) book can't strip the grindstone-block marker.
                if (result.getType() == Material.ENCHANTED_BOOK && (isAbilityBook(e.getInventory().getFirstItem()) || isAbilityBook(e.getInventory().getSecondItem()))) {
                    meta.setUnbreakable(true);
                    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    changed = true;
                }
                if (changed) {
                    result.setItemMeta(meta);
                    e.setResult(result);
                }
            }
        }
    }

    @EventHandler
    public void onUseGrindstone(PrepareGrindstoneEvent e) {
        if (isAbilityBook(e.getInventory().getUpperItem()) || isAbilityBook(e.getInventory().getLowerItem())) {
            // Grindstones disenchant books for XP - without this, an ability book could be
            // ground down to a plain book, turned back into bookshelves, and re-broken for
            // more ability books, farming free enchants/XP in a loop.
            e.setResult(null);
        }
    }

    private static boolean isAbilityBook(ItemStack item) {
        return item != null && item.getType() == Material.ENCHANTED_BOOK && item.getItemMeta() != null && item.getItemMeta().isUnbreakable();
    }

    private boolean isEmpty(ItemStack[] contents) {
        return Arrays.stream(contents).allMatch(Objects::isNull);
    }

    public void dropItems(int amount, Block block) {
        int i = 0;
        Random r = new Random();
        List<Material> list = ItemLists.getList(state.getSetting());
        while (i < amount) {
            Material material = list.get(r.nextInt(list.size()));
            ItemStack newItem = new ItemStack(material);
            if (material == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) newItem.getItemMeta();
                ItemLists.getRandomEnchant(meta);
                newItem.setItemMeta(meta);

            } else if (material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION || material == Material.TIPPED_ARROW) {
                PotionMeta meta = (PotionMeta) newItem.getItemMeta();
                ItemLists.getRandomPotion(state.getSetting(), meta);
                newItem.setItemMeta(meta);

            }
            block.getWorld().dropItemNaturally(block.getLocation(), newItem);
            i++;
        }

        // Otherwise do nothing
    }



}
