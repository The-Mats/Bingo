package me.mats.common.game.waiting;

import me.mats.common.enums.Color;
import me.mats.common.game.GameManager;
import me.mats.common.game.GameState;
import me.mats.common.game.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Generic waiting-lobby lifecycle shared by every game: pooled waiting world, team roster,
// team-selector item, join countdown. A concrete game fills in what happens once the
// countdown ends (buildNextState) and whatever extra per-player waiting UI it wants
// (onWaitingPlayerAdded) - and still owns onPlayerReconnect from GameState.
public abstract class WaitingState<M extends GameManager<?>> extends GameState<M> {

    // The fixed team roster every game starts from. Games needing a different roster/count
    // can still call manager.addTeam(...) themselves if this ever needs to become a hook.
    private static final List<Color> ROSTER_COLORS = List.of(
            Color.T_RED, Color.T_BLUE, Color.T_GREEN, Color.T_YELLOW, Color.T_ORANGE,
            Color.T_CYAN, Color.T_LIME, Color.T_PURPLE, Color.T_PINK
    );
    private static final List<String> ROSTER_NAMES = List.of(
            "Red", "Blue", "Green", "Yellow", "Orange", "Cyan", "Lime", "Purple", "Pink"
    );

    // The team selector item
    protected final ItemStack teamSelector;

    // The waiting world
    protected WaitingWorld waitingWorld;

    // The countdown
    protected WaitingCountdown waitingCountdown;

    protected final WaitingWorldListener listener;

    protected WaitingState(M manager) {
        super.manager = manager;
        // Prepare Team Selector
        ItemStack teamSelector = new ItemStack(Material.WHITE_BED);
        ItemMeta tsMeta = teamSelector.getItemMeta();
        tsMeta.displayName(Component.text("Team Selector").color(TextColor.color(0xFCCB00)).decoration(TextDecoration.ITALIC,false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("[Right Click]").color(TextColor.color(0x808080)));
        tsMeta.lore(lore);
        teamSelector.setItemMeta(tsMeta);
        this.teamSelector = teamSelector;
        listener = new WaitingWorldListener(manager);
    }

    // Hook: build whatever state the game transitions into once the countdown finishes.
    protected abstract GameState<?> buildNextState();

    // Hook: send whatever extra per-player waiting UI a concrete game wants
    // (e.g. bingo's advancement tab default + welcome message).
    protected void onWaitingPlayerAdded(Player p) {
    }

    // Hook: start whatever extra per-game ticking UI runs during the wait
    // (e.g. bingo's "5x5 - Default" action bar). Called at the end of start().
    protected void onWaitingStart() {
    }

    // Hook: stop whatever onWaitingStart() started. Called first thing in both
    // abort() and stop(), before any of the shared waiting-lobby cleanup.
    protected void onWaitingStop() {
    }

    @Override
    public void start() {
        // Register the fixed team roster and the Waiting Listener
        for (int i = 0; i < ROSTER_NAMES.size(); i++) {
            manager.addTeam(ROSTER_NAMES.get(i), ROSTER_COLORS.get(i).getColorCode());
        }

        Bukkit.getPluginManager().registerEvents(listener, manager.getPlugin());

        // Check if there is an available waiting world and load it
        if (WaitingWorld.getAvailable().length >= 1) {
            this.waitingWorld = new WaitingWorld();
        } // Need an Else here to stop the game

        // Create a countdown
        waitingCountdown = new WaitingCountdown(getManager());
        // Send invitation to all eligible Players
        waitingCountdown.start(90, true);
        manager.invitePlayers();

        onWaitingStart();
    }

    @Override
    public void abort() {
        onWaitingStop();

        waitingCountdown.stop();
        HandlerList.unregisterAll(listener);

        for (Team team : manager.getTeams()) {
            team.removeWaitingTeam();
        }

        if (waitingWorld != null) {
            waitingWorld.free();
        }

        manager.teardownAndEnd();
    }

    @Override
    public void stop() {
        onWaitingStop();

        // Put Players without a Team in random Teams
        Random r = new Random();
        for (String playerName : manager.getDefaultTeam().getEntries()) {
            Player p = Bukkit.getPlayer(playerName);
            Team team = manager.getTeams().get(r.nextInt(manager.getTeams().size()));
            team.addPlayer(p);
        }

        // Remove WaitingTeam
        for (Team team : manager.getTeams()) {
            team.removeWaitingTeam();
        }
        for (Player p : manager.getPlayers()) {
            p.setFireTicks(0);
            p.getInventory().clear();
        }

        // Teleport Players to new World and free current world
        for (Player p : manager.getPlayers()) {
            p.teleport(manager.getWorld().getSpawnLocation());
        }
        // Unregister Listener
        HandlerList.unregisterAll(listener);

        waitingWorld.free();

        // New State and start it
        manager.setGameState(buildNextState());
        manager.getGameState().start();
    }

    @Override
    public void addPlayer(Player p) {
        p.clearActivePotionEffects();
        p.teleport(waitingWorld.getWorld().getSpawnLocation());
        p.getInventory().clear();
        p.getInventory().setItem(0, teamSelector);
        p.setLevel(waitingCountdown.getRemainingTime());
        p.setExp((float) waitingCountdown.getRemainingTime() / waitingCountdown.getMaxTime());
        onWaitingPlayerAdded(p);
    }

    // Getters
    public WaitingCountdown getWaitingCountdown() {
        return waitingCountdown;
    }
}
