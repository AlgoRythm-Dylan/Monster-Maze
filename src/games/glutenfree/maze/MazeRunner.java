package games.glutenfree.maze;

import games.glutenfree.Direction;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Random;

public class MazeRunner {

    private MazeRunnerState state;
    private Direction direction;
    private Entity entity;
    private float movementSpeed = 0.001f;
    private long wasLastStuck;
    public float targetX, targetY;
    private float currentX, currentY;
    private Maze maze;
    public MazeRunner(Entity entity){
        this.entity = entity;
        setState(MazeRunnerState.WAITING);
    }

    public ArrayList<Direction> getValidDirections(){
        ArrayList<Direction> valid = new ArrayList<>();
        int mazeX = (int) Math.floor(currentX - maze.getStartLocation().getBlockX());
        int mazeY = (int) Math.floor(currentY - maze.getStartLocation().getBlockZ());
        MazeBlock north = maze.getMazeBlockAt(mazeX, mazeY - 1);
        MazeBlock south = maze.getMazeBlockAt(mazeX, mazeY + 1);
        MazeBlock east = maze.getMazeBlockAt(mazeX + 1, mazeY);
        MazeBlock west = maze.getMazeBlockAt(mazeX - 1, mazeY);
        if(north != null && north.getBlockType() == MazeBlockType.MAZE)
            valid.add(Direction.NORTH);
        if(south != null && south.getBlockType() == MazeBlockType.MAZE)
            valid.add(Direction.SOUTH);
        if(east != null && east.getBlockType() == MazeBlockType.MAZE)
            valid.add(Direction.EAST);
        if(west != null && west.getBlockType() == MazeBlockType.MAZE)
            valid.add(Direction.WEST);
        return valid;
    }

    private void removeDirection(ArrayList<Direction> directions, Direction directionToRemove){
        for(int i = 0; i < directions.size(); i++)
            if(directions.get(i).toFloat() == directionToRemove.toFloat())
                directions.remove(i);
    }

    public void findTargetBlock(){
        ArrayList<Direction> availableDirections = getValidDirections();
        if(availableDirections.size() == 0){
            this.setState(MazeRunnerState.WAITING);
        }
        else{
            if(availableDirections.size() == 1){
                // No choice but to take that direction
                //targetYaw = availableDirections.get(0).toFloat();
                direction = availableDirections.get(0);
                setState(MazeRunnerState.RUNNING);
            }
            else{
                // make a random choice
                if(direction != null)
                    removeDirection(availableDirections, direction.opposite());
                Random random = new Random();
                int index = random.nextInt(availableDirections.size());
                direction = availableDirections.get(index);
                setState(MazeRunnerState.RUNNING);
            }
        }
        if(direction == Direction.NORTH) targetY = currentY - 1;
        if(direction == Direction.SOUTH) targetY = currentY + 1;
        if(direction == Direction.WEST) targetX = currentX - 1;
        if(direction == Direction.EAST) targetX = currentX + 1;
    }

    public void moveInWorld(){
        Location location = entity.getLocation();
        location.setYaw(direction.toFloat());
        location.setX(currentX);
        location.setZ(currentY);
        entity.teleport(location);
    }

    public void update(long deltaTime){
        if(state == MazeRunnerState.RUNNING){
            boolean metTarget = false;
            if(direction == Direction.NORTH){
                // negative z ("y")
                currentY -= movementSpeed * deltaTime;
                metTarget = currentY <= targetY;
            }
            else if(direction == Direction.EAST){
                // positive x
                currentX += movementSpeed * deltaTime;
                metTarget = currentX >= targetX;
            }
            else if(direction == Direction.SOUTH){
                // positive z ("y")
                currentY += movementSpeed * deltaTime;
                metTarget = currentY >= targetY;
            }
            else if(direction == Direction.WEST){
                // negative x
                currentX -= movementSpeed * deltaTime;
                metTarget = currentX <= targetX;
            }
            if(metTarget){
                currentX = (float)Math.floor(currentX) + 0.5f; // re-align
                currentY = (float)Math.floor(currentY) + 0.5f;
                setState(MazeRunnerState.WAITING);
            }
            else{
                moveInWorld();
            }
        }
        else if(state == MazeRunnerState.WAITING){
            findTargetBlock();
        }
    }

    public Maze getMaze() {
        return maze;
    }

    public void setMaze(Maze maze) {
        this.maze = maze;
        currentX = (float)entity.getLocation().getX();
        currentY = (float)entity.getLocation().getZ();
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public MazeRunnerState getState() {
        return state;
    }

    public void setState(MazeRunnerState state) {
        this.state = state;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public float getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(float movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

}
