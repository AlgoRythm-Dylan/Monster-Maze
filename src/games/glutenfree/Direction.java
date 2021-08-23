package games.glutenfree;

public enum Direction {
    NORTH(180f),
    EAST(270f),
    SOUTH(0f),
    WEST(90f),
    LEFT(1),
    RIGHT(2);

    private float value;

    Direction(float angle){
        value = angle;
    }

    public float toFloat(){
        return value;
    }

    public Direction opposite(){
        if(value == NORTH.toFloat()) return SOUTH;
        if(value == SOUTH.toFloat()) return NORTH;
        if(value == EAST.toFloat()) return WEST;
        else return EAST;
    }

}
