package games.glutenfree.maze;

import java.util.ArrayList;
import java.util.Random;

public class MazePlatformSpawner {

    Random random;
    ArrayList<MazeChunk> innerQuads;
    int lastChoice = -1;
    public MazePlatformSpawner(ArrayList<MazeChunk> innerQuads){
        this.innerQuads = innerQuads;
        random = new Random();
    }

    // Returns a MazeBlock with top-level-parent-normalized coords for the platform to be spawned
    public MazeBlock next(){
        int nextChoice;
        if(lastChoice != -1)
            while(lastChoice == (nextChoice = random.nextInt(4)));
        else
            nextChoice = random.nextInt(4);
        MazeBlock blockOnParent = new MazeBlock();
        MazeChunk choice = innerQuads.get(nextChoice);
        MazeBlock blockInQuad = choice.blocks.get(random.nextInt(choice.blocks.size()));
        blockOnParent.setBlockLocation(
                blockInQuad.getBlockX() + choice.posX + ((MazeChunk)choice.parent).posX,
                blockInQuad.getBlockY() + choice.posY + ((MazeChunk)choice.parent).posY
        );
        lastChoice = nextChoice;
        return blockOnParent;
    }

}
