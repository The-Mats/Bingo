package me.mats.common.customInventory;

import me.mats.common.game.Team;
import me.mats.common.game.ingame.abilities.Abilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static me.mats.common.message.MessageBuilder.roman;

import java.util.ArrayList;
import java.util.List;

// The generic abilities shop shared by every game: the 10 abilities on the common Abilities
// roster (Movement, Looter, Miner, Keep Inventory, Lucky Diamonds, Easter Bunny, Time Wizard,
// Teleporter), spending ability points earned per player (plus whatever bonus a concrete game
// grants). A concrete game with its own exclusive abilities (see bingo's Thief/Gapper) adds
// their items directly to `inventory` in its own constructor, after calling super().
public abstract class AbilitiesInventory extends CustomInventory<Inventory> {
    protected final Team team;
    protected final Abilities abilities;

    protected int abilityPoints;

    protected AbilitiesInventory(Team team, Abilities abilities, int extraAbilityPoints) {
        this.team = team;
        this.abilities = abilities;
        this.abilityPoints = team.getPlayers().size()+extraAbilityPoints;

        inventory =  Bukkit.createInventory(null, 18, title());
        ItemStack feather = new ItemStack(Material.FEATHER);
        ItemMeta meta = feather.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.displayName(Component.text("Movement", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("Gives you fast movement on all Elements:", NamedTextColor.GRAY));
        lore.add(roman(" - Haste II", NamedTextColor.GREEN));
        lore.add(roman(" - Speed II", NamedTextColor.GREEN));
        lore.add(roman(" - Dolphin's Grace", NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        feather.setItemMeta(meta);
        inventory.setItem(0, feather);

        ItemStack sword = new ItemStack(Material.GOLDEN_SWORD);
        meta = sword.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        meta.displayName(Component.text("Looter", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("Makes getting rare loot a piece of cake", NamedTextColor.GRAY));
        lore.add(roman(" - Looting I | Looting III on all Swords", NamedTextColor.GREEN));
        lore.add(roman(" - Strength I | Strength II", NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        sword.setItemMeta(meta);
        inventory.setItem(1, sword);

        ItemStack pick = new ItemStack(Material.GOLDEN_PICKAXE);
        meta = pick.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        meta.displayName(Component.text("Miner", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("Makes getting resources a piece of cake", NamedTextColor.GRAY));
        lore.add(roman(" - Efficiency II | Efficiency IV  on all Tools", NamedTextColor.GREEN));
        lore.add(roman(" - Fortune I | Fortune III on all Pickaxes", NamedTextColor.GREEN));
        lore.add(roman(" - Night Vision", NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        pick.setItemMeta(meta);
        inventory.setItem(2, pick);

        ItemStack chest = new ItemStack(Material.CHEST_MINECART);
        meta = chest.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.displayName(Component.text("Keep Inventory", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("Never lose a thing (except the Game maybe)", NamedTextColor.GRAY));
        lore.add(roman(" - Keep Inventory", NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        chest.setItemMeta(meta);
        inventory.setItem(3, chest);

        ItemStack diamond = new ItemStack(Material.DIAMOND);
        meta = diamond.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.displayName(Component.text("Lucky Diamonds", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("You need a bit of luck for this one", NamedTextColor.GRAY));
        lore.add(roman(" - Mining diamonds drops a random Item for each diamond", NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        diamond.setItemMeta(meta);
        inventory.setItem(4, diamond);

        ItemStack egg = new ItemStack(Material.TURTLE_EGG);
        meta = egg.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.displayName(Component.text("Easter Bunny", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("Make sure to find them all", NamedTextColor.GRAY));
        lore.add(roman(" - Killing Chickens or Bunnies drops random Mob Spawn Eggs", NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        egg.setItemMeta(meta);
        inventory.setItem(5, egg);

        ItemStack clock = new ItemStack(Material.CLOCK);
        meta = clock.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.displayName(Component.text("Time Wizard", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("Travel back in Time and change your fortune", NamedTextColor.GRAY));
        lore.add(roman(" - If you open a Loot Chest and you don't take any items, opening it again regens it", NamedTextColor.GREEN));
        lore.add(roman(" - Luck V", NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        clock.setItemMeta(meta);
        inventory.setItem(6, clock);

        ItemStack compass = new ItemStack(Material.COMPASS);
        meta = compass.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.displayName(Component.text("Teleporter", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("Always be united with your friends", NamedTextColor.GRAY));
        lore.add(roman(" - TP to Players from the Team", NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        compass.setItemMeta(meta);
        inventory.setItem(7, compass);


        ItemStack pearl = new ItemStack(Material.ENDER_PEARL);
        meta = pearl.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.displayName(Component.text("Enderman", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("Learned to port by looking at those beautiful eyes", NamedTextColor.GRAY));
        lore.add(roman(" - Throwing an Ender Pearl has a 50% Chance to port you 1000 Blocks in the thrown direction",
                NamedTextColor.GREEN));
        lore.add(roman(" - No damage when using Ender Pearls",
                NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        pearl.setItemMeta(meta);
        inventory.setItem(8, pearl);


        ItemStack spy = new ItemStack(Material.SPYGLASS);
        meta = spy.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.displayName(Component.text("Stalker", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("No one here, nothing to see", NamedTextColor.GRAY));
        lore.add(roman(" - Get a Spyglass",
                NamedTextColor.GREEN));
        lore.add(roman(" - Get 5 Wandering Trader Eggs",
                NamedTextColor.GREEN));
        lore.add(roman(" - Invisibility",
                NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        spy.setItemMeta(meta);
        inventory.setItem(9, spy);

        ItemStack bow = new ItemStack(Material.BOW);
        meta = bow.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.displayName(Component.text("Sniper", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("I hope one arrow is enough", NamedTextColor.GRAY));
        lore.add(roman(" - Get a Bow with Infinity",
                NamedTextColor.GREEN));
        lore.add(roman(" - Get one Arrow",
                NamedTextColor.GREEN));
        lore.add(roman(" - Resistance I",
                NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        bow.setItemMeta(meta);
        inventory.setItem(10, bow);


        ItemStack fire = new ItemStack(Material.FIRE_CHARGE);
        meta = fire.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.displayName(Component.text("Pyrokinetic", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("Let there be flames", NamedTextColor.GRAY));
        lore.add(roman(" - Auto Smelt and Unbreaking on all Pickaxes (except wood)",
                NamedTextColor.GREEN));
        lore.add(roman(" - Fire Aspect II on all Swords",
                NamedTextColor.GREEN));
        lore.add(roman(" - Fire Resistance (of course)",
                NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        fire.setItemMeta(meta);
        inventory.setItem(11, fire);


        ItemStack fireworks = new ItemStack(Material.FIREWORK_ROCKET);
        meta = fireworks.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.displayName(Component.text("Rocketman", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("And I think it's gonna be a long, long time", NamedTextColor.GRAY));
        lore.add(roman(" - Get 5 Fireworks at the start", NamedTextColor.GREEN));
        lore.add(roman(" - Get 1 Firework on every respawn", NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        fireworks.setItemMeta(meta);
        inventory.setItem(12, fireworks);


        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        meta = book.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.displayName(Component.text("Bookworm", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("Lorom Ipsum and so on", NamedTextColor.GRAY));
        lore.add(roman(" - Bookshelves drop enchanted books with up to 4 enchants", NamedTextColor.GREEN));
        lore.add(roman(" - Personal anvil in your inventory", NamedTextColor.GREEN));
        lore.add(roman(" - No anvil costs", NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        book.setItemMeta(meta);
        inventory.setItem(13, book);
    }

    protected Component title() {
        return Component.text("Abilities: ", NamedTextColor.DARK_GRAY).append(Component.text(abilityPoints, NamedTextColor.GREEN));
    }

    protected void updateInventoryTitle() {
        Inventory newGui =  Bukkit.createInventory(null, 18, title());
        newGui.setContents(inventory.getContents());
        inventory = newGui;
        for (Player p : team.getPlayers()) {
            if (CustomInventoryManager.getInventory(p.getOpenInventory()) == this) {
                CustomInventoryManager.openInventory(p, this);
            }
        }

    }


    @Override
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (team.getManager().getPlayers().contains(p) && e.getCurrentItem() != null) {
            ItemStack item = e.getCurrentItem();
            List<Player> list = abilities.getCorrespondingList(item.getType());
            // Found a correct list
            if (list != null) {
                if (item.getItemMeta().hasEnchants() && e.getClick().isLeftClick() && abilityPoints > 0) {
                    List<Player> listOther = abilities.getOtherList(item.getType());
                    if (listOther != null && !listOther.contains(team.getPlayers().get(0))) {
                        list.removeAll(team.getPlayers());
                        listOther.addAll(team.getPlayers());

                        ItemMeta meta = item.getItemMeta();
                        List<Component> lore = meta.lore();
                        lore.set(0, roman("Level: 2", NamedTextColor.YELLOW));
                        meta.lore(lore);
                        item.setItemMeta(meta);

                        abilityPoints--;
                        updateInventoryTitle();

                    }
                } else if (!item.getItemMeta().hasEnchants() && e.getClick().isLeftClick() && abilityPoints > 0) {
                    // Case: Player is selecting a new ability
                    list.addAll(team.getPlayers()); // Add players
                    ItemMeta meta = item.getItemMeta();
                    meta.addEnchant(Enchantment.LUCK, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                    List<Component> lore = meta.lore();
                    lore.set(0, roman("Level: 1", NamedTextColor.YELLOW));
                    meta.lore(lore);
                    item.setItemMeta(meta);
                    abilityPoints--;
                    updateInventoryTitle();


                } else if (item.getItemMeta().hasEnchants() && e.getClick().isRightClick()) {
                    // We need to first check if team is going from lvl 2 to 1
                    List<Player> listOther = abilities.getOtherList(item.getType());
                    // Check if one player is in the lvl 2 list
                    if (listOther != null && listOther.contains(team.getPlayers().get(0))) {
                        listOther.removeAll(team.getPlayers());
                        list.addAll(team.getPlayers());

                        ItemMeta meta = item.getItemMeta();
                        List<Component> lore = meta.lore();
                        lore.set(0, roman("Level: 1", NamedTextColor.YELLOW));
                        meta.lore(lore);
                        item.setItemMeta(meta);

                        abilityPoints++;
                        updateInventoryTitle();

                    } else {
                        // Case: Player is removing lvl 1 ability
                        list.removeAll(team.getPlayers());
                        ItemMeta meta = item.getItemMeta();
                        meta.removeEnchant(Enchantment.LUCK);
                        List<Component> lore = meta.lore();
                        lore.set(0, roman("Level: 0", NamedTextColor.YELLOW));
                        meta.lore(lore);
                        item.setItemMeta(meta);
                        abilityPoints++;
                        updateInventoryTitle();

                    }

                }
            }


        }
    }
}
