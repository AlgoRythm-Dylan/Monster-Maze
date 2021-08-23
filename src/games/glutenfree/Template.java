package games.glutenfree;

import org.bukkit.Location;
import org.bukkit.Material;

public class Template {

    private Location starLocation;
    private int x, y, z;
    public Template(Location startLocation, int x, int y, int z){
        this.starLocation = startLocation;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void blitTo(Location location){
        for(int loopX = 0; loopX < x; loopX++){
            for(int loopY = 0; loopY < y; loopY++){
                for(int loopZ = 0; loopZ < z; loopZ++){
                    location.getWorld().getBlockAt(
                            location.getBlockX() + loopX,
                            location.getBlockY() + loopY,
                            location.getBlockZ() + loopZ
                    ).setType(starLocation.getWorld().getBlockAt(
                            starLocation.getBlockX() + loopX,
                            starLocation.getBlockY() + loopY,
                            starLocation.getBlockZ() + loopZ
                    ).getType());
                }
            }
        }
    }

    public void clear(Location location){
        for(int loopX = 0; loopX < x; loopX++){
            for(int loopY = 0; loopY < y; loopY++){
                for(int loopZ = 0; loopZ < z; loopZ++){
                    location.getWorld().getBlockAt(
                            location.getBlockX() + loopX,
                            location.getBlockY() + loopY,
                            location.getBlockZ() + loopZ
                    ).setType(Material.AIR);
                }
            }
        }
    }

    public Location getStarLocation() {
        return starLocation;
    }

    public void setStarLocation(Location starLocation) {
        this.starLocation = starLocation;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }
}
