package games.glutenfree.commands;

import games.glutenfree.MonsterMaze;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaceIndicatorCommand implements CommandExecutor {

    private MonsterMaze game;
    public PlaceIndicatorCommand(MonsterMaze game){
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;
        Location blitLocation = player.getLocation();
        Bukkit.broadcastMessage("Placing indicator...");
        game.getIndicatorTemplate().blitTo(blitLocation);
        Bukkit.getScheduler().scheduleSyncDelayedTask(game, () -> {
            game.getIndicatorTemplate().clear(blitLocation);
        }, 500);
        return true;
    }

}
