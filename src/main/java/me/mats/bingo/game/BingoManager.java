package me.mats.bingo.game;

import me.mats.advancementinteraction.AdvancementInteraction;
import me.mats.bingo.Bingo;
import me.mats.common.enums.Color;
import me.mats.bingo.game.ingame.BingoIngameState;
import me.mats.bingo.game.waiting.BingoWaitingState;
import me.mats.common.GeneralListener;
import me.mats.bingo.message.BingoMessage;
import me.mats.bingo.message.BingoMessages;
import me.mats.common.game.GameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BingoManager extends GameManager<BingoTeam> {

    // Tracks all running Bingo games specifically (as opposed to GameManager's cross-game registry)
    public static List<BingoManager> runningGames = new ArrayList<>();
    public static List<World> worlds = new ArrayList<>();

    // Static number tracker for naming
    private static int num = 0;

    private static String nextName() {
        num++;
        return "Bingo" + num;
    }

    // Simple getter for all games
    public static List<BingoManager> getRunningGames() {
        return runningGames;
    }

    // Check if a Player is in a Bingo game
    public static boolean inBingo(Player p) {
        for (BingoManager bm : runningGames) {
            if (bm.getPlayers().contains(p)) {
                return true;
            }
        }
        return false;
    }

    // Get the Bingo game of a player
    public static BingoManager getBingo(Player p) {
        for (BingoManager bm : runningGames) {
            if (bm.getPlayers().contains(p)) {
                return bm;
            }
        }
        return null;
    }

    // Find a running game a player (by UUID) belongs to, even if their Player object is stale
    // (e.g. they disconnected and are logging back in with a fresh Player instance)
    public static BingoManager getBingoByUUID(UUID uuid) {
        for (BingoManager bm : runningGames) {
            for (Player p : bm.getPlayers()) {
                if (p.getUniqueId().equals(uuid)) {
                    return bm;
                }
            }
        }
        return null;
    }

    public static List<World> getWorlds() {
        return worlds;
    }


    // Non-static stuff

    public BingoManager(Bingo plugin) {
        super(nextName(), plugin);
        worlds.add(getWorld());

        runningGames.add(this);
        setGameState(new BingoWaitingState(this));
        getGameState().start();
    }

    @Override
    protected String getSpawnStructureResource() {
        return "bingospawn.nbt";
    }

    @Override
    protected BingoTeam createTeam(String name, int colorCode) {
        return new BingoTeam(name, colorCode, this);
    }

    @Override
    protected Component getPlayerListFooter() {
        String name = getName();
        return Component.newline().append(Component.text("Playing ", NamedTextColor.GRAY)).append(BingoMessage.BINGO.getComponent()).append(Component.text(name.charAt(name.length() - 1), TextColor.color(0xc2f4f9)));
    }

    @Override
    protected void restoreLobbyDefaults(Player p) {
        GeneralListener.setDefaults(p);
    }

    @Override
    public Component getBrandPrefix() {
        return BingoMessage.BINGO_PREFIX.getComponent();
    }

    @Override
    public Component getBrandWordmark() {
        return BingoMessage.BINGO.getComponent();
    }

    @Override
    protected void onPlayerAdded(Player p) {
        // This makes sure that our AdvancementPacket ProtocolLib Handler works
        AdvancementInteraction.getInstance().addBingoPlayer(p);
    }

    @Override
    protected void onPlayerRemoved(Player p) {
        AdvancementInteraction.getInstance().removeBingoPlayer(p);
    }

    @Override
    protected void onPlayerReconnected(Player oldPlayer, Player newPlayer) {
        AdvancementInteraction.getInstance().removeBingoPlayer(oldPlayer);
        AdvancementInteraction.getInstance().addBingoPlayer(newPlayer);
        newPlayer.sendMessage(BingoMessages.bingo("Welcome back to Bingo"));
    }

    @Override
    public World getNetherWorld() {
        boolean firstTime = !isNetherNotNull();
        World w = super.getNetherWorld();
        if (firstTime) {
            worlds.add(w);
        }
        return w;
    }

    @Override
    public World getEndWorld() {
        boolean firstTime = !isEndNotNull();
        World w = super.getEndWorld();
        if (firstTime) {
            worlds.add(w);
        }
        return w;
    }

    @Override
    public void invitePlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!GameManager.inAnyGame(p)) {
                p.sendMessage(BingoMessage.BINGO_PREFIX.getComponent().append(Component.text("[CLICK]", NamedTextColor.YELLOW).clickEvent(ClickEvent.runCommand("/bingo join " + getName())).hoverEvent(HoverEvent.showText(Component.text("Click to join this Game", Color.STD_COLOR.getTextColor())))).append(Component.text(" to join " + getName(), Color.STD_COLOR.getTextColor())));
            }
        }
    }

    public boolean inWaitingState() {
        return getGameState() instanceof BingoWaitingState;
    }

    public boolean inIngameState() {
        return getGameState() instanceof BingoIngameState;
    }

    @Override
    public void endGame() {
        super.endGame();
        runningGames.remove(this);
    }
}
