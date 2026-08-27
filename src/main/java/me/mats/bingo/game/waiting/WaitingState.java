package me.mats.bingo.game.waiting;

import me.mats.advancementinteraction.TeamAdvancements;
import me.mats.bingo.enums.Color;
import me.mats.bingo.game.BingoManager;
import me.mats.bingo.game.BingoTeam;
import me.mats.bingo.game.GameState;
import me.mats.bingo.game.ingame.BingoLists;
import me.mats.bingo.game.ingame.IngameState;
import me.mats.bingo.message.MessageBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class WaitingState extends GameState {
    // All BingoTeams - A class for easy team change/selection
    private BingoLists.ListType setting = BingoLists.ListType.DEFAULT;
    private int size = 5;
    private int extraAbilityPoints = 0;
    private int spawnTime = 60;

    // The team selector item
    private final ItemStack teamSelector;

    // The waiting world
    private WaitingWorld waitingWorld;

    // The countdown
    private WaitingCountdown waitingCountdown;

    // Default AdvancementTab
    private final TeamAdvancements defAdvancementTab = new TeamAdvancements("bingo", "white_concrete");

    private final WaitingWorldListener listener;

    // Keeps the "5x5 - Default" action bar visible above the hotbar (action bar text fades if not resent)
    private int actionBarTaskId = -1;

    public WaitingState(BingoManager manager) {
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

    @Override
    public void start() {
        // Register BingoTeams and the Waiting Listener
        new BingoTeam("Red", Color.T_RED.getColorCode(), manager);
        new BingoTeam("Blue", Color.T_BLUE.getColorCode(), manager);
        new BingoTeam("Green", Color.T_GREEN.getColorCode(), manager);
        new BingoTeam("Yellow", Color.T_YELLOW.getColorCode(), manager);
        new BingoTeam("Orange", Color.T_ORANGE.getColorCode(), manager);
        new BingoTeam("Cyan", Color.T_CYAN.getColorCode(), manager);
        new BingoTeam("Lime", Color.T_LIME.getColorCode(), manager);
        new BingoTeam("Purple", Color.T_PURPLE.getColorCode(), manager);
        new BingoTeam("Pink", Color.T_PINK.getColorCode(), manager);


        Bukkit.getPluginManager().registerEvents(listener, manager.getPlugin());

        // Check if there is an available waiting world and load it
        if (WaitingWorld.getAvailable().length >= 1) {
            this.waitingWorld = new WaitingWorld();
        } // Need an Else here to stop the bingo

        // Create a countdown
        waitingCountdown = new WaitingCountdown(getManager());
        // Send invitation to all Players not in a Bingo game
         waitingCountdown.start(90, true);
         manager.invitePlayers();

        actionBarTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(manager.getPlugin(), this::sendSizeTypeActionBar, 0, 20);
    }

    @Override
    public void abort() {
        waitingCountdown.stop();
        Bukkit.getScheduler().cancelTask(actionBarTaskId);
        HandlerList.unregisterAll(listener);

        for (BingoTeam bTeam : manager.getBingoTeams()) {
            bTeam.removeWaitingTeam();
        }

        if (waitingWorld != null) {
            waitingWorld.free();
        }

        manager.teardownAndEnd();
    }

    @Override
    public void stop() {
        // Put Players without a Team in random Teams
        Random r = new Random();
        for (String playerName : manager.getDefaultTeam().getEntries()) {
            Player p = Bukkit.getPlayer(playerName);
            BingoTeam bingoT = manager.getBingoTeams().get(r.nextInt(manager.getBingoTeams().size()));
            bingoT.addPlayer(p);
        }

        // Remove WaitingTeam
        for (BingoTeam bTeam : manager.getBingoTeams()) {
            bTeam.removeWaitingTeam();
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
        Bukkit.getScheduler().cancelTask(actionBarTaskId);

        waitingWorld.free();

        // New State and start it
        manager.setGameState(new IngameState(manager, size, setting, extraAbilityPoints, spawnTime));
        manager.getGameState().start();
    }

    @Override
    public void resendAdvancements(Player p) {
        BingoTeam team = manager.getTeam(p);
        if (team != null) {
            team.getAdvancement().sendRootAdvancement(p);
        } else {
            defAdvancementTab.sendRootAdvancement(p);
        }
    }

    @Override
    public void addPlayer(Player p) {
        p.clearActivePotionEffects();
        p.teleport(waitingWorld.getWorld().getSpawnLocation());
        p.getInventory().clear();
        p.getInventory().setItem(0, teamSelector);
        p.setLevel(waitingCountdown.getRemainingTime());
        p.setExp((float) waitingCountdown.getRemainingTime() / waitingCountdown.getMaxTime());
        defAdvancementTab.sendRootAdvancement(p);
        p.sendActionBar(buildSizeTypeComponent());
        p.sendMessage(MessageBuilder.bingo("Welcome to Bingo"));
    }

    // "5x5 - Default" style indicator shown above the hotbar while waiting
    private Component buildSizeTypeComponent() {
        TextColor typeColor = switch (setting) {
            case DEFAULT -> Color.OVERWORLD.getTextColor();
            case HARD -> Color.NETHER.getTextColor();
            case END -> Color.END.getTextColor();
        };

        return Component.text(size + "x" + size, Color.COMMAND_YELLOW.getTextColor())
                .append(Component.text(" - ", Color.STD_COLOR.getTextColor()))
                .append(Component.text(MessageBuilder.capitalize(setting.toString().toLowerCase()), typeColor));
    }

    private void sendSizeTypeActionBar() {
        Component comp = buildSizeTypeComponent();
        for (Player p : manager.getPlayers()) {
            p.sendActionBar(comp);
        }
    }

    // Getters and Setters
    public WaitingCountdown getWaitingCountdown() {
        return waitingCountdown;
    }

    public void setSetting(BingoLists.ListType setting) {
        this.setting = setting;
        sendSizeTypeActionBar();
    }

    public void setSize(int size) {
        this.size = size;
        sendSizeTypeActionBar();
    }

    public void setExtraAbilityPoints(int extraAbilityPoints) {
        this.extraAbilityPoints = extraAbilityPoints;
    }

    public void setSpawnTime(int spawnTime) {
        this.spawnTime = spawnTime;
    }
}
