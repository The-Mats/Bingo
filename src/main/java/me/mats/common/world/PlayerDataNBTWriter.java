package me.mats.common.world;

import de.tr7zw.nbtapi.NBTCompound;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class PlayerDataNBTWriter {

    public static void writePlayerData(NBTCompound comp, Player p) {
        comp.setFloat("DataVersion", 0.1f);
        comp.setDouble("PosX", p.getLocation().getX());
        comp.setDouble("PosY", p.getLocation().getY());
        comp.setDouble("PosZ", p.getLocation().getZ());
        comp.setItemStackArray("EnderItems", p.getEnderChest().getContents());
        comp.setItemStackArray("Inventory", p.getInventory().getContents());
        comp.setFloat("FoodExhaustionLevel", p.getExhaustion());
        comp.setInteger("FoodLevel", p.getFoodLevel());
        comp.setFloat("FoodSaturationLevel", p.getSaturation());

        GameMode mode = p.getGameMode();
        switch (mode) {
            case SURVIVAL -> comp.setInteger("PlayerGameType", 0);
            case CREATIVE -> comp.setInteger("PlayerGameType", 1);
            case ADVENTURE -> comp.setInteger("PlayerGameType", 2);
            case SPECTATOR -> comp.setInteger("PlayerGameType", 3);
        }
        comp.setDouble("Health", p.getHealth());
        comp.setInteger("SelectedItemSlot", p.getInventory().getHeldItemSlot());
        comp.setFloat("XpProgress", p.getExp());
        comp.setInteger("XpLevel", p.getLevel());

        comp.setInteger("EnchantmentSeed", p.getEnchantmentSeed());
    }

    public static void readPlayerData(NBTCompound comp, Player p) {
        p.getLocation().setX(comp.getDouble("PosX"));
        p.getLocation().setX(comp.getDouble("PosY"));
        p.getLocation().setX(comp.getDouble("PosZ"));
        p.getEnderChest().setContents(comp.getItemStackArray("EnderItems"));
        p.getInventory().setContents(comp.getItemStackArray("Inventory"));
        p.setExhaustion(comp.getFloat("FoodExhaustionLevel"));
        p.setFoodLevel(comp.getInteger("FoodLevel"));
        p.setSaturation(comp.getFloat("FoodSaturationLevel"));


        int mode = comp.getInteger("PlayerGameType");
        switch (mode) {
            case 0 -> p.setGameMode(GameMode.SURVIVAL);
            case 1 -> p.setGameMode(GameMode.CREATIVE);
            case 2 -> p.setGameMode(GameMode.ADVENTURE);
            case 3 -> p.setGameMode(GameMode.SPECTATOR);
        }
        p.setHealth(comp.getDouble("Health"));
        p.getInventory().setHeldItemSlot(comp.getInteger("SelectedItemSlot"));
        p.setExp(comp.getFloat("XpProgress"));
        p.setLevel(comp.getInteger("XpLevel"));
        p.setEnchantmentSeed(comp.getInteger("EnchantmentSeed"));
    }

}


