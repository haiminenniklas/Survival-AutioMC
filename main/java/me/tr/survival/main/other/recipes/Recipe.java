package me.tr.survival.main.other.recipes;

import me.tr.survival.main.Main;
import me.tr.survival.main.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

import java.util.Map;

public abstract class Recipe {

    private ItemStack result;
    private ShapedRecipe recipe;

    public Recipe(ItemStack result) {
        this.result = result;

        this.recipe = new ShapedRecipe(this.getKey(), this.getResult());

        this.recipe.shape(getShape());

        for(Map.Entry<Character, ItemStack> entry : getIngredients().entrySet()) {
            MaterialData data = entry.getValue().getData();
            if(data != null) {
                this.recipe.setIngredient(entry.getKey(), entry.getValue().getData());
            }
        }

    }

    public ItemStack getResult() {
        return result;
    }

    public abstract String[] getShape();

    public abstract Map<Character, ItemStack> getIngredients();

    public NamespacedKey getKey() {
        return new NamespacedKey(Main.getInstance(), this.result.getType().name().toLowerCase());
    }

    public ShapedRecipe getShapedRecipe() {
        return this.recipe;
    }

    public static Recipe[] getRecipes() {
        return new Recipe[] {
                new CustomGunpowderRecipe(),
                new CustomIronbarRecipe(),
                new CustomSpawnerRecipe(),
                new CustomZombieEggRecipe(),
                //new CustomEndPearlRecipe()
        };
    }

    public static void load() {
        for(Recipe recipe : getRecipes()) {
            Bukkit.addRecipe(recipe.getShapedRecipe());
        }
    }

}
