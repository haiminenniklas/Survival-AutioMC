package me.tr.survival.main.util;

import org.bukkit.WeatherType;

public class Weathers {

    public static WeatherType SUNNY = WeatherType.CLEAR, RAINY = WeatherType.DOWNFALL;

    public static WeatherType getWeatherType(String name) {
        switch(name.toLowerCase()) {
            case "sateinen":
                return WeatherType.DOWNFALL;
            case "selkeä":
                return WeatherType.CLEAR;
            default:
                return null;
        }
    }

}
