package games.glutenfree.commands;

import games.glutenfree.MonsterMaze;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ResetGameCommand implements CommandExecutor {

    private MonsterMaze game;
    public ResetGameCommand(MonsterMaze game){
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        game.resetGame();
        return true;
    }
}
