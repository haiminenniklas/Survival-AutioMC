package me.tr.survival.main.other.recipes;

import me.tr.survival.main.other.TravelManager;
import me.tr.survival.main.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CustomEndPearlRecipe extends Recipe {

    public CustomEndPearlRecipe() {
        super(TravelManager.getPearlItem());
    }

    @Override
    public String[] getShape() {
        return new String[] {
                "DEG",
                "EPE",
                "GED"

        };
    }

    @Override
    public Map<Character, ItemStack> getIngredients() {
        Map<Character, ItemStack> map = new HashMap<>();

        map.put('G', ItemUtil.makeItem(Material.GOLD_BLOCK));
        map.put('D', ItemUtil.makeItem(Material.DIAMOND_BLOCK));
        map.put('E', ItemUtil.makeItem(Material.DIAMOND_BLOCK));
        map.put('P', ItemUtil.makeItem(Material.ENDER_PEARL));

        return map;
    }
}
