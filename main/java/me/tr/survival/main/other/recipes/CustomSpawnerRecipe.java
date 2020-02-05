package me.tr.survival.main.other.recipes;

import me.tr.survival.main.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CustomSpawnerRecipe extends Recipe {

    public CustomSpawnerRecipe() {
        super(new ItemStack(Material.SPAWNER));
    }

    @Override
    public String[] getShape() {
        return new String[] {
                "IBI",
                "BOB",
                "IBI"
        };
    }

    @Override
    public Map<String, ItemStack> getIngredients() {
        Map<String, ItemStack> map = new HashMap<>();

        map.put("I", ItemUtil.makeItem(Material.IRON_BLOCK));
        map.put("O", ItemUtil.makeItem(Material.OBSIDIAN));
        map.put("B", ItemUtil.makeItem(Material.IRON_BARS));

        return map;
    }

}
