package me.mats.common.game;

import me.mats.common.world.ChunkPreloader;
import me.mats.common.world.WorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

// Everything a running team game needs regardless of which game it is: its own fresh
// world (+ nether/end on demand), player/team bookkeeping, the waiting-lobby-to-teardown
// lifecycle. A concrete game (BingoManager, ...) fills in the few hooks below - which
// spawn structure to place, how to build its own Team subtype, and how it plugs into
// whatever external systems (advancement UI, etc.) it uses.
public abstract class GameManager<T extends me.mats.common.game.Team> {

    // Tracks every running game across every game type, so shared code (lobby listeners,
    // commands) can ask "is this player in a game" without knowing which kind.
    private static final List<GameManager<?>> ALL = new ArrayList<>();

    public static boolean inAnyGame(Player p) {
        for (GameManager<?> g : ALL) {
            if (g.getPlayers().contains(p)) {
                return true;
            }
        }
        return false;
    }

    public static GameManager<?> getGame(Player p) {
        for (GameManager<?> g : ALL) {
            if (g.getPlayers().contains(p)) {
                return g;
            }
        }
        return null;
    }

    // Find a running game a player (by UUID) belongs to, even if their Player object is stale
    // (e.g. they disconnected and are logging back in with a fresh Player instance)
    public static GameManager<?> getGameByUUID(UUID uuid) {
        for (GameManager<?> g : ALL) {
            for (Player p : g.getPlayers()) {
                if (p.getUniqueId().equals(uuid)) {
                    return g;
                }
            }
        }
        return null;
    }

    private final String name;
    private final JavaPlugin plugin;

    private final List<Player> players = new ArrayList<>();

    private final Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
    private final Team defaultTeam = board.registerNewTeam("default");

    private final List<T> teams = new ArrayList<>();

    private GameState<?> gameState;

    private final World world;
    private World netherWorld;
    private World endWorld;

    protected GameManager(String name, JavaPlugin plugin) {
        defaultTeam.color(NamedTextColor.GRAY);
        this.name = name;
        this.plugin = plugin;

        // World stuff here to prevent lag for now
        WorldCreator worldCreator = new WorldCreator(name);
        world = worldCreator.createWorld();

        String spawnResource = getSpawnStructureResource();
        InputStream spawnFile = plugin.getResource(spawnResource);
        if (spawnFile != null) {
            try {
                Structure spawn = Bukkit.getStructureManager().loadStructure(spawnFile);
                spawn.place(world, new BlockVector(-15, world.getSpawnLocation().getBlockY() + 100, -15), false, StructureRotation.NONE, Mirror.NONE, 0, 1, new Random());
            } catch (IOException e) {
                Bukkit.getLogger().warning("Couldn't load " + spawnResource + ": " + e.getMessage());
            }
        } else {
            Bukkit.getLogger().warning("Couldn't load " + spawnResource);
        }
        configWorld(world);
        world.setSpawnLocation(0, world.getSpawnLocation().getBlockY() + 100, 0);

        // Warm up the terrain below the spawn platform now, while there's still the full
        // waiting lobby + spawn countdown ahead of us, instead of leaving it for players to trigger.
        ChunkPreloader.preload(world, world.getSpawnLocation(), world.getViewDistance());

        ALL.add(this);
    }

    // --- Hooks a concrete game must implement ---

    // Resource name (nbt file, bundled in the plugin jar) of the spawn platform structure
    // to place in the freshly created main world.
    protected abstract String getSpawnStructureResource();

    // Build a new team of this game's own Team subtype (backing addTeam()).
    protected abstract T createTeam(String name, int colorCode);

    // Invite eligible players (however this game defines "eligible") to join.
    public abstract void invitePlayers();

    // The player-list footer shown to players in this game (branding is per game).
    protected abstract Component getPlayerListFooter();

    // Reset a leaving/disconnecting player back to whatever this game's "lobby" defaults are.
    protected abstract void restoreLobbyDefaults(Player p);

    // --- Optional branding hooks, used by shared UI (e.g. the waiting countdown) that would
    // otherwise have to hardcode one game's wordmark. Empty by default. ---

    // A short branded chat prefix (e.g. "[BINGO] "), prepended to shared status messages.
    public Component getBrandPrefix() {
        return Component.empty();
    }

    // A branded wordmark shown as the big countdown title text (e.g. the "BINGO" logo).
    public Component getBrandWordmark() {
        return Component.empty();
    }

    // Convenience for shared UI: brand prefix + a plain status message.
    public Component brandedStatus(String msg) {
        return getBrandPrefix().append(Component.text(msg, me.mats.common.enums.Color.STD_COLOR.getTextColor()));
    }

    // --- Hooks a concrete game may optionally use to hang extra per-player wiring off
    // add/remove/reconnect (e.g. registering with an external advancement-packet system) ---

    protected void onPlayerAdded(Player p) {
    }

    protected void onPlayerRemoved(Player p) {
    }

    protected void onPlayerReconnected(Player oldPlayer, Player newPlayer) {
    }

    // --- World lifecycle ---

    public void configWorld(World world) {
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        world.setPVP(false);
        world.setKeepSpawnInMemory(false);
    }

    public World getNetherWorld() {
        if (netherWorld == null) {
            WorldCreator wc = new WorldCreator(name + "_nether");
            wc.environment(World.Environment.NETHER);
            netherWorld = wc.createWorld();
            configWorld(netherWorld);
        }
        return netherWorld;
    }

    public boolean isNetherNotNull() {
        return netherWorld != null;
    }

    public World getEndWorld() {
        if (endWorld == null) {
            WorldCreator wc = new WorldCreator(name + "_the_end");
            wc.environment(World.Environment.THE_END);
            endWorld = wc.createWorld();
            configWorld(endWorld);
        }
        return endWorld;
    }

    public boolean isEndNotNull() {
        return endWorld != null;
    }

    public void deleteWorlds() {
        WorldManager.deleteWorld(world);
        if (netherWorld != null) {
            WorldManager.deleteWorld(netherWorld);
        }
        if (endWorld != null) {
            WorldManager.deleteWorld(endWorld);
        }
    }

    // --- Team lifecycle ---

    public T addTeam(String name, int colorCode) {
        T team = createTeam(name, colorCode);
        teams.add(team);
        return team;
    }

    public T getTeam(Material material) {
        for (T team : teams) {
            if (team.getWaitingTeam().getItem().getType().equals(material)) {
                return team;
            }
        }
        return null;
    }

    public T getTeam(Player p) {
        for (T team : teams) {
            if (team.getPlayers().contains(p)) {
                return team;
            }
        }
        return null;
    }

    public List<T> getTeams() {
        return teams;
    }

    // --- Player lifecycle ---

    public void addPlayer(Player p) {
        defaultTeam.addPlayer(p);
        p.displayName(Component.text(p.getName(), NamedTextColor.GRAY));
        p.setScoreboard(board);
        p.sendPlayerListFooter(getPlayerListFooter());
        players.add(p);
        gameState.addPlayer(p);
        onPlayerAdded(p);

        for (Player p2 : Bukkit.getOnlinePlayers()) {
            // Show this game's players, hide the rest
            if (players.contains(p2)) {
                p.showPlayer(plugin, p2);
                p2.showPlayer(plugin, p);
            } else {
                p.hidePlayer(plugin, p2);
                p2.hidePlayer(plugin, p);
            }
        }
    }

    // Remove a single player from this game (self-service leave). Ends the game entirely if it was the last player.
    public void removePlayer(Player p) {
        T team = getTeam(p);
        if (team != null) {
            team.removePlayer(p);
        }
        defaultTeam.removePlayer(p);
        players.remove(p);

        List<NamespacedKey> recipeNames = new ArrayList<>();
        Bukkit.recipeIterator().forEachRemaining(r -> recipeNames.add(((Keyed) r).getKey()));
        p.undiscoverRecipes(recipeNames);

        restoreLobbyDefaults(p);
        p.teleport(Bukkit.getWorld("world").getSpawnLocation());
        p.setGameMode(GameMode.SURVIVAL);
        p.clearActivePotionEffects();
        p.getInventory().clear();
        onPlayerRemoved(p);

        for (Player p2 : Bukkit.getOnlinePlayers()) {
            if (!inAnyGame(p2)) {
                p2.showPlayer(plugin, p);
                p.showPlayer(plugin, p2);
            } else {
                p.hidePlayer(plugin, p2);
                p2.hidePlayer(plugin, p);
            }
        }

        // No players left, nothing more to play for
        if (players.isEmpty()) {
            gameState.abort();
        }
    }

    // Re-attaches a returning player (fresh Player object after a relog) to their still-running game.
    // Their world/position/inventory are already restored by vanilla; this just re-syncs our own bookkeeping.
    public void reconnectPlayer(Player newPlayer) {
        Player oldPlayer = null;
        for (Player existing : players) {
            if (existing.getUniqueId().equals(newPlayer.getUniqueId())) {
                oldPlayer = existing;
                break;
            }
        }
        if (oldPlayer == null) {
            return;
        }

        players.set(players.indexOf(oldPlayer), newPlayer);

        T team = null;
        for (T t : teams) {
            int idx = t.getPlayers().indexOf(oldPlayer);
            if (idx != -1) {
                t.getPlayers().set(idx, newPlayer);
                team = t;
                break;
            }
        }

        gameState.onPlayerReconnect(oldPlayer, newPlayer);

        newPlayer.setScoreboard(board);
        if (team != null) {
            newPlayer.displayName(Component.text(newPlayer.getName(), net.kyori.adventure.text.format.TextColor.color(team.getColorCode())));
        } else {
            newPlayer.displayName(Component.text(newPlayer.getName(), NamedTextColor.GRAY));
        }
        newPlayer.sendPlayerListFooter(getPlayerListFooter());

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (players.contains(other)) {
                newPlayer.showPlayer(plugin, other);
                other.showPlayer(plugin, newPlayer);
            } else {
                newPlayer.hidePlayer(plugin, other);
                other.hidePlayer(plugin, newPlayer);
            }
        }

        onPlayerReconnected(oldPlayer, newPlayer);
    }

    // Resets one player back to "in the shared lobby" state: undiscover this game's recipes,
    // restore lobby defaults, teleport out, clear state, and re-sync visibility against every
    // other online player. Used whenever this player's involvement with the game is fully over
    // (teardownAndEnd, and a finished-state teardown) - as opposed to removePlayer(), which also
    // has to actively hide this player from the game's still-running teammates.
    public void resetPlayerToLobby(Player p, List<NamespacedKey> recipeNames) {
        p.undiscoverRecipes(recipeNames);
        restoreLobbyDefaults(p);
        p.teleport(Bukkit.getWorld("world").getSpawnLocation());
        p.setGameMode(GameMode.SURVIVAL);
        p.clearActivePotionEffects();
        p.getInventory().clear();
        onPlayerRemoved(p);

        for (Player p2 : Bukkit.getOnlinePlayers()) {
            if (!inAnyGame(p2)) {
                p2.showPlayer(plugin, p);
                p.showPlayer(plugin, p2);
            }
        }
    }

    // Shared teardown used when a game is force-ended (abort) rather than reaching its natural finish
    public void teardownAndEnd() {
        List<NamespacedKey> recipeNames = new ArrayList<>();
        Bukkit.recipeIterator().forEachRemaining(r -> recipeNames.add(((Keyed) r).getKey()));

        for (Player p : players) {
            resetPlayerToLobby(p, recipeNames);
        }

        for (Team t : board.getTeams()) {
            t.unregister();
        }

        deleteWorlds();
        endGame();
    }

    public void sendChat(Component comp) {
        for (Player p : players) {
            p.sendMessage(comp);
        }
    }

    public void endGame() {
        Bukkit.getLogger().info("Ended " + name);
        ALL.remove(this);
    }

    // Getters and Setters
    public List<Player> getPlayers() {
        return players;
    }

    public String getName() {
        return name;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public GameState<?> getGameState() {
        return gameState;
    }

    public void setGameState(GameState<?> gameState) {
        this.gameState = gameState;
    }

    public Scoreboard getBoard() {
        return board;
    }

    public World getWorld() {
        return world;
    }

    public Team getDefaultTeam() {
        return defaultTeam;
    }
}
