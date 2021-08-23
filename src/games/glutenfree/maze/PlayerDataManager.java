package games.glutenfree.maze;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerDataManager {

    private HashMap<UUID, PlayerData> data;
    public PlayerDataManager(){
        data = new HashMap<>();
    }

    public PlayerData createDataFor(Player player){
        PlayerData playerData = new PlayerData();
        data.put(player.getUniqueId(), playerData);
        return playerData;
    }

    public PlayerData get(Player player){
        return data.get(player.getUniqueId());
    }

    public void reset(){
        data.clear();
    }

}
