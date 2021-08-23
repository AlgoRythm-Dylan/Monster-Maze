package games.glutenfree.commands;

import games.glutenfree.MonsterMaze;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SummonTestEntityCommand implements CommandExecutor {

    private MonsterMaze game;
    public SummonTestEntityCommand(MonsterMaze game){
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;
        Entity testEntity = game.summonEntity(player.getLocation());
        testEntity.setCustomName("TEST ENTITY");
        testEntity.setCustomNameVisible(true);
        return true;
    }

}
