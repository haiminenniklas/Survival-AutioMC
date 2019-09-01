package me.tr.survival.main.other;

import org.bukkit.enchantments.Enchantment;

public class Enchant {

    private Enchantment ench;
    private int level;

    public Enchant(Enchantment ench, int level){
        this.ench = ench;
        this.level = level;
    }

    public Enchantment getEnchantment(){
        return ench;
    }

    public int getLevel(){
        return level;
    }

}
