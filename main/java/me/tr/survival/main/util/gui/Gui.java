package me.tr.survival.main.util.gui;

import java.util.*;

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
    private final Set<Button> buttons;
    private String title;
    private int size;

    private Inventory inv;

    private boolean partiallyTouchable;
    private int[] allowedSlots;

    public Gui(String title, int size){

        if(!(guis.contains(this))){
            guis.add(this);
        }
        this.pages = new HashMap<>();
        this.items = new HashMap<>();
        this.buttons = new HashSet<>();
        this.title = title;
        this.size = size;
        this.partiallyTouchable = false;
        this.allowedSlots = new int[0];

        this.inv = null;

        init();
    }

    public boolean isPartiallyTouchable() {
        return this.partiallyTouchable;
    }

    public void setPartiallyTouchable(boolean value) {
        this.partiallyTouchable = value;
    }

    public int[] getAllowedSlots() {
        return this.allowedSlots;
    }

    public void setAllowedSlots(int... slots) {
        this.allowedSlots = slots;
    }

    public boolean clickedAllowedSlot(int clickedSlot) {

        if(this.allowedSlots.length < 1) return false;

        for(int i = 0; i < this.allowedSlots.length; i++) {
            int allowedSlot = this.allowedSlots[i];
            if(clickedSlot == allowedSlot) {
                return true;
            }
        }

        return false;

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

        if(items != null) {
            if(!items.isEmpty() && items.containsKey(pos)) {
                return items.get(pos);
            }
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
        if(button != null) {
            if(button.item != null) {
                buttons.add(button);
            }
        }
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

    public void hardRefresh(Player player) {
        this.close(player);
        this.open(player);
    }

    public void refresh(Player player) {
        /*HashMap<Integer, ItemStack> items = this.items.get(1);
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
        player.updateInventory(); */
        this.hardRefresh(player);
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

        final long start = System.currentTimeMillis();
        System.out.println("[/Gui] Started opening a GUI...");

        System.out.println("[/Gui] Checking for pages...");
        if(getPages() == null || getPages().isEmpty()){
            throw new IllegalArgumentException("You must have at least 1 page in your gui!");
        }

        System.out.println("[/Gui] Creating an empty inventory...");
        this.inv = Bukkit.createInventory(null, this.size, this.title + "§r");
        if(this.items.isEmpty() && this.buttons.isEmpty()){
            player.openInventory(inv);
            return inv;
        }

        System.out.println("[/Gui] Looping given items and adding them...");
        final long itemLoopStart = System.currentTimeMillis();
        HashMap<Integer, ItemStack> items = this.items.get(1);
        if(!this.items.isEmpty()) {
            for(Map.Entry<Integer, ItemStack> e : items.entrySet()) {
                inv.setItem(e.getKey(), e.getValue());
            }
        }
        System.out.println("[/Gui] Item adding took in total " + (System.currentTimeMillis() - itemLoopStart) + "ms!");


        System.out.println("[/Gui] Looping given Buttons and adding them...");
        final long buttonLoopStart = System.currentTimeMillis();
        if(!this.buttons.isEmpty()) {
            for(Button b : this.getButtons()) {
                inv.setItem(b.pos, b.item);
            }
        }
        System.out.println("[/Gui] Button adding took in total " + (System.currentTimeMillis() - buttonLoopStart) + "ms!");

        playerPages.put(player, 1);

        System.out.println("[/Gui] Opening the Inventory...");
        player.openInventory(inv);

        System.out.println("[/Gui] Gui opening took in total " + (System.currentTimeMillis() - start) + "ms!");

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
            if(items.containsKey(pos)) {
                items.replace(pos, item);
            } else items.put(pos, item);
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

    public Set<Button> getButtons(){
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