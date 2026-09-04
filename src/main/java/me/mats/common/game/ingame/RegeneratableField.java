package me.mats.common.game.ingame;

// Opt-in: an ingame state whose game has a "field" (bingo's board, ...) that can be regenerated
// on demand. Implement this only if the generic "field new" command should apply to this game -
// a game with no such concept simply doesn't implement it, and the command won't offer it.
public interface RegeneratableField {

    void regenerateField();
}
