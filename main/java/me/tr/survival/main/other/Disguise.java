/*package me.tr.survival.main.other;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class Disguise {

    public static HashMap<UUID, String> skins = new HashMap<>();

    public static boolean changeSkin(Player player) {


     /*  String targetName = PLAYERS[new Random().nextInt(PLAYERS.length-1)];

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        GameProfile profile = ((CraftPlayer)player).getProfile();

        if(setSkin(profile, target.getUniqueId())) {

            // Change player's name

            try {
                Field ff = profile.getClass().getDeclaredField("name");
                ff.setAccessible(true);
                ff.set(profile, targetName);
            } catch(NoSuchFieldException | IllegalAccessException ex) {
                ex.printStackTrace();
                return false;
            }

            skins.put(player.getUniqueId(), targetName);
            return true;
        }
        return false;
     return false;
    }

    public static boolean setSkin(GameProfile profile, UUID uuid) {
        /*try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false", UUIDTypeAdapter.fromUUID(uuid))).openConnection();
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                String reply = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                String skin = reply.split("\"value\":\"")[1].split("\"")[0];
                String signature = reply.split("\"signature\":\"")[1].split("\"")[0];
                profile.getProperties().put("textures", new Property("textures", skin, signature));
                return true;
            } else {
                System.out.println("Connection could not be opened (Response code " + connection.getResponseCode() + ", " + connection.getResponseMessage() + ")");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static final String[] PLAYERS = new String[] {

            "Alejandro",
            "PAPA2506",
            "IamBlaise",
            "CapriSunDiego",
            "Accrot"

    };

}
*/