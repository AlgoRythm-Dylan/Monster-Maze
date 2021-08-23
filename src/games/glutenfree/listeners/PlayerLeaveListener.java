package games.glutenfree.listeners;

import games.glutenfree.MonsterMaze;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {

    MonsterMaze game;
    public PlayerLeaveListener(MonsterMaze game){
        this.game = game;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        game.handlePlayerLeave(event.getPlayer());
    }

}
