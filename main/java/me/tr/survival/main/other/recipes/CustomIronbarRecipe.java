package me.tr.survival.main.other.recipes;

import me.tr.survival.main.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CustomIronbarRecipe extends Recipe {

    public CustomIronbarRecipe() {
        super(new ItemStack(Material.IRON_BARS, 16));
    }

    @Override
    public String[] getShape() {
        return new String[] {
                "III",
                "III"
        };
    }

    @Override
    public Map<String, ItemStack> getIngredients() {
        Map<String, ItemStack> map = new HashMap<>();

        map.put("I", ItemUtil.makeItem(Material.IRON_BLOCK));

        return map;
    }
}
