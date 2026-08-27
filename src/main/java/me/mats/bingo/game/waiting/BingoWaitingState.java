package me.mats.bingo.game.waiting;

import me.mats.advancementinteraction.TeamAdvancements;
import me.mats.common.enums.Color;
import me.mats.bingo.game.BingoManager;
import me.mats.bingo.game.BingoTeam;
import me.mats.common.game.GameState;
import me.mats.bingo.game.ingame.BingoLists;
import me.mats.bingo.game.ingame.IngameState;
import me.mats.bingo.message.BingoMessages;
import me.mats.common.message.MessageBuilder;
import me.mats.common.game.waiting.WaitingState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BingoWaitingState extends WaitingState<BingoManager> {
    private BingoLists.ListType setting = BingoLists.ListType.DEFAULT;
    private int size = 5;
    private int extraAbilityPoints = 0;
    private int spawnTime = 60;

    // Default AdvancementTab
    private final TeamAdvancements defAdvancementTab = new TeamAdvancements("bingo", "white_concrete");

    // Keeps the "5x5 - Default" action bar visible above the hotbar (action bar text fades if not resent)
    private int actionBarTaskId = -1;

    public BingoWaitingState(BingoManager manager) {
        super(manager);
    }

    @Override
    protected void onWaitingStart() {
        actionBarTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(manager.getPlugin(), this::sendSizeTypeActionBar, 0, 20);
    }

    @Override
    protected void onWaitingStop() {
        Bukkit.getScheduler().cancelTask(actionBarTaskId);
    }

    @Override
    protected GameState<?> buildNextState() {
        return new IngameState(manager, size, setting, extraAbilityPoints, spawnTime);
    }

    @Override
    protected void onWaitingPlayerAdded(Player p) {
        defAdvancementTab.sendRootAdvancement(p);
        p.sendActionBar(buildSizeTypeComponent());
        p.sendMessage(BingoMessages.bingo("Welcome to Bingo"));
    }

    @Override
    public void onPlayerReconnect(Player oldPlayer, Player newPlayer) {
        BingoTeam team = manager.getTeam(newPlayer);
        if (team != null) {
            team.getAdvancement().sendRootAdvancement(newPlayer);
        } else {
            defAdvancementTab.sendRootAdvancement(newPlayer);
        }
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
