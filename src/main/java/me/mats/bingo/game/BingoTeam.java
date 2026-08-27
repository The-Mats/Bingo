package me.mats.bingo.game;

import me.mats.advancementinteraction.TeamAdvancements;
import me.mats.bingo.enums.Color;
import me.mats.bingo.game.ingame.IngameState;
import me.mats.bingo.customInventory.BackpackInventory;
import me.mats.bingo.message.Message;
import me.mats.bingo.message.MessageBuilder;
import me.mats.bingo.game.waiting.WaitingTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.scoreboard.Team;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static me.mats.bingo.enums.Color.PLAYER;

public class BingoTeam {
    private final String name;
    private final int colorCode;
    private final Team team;
    private final TeamAdvancements advancement;
    private final BingoManager manager;
    private final List<Player> players = new ArrayList<>();
    private WaitingTeam waitingTeam;
    private boolean[][] bingoField;
    private boolean halfDone = false;
    private int collectedBingoItems;

    // This is only used if the Team has thief ability
    private final List<int[]> missingPositions = new ArrayList<>();

    private boolean winner = false;


    // Backpack stuff
    private final BackpackInventory backpackInventory;
    private final ItemStack backpackItem;



    public BingoTeam(String name, int colorCode, BingoManager manager) {
        this.backpackInventory = new BackpackInventory(manager, this);
        this.name = name;
        this.colorCode = colorCode;
        this.manager = manager;
        this.advancement = new TeamAdvancements("bingo", name+"_concrete");
        this.team = manager.getBoard().registerNewTeam(name);
        this.waitingTeam = new WaitingTeam(this);

        // Set Color and Prefix for above head and List
        team.color(NamedTextColor.GRAY);
        team.prefix(Component.text("[").color(NamedTextColor.DARK_GRAY).append(Component.text(name,TextColor.color(colorCode)).append(Component.text("] ", NamedTextColor.DARK_GRAY))));

        // Backpack ItemStack creation
        backpackItem = new ItemStack(Material.BUNDLE);
        BundleMeta meta = (BundleMeta) backpackItem.getItemMeta();
        meta.displayName(Component.text("Backpack").color(TextColor.color(colorCode)).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        backpackItem.setItemMeta(meta);

        // Add Team to BingoManager
        manager.getBingoTeams().add(this);
    }

    // Remove reference to waiting Time as it is no longer needed
    public void removeWaitingTeam() {
        this.waitingTeam = null;
    }

    public void removePlayer(Player p) {
        team.removePlayer(p);
        players.remove(p);

        if (waitingTeam != null) {
            waitingTeam.removePlayer(p);
        }
    }

    public void addPlayer(Player p) {
        BingoTeam bTeam = manager.getTeam(p);

        if(bTeam != null)
            bTeam.removePlayer(p);

        team.addPlayer(p);
        players.add(p);

        waitingTeam.addPlayer(p);
    }

    public boolean isFound(int[] position) {
        return bingoField[position[0]][position[1]];
    }

    public void checkForBingo(int[] position, String advancementName) {
        int x = position[0];
        int y = position[1];
        if (!bingoField[x][y]) {
            bingoField[x][y] = true;
            collectedBingoItems++;
            TeamAdvancements.grantAdvancement(players, "bingo", advancementName);
            sendChat(Message.BINGO_PREFIX.getComponent().append(MessageBuilder.buildMsg(List.of(MessageBuilder.capitalize(advancementName.replace("_", " ")), " collected"), List.of(NamedTextColor.GREEN.value(), Color.STD_COLOR.getColorCode()))));
            sendTitle(MessageBuilder.capitalize(advancementName.replace("_", " ")));

            // Check for Bingo
            IngameState state =(IngameState) manager.getGameState();
            int size = state.getSize();
            int halfSize = (int) Math.ceil((float) size/2);

            boolean done = false;
            boolean bingo = true;
            int collectedAmount = 0;
            int[] lastPosition = new int[2];
            for (int i = 0; i < size; i++) {
                if (!bingoField[i][y]) {
                    bingo = false;
                    lastPosition[0] = i;
                    lastPosition[1] = y;
                } else {
                    collectedAmount++;
                }
            }
            // Check if thief ability and if won then
            if (!bingo && collectedAmount==size-1 && state.getAbilities().getThiefAbilityList().contains(players.get(0))) {
                bingo = hasOtherTeam(lastPosition);
            }

            if (!halfDone && collectedAmount >= halfSize) {
                done = true;
            }

            // If no Bingo in row then maybe column
            if (!bingo) {
                bingo = true;
                collectedAmount = 0;
                for (int i = 0; i < size; i++) {
                    if (!bingoField[x][i]) {
                        bingo = false;
                        lastPosition[0] = x;
                        lastPosition[1] = i;
                    } else {
                        collectedAmount++;
                    }
                }

                // Check if thief ability and if won then
                if (!bingo && collectedAmount==size-1 && state.getAbilities().getThiefAbilityList().contains(players.get(0))) {
                    bingo = hasOtherTeam(lastPosition);
                }

                if (!halfDone && collectedAmount >= halfSize) {
                    done = true;
                }

                // If no in column then maybe diagonal
                if (!bingo) {
                    if (x == y) {
                        bingo = true;
                        collectedAmount = 0;
                        for (int i = 0; i < size; i++) {
                            if (!bingoField[i][i]) {
                                bingo = false;
                                lastPosition[0] = i;
                                lastPosition[1] = i;
                            } else {
                                collectedAmount++;
                            }
                        }
                        // Check if thief ability and if won then
                        if (!bingo && collectedAmount==size-1 && state.getAbilities().getThiefAbilityList().contains(players.get(0))) {
                            bingo = hasOtherTeam(lastPosition);
                        }
                        if (!halfDone && collectedAmount >= halfSize) {
                            done = true;
                        }
                    }
                    if (x+y == size-1) {
                        bingo = true;
                        collectedAmount = 0;
                        for (int i = 0; i < size; i++) {
                            if (!bingoField[i][size-1-i]) {
                                bingo = false;
                                lastPosition[0] = i;
                                lastPosition[1] = size-1-i;
                            } else {
                                collectedAmount++;
                            }
                        }
                        // Check if thief ability and if won then
                        if (!bingo && collectedAmount==size-1 && state.getAbilities().getThiefAbilityList().contains(players.get(0))) {
                            bingo = hasOtherTeam(lastPosition);
                        }
                        if (!halfDone && collectedAmount >= halfSize) {
                            done = true;
                        }
                    }
                }
            }

            // Send half done message
            if (done) {
                halfDone = true;
                state.getManager().sendChat(Message.BINGO_PREFIX.getComponent().append(Component.text("Team ", Color.STD_COLOR.getTextColor()).append(Component.text(name, TextColor.color(colorCode)).decorate(TextDecoration.BOLD)).append(Component.text(" is half done", Color.STD_COLOR.getTextColor()))));
            }

            if (bingo) {
                winner = true;
                state.stop();
            } else {
                state.validateNewBingoPosition(position);
            }
        }
    }

    private boolean hasOtherTeam(int[] lastPosition) {
        missingPositions.add(lastPosition); // Duplicates are possible in this List
        for (BingoTeam b : manager.getBingoTeams()) {
            if (b.isFound(lastPosition)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasWonWithNewPosition(int[] newPosition) {
        for (int[] position : missingPositions) {
            if (Arrays.equals(position, newPosition)) {
                winner = true;
                return true;

            }
        }
        return false;
    }

    public void extendLongestBingoLine() {
        // 1. We need to check what the longest bingo line is
        IngameState state = (IngameState) manager.getGameState();
        int size = state.getSize();

        int maxLength = 0;
        int maxRow = -1;
        int maxCol = -1;
        boolean isDiagonal = false;

        // Check rows and columns
        for (int i = 0; i < size; i++) {
            int rowLength = 0;
            int colLength = 0;
            for (int j = 0; j < size; j++) {
                if (bingoField[i][j]) {
                    rowLength++;
                }
                if (bingoField[j][i]) {
                    colLength++;
                }
            }
            if (rowLength >= maxLength) {
                maxLength = rowLength;
                maxRow = i;
                maxCol = -1; // Reset maxCol if a longer row is found
            }
            if (colLength >= maxLength) {
                maxLength = colLength;
                maxRow = -1; // Reset maxRow if a longer column is found
                maxCol = i;
            }
        }

        // Check diagonals
        int diagonal1Length = 0;
        int diagonal2Length = 0;
        for (int i = 0; i < size; i++) {
            if (bingoField[i][i]) {
                diagonal1Length++;
            }
            if (bingoField[i][size - 1 - i]) {
                diagonal2Length++;
            }
        }

        if (diagonal1Length >= maxLength) {
            maxLength = diagonal1Length;
            maxRow = -1; // Reset maxRow if a longer diagonal is found
            maxCol = -1; // Reset maxCol if a longer diagonal is found
            isDiagonal = true;
        }
        if (diagonal2Length >= maxLength) {
            maxLength = diagonal2Length;
            maxRow = -1; // Reset maxRow if a longer diagonal is found
            maxCol = -1; // Reset maxCol if a longer diagonal is found
            isDiagonal = true;
        }

        int[] newPosition = new int[2];
        // Find the first false position in the longest line
        if (isDiagonal) {
            if (diagonal1Length == maxLength) {
                for (int i = 0; i < size; i++) {
                    if (!bingoField[i][i]) {
                        newPosition[0] = i;
                        newPosition[1] = i;
                        break;
                    }
                }
            } else {
                for (int i = 0; i < size; i++) {
                    if (!bingoField[i][size - 1 - i]) {
                        newPosition[0] = i;
                        newPosition[1] = size - 1 - i;
                        break;
                    }
                }
            }
        } else if (maxCol == -1) {
            for (int j = 0; j < size; j++) {
                if (!bingoField[maxRow][j]) {
                    newPosition[0] = maxRow;
                    newPosition[1] = j;
                    break;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (!bingoField[i][maxCol]) {
                    newPosition[0] = i;
                    newPosition[1] = maxCol;
                    break;
                }
            }
        }
        checkForBingo(newPosition, state.getMaterialFromPosition(newPosition).toString().toLowerCase());
    }

    public void sendChat(Component comp) {
        for (Player p : players) {
            p.sendMessage(comp);
        }
    }

    public void sendTitle(String name) {
        final Title title = Title.title(Component.text(" "), Component.text("§o§a+ §8"+name), Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(1200), Duration.ofMillis(500)));

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

    public Pair<BundleMeta,ItemStack> removeItemFromBackPack() {
        if (backpackInventory.getInventory().isEmpty()) {
            return null;
        } else {
            for (int i = backpackInventory.getInventory().getSize()-1; i >= 0; i--) {
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
    public Team getScoreboardTeam() {
        return team;
    }

    public String getName() {
        return name;
    }

    public int getColorCode() {
        return colorCode;
    }

    public boolean[][] getBingoField() {
        return bingoField;
    }

    public WaitingTeam getWaitingTeam() {
        return waitingTeam;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public TeamAdvancements getAdvancement() {
        return advancement;
    }
    public BackpackInventory getBackpackInventory() {
        return backpackInventory;
    }

    public ItemStack getBackpackItem() {
        return backpackItem;
    }

    public boolean isWinner() {
        return winner;
    }

    public void setBingoField(boolean[][] bingoField) {
        this.bingoField = bingoField;
    }

    public int getCollectedBingoItems() {
        return collectedBingoItems;
    }
}