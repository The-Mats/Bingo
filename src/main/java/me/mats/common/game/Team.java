package me.mats.common.game;

import me.mats.common.customInventory.BackpackInventory;
import me.mats.common.game.waiting.WaitingTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// Generic per-team bookkeeping shared by every game mode: scoreboard team, roster,
// lobby item/team-selector wiring and the shared backpack. Game-specific win logic
// (e.g. bingo's field) belongs on a subclass.
public abstract class Team {

    protected final String name;
    protected final int colorCode;
    protected final org.bukkit.scoreboard.Team scoreboardTeam;
    protected final GameManager<?> manager;
    protected final List<Player> players = new ArrayList<>();
    protected WaitingTeam waitingTeam;

    // Backpack stuff
    private final BackpackInventory backpackInventory;
    private final ItemStack backpackItem;

    protected Team(String name, int colorCode, GameManager<?> manager) {
        this.name = name;
        this.colorCode = colorCode;
        this.manager = manager;
        this.scoreboardTeam = manager.getBoard().registerNewTeam(name);

        scoreboardTeam.color(NamedTextColor.GRAY);
        scoreboardTeam.prefix(Component.text("[").color(NamedTextColor.DARK_GRAY).append(Component.text(name, TextColor.color(colorCode)).append(Component.text("] ", NamedTextColor.DARK_GRAY))));

        this.backpackInventory = new BackpackInventory(manager, this);
        ItemStack backpackItem = new ItemStack(Material.BUNDLE);
        BundleMeta meta = (BundleMeta) backpackItem.getItemMeta();
        meta.displayName(Component.text("Backpack").color(TextColor.color(colorCode)).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        backpackItem.setItemMeta(meta);
        this.backpackItem = backpackItem;

        this.waitingTeam = new WaitingTeam(this);
    }

    // Called once this team's WaitingTeam registers a newly joined player, so a concrete
    // game can hang its own per-player waiting UI off it (e.g. bingo's advancement tab + chat message)
    public void onWaitingTeamJoined(Player p) {
    }

    public void removeWaitingTeam() {
        this.waitingTeam = null;
    }

    public void removePlayer(Player p) {
        scoreboardTeam.removePlayer(p);
        players.remove(p);

        if (waitingTeam != null) {
            waitingTeam.removePlayer(p);
        }
    }

    public void addPlayer(Player p) {
        Team existing = manager.getTeam(p);

        if (existing != null)
            existing.removePlayer(p);

        scoreboardTeam.addPlayer(p);
        players.add(p);

        waitingTeam.addPlayer(p);
    }

    public void sendChat(Component comp) {
        for (Player p : players) {
            p.sendMessage(comp);
        }
    }

    public void sendTitle(String name) {
        final Title title = Title.title(Component.text(" "), Component.text("§o§a+ §8" + name), Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(1200), Duration.ofMillis(500)));

        for (Player p : players) {
            p.showTitle(title);
            p.playSound(p, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.MASTER, 1F, 1F);
            p.spawnParticle(Particle.TOTEM, p.getLocation(), 100, 0, 0, 0, 0.5);
        }
    }

    // Backpack stuff
    public BundleMeta updateBackPack() {
        BundleMeta meta = (BundleMeta) backpackItem.getItemMeta();
        meta.setItems(Arrays.stream(backpackInventory.getInventory().getContents()).toList().stream().filter(Objects::nonNull).collect(Collectors.toList()));
        backpackItem.setItemMeta(meta);

        // Update for People who might have the Inventory open
        for (HumanEntity h : backpackInventory.getInventory().getViewers()) {
            Player player = (Player) h;
            player.getInventory().getItemInMainHand().setItemMeta(meta);
        }

        return meta;
    }

    public BundleMeta addItemToBackPack(ItemStack item) {
        if (backpackInventory.getInventory().firstEmpty() == -1) {
            return null;
        } else {
            backpackInventory.getInventory().addItem(item);
            return updateBackPack();
        }
    }

    public Pair<BundleMeta, ItemStack> removeItemFromBackPack() {
        if (backpackInventory.getInventory().isEmpty()) {
            return null;
        } else {
            for (int i = backpackInventory.getInventory().getSize() - 1; i >= 0; i--) {
                if (backpackInventory.getInventory().getItem(i) != null && backpackInventory.getInventory().getItem(i).getType() != Material.AIR) {
                    ItemStack removed = backpackInventory.getInventory().getItem(i);
                    backpackInventory.getInventory().clear(i);
                    return Pair.of(updateBackPack(), removed);
                }
            }
        }
        return null;
    }

    // Getters and Setters
    public org.bukkit.scoreboard.Team getScoreboardTeam() {
        return scoreboardTeam;
    }

    public String getName() {
        return name;
    }

    public int getColorCode() {
        return colorCode;
    }

    public WaitingTeam getWaitingTeam() {
        return waitingTeam;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public GameManager<?> getManager() {
        return manager;
    }

    public BackpackInventory getBackpackInventory() {
        return backpackInventory;
    }

    public ItemStack getBackpackItem() {
        return backpackItem;
    }
}
