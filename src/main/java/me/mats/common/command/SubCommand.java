package me.mats.common.command;

import me.mats.common.game.GameManager;
import org.bukkit.entity.Player;

import java.util.List;

// One top-level branch of a GameCommand (create/join/leave/end/field/ability, ...), registered
// by name in its subcommand map instead of living as another if-else branch there.
public interface SubCommand<M extends GameManager<?>> {

    // args is everything after this subcommand's own name (e.g. for "/bingo field size 6",
    // args = {"size", "6"}).
    void execute(GameCommand<M> cmd, Player p, String[] args);

    // Suggestions for the given (partial) args, using the same slicing as execute(). No
    // suggestions by default.
    default List<String> tabComplete(GameCommand<M> cmd, Player p, String[] args) {
        return List.of();
    }

    // Whether this subcommand should be offered/used at all right now (e.g. "join" only if a
    // game is running, "field" only if this game's state actually has one). Always available
    // by default.
    default boolean isAvailable(GameCommand<M> cmd, Player p) {
        return true;
    }
}
