package games.glutenfree.maze;

import org.bukkit.Material;

public class MazeBlock {

    private MazeBlockType blockType;
    private int blockX, blockY;
    private Material material;
    public MazeBlock(){
        blockType = MazeBlockType.AIR;
    }

    public MazeBlockType getBlockType() {
        return blockType;
    }

    public void setBlockType(MazeBlockType blockType) {
        this.blockType = blockType;
    }

    public void setBlockLocation(int x, int y){
        blockX = x;
        blockY = y;
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public MazeBlock copy(){
        MazeBlock block = new MazeBlock();
        block.setBlockType(getBlockType());
        block.setBlockLocation(getBlockX(), getBlockY());
        block.setMaterial(getMaterial());
        return block;
    }

}
