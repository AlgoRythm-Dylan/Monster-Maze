package games.glutenfree.maze;

import games.glutenfree.Cooldown;

public class PlayerData {

    int roundsSurvived, tokens, score, safePadsFirst;
    String deathReason;
    boolean didDie;
    Cooldown powerupCooldown;
    public PlayerData(){
        roundsSurvived = 0;
        tokens = 0;
        score = 0;
        didDie = false;
        powerupCooldown = new Cooldown(500);
    }

    public int getRoundsSurvived() {
        return roundsSurvived;
    }

    public void setRoundsSurvived(int roundsSurvived) {
        this.roundsSurvived = roundsSurvived;
    }

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public void addTokens(int amount){
        this.tokens += amount;
        if(tokens > 50) tokens = 50;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getSafePadsFirst() {
        return safePadsFirst;
    }

    public void setSafePadsFirst(int safePadsFirst) {
        this.safePadsFirst = safePadsFirst;
    }

    public void recordSafePadFirst(){
        this.safePadsFirst++;
    }

    public boolean testPowerupCooldown(String which){
        return powerupCooldown.test(which);
    }

    public void addScore(int amount){
        score += amount;
    }

    public void recordRoundSurvived(){
        roundsSurvived += 1;
    }

    public String getDeathReason() {
        return deathReason;
    }

    public void setDeathReason(String deathReason) {
        this.deathReason = deathReason;
    }

    public boolean getDidDie() {
        return didDie;
    }

    public void setDidDie(boolean didDie) {
        this.didDie = didDie;
    }
}
