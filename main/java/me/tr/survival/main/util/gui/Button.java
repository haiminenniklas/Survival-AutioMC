package me.tr.survival.main.util.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class Button {

    public int pos;
    @Deprecated public Inventory inv;
    public ItemStack item;
    private int page;

    @Deprecated
    public Button(Inventory inv, int pos, ItemStack item){
        this.inv = inv;
        this.pos = pos;
        this.item = item;
        this.page = 1;
    }

    public Button(int page, int pos, ItemStack item) {
        this.page = page;
        this.pos = pos;
        this.item = item;
    }

    public abstract void onClick(Player clicker, ClickType clickType);

}

