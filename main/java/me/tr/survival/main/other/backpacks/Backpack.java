package me.tr.survival.main.other.backpacks;

public class Backpack {

    enum Level {

        ONE(9, "§a§lLEVEL 1");

        private int size;
        private String displayName;

        Level(int size, String displayName) {

            this.size = size;
            this.displayName = displayName;

        }

    }

}
