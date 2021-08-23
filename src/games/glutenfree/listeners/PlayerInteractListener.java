package games.glutenfree.listeners;

import games.glutenfree.MonsterMaze;
import games.glutenfree.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlayerInteractListener implements Listener {

    private ArrayList<Material> signs;
    private ArrayList<Material> trapDoors;

    private MonsterMaze game;
    public PlayerInteractListener(MonsterMaze game){
        this.game = game;
        signs = new ArrayList<>();
        signs.add(Material.SPRUCE_WALL_SIGN);
        signs.add(Material.SPRUCE_SIGN);
        signs.add(Material.OAK_WALL_SIGN);
        signs.add(Material.OAK_SIGN);
        signs.add(Material.BIRCH_SIGN);
        signs.add(Material.BIRCH_WALL_SIGN);
        signs.add(Material.ACACIA_SIGN);
        signs.add(Material.ACACIA_WALL_SIGN);
        signs.add(Material.WARPED_SIGN);
        signs.add(Material.WARPED_WALL_SIGN);
        signs.add(Material.CRIMSON_SIGN);
        signs.add(Material.CRIMSON_WALL_SIGN);

        trapDoors = new ArrayList<>();
        trapDoors.add(Material.ACACIA_TRAPDOOR);
        trapDoors.add(Material.BIRCH_TRAPDOOR);
        trapDoors.add(Material.OAK_TRAPDOOR);
        trapDoors.add(Material.CRIMSON_TRAPDOOR);
        trapDoors.add(Material.JUNGLE_TRAPDOOR);
        trapDoors.add(Material.WARPED_TRAPDOOR);
        trapDoors.add(Material.SPRUCE_TRAPDOOR);
        trapDoors.add(Material.DARK_OAK_TRAPDOOR);
    }

    private boolean isASign(Material material){
        for(Material signMat : signs)
            if(material == signMat) return true;
        return false;
    }

    private boolean isATrapdoor(Material material){
        for(Material doorMat : trapDoors)
            if(material == doorMat) return true;
        return false;
    }

    // for now only works with spruce signs
    private BlockState getBlockAboveIfSign(Location location){
        BlockState above = location.getWorld()
                .getBlockAt(location.getBlockX(), location.getBlockY() + 1, location.getBlockZ())
                .getState();
        if(isASign(above.getType())) return above;
        return null;
    }

    public void isHoldingSpecialItem(){

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Block clicked = event.getClickedBlock();
        Player player = event.getPlayer();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if(itemStack.getAmount() != 0){
            String specialItemName = Util.getSpecialItemKey(game.getMazeItemKey(), itemStack);
            if(specialItemName != null){
                if(specialItemName.equalsIgnoreCase("repulse")) {
                    game.repulse(player);
                }
                else if(specialItemName.equalsIgnoreCase("one-up")){
                    game.oneUp(player);
                }
                else if(specialItemName.equalsIgnoreCase("speed-boost")){
                    game.speedBoost(player);
                }
            }
        }
        if(clicked == null) return;
        else if(clicked.getType() == Material.STONE_BUTTON){
            BlockState block = getBlockAboveIfSign(event.getClickedBlock().getLocation());
            if(block != null){
                Sign sign = (Sign) block;
                String text = sign.getLine(1);
                if(text.equalsIgnoreCase("begin game")){
                    game.startGame();
                }
                else if(text.equalsIgnoreCase("join game")){
                    if(game.addPlayer(event.getPlayer())){
                        Bukkit.broadcastMessage(String.format("%s%s%s%s joined the game!",
                                ChatColor.GREEN, ChatColor.BOLD, event.getPlayer().getDisplayName(), ChatColor.RESET));
                    }
                    else{
                        event.getPlayer().sendMessage(ChatColor.RED + "Couldn't add you to the game (are you already in it?)");
                    }
                }
                else if(text.equalsIgnoreCase("classic mode")){
                    if(game.isClassicMode()){
                        Bukkit.broadcastMessage(String.format("Classic mode: %s%sDISABLED", ChatColor.RED, ChatColor.BOLD));
                        game.setClassicMode(false);
                    }
                    else{
                        Bukkit.broadcastMessage(String.format("Classic mode: %s%sENABLED", ChatColor.GREEN, ChatColor.BOLD));
                        game.setClassicMode(true);
                    }
                }
            }
        }
        else if(isATrapdoor(event.getClickedBlock().getType()) &&
                event.getPlayer().getGameMode() != GameMode.CREATIVE){
            // stop fuckin with ma DOORS
            event.setCancelled(true);
        }
    }

}
