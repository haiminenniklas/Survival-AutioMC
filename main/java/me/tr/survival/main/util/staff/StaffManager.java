package me.tr.survival.main.util.staff;

import me.tr.survival.main.other.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaffManager implements Listener {

    private static Map<UUID, Map<Material, Integer>> blocksPerHour = new HashMap<>();

    public static int getBlockMinedPerHour(UUID uuid, Material mat) {

        if(blocksPerHour.containsKey(uuid)) {

            Map<Material, Integer> blockData = blocksPerHour.get(uuid);
            if(blockData.containsKey(mat)) {

                int minedTotal = blockData.get(mat);
                long hoursPlayed = (System.currentTimeMillis() - Util.getWhenLogged(uuid)) / 1000 / 60 / 60;

                return minedTotal / (int) hoursPlayed;

            }

        }

        return 0;

    }

    @EventHandler
    public void onMine(BlockBreakEvent e) {

        Player player = e.getPlayer();
        Block block = e.getBlock();
        UUID uuid = player.getUniqueId();

        if(Util.isMineralOre(block)) {

            if(!blocksPerHour.containsKey(uuid)) {
                Map<Material, Integer> map = new HashMap<>();
                map.put(block.getType(), 1);
                blocksPerHour.put(uuid, map);
            } else {

                Map<Material, Integer> map = blocksPerHour.get(uuid);
                if(map.containsKey(block.getType())) {

                    int current = map.get(block.getType());
                    map.put(block.getType(), current + 1);

                } else {
                    map.put(block.getType(), 1);
                }

            }

            int minedPerHour = getBlockMinedPerHour(uuid, block.getType());

            if(block.getType() == Material.DIAMOND_ORE) {

                if(minedPerHour >= 15 && minedPerHour % 5 == 0) {

                    Util.broadcastStaff("§6§lXRAY §7» Pelaajan §6" + player.getName() + " §7BPH §o(blockit per tunti) §btimanteille §7on §6" + minedPerHour + "§7!");

                }

            } else if(block.getType() == Material.EMERALD_ORE) {
                if(minedPerHour >= 5 && minedPerHour % 5 == 0) {

                    Util.broadcastStaff("§6§lXRAY §7» Pelaajan §6" + player.getName() + " §7BPH §o(blockit per tunti) §aemeraldeille §7on §6" + minedPerHour + "§7!");

                }
            }


        }


    }


}
