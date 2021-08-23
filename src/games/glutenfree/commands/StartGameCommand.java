package games.glutenfree.commands;

import games.glutenfree.MonsterMaze;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StartGameCommand implements CommandExecutor {

    private MonsterMaze game;
    public StartGameCommand(MonsterMaze game){
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        game.startGame();
        return true;
    }

}
