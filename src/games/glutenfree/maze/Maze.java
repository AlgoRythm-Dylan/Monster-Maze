package games.glutenfree.maze;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Random;

/*

    MAZE: "image" of MazeBlocks
    Since a maze is horizontal, not vertical, x & y of the image
    actually refer to x & z of the Minecraft world

 */

public class Maze {

    public ArrayList<MazeBlock> blocks;
    protected Location startLocation;
    protected int width, height;

    public Maze(int width, int height){
        this.width = width;
        this.height = height;
        blocks = new ArrayList<>(width * height);
        fillWithEmpty();
    }

    private void fillWithEmpty(){
        for(int i = 0; i < width * height; i++)
            blocks.add(new MazeBlock());
    }

    public void updateBlockPositions(){
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                int index = (x * width) + y;
                blocks.get(index).setBlockLocation(x, y);
            }
        }
    }

    public void parse(){
        if(startLocation == null)
            throw new RuntimeException("startLocation not set!");
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                int index = (x * width) + y;
                Block block = this.getBlockAt(x, y);
                blocks.get(index).setBlockLocation(x, y);
                blocks.get(index).setMaterial(block.getType());
                switch(block.getType()){
                    case SNOW_BLOCK -> {
                        blocks.get(index).setBlockType(MazeBlockType.MAZE);
                        break;
                    }
                    case AIR -> {
                        blocks.get(index).setBlockType(MazeBlockType.AIR);
                        break;
                    }
                    case REDSTONE_BLOCK -> {
                        blocks.get(index).setBlockType(MazeBlockType.SPAWNER);
                        break;
                    }
                }
            }
        }
    }

    public ArrayList<MazeBlock> getBlocksOfType(MazeBlockType type){
        ArrayList<MazeBlock> found = new ArrayList<>();
        for(int i = 0; i < blocks.size(); i++)
            if(blocks.get(i).getBlockType() == type)
                found.add(blocks.get(i));
        return found;
    }

    public ArrayList<Location> getInitialSpawnPositions(int amount){
        ArrayList<Location> locations = new ArrayList<>(amount);
        ArrayList<MazeBlock> blocks = getBlocksOfType(MazeBlockType.MAZE);
        Random random = new Random();
        for(int i = 0; i < amount; i++){
            MazeBlock block = blocks.get(random.nextInt(blocks.size() - 1));
            locations.add(new Location(startLocation.getWorld(),
                (double) block.getBlockX() + startLocation.getBlockX() + 0.5,
                startLocation.getY() + 1,
                (double) block.getBlockY() + startLocation.getBlockZ() + 0.5));
        }
        return locations;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Location centerOfMaze(){
        return new Location(
                startLocation.getWorld(),
                startLocation.getX() + (width / 2),
                startLocation.getY() + 0.5,
                startLocation.getZ() + (height / 2)
        );
    }

    public boolean isWithinBounds(Location location){
        return
            (location.getBlockX() >= startLocation.getBlockX() && location.getBlockX() <= startLocation.getBlockX() + width) &&
            (location.getBlockY() == startLocation.getBlockY()) &&
            (location.getBlockZ() >= startLocation.getBlockZ() && location.getBlockZ() <= startLocation.getBlockZ() + height);
    }

    public MazeBlock getMazeBlockAt(int x, int y){
        int index = (x * this.width) + y;
        if(index < 0 || index > blocks.size()) return null;
        return blocks.get(index);
    }

    public void setMazeBlockAt(int x, int y, MazeBlock block){
        blocks.set((x * this.width) + y, block);
    }

    public Block getBlockAt(int x, int y){
        return startLocation.getWorld().getBlockAt(
                startLocation.getBlockX() + x,
                startLocation.getBlockY(),
                startLocation.getBlockZ() + y
        );
    }

    public MazeChunk getChunk(int x, int y, int chunkWidth, int chunkHeight){
        MazeChunk chunk = new MazeChunk(chunkWidth, chunkHeight);
        chunk.setPosition(x, y);
        chunk.startLocation = startLocation;
        for(int loopX = x; loopX - x < chunkWidth; loopX++)
            for(int loopY = y; loopY - y < chunkHeight; loopY++)
                chunk.setMazeBlockAt(loopX - x, loopY - y, getMazeBlockAt(loopX, loopY).copy());
        chunk.setParent(this);
        chunk.updateBlockPositions();
        return chunk;
    }

    public MazePlatform createPlatform(int x, int y, int platWidth, int platHeight){
        MazeChunk original, platform;
        original = getChunk(x, y, platWidth, platHeight);
        platform = original.copy();
        for(MazeBlock block : platform.blocks){
            block.setBlockType(MazeBlockType.PLATFORM);
            block.setMaterial(Material.GREEN_TERRACOTTA);
        }
        return new MazePlatform(original, platform);
    }

    public ArrayList<MazeChunk> getQuadrants(){
        ArrayList<MazeChunk> quads = new ArrayList<>(4);
        int quadWidth = width / 2;
        int quadHeight = height / 2;
        for(int x = 0; x < 2; x++)
            for(int y = 0; y < 2; y++)
                quads.add(getChunk(x * quadWidth, y * quadHeight, quadWidth, quadHeight));
        return quads;
    }

    public ArrayList<MazeChunk> getPlatformAreas(){
        ArrayList<MazeChunk> platformAreas = new ArrayList<>(4);
        for(MazeChunk chunk : getQuadrants()){
            MazeChunk innerChunk = chunk.getChunk(
                    (int)(chunk.getWidth() * 0.4),
                    (int)(chunk.getHeight() * 0.4),
                    (int)(chunk.getWidth() * 0.2),
                    (int)(chunk.getHeight() * 0.2)
            );
            platformAreas.add(innerChunk);
        }
        return platformAreas;
    }

}
