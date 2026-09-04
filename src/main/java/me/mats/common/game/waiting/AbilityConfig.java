package me.mats.common.game.waiting;

// Opt-in: a waiting state whose game uses the common ability system's extra-points/spawn-time
// knobs. Implement this only if the generic "ability points/time" commands should apply - a
// game with no abilities simply doesn't implement it.
public interface AbilityConfig {

    int getExtraAbilityPoints();

    void setExtraAbilityPoints(int extraAbilityPoints);

    int getSpawnTime();

    void setSpawnTime(int spawnTime);
}
