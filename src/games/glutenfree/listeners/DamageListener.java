package games.glutenfree.listeners;

import games.glutenfree.MonsterMaze;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

    MonsterMaze game;
    public DamageListener(MonsterMaze game){
        this.game = game;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Firework)
            event.setCancelled(true);
    }

}
