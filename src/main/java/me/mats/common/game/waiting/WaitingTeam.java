package me.mats.common.game.waiting;

import me.mats.common.enums.Color;
import me.mats.common.game.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WaitingTeam {
    private final ItemStack item;
    private final Team team;

    public WaitingTeam(Team team) {
        this.team = team;

        ItemStack item = new ItemStack(Material.valueOf(team.getName().toUpperCase()+"_CONCRETE"));
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(Component.text("Team "+team.getName(), TextColor.color(team.getColorCode())).decoration(TextDecoration.BOLD, true));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(" "));
        lore.add(Component.text("Click to join ", TextColor.color(Color.STD_COLOR.getColorCode()))
                        .append(Component.text("Team "+team.getName(), TextColor.color(team.getColorCode()))));

        itemMeta.lore(lore);
        item.setItemMeta(itemMeta);
        this.item = item;
    }

    public void updateLobbyBlocks(Player p) {
        World w = p.getWorld();
        List<Location> locations = List.of(new Location(w,-17,107,12), new Location(w,-17,106,0), new Location(w,-17,107,0), new Location(w,-17,107,-12),
                new Location(w,-15,107,-14), new Location(w,-3,106,-14), new Location(w,-3,107,-14), new Location(w,9,107,-14),
                new Location(w,-15,107,14), new Location(w,-3,106,14), new Location(w,-3,107,14), new Location(w,9,107,14),
                new Location(w,11,107,12), new Location(w,11,106,0), new Location(w,11,107,0), new Location(w,11,107,-12));

        BlockData data =  Material.valueOf(team.getName().toUpperCase()+"_WOOL").createBlockData();
        for (Location loc : locations) {
            p.sendBlockChange(loc, data);
        }
    }

    public ItemStack getItem() {
        return item;
    }

    public void removePlayer(Player p) {
        ItemMeta meta = item.getItemMeta();
        List<Component> lore = meta.lore();
        assert lore != null;
        lore.removeIf(l -> (l.toString().contains(p.getName())));
        if(team.getScoreboardTeam().getEntries().isEmpty())
            lore.remove(Component.text(" "));

        meta.lore(lore);
        item.setItemMeta(meta);
    }

    public void addPlayer(Player p) {
        p.displayName(Component.text(p.getName(), TextColor.color(team.getColorCode())));
        updateLobbyBlocks(p);

        // Let the concrete game refresh whatever per-player waiting UI it manages
        team.onWaitingTeamJoined(p);

        ItemMeta meta = item.getItemMeta();
        List<Component> lore = meta.lore();
        assert lore != null;
        lore.add(1, Component.text("§8>> ", NamedTextColor.DARK_GRAY).append(Component.text(p.getName(), TextColor.color(team.getColorCode()))));
        if (team.getScoreboardTeam().getEntries().size() == 1) {
            lore.add(lore.size()-1, Component.text(" "));
        }
        meta.lore(lore);
        item.setItemMeta(meta);
    }

}
