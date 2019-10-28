package me.tr.survival.main.util.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.tr.survival.main.util.callback.TypedCallback;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Gui implements Listener {

    private HashMap<Integer, Inventory> pages;
    private HashMap<Integer, HashMap<Integer, ItemStack>> items;
    public static List<Gui> guis = new ArrayList<>();
    private HashMap<Player, Integer> playerPages = new HashMap<>();
    private List<Button> buttons = new ArrayList<>();
    private String title;
    private int size;

    public Gui(String title, int size){

        if(!(guis.contains(this))){
            guis.add(this);
        }
        this.pages = new HashMap<>();
        this.items = new HashMap<>();
        this.title = title;
        this.size = size;
        init();
    }

    public HashMap<Integer, Inventory> getPages() {
        return pages;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public void init(){
        createPage(false);
    }

    public ItemStack getItem(int pos) {

        HashMap<Integer, ItemStack> items = this.items.get(1);

        if(!items.isEmpty() && items.containsKey(pos)) {
            return items.get(pos);
        }

        return null;

    }

    public int nextEmptySlot() {
        ItemStack[] items = getPages().get(1).getContents();

        for(int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if(item == null) return i;
        }

        return 0;

    }

    public void addButton(Button button){
        buttons.add(button);
        //button.inv.setItem(button.pos, button.item);
    }

    public Inventory createPage(boolean addPageButtons){

        if(getPages() == null || getPages().isEmpty()){
            return createPage(1, false);
        } else {
            return createPage(getPages().size() + 1, addPageButtons);
        }
    }

    private Inventory createPage(int page, boolean addPageButtons){
        Inventory inv = Bukkit.createInventory(null, getSize(), getTitle());
        getPages().put(page, inv);

        if(addPageButtons){
            addPageButtons(inv);
        }

        return inv;
    }

    public void addPageButtons(Inventory inv){

        ItemStack nextPageItem = new ItemStack(Material.PAPER);
        ItemMeta nextMeta = nextPageItem.getItemMeta();
        nextMeta.setDisplayName("§7Seuraava sivu");
        nextPageItem.setItemMeta(nextMeta);

        ItemStack prevPageItem = new ItemStack(Material.PAPER);
        ItemMeta prevMeta = nextPageItem.getItemMeta();
        prevMeta.setDisplayName("§7Viimeinen sivu");
        prevPageItem.setItemMeta(prevMeta);

        addButton(new Button(inv, inv.getSize() - 1, nextPageItem){
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                nextPage(clicker);
            }
        });
        addButton(new Button(inv, inv.getSize() - 9, prevPageItem){
            @Override
            public void onClick(Player clicker, ClickType clickType) {
                previousPage(clicker);
            }
        });
    }

    public void addPageButtons(int page){

        if(getPages() == null || getPages().isEmpty() || getPages().get(page) == null){
            throw new IllegalArgumentException("You must have at least 1 page in your gui!");
        }

        Inventory inv = getPages().get(page);
        addPageButtons(inv);

    }

    public void removePage(int page){

        if(getPages() == null || getPages().isEmpty() || getPages().get(page) == null){
            throw new IllegalArgumentException("You don't have any pages in your inventory or the page you're removing doesn't exist!");
        }

        getPages().remove(page);

    }

    public void removeAllPages(){
        getPages().clear();
    }

    public Inventory open(Player player){

        if(getPages() == null || getPages().isEmpty()){
            throw new IllegalArgumentException("You must have at least 1 page in your gui!");
        }

        Inventory inv = Bukkit.createInventory(null, this.size, this.title + "§r");
        if(this.items.isEmpty() && this.buttons.isEmpty()){
            player.openInventory(inv);
            return inv;
        }

        HashMap<Integer, ItemStack> items = this.items.get(1);
        if(!this.items.isEmpty()) {
            for(Map.Entry<Integer, ItemStack> e : items.entrySet()) {
                inv.setItem(e.getKey(), e.getValue());
            }
        }

        if(!this.buttons.isEmpty()) {
            for(Button b : this.getButtons()) {
                inv.setItem(b.pos, b.item);
            }
        }

        playerPages.put(player, 1);
        player.openInventory(inv);
        return inv;
    }

    public void addItem(int page, ItemStack item, int pos) {

        if(getPages() == null || getPages().isEmpty()){
            throw new IllegalArgumentException("You must have at least 1 page in your gui!");
        }


        if(!this.items.containsKey(page)){
            HashMap<Integer, HashMap<Integer, ItemStack>> list = new HashMap<>();
            HashMap<Integer, ItemStack> items = new HashMap<>();
            items.put(pos, item);
            list.put(page, items);
            this.items.put(page, items);
        } else {
            HashMap<Integer, ItemStack> items = this.items.get(page);
            items.put(pos, item);
            this.items.replace(page, items);
        }

    }

    public Inventory openPage(Player player, int page){

        if(getPages() == null || getPages().isEmpty() || getPages().get(page) == null){
            throw new IllegalArgumentException("You must have at least 1 page in your gui!");
        }

        Inventory inv = getPages().get(page);
        player.openInventory(inv);
        playerPages.put(player, page);
        return inv;

    }

    public void nextPage(Player player){

        if(getPages() == null || getPages().isEmpty()){
            throw new IllegalArgumentException("You must have at least 1 page in your gui!");
        }

        if(playerPages.containsKey(player)){

            int currentPage = playerPages.get(player);
            if(getPages().size() >= currentPage + 1){
                Inventory nextPage = getPages().get(currentPage + 1);
                player.openInventory(nextPage);
                playerPages.put(player, currentPage + 1);
            }

        }

    }

    public void previousPage(Player player){
        if(getPages() == null || getPages().isEmpty()){
            throw new IllegalArgumentException("You must have at least 1 page in your gui!");
        }

        if(playerPages.containsKey(player)){

            int currentPage = playerPages.get(player);
            if(currentPage - 1 >= 1){
                Inventory nextPage = getPages().get(currentPage - 1);
                player.openInventory(nextPage);
                playerPages.put(player, currentPage - 1);
            }

        }
    }

    public Button getButton(int pos) {
        for(Button b : getButtons()) {
            if(b.pos == pos) return b;
        }
        return null;
    }

    public int getPage(Player player){
        if(playerPages.containsKey(player)){
            return playerPages.get(player);
        }
        else {
            return 0;
        }
    }

    public void close(Player player) {
        if(getPlayerPages().containsKey(player)) {
            getPlayerPages().remove(player);
        }
        player.closeInventory();
    }

    public List<Button> getButtons(){
        return buttons;
    }

    public HashMap<Player, Integer> getPlayerPages(){
        return playerPages;
    }

    public static Gui getGui(Player player){
        for(Gui gui : Gui.guis){

            if(gui.getPlayerPages().containsKey(player)){
                return gui;
            }

        }
        return null;
    }

    public static Gui openGui(Player player, String title, int size, TypedCallback<Gui> cb) {
        Gui gui = new Gui(title, size);
        cb.execute(gui);
        gui.open(player);
        return gui;
    }

}