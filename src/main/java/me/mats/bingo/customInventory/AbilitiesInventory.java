package me.mats.bingo.customInventory;

import me.mats.bingo.game.BingoTeam;
import me.mats.bingo.game.ingame.IngameState;
import me.mats.common.customInventory.CustomInventory;
import me.mats.common.customInventory.CustomInventoryManager;
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

public class AbilitiesInventory extends CustomInventory<Inventory> {
    private final IngameState state;

    private int abilityPoints;
    private final BingoTeam bingoTeam;

    public AbilitiesInventory(IngameState state, BingoTeam bingoTeam) {
        this.state = state;
        this.bingoTeam = bingoTeam;
        this.abilityPoints = bingoTeam.getPlayers().size()+state.getExtraAbilityPoints();

        inventory =  Bukkit.createInventory(null, 27, Component.text("Abilities: ", NamedTextColor.DARK_GRAY).append(Component.text(abilityPoints, NamedTextColor.GREEN)));
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

        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        meta = helmet.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        meta.displayName(Component.text("Thief", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("Snatch the Win with the help of other teams", NamedTextColor.GRAY));
        lore.add(roman(" - If only one Item is missing for a Bingo and another Team has this you win", NamedTextColor.GREEN));
        lore.add(roman(" - Invisibility", NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        helmet.setItemMeta(meta);
        inventory.setItem(8, helmet);

        ItemStack gap = new ItemStack(Material.GOLDEN_APPLE);
        meta = gap.getItemMeta();

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.displayName(Component.text("Gapper", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        lore = new ArrayList<>();
        lore.add(roman("Level: 0", NamedTextColor.YELLOW));
        lore.add(Component.text(""));
        lore.add(roman("Use the full Powers of the Enchanted Golden Apple", NamedTextColor.GRAY));
        lore.add(roman(" - If you eat an Enchanted Golden Apple you're biggest Bingo Line gets another item", NamedTextColor.GREEN));
        lore.add(roman(" - Regeneration II", NamedTextColor.GREEN));
        lore.add(Component.text(""));
        lore.add(roman("LEFT CLICK", NamedTextColor.YELLOW).append(roman(" to gain Ability", NamedTextColor.GRAY)));
        lore.add(roman("RIGHT CLICK", NamedTextColor.YELLOW).append(roman(" to lose Ability", NamedTextColor.GRAY)));
        meta.lore(lore);

        gap.setItemMeta(meta);
        inventory.setItem(9, gap);
    }

    private void updateInventoryTitle() {
        Inventory newGui =  Bukkit.createInventory(null, 27, Component.text("Abilities: ", NamedTextColor.DARK_GRAY).append(Component.text(abilityPoints, NamedTextColor.GREEN)));
        newGui.setContents(inventory.getContents());
        inventory = newGui;
        for (Player p : bingoTeam.getPlayers()) {
            if (CustomInventoryManager.getInventory(p.getOpenInventory()) == this) {
                CustomInventoryManager.openInventory(p, this);
            }
        }

    }


    @Override
    public void onClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (state.getManager().getPlayers().contains(p) && e.getCurrentItem() != null) {
            ItemStack item = e.getCurrentItem();
            List<Player> list = state.getAbilities().getCorrespondingList(item.getType());
            // Found a correct list
            if (list != null) {
                if (item.getItemMeta().hasEnchants() && e.getClick().isLeftClick() && abilityPoints > 0) {
                    List<Player> listOther = state.getAbilities().getOtherList(item.getType());
                    if (listOther != null && !listOther.contains(bingoTeam.getPlayers().get(0))) {
                        list.removeAll(bingoTeam.getPlayers());
                        listOther.addAll(bingoTeam.getPlayers());

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
                    list.addAll(bingoTeam.getPlayers()); // Add players
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
                    List<Player> listOther = state.getAbilities().getOtherList(item.getType());
                    // Check if one player is in the lvl 2 list
                    if (listOther != null && listOther.contains(bingoTeam.getPlayers().get(0))) {
                        listOther.removeAll(bingoTeam.getPlayers());
                        list.addAll(bingoTeam.getPlayers());

                        ItemMeta meta = item.getItemMeta();
                        List<Component> lore = meta.lore();
                        lore.set(0, roman("Level: 1", NamedTextColor.YELLOW));
                        meta.lore(lore);
                        item.setItemMeta(meta);

                        abilityPoints++;
                        updateInventoryTitle();

                    } else {
                        // Case: Player is removing lvl 1 ability
                        list.removeAll(bingoTeam.getPlayers());
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
