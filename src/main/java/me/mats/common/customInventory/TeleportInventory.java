package me.mats.common.customInventory;

import me.mats.common.enums.Color;
import me.mats.common.game.GameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextColor;
import me.mats.common.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

import static me.mats.common.enums.Color.*;
import static me.mats.common.message.MessageBuilder.roman;

public class TeleportInventory extends CustomInventory<Inventory> {

    private final GameManager<?> manager;

    public TeleportInventory(GameManager<?> manager, Player p) {
        this.manager = manager;

        inventory = Bukkit.createInventory(null, InventoryType.HOPPER, Component.text("Teleporter", NamedTextColor.DARK_GRAY).decoration(TextDecoration.UNDERLINED, true));

        for (Player p2 : manager.getTeam(p).getPlayers()) {
            if (p != p2) {
                ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(p2.getUniqueId()));
                meta.displayName(Component.text(p2.getName(), NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
                List<Component> lore = new ArrayList<>();
                lore.add(roman("CLICK", NamedTextColor.YELLOW).append(roman(" to teleport to "+p2.getName(), NamedTextColor.GRAY)));
                meta.lore(lore);
                playerHead.setItemMeta(meta);
                inventory.addItem(playerHead);
            }
        }

    }


    @Override
    public void onClick(InventoryClickEvent e) {
        if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.PLAYER_HEAD) {
            ItemStack clickedHead = e.getCurrentItem();
            SkullMeta meta = (SkullMeta) clickedHead.getItemMeta();
            if (meta.getOwningPlayer() != null && meta.getOwningPlayer().getPlayer() != null) {
                // Teleport to other player if specific conditions are met
                Player tpPlayer = (Player) e.getWhoClicked();
                if (tpPlayer.getNoDamageTicks() == 0 && tpPlayer.getFallDistance() == 0) {

                    Player toPlayer = meta.getOwningPlayer().getPlayer();
                    // Check if we need to adjust Dimension Label
                    if (tpPlayer.getWorld() != toPlayer.getWorld()) {
                        Component dim = Component.empty();
                        switch (toPlayer.getWorld().getEnvironment()) {
                            case NORMAL -> dim = Component.text("O", OVERWORLD.getTextColor());
                            case NETHER -> dim = Component.text("N", NETHER.getTextColor());
                            case THE_END -> dim = Component.text("E", END.getTextColor());
                        }
                        Team playerTeam = manager.getBoard().getPlayerTeam(tpPlayer);
                        tpPlayer.playerListName(playerTeam.prefix().append(Component.text(tpPlayer.getName(), playerTeam.color())).append(Component.text(" ")).append(Message.O_BRACKET.getComponent()).append(dim).append(Message.C_BRACKET.getComponent()));
                    }
                    tpPlayer.teleport(toPlayer);

                } else {
                    tpPlayer.closeInventory();
                    tpPlayer.sendMessage(manager.getBrandPrefix().append(Component.text("Stop falling or taking damage!", NamedTextColor.RED)));
                }
            }
        }
        e.setCancelled(true);
    }
}
