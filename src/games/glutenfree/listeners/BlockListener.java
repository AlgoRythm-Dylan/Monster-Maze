package games.glutenfree.listeners;

import games.glutenfree.MonsterMaze;
import games.glutenfree.Quadrant;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.ArrayList;

public class BlockListener implements Listener {

    private MonsterMaze game;
    public BlockListener(MonsterMaze game){
        this.game = game;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(game.getMaze().isWithinBounds(event.getBlockPlaced().getLocation())){
            for(Location location : getMazeMirrorLocations(event.getBlockPlaced().getLocation())){
                game.getMaze().getStartLocation().getWorld().getBlockAt(location).setType(event.getBlockPlaced().getType());
                Location flipped = new Location(location.getWorld(), location.getBlockZ(), location.getBlockY(), location.getBlockX());
                game.getMaze().getStartLocation().getWorld().getBlockAt(flipped).setType(event.getBlockPlaced().getType());
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(game.getMaze().isWithinBounds(event.getBlock().getLocation())){
            for(Location location : getMazeMirrorLocations(event.getBlock().getLocation())){
                game.getMaze().getStartLocation().getWorld().getBlockAt(location).setType(Material.AIR);
                Location flipped = new Location(location.getWorld(), location.getBlockZ(), location.getBlockY(), location.getBlockX());
                game.getMaze().getStartLocation().getWorld().getBlockAt(flipped).setType(Material.AIR);
            }
        }
    }

    private Quadrant getQuadrant(int x, int y){
        if(x < 0 && y < 0) return Quadrant.LOWER_LEFT;
        else if(x >= 0 && y < 0) return Quadrant.LOWER_RIGHT;
        else if(x >= 0 && y >= 0) return Quadrant.UPPER_RIGHT;
        else return Quadrant.UPPER_LEFT;
    }

    private ArrayList<Location> getMazeMirrorLocations(Location eventLocation){
        ArrayList<Location> locations = new ArrayList<>(3);
        Location mazeCenter = game.getMaze().centerOfMaze();
        /*
                   |
                   |
                X  |  O
        -----------|-------------
                O  |  O
                   |
                   |
        */
        // First, normalize mazeCenter as the (0, 0) origin
        // then, each quadrant is either (+, +), (+, -), (-, +), or (-, -)
        // then, determine the quadrant the block is in
        // duplicate coordinates, modifying only their signs
        // return denormalized
        int offsetXFromZero, offsetZFromZero;
        offsetXFromZero = mazeCenter.getBlockX();
        offsetZFromZero = mazeCenter.getBlockZ();
        int mappedBlockX, mappedBlockZ;
        mappedBlockX = eventLocation.getBlockX() - offsetXFromZero;
        mappedBlockZ = eventLocation.getBlockZ() - offsetZFromZero;
        int absX, absZ;
        absX = Math.abs(mappedBlockX);
        absZ = Math.abs(mappedBlockZ);
        locations.add(new Location(mazeCenter.getWorld(), mazeCenter.getBlockX() - absX, mazeCenter.getBlockY(), mazeCenter.getBlockZ() + absZ));
        locations.add(new Location(mazeCenter.getWorld(), mazeCenter.getBlockX() + absX, mazeCenter.getBlockY(), mazeCenter.getBlockZ() + absZ));
        locations.add(new Location(mazeCenter.getWorld(), mazeCenter.getBlockX() - absX, mazeCenter.getBlockY(), mazeCenter.getBlockZ() - absZ));
        locations.add(new Location(mazeCenter.getWorld(), mazeCenter.getBlockX() + absX, mazeCenter.getBlockY(), mazeCenter.getBlockZ() - absZ));
        return locations;
    }

}
