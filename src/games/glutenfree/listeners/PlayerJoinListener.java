package games.glutenfree.listeners;

import games.glutenfree.MonsterMaze;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    MonsterMaze game;
    public PlayerJoinListener(MonsterMaze game){
        this.game = game;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player player = e.getPlayer();
        game.resetPlayer(player);
        if(game.isRunning())
            game.makePlayerSpectator(e.getPlayer());
    }

}
