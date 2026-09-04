package me.mats.common.game.ingame.abilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

import static me.mats.common.message.MessageBuilder.roman;

// Raw material -> cooked result, derived once from Bukkit's registered furnace/smoker/blast
// furnace recipes instead of walking every recipe per dropped item.
public class AutoSmeltCache {

    // Fake "enchantment" lore marking a Pyro-crafted pickaxe. Anvils only expose renaming and
    // enchant merging to players, never lore edits, so this can't be forged through normal play.
    public static final Component AUTO_SMELT_LORE = roman("Auto Smelt", NamedTextColor.GRAY);

    private static final Map<Material, ItemStack> COOKING_RESULTS = build();

    // Null if the material has no cooking recipe.
    public static ItemStack get(Material material) {
        return COOKING_RESULTS.get(material);
    }

    public static boolean hasAutoSmelt(ItemStack tool) {
        ItemMeta meta = tool.getItemMeta();
        return meta != null && meta.hasLore() && meta.lore().contains(AUTO_SMELT_LORE);
    }

    private static Map<Material, ItemStack> build() {
        Map<Material, ItemStack> cache = new EnumMap<>(Material.class);
        Iterator<Recipe> recipeIterator = Bukkit.recipeIterator();
        while (recipeIterator.hasNext()) {
            Recipe recipe = recipeIterator.next();
            if (recipe instanceof CookingRecipe<?> cookingRecipe) {
                ItemStack result = cookingRecipe.getResult();
                RecipeChoice choice = cookingRecipe.getInputChoice();
                if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
                    for (Material mat : materialChoice.getChoices()) {
                        cache.putIfAbsent(mat, result);
                    }
                } else if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
                    for (ItemStack stack : exactChoice.getChoices()) {
                        cache.putIfAbsent(stack.getType(), result);
                    }
                }
            }
        }
        return cache;
    }
}
