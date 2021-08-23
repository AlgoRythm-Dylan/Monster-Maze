package games.glutenfree.commands;

import games.glutenfree.MonsterMaze;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class AddMeToGameCommand implements CommandExecutor {

    private MonsterMaze game;
    public AddMeToGameCommand(MonsterMaze game){
        this.game = game;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length > 0){
            if(strings[0].equals("all")){
                for(Player player : Bukkit.getServer().getOnlinePlayers()){
                    if(game.addPlayer(player)){
                        Bukkit.broadcastMessage("Added " + player.getDisplayName() + " to the game!");
                    }
                }
                return true;
            }
            else{
                return false;
            }
        }
        else{
            if(!(commandSender instanceof Player)) return false;
            Player player = (Player) commandSender;
            if(game.addPlayer(player)){
                Bukkit.broadcastMessage("Added " + player.getDisplayName() + " to the game!");
            }
            else{
                player.sendMessage(ChatColor.RED + "Couldn't add you to the game. Are you already in it?");
            }
            return true;
        }
    }
}
