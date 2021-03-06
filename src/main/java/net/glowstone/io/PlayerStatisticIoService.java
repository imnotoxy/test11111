package net.glowstone.io;

import net.glowstone.GlowServer;
import net.glowstone.entity.GlowPlayer;
import net.glowstone.util.StatisticMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class PlayerStatisticIoService {

    private GlowServer server;
    private File statsDir;

    public PlayerStatisticIoService(GlowServer server, File statsDir) {
        this.server = server;
        this.statsDir = statsDir;
    }

    /**
     * Gets the statistics file for the given UUID
     *
     * @param uuid the UUID of the player
     * @return the statistics file of the given UUID
     */
    private File getPlayerFile(UUID uuid) {
        if (!statsDir.isDirectory() && !statsDir.mkdirs()) {
            server.getLogger().warning("Failed to create directory: " + statsDir);
        }
        return new File(statsDir, uuid + ".json");
    }

    /**
     * Reads the stats of a player from its statistics file and writes the values to the StatisticMap.
     *
     * @param player the player to read the statistics from
     */
    public void readStats(GlowPlayer player) {
        File statsFile = getPlayerFile(player.getUniqueId());
        player.getStatisticMap().getValues().clear();
        if (statsFile.exists()) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(new FileReader(statsFile));
                for (Object o : json.keySet()) {
                    String key = (String) o;
                    Long longValue = null;
                    if (json.get(o) instanceof Long) {
                        longValue = (Long) json.get(o);
                    } else if (json.get(o) instanceof JSONObject) {
                        JSONObject object = (JSONObject) json.get(o);
                        if (object.containsKey("value")) {
                            longValue = (Long) object.get("value");
                        }
                    } else {
                        GlowServer.logger.warning("Unknown statistic type for '" + key + "': " + json.get(o) + " (" + json.get(o).getClass().getSimpleName() + ")");
                    }
                    if (longValue != null) {
                        player.getStatisticMap().getValues().put(key, longValue.intValue());
                    }
                }
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Writes the statistics of the player into its statistics file.
     *
     * @param player the player to write the statistics file from
     */
    public void writeStats(GlowPlayer player) {
        File file = getPlayerFile(player.getUniqueId());
        StatisticMap map = player.getStatisticMap();
        JSONObject json = new JSONObject(map.getValues());
        try {
            FileWriter writer = new FileWriter(file, false);
            writer.write(json.toJSONString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
