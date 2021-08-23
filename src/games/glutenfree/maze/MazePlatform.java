package games.glutenfree.maze;


import games.glutenfree.Template;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.util.HashSet;
import java.util.UUID;

public class MazePlatform {

    MazeChunk original, platform;
    BoundingBox box;
    long age, lifeSpan;
    int platformHeightOffset = 12;
    Template indicator;
    boolean doesAge = true;
    boolean isUntouched = true;
    HashSet<UUID> visited;
    public MazePlatform(MazeChunk original, MazeChunk platform){
        this.original = original;
        this.platform = platform;
        lifeSpan = 15000; // 15 second default life
        age = 0;
        box = new BoundingBox(
                this.platform.getStartLocation().getBlockX() + platform.getMazeBlockAt(0, 0).getBlockX() + platform.posX,
                this.platform.getStartLocation().getBlockY(),
                this.platform.getStartLocation().getBlockZ() + platform.getMazeBlockAt(0, 0).getBlockY() + platform.posY,
                this.platform.getStartLocation().getBlockX() + platform.getMazeBlockAt(original.getWidth() - 1, original.getHeight() - 1).getBlockX() + 1 + platform.posX,
                this.platform.getStartLocation().getBlockY() + 1.5,
                this.platform.getStartLocation().getBlockZ() + platform.getMazeBlockAt(original.getWidth() - 1, original.getHeight() - 1).getBlockY() + 1  + platform.posY
        );
        visited = new HashSet<>();
    }

    public void restoreOriginal(){
        original.restore();
        if(indicator != null)
            indicator.clear(getPlatformLocation());
    }

    public void placePlatform(){
        platform.restore();
    }

    public boolean entityIsOnPlatform(Entity entity){
        BoundingBox eBox = entity.getBoundingBox();
        return box.overlaps(eBox);
    }

    public void setAllMaterial(Material material){
        for(MazeBlock block : platform.blocks){
            block.setMaterial(material);
        }
        this.placePlatform();
    }

    public void update(long deltaTime){
        if(doesAge)
            age += deltaTime;
        Material platMat = this.platform.blocks.get(0).getMaterial();
        if(platMat == Material.GREEN_TERRACOTTA && (double)age/(double)lifeSpan > 0.5){
            setAllMaterial(Material.YELLOW_TERRACOTTA);
        }
        if(platMat == Material.YELLOW_TERRACOTTA && (double)age/(double)lifeSpan > 0.8){
            setAllMaterial(Material.RED_TERRACOTTA);
        }
    }

    public long getLifeSpan() {
        return lifeSpan;
    }

    public void setLifeSpan(long lifeSpan) {
        this.lifeSpan = lifeSpan;
    }

    public boolean isExpired(){
        return age >= lifeSpan;
    }

    public Template getIndicator() {
        return indicator;
    }

    public void setIndicator(Template indicator) {
        this.indicator = indicator;
    }

    private Location getPlatformLocation(){
        int platX = platform.getStartLocation().getBlockX() + platform.getMazeBlockAt(0, 0).getBlockX() + platform.posX;
        int platZ = platform.getStartLocation().getBlockZ() + platform.getMazeBlockAt(0, 0).getBlockY() + platform.posY;
        int offsetX = Math.round((platform.getWidth() - this.indicator.getX()) / 2f);
        int offsetZ = Math.round((platform.getHeight() - this.indicator.getZ()) / 2f);
        return new Location(platform.getStartLocation().getWorld(),
                platX + offsetX,
                platform.getStartLocation().getBlockY() + platformHeightOffset,
                platZ + offsetZ);
    }

    public void placeIndicator(){
        if(indicator == null) return;
        indicator.blitTo(getPlatformLocation());
    }

    public void removeIndicator(){
        if(indicator == null) return;
        indicator.clear(getPlatformLocation());
    }

    public int getPlatformHeightOffset() {
        return platformHeightOffset;
    }

    public void setPlatformHeightOffset(int platformHeightOffset) {
        this.platformHeightOffset = platformHeightOffset;
    }

    public void enableAging(){
        doesAge = true;
    }

    public void disableAging(){
        doesAge = false;
    }

    public boolean isUntouched() {
        return isUntouched;
    }

    public void setUntouched(boolean untouched) {
        isUntouched = untouched;
    }

    public boolean playerHasVisited(Player player){
        return visited.contains(player.getUniqueId());
    }

    public void recordPlayerVisit(Player player){
        visited.add(player.getUniqueId());
    }

    public int visitCount(){
        return visited.size();
    }

}
