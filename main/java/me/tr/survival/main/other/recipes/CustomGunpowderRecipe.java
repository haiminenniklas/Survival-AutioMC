package me.tr.survival.main.other.recipes;

import me.tr.survival.main.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CustomGunpowderRecipe extends Recipe {

    public CustomGunpowderRecipe() {
        super(new ItemStack(Material.GUNPOWDER));
    }

    @Override
    public String[] getShape() {
        return new String[] {
                "GCR"
        };
    }

    @Override
    public Map<String, ItemStack> getIngredients() {
        Map<String, ItemStack> map = new HashMap<>();

        map.put("G", ItemUtil.makeItem(Material.GLOWSTONE_DUST));
        map.put("C", ItemUtil.makeItem(Material.COAL));
        map.put("R", ItemUtil.makeItem(Material.REDSTONE));

        return map;
    }
}
