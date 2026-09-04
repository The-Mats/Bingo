package me.mats.common.game.finished;

import me.mats.common.game.GameManager;
import me.mats.common.game.GameState;
import me.mats.common.game.Team;
import me.mats.common.game.waiting.WaitingWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

// Generic "show the winner, then teleport everyone back to the lobby" lifecycle shared by
// every game. onPlayerReconnect is deliberately left to the concrete game (like WaitingState
// does) since what "show the win result again after a relog" means is game-specific (bingo's
// advancement grid, or something else entirely for another game).
public abstract class FinishedState<M extends GameManager<?>> extends GameState<M> {
    protected WaitingWorld waitingWorld;
    protected final FinishedListener listener;
    protected final Team winner;
    protected FinishedCountdown finishedCountdown;

    protected FinishedState(M manager, Team winner) {
        super.manager = manager;
        this.winner = winner;

        // May come back null (pool exhausted, or every remaining world failed to load) - the
        // caller checks getWaitingWorld() and falls back to ending the game immediately instead
        // of showing the win celebration.
        this.waitingWorld = WaitingWorld.tryAcquire();
        listener = new FinishedListener(manager);
    }


    @Override
    public void start() {
        Bukkit.getPluginManager().registerEvents(listener, manager.getPlugin());

        World w = waitingWorld.getWorld();
        List<Location> locations = List.of(new Location(w,-17,107,12), new Location(w,-17,106,0), new Location(w,-17,107,0), new Location(w,-17,107,-12),
                new Location(w,-15,107,-14), new Location(w,-3,106,-14), new Location(w,-3,107,-14), new Location(w,9,107,-14),
                new Location(w,-15,107,14), new Location(w,-3,106,14), new Location(w,-3,107,14), new Location(w,9,107,14),
                new Location(w,11,107,12), new Location(w,11,106,0), new Location(w,11,107,0), new Location(w,11,107,-12));
        for (Location loc : locations) {
            loc.getBlock().setType(Material.valueOf(winner.getName().toUpperCase()+"_WOOL"));
        }

        finishedCountdown = new FinishedCountdown(manager, this);
        finishedCountdown.start(60);

        for (Player p : manager.getPlayers()) {
            p.setLevel(0);
            p.setExp(0.999F);
            p.showTitle(Title.title(Component.text("Team "+winner.getName(), TextColor.color(winner.getColorCode())), Component.text("GOT A ", NamedTextColor.GRAY).append(manager.getBrandWordmark()), Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(10000), Duration.ofMillis(500))));

        }
        int delay = 80;
        for (Player p2 : winner.getPlayers()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(manager.getPlugin(),new NameRunnable(p2.getName()), delay);
            delay += 20;
        }
    }

    // Runnable to show Names of all Team players
    private class NameRunnable implements Runnable {
        private final String name;

        public NameRunnable(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            for (Player p : manager.getPlayers()) {
                p.sendTitlePart(TitlePart.SUBTITLE, Component.text(name, NamedTextColor.GRAY));
            }
        }
    }


    @Override
    public void abort() {
        if (finishedCountdown != null) {
            finishedCountdown.cancel();
        }
        stop();
    }

    @Override
    public void stop() {
        List<NamespacedKey> recipeNames = new ArrayList<>();
        Bukkit.recipeIterator().forEachRemaining(r -> recipeNames.add(((org.bukkit.Keyed) r).getKey()));

        for (Player p : manager.getPlayers()) {
            manager.resetPlayerToLobby(p, recipeNames);
        }

        for (org.bukkit.scoreboard.Team t : manager.getBoard().getTeams()) {
            t.unregister();
        }

        HandlerList.unregisterAll(listener);
        waitingWorld.free();
        // Remove the game's worlds (done here for a smoother transition)
        manager.deleteWorlds();
        manager.endGame();
    }

    @Override
    public void addPlayer(Player p) {
        p.sendMessage(manager.brandedStatus("Ending soon"));
        p.teleport(waitingWorld.getWorld().getSpawnLocation());
    }

    public WaitingWorld getWaitingWorld() {
        return waitingWorld;
    }
}
