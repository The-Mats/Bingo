package me.mats.bingo.game;

import me.mats.advancementinteraction.TeamAdvancements;
import me.mats.common.enums.Color;
import me.mats.bingo.game.ingame.IngameState;
import me.mats.bingo.message.BingoMessage;
import me.mats.bingo.message.BingoMessages;
import me.mats.common.message.MessageBuilder;
import me.mats.common.game.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BingoTeam extends Team {
    private final TeamAdvancements advancement;
    private final BingoManager bingoManager;
    private boolean[][] bingoField;
    private boolean halfDone = false;
    private int collectedBingoItems;

    // This is only used if the Team has thief ability
    private final List<int[]> missingPositions = new ArrayList<>();

    private boolean winner = false;

    public BingoTeam(String name, int colorCode, BingoManager manager) {
        super(name, colorCode, manager);
        this.bingoManager = manager;
        this.advancement = new TeamAdvancements("bingo", name+"_concrete");
    }

    // Refresh this team's advancement tab for a player who just joined it in the waiting lobby
    @Override
    public void onWaitingTeamJoined(Player p) {
        advancement.sendRootAdvancement(p);
        p.sendMessage(BingoMessages.bingo("You joined Team ").append(Component.text(name, TextColor.color(colorCode)).decorate(TextDecoration.BOLD)));
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
            sendChat(BingoMessage.BINGO_PREFIX.getComponent().append(MessageBuilder.buildMsg(List.of(MessageBuilder.capitalize(advancementName.replace("_", " ")), " collected"), List.of(net.kyori.adventure.text.format.NamedTextColor.GREEN.value(), Color.STD_COLOR.getColorCode()))));
            sendTitle(MessageBuilder.capitalize(advancementName.replace("_", " ")));

            // Check for Bingo
            IngameState state = (IngameState) bingoManager.getGameState();
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
                state.getManager().sendChat(BingoMessage.BINGO_PREFIX.getComponent().append(Component.text("Team ", Color.STD_COLOR.getTextColor()).append(Component.text(name, TextColor.color(colorCode)).decorate(TextDecoration.BOLD)).append(Component.text(" is half done", Color.STD_COLOR.getTextColor()))));
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
        for (BingoTeam b : bingoManager.getTeams()) {
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
        IngameState state = (IngameState) bingoManager.getGameState();
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

    // Getters and Setters
    public boolean[][] getBingoField() {
        return bingoField;
    }

    public TeamAdvancements getAdvancement() {
        return advancement;
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
