package me.mats.common.command;

import me.mats.common.game.GameManager;
import me.mats.common.message.MessageBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Generic "/<game> create|join|leave|end|field|ability" command shared by every team game. Each
// branch is its own SubCommand (CreateSubCommand, JoinSubCommand, ...) registered by name below
// instead of living as another if-else branch here - "field"/"ability" only show up (and only
// work) for a game whose state opts into FieldConfig/RegeneratableField/AbilityConfig; a game
// implementing none of those still gets a fully working create/join/leave/end for free.
public abstract class GameCommand<M extends GameManager<?>> implements CommandExecutor, TabCompleter {

    protected final JavaPlugin plugin;
    private final Class<M> gameClass;
    private final Map<String, SubCommand<M>> subCommands = new LinkedHashMap<>();

    protected GameCommand(JavaPlugin plugin, Class<M> gameClass) {
        this.plugin = plugin;
        this.gameClass = gameClass;
        registerSubCommand("create", new CreateSubCommand<>());
        registerSubCommand("join", new JoinSubCommand<>());
        registerSubCommand("leave", new LeaveSubCommand<>());
        registerSubCommand("end", new EndSubCommand<>());
        registerSubCommand("field", new FieldSubCommand<>());
        registerSubCommand("ability", new AbilitySubCommand<>());
    }

    // Lets a concrete game's command add its own extra subcommand on top of the generic ones.
    protected final void registerSubCommand(String name, SubCommand<M> subCommand) {
        subCommands.put(name, subCommand);
    }

    // Hook: this game's display name used in generic messages ("Bingo", "Streak").
    protected abstract String gameName();

    // Hook: construct and start a brand-new game of this type.
    protected abstract M createGame();

    // Lookups every SubCommand needs, kept here since only GameCommand knows this game's type.
    M getGame(Player p) {
        return GameManager.getGameOfType(p, gameClass);
    }

    List<M> getRunningGames() {
        return GameManager.getGamesOfType(gameClass);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            return true;
        }
        if (args.length == 0) {
            p.sendMessage(MessageBuilder.error("Wrong usage"));
            return true;
        }
        SubCommand<M> sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            p.sendMessage(MessageBuilder.error("Wrong usage"));
            return true;
        }
        sub.execute(this, p, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p) || args.length == 0) {
            return List.of();
        }
        if (args.length == 1) {
            List<String> names = new ArrayList<>();
            for (Map.Entry<String, SubCommand<M>> entry : subCommands.entrySet()) {
                if (entry.getValue().isAvailable(this, p)) {
                    names.add(entry.getKey());
                }
            }
            return names;
        }
        SubCommand<M> sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            return List.of();
        }
        return sub.tabComplete(this, p, Arrays.copyOfRange(args, 1, args.length));
    }
}
