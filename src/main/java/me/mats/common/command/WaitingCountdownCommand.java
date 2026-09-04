package me.mats.common.command;

import me.mats.common.enums.Color;
import me.mats.common.game.GameManager;
import me.mats.common.game.waiting.WaitingCountdown;
import me.mats.common.game.waiting.WaitingState;
import me.mats.common.message.MessageBuilder;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WaitingCountdownCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player p) {
            GameManager<?> game = GameManager.getGame(p);
            WaitingCountdown waitingCountdown = (game != null && game.getGameState() instanceof WaitingState<?> ws) ? ws.getWaitingCountdown() : null;
            if (waitingCountdown != null) {
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("start")) {
                        if (waitingCountdown.isCounting()) {
                            p.sendMessage(MessageBuilder.error("Countdown is already counting"));
                        } else {
                            if (waitingCountdown.getRemainingTime() != 0) {
                                waitingCountdown.start(waitingCountdown.getRemainingTime(), false);
                            } else {
                                waitingCountdown.start(90, true);
                            }
                            p.sendMessage(MessageBuilder.command("Started Countdown", TextColor.color(Color.SUC_COLOR.getColorCode())));
                        }
                    } else if (args[0].equalsIgnoreCase("stop")) {
                        if (waitingCountdown.isCounting()) {
                            waitingCountdown.stop();
                            p.sendMessage(MessageBuilder.command("Stopped Countdown", TextColor.color(Color.SUC_COLOR.getColorCode())));
                        } else {
                            p.sendMessage(MessageBuilder.error("Countdown is not counting"));
                        }
                    } else if (args[0].equalsIgnoreCase("faststart")) {
                        if (waitingCountdown.isCounting()) {
                            if (waitingCountdown.getRemainingTime() > 10) {
                                waitingCountdown.setRemainingTime(10);
                                p.sendMessage(MessageBuilder.command("Set Countdown to 10 seconds", TextColor.color(Color.SUC_COLOR.getColorCode())));
                            } else {
                                p.sendMessage(MessageBuilder.error("Game starting in under 10 seconds"));
                            }
                        } else {
                            p.sendMessage(MessageBuilder.error("Countdown is not counting"));
                        }
                    } else {
                        p.sendMessage(MessageBuilder.error("Wrong usage"));
                    }
                } else if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("set")) {
                        for (char c : args[1].toCharArray()) {
                            if (!Character.isDigit(c)) {
                                p.sendMessage(MessageBuilder.error("Please enter the number of seconds"));
                                return false;
                            }
                        }
                        int seconds = Integer.parseInt(args[1]);
                        waitingCountdown.setRemainingTime(seconds);
                        p.sendMessage(MessageBuilder.command("Set the Countdown to §e"+seconds+" §aseconds", TextColor.color(Color.SUC_COLOR.getColorCode())));
                    } else if (args[0].equalsIgnoreCase("add")) {
                        for (char c : args[1].toCharArray()) {
                            if (!Character.isDigit(c)) {
                                p.sendMessage(MessageBuilder.error("Please enter the number of seconds"));
                                return false;
                            }
                        }
                        int seconds = Integer.parseInt(args[1]);
                        if (waitingCountdown.isCounting()) {
                            // To reach intended number
                            waitingCountdown.setRemainingTime(waitingCountdown.getRemainingTime()+seconds+1);
                        } else {
                            waitingCountdown.setRemainingTime(waitingCountdown.getRemainingTime()+seconds);
                        }
                        p.sendMessage(MessageBuilder.command("Added §e"+seconds+" §aseconds to the Countdown", TextColor.color(Color.SUC_COLOR.getColorCode())));
                    } else {
                        p.sendMessage(MessageBuilder.error("Wrong usage"));
                    }
                } else {
                    p.sendMessage(MessageBuilder.error("Wrong number of arguments"));
                }
            } else {
                p.sendMessage(MessageBuilder.error("You're in no game (or it has already progressed too far)"));
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> tabComplete = new ArrayList<>();
        if (sender instanceof Player p) {
            GameManager<?> game = GameManager.getGame(p);
            if (game != null && game.getGameState() instanceof WaitingState<?>) {
                if (args.length == 1) {
                    tabComplete.add("start");
                    tabComplete.add("stop");
                    tabComplete.add("faststart");
                    tabComplete.add("set");
                    tabComplete.add("add");
                }
            }
        }
        return tabComplete;
    }
}
