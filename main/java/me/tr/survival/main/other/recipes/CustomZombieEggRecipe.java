package me.tr.survival.main.other.recipes;

import me.tr.survival.main.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CustomZombieEggRecipe extends Recipe {


    public CustomZombieEggRecipe() {
        super(new ItemStack(Material.ZOMBIE_SPAWN_EGG));
    }

    @Override
    public String[] getShape() {
        return new String[] {
                "RRR",
                "RHR",
                "RRR"
        };
    }

    @Override
    public Map<String, ItemStack> getIngredients() {
        Map<String, ItemStack> map = new HashMap<>();

        map.put("R", ItemUtil.makeItem(Material.ROTTEN_FLESH, 64));
        map.put("H", ItemUtil.makeItem(Material.ZOMBIE_HEAD));

        return map;
    }
}
