package me.mats.bingo.command;


import me.mats.bingo.Bingo;
import me.mats.bingo.game.ingame.BingoLists;
import me.mats.bingo.game.BingoManager;
import me.mats.bingo.game.ingame.IngameState;
import me.mats.bingo.game.waiting.BingoWaitingState;
import me.mats.bingo.message.BingoMessages;
import me.mats.common.message.MessageBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BingoCommand implements CommandExecutor, TabCompleter {

    private Bingo plugin;

    public BingoCommand(Bingo plugin) {
        this.plugin = plugin;
    }

    // TODO: Split into sub CommandHandlers
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player p) {
            if (args.length == 1 && args[0].equalsIgnoreCase("create")) {
                p.sendMessage(BingoMessages.bingo("Starting Bingo..."));
                plugin.startBingo();

            } else if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
                if (!BingoManager.inBingo(p)) {
                    boolean found = false;
                    for (BingoManager bm : BingoManager.getRunningGames()) {
                        if (args[1].equalsIgnoreCase(bm.getName())) {
                            bm.addPlayer(p);
                            found = true;
                            // Add some Title
                            break;
                        }
                    }
                    if (!found)
                        p.sendMessage(MessageBuilder.error("Bingo Game doesn't exist"));
                } else {
                    p.sendMessage(MessageBuilder.error("Already playing Bingo"));
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("leave")) {
                BingoManager bingo = BingoManager.getBingo(p);
                if (bingo != null) {
                    bingo.removePlayer(p);
                    p.sendMessage(BingoMessages.bingo("You left the Bingo"));
                } else {
                    p.sendMessage(MessageBuilder.error("You're not playing Bingo"));
                }

            } else if (args.length == 1 && args[0].equalsIgnoreCase("end")) {
                BingoManager bingo = BingoManager.getBingo(p);
                if (bingo != null) {
                    bingo.sendChat(BingoMessages.bingo("The Bingo was ended"));
                    bingo.getGameState().abort();
                } else {
                    p.sendMessage(MessageBuilder.error("You're not playing Bingo"));
                }

            } else if (args.length >= 2 && args[0].equalsIgnoreCase("field")) {
                BingoManager bingo = BingoManager.getBingo(p);
                if (bingo != null) {
                    if (bingo.inWaitingState()) {
                        BingoWaitingState wState = (BingoWaitingState) bingo.getGameState();
                        // Checked that the player is in a Bingo in Waiting State
                        if (args[1].equalsIgnoreCase("size") && args.length == 3) {
                            try {
                                int size = Integer.parseInt(args[2]);
                                wState.setSize(size);
                                p.sendMessage(BingoMessages.bingo("Changed the Size to §e"+size));
                            } catch (NumberFormatException e) {
                                p.sendMessage(MessageBuilder.error("Please enter a valid Size"));
                                return true;
                            }
                        } else if (args[1].equalsIgnoreCase("type") && args.length == 3) {
                            try {
                                BingoLists.ListType type = BingoLists.ListType.valueOf(args[2].toUpperCase());
                                wState.setSetting(type);
                                p.sendMessage(BingoMessages.bingo("Changed the Type to §e"+type));
                            } catch (IllegalArgumentException e) {
                                p.sendMessage(MessageBuilder.error("Please enter a valid Type"));
                                return true;
                            }
                        } else {
                            p.sendMessage(MessageBuilder.error("Wrong usage"));
                        }
                    } else if (bingo.inIngameState()) {
                        IngameState iState = (IngameState) bingo.getGameState();

                        if (args[1].equalsIgnoreCase("new") && args.length == 2){
                            iState.newBingoField();
                            p.sendMessage(BingoMessages.bingo("Created a new Bingo Field"));
                        } else {
                            p.sendMessage(MessageBuilder.error("Wrong usage"));
                        }

                    } else {
                        p.sendMessage(MessageBuilder.error("The bingo is in the wrong State"));
                    }

                } else {
                    p.sendMessage(MessageBuilder.error("You're not playing Bingo"));
                }

            } else if (args.length >= 3 && args[0].equalsIgnoreCase("ability")) {
                BingoManager bingo = BingoManager.getBingo(p);
                if (bingo != null) {
                    if (bingo.inWaitingState()) {
                        BingoWaitingState wState = (BingoWaitingState) bingo.getGameState();
                        // Checked that the player is in a Bingo in Waiting State
                        if (args[1].equalsIgnoreCase("points") && args.length == 3) {
                            try {
                                int points = Integer.parseInt(args[2]);
                                if (points >= 0) {
                                    wState.setExtraAbilityPoints(points);
                                    p.sendMessage(BingoMessages.bingo("Changed the extra Ability points to §e"+points));
                                } else {
                                    p.sendMessage(MessageBuilder.error("Please enter a positive Number"));
                                }

                            } catch (NumberFormatException e) {
                                p.sendMessage(MessageBuilder.error("Please enter a valid Number"));
                                return true;
                            }
                        } else if (args[1].equalsIgnoreCase("time") && args.length == 3) {
                            try {
                                int time = Integer.parseInt(args[2]);
                                if (time > 10) {
                                    wState.setSpawnTime(time);
                                    p.sendMessage(BingoMessages.bingo("Changed the spawn time to §e"+time));
                                } else {
                                    p.sendMessage(MessageBuilder.error("Please enter a Number bigger than 10"));
                                }

                            } catch (NumberFormatException e) {
                                p.sendMessage(MessageBuilder.error("Please enter a valid Number"));
                                return true;
                            }
                        } else {
                            p.sendMessage(MessageBuilder.error("Wrong usage"));
                        }
                    } else {
                        p.sendMessage(MessageBuilder.error("The bingo is in the wrong State"));
                    }

                } else {
                    p.sendMessage(MessageBuilder.error("You're not playing Bingo"));
                }
            } else {
                p.sendMessage(MessageBuilder.error("Wrong usage"));
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p = (Player) sender;
        BingoManager bingo = BingoManager.getBingo(p);

        List<String> tabComplete = new ArrayList<>();
        if (args.length == 1) {
            tabComplete.add("create");
            if (BingoManager.getRunningGames().size() > 0) {
                tabComplete.add("join");
            }
            if (bingo != null) {
                tabComplete.add("field");
                tabComplete.add("leave");
                tabComplete.add("end");
            }
            if (bingo != null && bingo.inWaitingState()) {
                tabComplete.add("ability");
            }

        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("join")) {
                for (BingoManager bm : BingoManager.getRunningGames()) {
                    tabComplete.add(bm.getName());
                }
            } else if (args[0].equalsIgnoreCase("field") && bingo != null) {
                if (bingo.inWaitingState()) {
                    tabComplete.add("size");
                    tabComplete.add("type");
                } else if (bingo.inIngameState()) {
                    tabComplete.add("new");
                }
            } else if (args[0].equalsIgnoreCase("ability") && bingo != null && bingo.inWaitingState()) {
                tabComplete.add("points");
                tabComplete.add("time");
            }
        }  else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("field") && args[1].equalsIgnoreCase("type") && bingo != null && bingo.inWaitingState()) {
                tabComplete.add("DEFAULT");
                tabComplete.add("HARD");
                tabComplete.add("END");
            }
        }
        return tabComplete;
    }
}
