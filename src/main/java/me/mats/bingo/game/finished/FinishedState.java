package me.mats.bingo.game.finished;

import me.mats.advancementinteraction.AdvancementInteraction;
import me.mats.bingo.Bingo;
import me.mats.bingo.GeneralListener;
import me.mats.bingo.game.BingoManager;
import me.mats.bingo.game.BingoTeam;
import me.mats.bingo.game.GameState;
import me.mats.bingo.game.waiting.WaitingWorld;
import me.mats.bingo.message.Message;
import me.mats.bingo.message.MessageBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class FinishedState extends GameState {
    private WaitingWorld waitingWorld;
    private final FinishedListener listener;
    private final BingoTeam winner;
    private FinishedCountdown finishedCountdown;

    // Kept around only so a relogging player can have the winning field re-sent to them
    private final List<String> winField;
    private final List<ItemStack> fieldItems;
    private final float[][] fieldPositions;

    public FinishedState(BingoManager manager, BingoTeam winner, List<String> winField, List<ItemStack> fieldItems, float[][] fieldPositions) {
        super.manager = manager;
        this.winner = winner;
        this.winField = winField;
        this.fieldItems = fieldItems;
        this.fieldPositions = fieldPositions;

        if (WaitingWorld.getAvailable().length >= 1) {
            this.waitingWorld = new WaitingWorld();
        } // Need an Else here to stop the bingo
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
            p.showTitle(Title.title(Component.text("Team "+winner.getName(), TextColor.color(winner.getColorCode())), Component.text("GOT A ", NamedTextColor.GRAY).append(Message.BINGO.getComponent()), Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(10000), Duration.ofMillis(500))));

        }
        int delay = 80;
        //List<String> demoNames = List.of("FireTV", "X__gamer", "HP_Siven", "ghettobanger2_0", "LUAP!23141512");
        for (Player p2 : winner.getPlayers()) {
        //for (String p2 : demoNames) {
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
        World mainWorld = Bukkit.getWorld("world");

        List<NamespacedKey> recipeNames = new ArrayList<>();
        Bukkit.recipeIterator().forEachRemaining(r -> recipeNames.add(((Keyed) r).getKey()));

        for (Player p : manager.getPlayers()) {

            // Remove Recipes again
            p.undiscoverRecipes(recipeNames);

            GeneralListener.setDefaults(p);
            p.teleport(mainWorld.getSpawnLocation());
            p.setGameMode(GameMode.SURVIVAL);
            p.clearActivePotionEffects();
            p.getInventory().clear();
            AdvancementInteraction.getInstance().removeBingoPlayer(p);

            // Show Lobby Players again
            for (Player p2 : Bukkit.getOnlinePlayers()) {
                if (!BingoManager.inBingo(p2)) {
                    // Player must be in Lobby
                    p2.showPlayer(Bingo.getInstance(), p);
                    p.showPlayer(Bingo.getInstance(), p2);
                }
            }
        }

        for (Team t : manager.getBoard().getTeams()) {
            t.unregister();
        }

        HandlerList.unregisterAll(listener);
        waitingWorld.free();
        // Remove the bingo worlds (Done here for smoother transition)
        manager.deleteWorlds();
        manager.endBingoGame();
    }

    @Override
    public void addPlayer(Player p) {
        p.sendMessage(MessageBuilder.bingo("Ending soon"));
        p.teleport(waitingWorld.getWorld().getSpawnLocation());
    }

    @Override
    public void resendAdvancements(Player p) {
        winner.getAdvancement().sendFinalField(List.of(p), new ArrayList<>(), winField, fieldItems, fieldPositions);
    }

    public WaitingWorld getWaitingWorld() {
        return waitingWorld;
    }
}
