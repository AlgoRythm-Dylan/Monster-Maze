package games.glutenfree.maze;

public class MazeChunk extends Maze {

    int posX, posY;
    Maze parent;
    public MazeChunk(int width, int height){
        super(width, height);
        posX = 0;
        posY = 0;
        updateBlockPositions();
    }

    public void restore(){
        for(MazeBlock block : blocks){
            startLocation.getWorld().getBlockAt(
                    parent.getStartLocation().getBlockX() + block.getBlockX() + posX,
                    parent.getStartLocation().getBlockY(),
                    parent.getStartLocation().getBlockZ() + block.getBlockY() + posY
            ).setType(block.getMaterial());
            if(parent != null)
                parent.getMazeBlockAt(block.getBlockX() + posX, block.getBlockY() + posY).setBlockType(block.getBlockType());
        }
    }

    public MazeChunk copy(){
        MazeChunk copy = new MazeChunk(width, height);
        copy.startLocation = startLocation;
        copy.setParent(parent);
        copy.setPosition(posX, posY);
        for(int i = 0; i < this.blocks.size(); i++){
            copy.blocks.set(i, blocks.get(i).copy());
        }
        copy.updateBlockPositions();
        return copy;
    }

    public Maze getParent() {
        return parent;
    }

    public void setParent(Maze parent) {
        this.parent = parent;
    }

    public void setPosition(int x, int y){
        posX = x;
        posY = y;
    }

}
