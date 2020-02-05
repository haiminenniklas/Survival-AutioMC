package me.tr.survival.main.other.recipes;

import me.tr.survival.main.Main;
import me.tr.survival.main.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Map;

public abstract class Recipe {

    private ItemStack result;

    public Recipe(ItemStack result) {
        this.result = result;

        for(Map.Entry<String, ItemStack> entry : getIngredients().entrySet()) {
            getShapedRecipe().setIngredient(entry.getKey().charAt(0), entry.getValue());
        }

    }

    public ItemStack getResult() {
        return result;
    }

    public abstract String[] getShape();

    public abstract Map<String, ItemStack> getIngredients();

    public NamespacedKey getKey() {
        return new NamespacedKey(Main.getInstance(), this.result.getType().name().toLowerCase());
    }

    public ShapedRecipe getShapedRecipe() {
        return new ShapedRecipe(this.getKey(), this.getResult());
    }

    public static Recipe[] getRecipes() {
        return new Recipe[] {
                new CustomGunpowderRecipe(),
                new CustomIronbarRecipe(),
                new CustomSpawnerRecipe(),
                new CustomZombieEggRecipe()
        };
    }

    public static void load() {
        for(Recipe recipe : getRecipes()) {
            Bukkit.addRecipe(recipe.getShapedRecipe());
        }
    }

}
