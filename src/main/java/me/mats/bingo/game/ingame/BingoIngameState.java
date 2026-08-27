package me.mats.bingo.game.ingame;


import me.mats.advancementinteraction.TeamAdvancements;
import me.mats.common.enums.Color;
import me.mats.bingo.game.BingoManager;
import me.mats.bingo.game.BingoTeam;
import me.mats.bingo.game.finished.BingoFinishedState;
import me.mats.bingo.message.BingoMessage;
import me.mats.bingo.message.BingoMessages;
import me.mats.common.message.Message;
import me.mats.common.message.MessageBuilder;
import me.mats.bingo.game.ingame.abilities.BingoAbilities;
import me.mats.bingo.game.ingame.abilities.BingoAbilitiesListener;
import me.mats.bingo.game.ingame.abilities.BingoAbilitiesMenuListener;
import me.mats.bingo.game.ingame.spawn.BingoSpawnCountdown;
import me.mats.bingo.game.ingame.spawn.BingoSpawnListener;
import me.mats.bingo.game.ingame.spawn.SpawnProtectionListener;
import me.mats.common.game.ingame.BackpackListener;
import me.mats.common.game.ingame.IngameState;
import me.mats.common.game.ingame.ItemLists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class BingoIngameState extends IngameState<BingoManager> {

    private Map<Material, BingoItem> bingoItemsMap = null;
    // This is needed for Gapper Ability
    private Map<BingoItem, Material> reverseMap = null;

    // These are not changeable at this point
    private final int size;

    private final BingoCollectListener bingoCollectListener;
    private final BackpackListener backpackListener;
    private final BingoSpawnListener spawnListener;
    private final SpawnProtectionListener spawnProtectionListener;
    private final BingoAbilitiesMenuListener abilitiesMenuListener;
    private final BingoAbilitiesListener abilitiesListener;
    private final int extraAbilityPoints;
    private final int spawnTime;

    // To later send final Advancement Screen
    private List<ItemStack> fieldItems;
    private float[][] fieldPositions;

    private BingoSpawnCountdown spawnCountdown;

    // Elapsed-time action bar, shown for the whole ingame phase
    private int timerTaskId = -1;
    private long elapsedSeconds = 0;
    private float gradientPhase = 0F;


    public BingoIngameState(BingoManager manager, int size, ItemLists.ListType setting, int extraAbilityPoints, int spawnTime){
        super.manager = manager;
        this.size = size;
        this.setting = setting;
        this.abilities = new BingoAbilities();
        this.extraAbilityPoints = extraAbilityPoints;
        this.spawnTime = spawnTime;

        this.bingoCollectListener = new BingoCollectListener(this);
        this.backpackListener = new BackpackListener(manager);
        this.spawnListener = new BingoSpawnListener(this);
        this.spawnProtectionListener = new SpawnProtectionListener(this);
        this.abilitiesMenuListener = new BingoAbilitiesMenuListener(this);
        this.abilitiesListener = new BingoAbilitiesListener(this);
        // Worldstuff here


    }

    public void newBingoField() {
        // Check if a Bingo Field has already been generated and if yes remove it
        List<String> removeAdvancements = getOldAdvancements();
        TeamAdvancements.revokeAdvancements(manager.getPlayers(), "bingo", removeAdvancements);

        int size2 = size*size;
        // Get the itemsCh
        bingoItemsMap = ItemLists.getRandoms(setting, size2, BingoItem::new);
        Material[] materialsList = bingoItemsMap.keySet().toArray(Material[]::new);

        // Generate field
        float[][] positions = new float[size2][2];

        boolean[][]  bingoField = new boolean[size][size];


        int n = 0;
        float x = 0;
        for (int i = 0; i < size; i++) {
            float y = 0;
            for (int j = 0; j < size; j++) {
                BingoItem item = bingoItemsMap.get(materialsList[n]);
                // Set bingoField to false
                bingoField[i][j] = false;
                // Set the position
                item.setPosition(new int[]{i,j});
                // Advancement Position
                positions[n] = new float[]{x, y};
                y += 0.9;
                n += 1;
            }
            x += 0.9;
        }

        // Add Field per Team (Deep Copy)
        for (BingoTeam team : manager.getTeams()) {
            boolean[][] copiedField = new boolean[bingoField.length][];
            for (int i = 0; i < bingoField.length; i++) {
                copiedField[i] = bingoField[i].clone();
            }
            team.setBingoField(copiedField);
        }

        // Generate ItemStacks
        List<ItemStack> items = new ArrayList<>(size2);
        for (Material material : materialsList) {
            ItemStack item = new ItemStack(material);
            if (material == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                ItemLists.getRandomEnchant(meta);

                item.setItemMeta(meta);
                bingoItemsMap.get(material).setMeta(meta);

                Bukkit.getLogger().info(meta.getAsString());

            } else if (material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION || material == Material.TIPPED_ARROW) {
                PotionMeta meta = (PotionMeta) item.getItemMeta();
                ItemLists.getRandomPotion(setting, meta);

                Bukkit.getLogger().info(meta.getAsString());

                item.setItemMeta(meta);
                bingoItemsMap.get(material).setMeta(meta);

                Bukkit.getLogger().info("Potion");
            }
            items.add(item);
        }

        // To later send final Advancement Screen
        fieldItems = items;
        fieldPositions = positions;
        // Send Packets
        for (BingoTeam bTeam : manager.getTeams()) {
            bTeam.getAdvancement().sendAdvancements(bTeam.getPlayers(), items, positions, removeAdvancements);
        }

        // Create reversed Map
        reverseMap = new HashMap<>();
        for (Map.Entry<Material, BingoItem> entry : bingoItemsMap.entrySet()) {
            reverseMap.put(entry.getValue(), entry.getKey());
        }
    }

    private List<String> getOldAdvancements() {
        // Check if a Bingo Field has already been generated and if yes remove it
        List<String> removeAdvancements = new ArrayList<>();
        if (bingoItemsMap != null) {
            Material[] oldMaterialsList = bingoItemsMap.keySet().toArray(Material[]::new);
            for (Material m : oldMaterialsList) {
                removeAdvancements.add(m.toString().toLowerCase());
            }
        }
        return removeAdvancements;
    }

    public void validateNewBingoPosition(int[] position) {
        boolean winnerExists = false;
        for (BingoTeam b : manager.getTeams()) {
            if (!b.getPlayers().isEmpty() && getAbilities().getThiefAbilityList().contains(b.getPlayers().get(0))) {
                boolean isWinner = b.hasWonWithNewPosition(position);
                if (isWinner) {
                    winnerExists = true;
                }

            }
        }
        if (winnerExists)
            stop();
    }

    public Material getMaterialFromPosition(int[] position) {
        for (BingoItem b : bingoItemsMap.values()) {
            if (Arrays.equals(b.getPosition(), position)) {
                return reverseMap.get(b);
            }
        }
        // Should never happen
        return null;
    }

    // Abilities are keyed off Player object identity, so a relog (fresh Player instance)
    // needs its entries swapped over explicitly. Also resends the advancement-grid UI the
    // relog wiped from their client.
    @Override
    public void onPlayerReconnect(Player oldPlayer, Player newPlayer) {
        getAbilities().replacePlayer(oldPlayer, newPlayer);

        BingoTeam team = manager.getTeam(newPlayer);
        if (team == null || bingoItemsMap == null) {
            return;
        }

        team.getAdvancement().sendRootAdvancement(newPlayer);
        team.getAdvancement().sendAdvancements(List.of(newPlayer), fieldItems, fieldPositions, new ArrayList<>());

        // Re-mark whatever this team already collected as done, since sendAdvancements alone only recreates the grid
        boolean[][] field = team.getBingoField();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (field[i][j]) {
                    String advancementName = getMaterialFromPosition(new int[]{i, j}).toString().toLowerCase();
                    TeamAdvancements.grantAdvancement(List.of(newPlayer), "bingo", advancementName);
                }
            }
        }
    }


    @Override
    public void start() {
        newBingoField();

        // Register required Listeners for Spawn
        Bukkit.getPluginManager().registerEvents(spawnListener, manager.getPlugin());
        Bukkit.getPluginManager().registerEvents(spawnProtectionListener, manager.getPlugin());
        Bukkit.getPluginManager().registerEvents(abilitiesMenuListener, manager.getPlugin());

        ItemStack abilitiesItem = new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        ItemMeta meta = abilitiesItem.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        meta.displayName(Component.text("Abilities", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        abilitiesItem.setItemMeta(meta);

        ItemStack crafting = new ItemStack(Material.CRAFTING_TABLE);
        meta = crafting.getItemMeta();
        meta.displayName(Component.text("Crafting Recipes", TextColor.color(0xFCCB00)).decoration(TextDecoration.ITALIC, false));
        crafting.setItemMeta(meta);

        ItemStack furnace = new ItemStack(Material.FURNACE);
        meta = furnace.getItemMeta();
        meta.displayName(Component.text("Smelting Recipes", TextColor.color(0xFCCB00)).decoration(TextDecoration.ITALIC, false));
        furnace.setItemMeta(meta);

        List<NamespacedKey> recipeNames = new ArrayList<>();
        Bukkit.recipeIterator().forEachRemaining(r -> recipeNames.add(((Keyed) r).getKey()));

        for (Player p : manager.getPlayers()) {
            Team playerTeam = manager.getBoard().getPlayerTeam(p);
            p.playerListName(playerTeam.prefix().append(Component.text(p.getName(), playerTeam.color())).append(Component.text(" ")).append(Message.O_BRACKET.getComponent()).append(Component.text("O", Color.OVERWORLD.getTextColor())).append(Message.C_BRACKET.getComponent()));
            p.getInventory().setItem(8, abilitiesItem);
            p.getInventory().setItem(0, crafting);
            p.getInventory().setItem(1, furnace);

            // Give Player all recipes
            p.discoverRecipes(recipeNames);
        }
        spawnCountdown = new BingoSpawnCountdown(this);
        spawnCountdown.start(spawnTime);
    }

    // Starts the elapsed-time action bar. Called by SpawnCountdown once it releases players ("Go!"),
    // not from start(), since the timer should track actual playtime, not the pre-game countdown.
    public void startTimer() {
        timerTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(manager.getPlugin(), this::tickTimer, 0, 20);
    }

    // Elapsed-time action bar, cycling through the bingo logo's blue-cyan palette as one solid color
    private void tickTimer() {
        Component comp = MessageBuilder.pulse(formatElapsedTime(elapsedSeconds), BingoMessages.BINGO_GRADIENT, gradientPhase);
        for (Player p : manager.getPlayers()) {
            p.sendActionBar(comp);
        }
        elapsedSeconds++;
        gradientPhase = (gradientPhase + 0.03F) % 1F;
    }

    private String formatElapsedTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    @Override
    public void abort() {
        if (spawnCountdown != null) {
            spawnCountdown.cancel();
        }
        Bukkit.getScheduler().cancelTask(timerTaskId);

        HandlerList.unregisterAll(bingoCollectListener);
        HandlerList.unregisterAll(backpackListener);
        HandlerList.unregisterAll(spawnListener);
        HandlerList.unregisterAll(spawnProtectionListener);
        HandlerList.unregisterAll(abilitiesListener);
        HandlerList.unregisterAll(abilitiesMenuListener);

        manager.teardownAndEnd();
    }

    @Override
    public void stop() {
        // 1. Determine Winner
        List<BingoTeam> winners = new ArrayList<>();
        for (BingoTeam b : manager.getTeams()) {
            if (b.isWinner())
                winners.add(b);
        }
        BingoTeam finalWinner = null;
        if (winners.size() == 1) {
            finalWinner = winners.get(0);
            for (Player p : manager.getPlayers()) {
                p.sendMessage(BingoMessages.bingo(Component.text("Team "+finalWinner.getName(), TextColor.color(finalWinner.getColorCode())).append(Component.text(" got a ", Color.STD_COLOR.getTextColor()).append(BingoMessage.BINGO.getComponent()))));            }
        } else {
            int maxCollectedBingoItems = 0;
            for (BingoTeam b : winners) {
                if (b.getCollectedBingoItems() > maxCollectedBingoItems) {
                    finalWinner = b;
                    maxCollectedBingoItems = b.getCollectedBingoItems();

                }
            }
            for (Player p : manager.getPlayers()) {
                p.sendMessage(BingoMessages.bingo(Component.text("Multiple Teams got a ", Color.STD_COLOR.getTextColor()).append(BingoMessage.BINGO.getComponent()).append(MessageBuilder.buildMsg(List.of(" but ", "Team "+finalWinner.getName(), " has ", Integer.toString(maxCollectedBingoItems), " Bingo Advancements"), List.of(Color.STD_COLOR.getColorCode(), finalWinner.getColorCode(), Color.STD_COLOR.getColorCode(), NamedTextColor.YELLOW.value(), Color.STD_COLOR.getColorCode())))));
            }
        }

        // 2. Get Winner Advancements and send them with entire new field without toasts
        List<String> winField = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (finalWinner.getBingoField()[i][j]) {
                    winField.add(getMaterialFromPosition(new int[]{i,j}).toString().toLowerCase());
                }
            }
        }

        finalWinner.getAdvancement().sendFinalField(manager.getPlayers(), getOldAdvancements(), winField, fieldItems, fieldPositions);

        // Unregister ingame Listeners either way
        HandlerList.unregisterAll(bingoCollectListener);
        HandlerList.unregisterAll(backpackListener);
        HandlerList.unregisterAll(spawnListener);
        HandlerList.unregisterAll(spawnProtectionListener);
        HandlerList.unregisterAll(abilitiesListener);
        Bukkit.getScheduler().cancelTask(timerTaskId);

        // 3. Teleport into the win celebration - or, if no waiting/finished world could be
        // checked out, just end the game now. The winner announcement and advancement grid
        // above have already been sent either way.
        BingoFinishedState finishedState = new BingoFinishedState(manager, finalWinner, winField, fieldItems, fieldPositions);
        if (finishedState.getWaitingWorld() != null) {
            for (Player p : manager.getPlayers()) {
                p.setGameMode(GameMode.SURVIVAL);
                p.teleport(finishedState.getWaitingWorld().getWorld().getSpawnLocation());
                p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1F, 1F);
            }

            manager.setGameState(finishedState);
            manager.getGameState().start();
        } else {
            Bukkit.getLogger().warning("No waiting world available to show the win screen - ending " + manager.getName() + " immediately");
            manager.teardownAndEnd();
        }
    }

    @Override
    public void addPlayer(Player p) {
        p.teleport(manager.getWorld().getSpawnLocation());
        p.setGameMode(GameMode.SPECTATOR);
        p.sendMessage(BingoMessages.bingo("You are now spectating"));
    }

    public int getSize() {
        return size;
    }

    public Map<Material, BingoItem> getBingoItemsMap() {
        return bingoItemsMap;
    }

    public BackpackListener getBackpackListener() {
        return backpackListener;
    }

    public BingoSpawnListener getSpawnListener() {
        return spawnListener;
    }

    public BingoAbilitiesListener getAbilitiesListener() {
        return abilitiesListener;
    }

    public BingoCollectListener getBingoCollectListener() {
        return bingoCollectListener;
    }

    public BingoAbilitiesMenuListener getAbilitiesMenuListener() {
        return abilitiesMenuListener;
    }

    @Override
    public BingoAbilities getAbilities() {
        return (BingoAbilities) abilities;
    }

    public int getExtraAbilityPoints() {
        return extraAbilityPoints;
    }
}
