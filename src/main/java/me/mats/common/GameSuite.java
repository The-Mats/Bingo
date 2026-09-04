package me.mats.common;

import me.mats.bingo.BingoModule;
import me.mats.common.command.PlayerDataCommand;
import me.mats.common.command.WaitingCountdownCommand;
import me.mats.common.command.WorldTPCommand;
import me.mats.common.customInventory.CustomInventoryListener;
import me.mats.common.game.waiting.WaitingWorld;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

// The plugin's single Bukkit entry point (see plugin.yml's "main:"). Sets up the generic
// listeners/commands every hosted game mode needs (lobby scoreboard/chat, custom-inventory click
// routing, worldtp/playerdata/countdown) plus the shared "kick everyone, free pooled worlds"
// shutdown, then hands off to each game mode's own GameModule (BingoModule, StreakModule, ...)
// for its command registration and world cleanup - so hosting another game mode never means
// mixing every game's glue together in one place.
public final class GameSuite extends JavaPlugin {

    private static final List<GameModule> GAME_MODULES = List.of(
            new BingoModule()
    );

    @Override
    public void onEnable() {
        getLogger().info("Starting Plugin...");

        getServer().getPluginManager().registerEvents(new GeneralListener(this), this);
        getServer().getPluginManager().registerEvents(new CustomInventoryListener(), this);
        getServer().getPluginManager().registerEvents(new ServerListPingListener(), this);

        WorldTPCommand worldTPCommand = new WorldTPCommand();
        Objects.requireNonNull(getCommand("worldtp")).setExecutor(worldTPCommand);
        Objects.requireNonNull(getCommand("worldtp")).setTabCompleter(worldTPCommand);

        PlayerDataCommand playerDataCommand = new PlayerDataCommand();
        Objects.requireNonNull(getCommand("playerdata")).setExecutor(playerDataCommand);
        Objects.requireNonNull(getCommand("playerdata")).setTabCompleter(playerDataCommand);

        WaitingCountdownCommand waitingCountdownCommand = new WaitingCountdownCommand();
        Objects.requireNonNull(getCommand("countdown")).setExecutor(waitingCountdownCommand);
        Objects.requireNonNull(getCommand("countdown")).setTabCompleter(waitingCountdownCommand);

        for (GameModule module : GAME_MODULES) {
            module.register(this);
        }
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.teleport(Bukkit.getWorld("world").getSpawnLocation());
            p.kick(Component.text("Restart"));
        }

        for (WaitingWorld ww : WaitingWorld.getWorlds()) {
            ww.freeAll();
        }
        WaitingWorld.setWorlds(null);

        for (GameModule module : GAME_MODULES) {
            module.cleanupWorlds();
        }

        GeneralListener.getStandardTeam().unregister();
        GeneralListener.getAdminTeam().unregister();
    }

    public static GameSuite getInstance() {
        return getPlugin(GameSuite.class);
    }
}
