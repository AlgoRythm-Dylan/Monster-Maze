package games.glutenfree;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import games.glutenfree.commands.*;
import games.glutenfree.listeners.*;
import games.glutenfree.maze.*;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class MonsterMaze extends JavaPlugin {

    private Location spawnRoomLocation, mazeStart;
    private Template cage, indicator;

    private final int CHECK_DELAY = 1;
    private final int SLOW_GAME_TICK = 20;
    private final double VERTICAL_HIT_DISTANCE = 0.2d;
    private final double HORIZONTAL_HIT_DISTANCE = 0.4d;
    private final double VERTICAL_BOOP_FORCE  = 1.05;
    private final double HORIZONTAL_BOOP_FORCE = 0.75;
    private final double REPULSE_RANGE = 5d;
    private final int HEALTH_PER_HIT = 4;
    private final int MAX_RUNNERS = 3000;
    private final int NEW_RUNNERS_PER_ROUND = 12;
    private final int REPULSE_COST = 10;
    private final int ONE_UP_COST = 5;
    private final int SPEED_BOOST_COST = 8;
    private Cooldown boopCooldown;
    private Random random;

    private NamespacedKey mazeItemKey;

    private int gameTickTask = -1;
    private int oncePerSecondTask = -1;
    private int releasePlayersTask = -1;

    private FixedMetadataValue specialEntity;

    private ArrayList<Player> playersInGame, deadPlayers, spectators;
    private ArrayList<MazeRunner> runners;
    private ArrayList<MazeBlock> placesToSpawn;

    private boolean running;
    private Maze maze;
    private MazePlatformSpawner platformSpawner;
    private boolean isClassicMode = false;
    private boolean isSingleplayerMode = false;
    private MazePlatform startPlatform, currentPlatform, lastPlatform;
    private ArrayList<MazePlatform> platforms;
    private long lastUpdateTime;
    private int currentRound, currentCountdown;
    private boolean playersAreReleased = false;
    private BossBar timeLeftBossbar;
    String classicModeString;
    PlayerDataManager dataManager;

    private Scoreboard gameScoreboard;
    private Team gameTeam;

    private PotionEffect noJump;
    private ProtocolManager pm;

    private final boolean DEV_MODE = false;

    @Override
    public void onEnable(){

        mazeItemKey = new NamespacedKey(this, "mm-powerup");

        spawnRoomLocation = new Location(getServer().getWorlds().get(0), 85, 12.5, 9, 90, 0);
        mazeStart = new Location(getServer().getWorlds().get(0), -42, 10, -42);
        cage = new Template(new Location(getServer().getWorlds().get(0), -130, 8, 11), 15, 3, 15);
        indicator = new Template(new Location(getServer().getWorlds().get(0), -130, 5, -4), 5, 8, 5);

        playersInGame = new ArrayList<>();
        deadPlayers = new ArrayList<>();
        runners = new ArrayList<>();
        platforms = new ArrayList<>();
        placesToSpawn = new ArrayList<>();
        spectators = new ArrayList<>();
        boopCooldown = new Cooldown(500);
        random = new Random();
        dataManager = new PlayerDataManager();

        specialEntity = new FixedMetadataValue(this, "special-entity");
        classicModeString = String.format("%s - %sClassic Mode", ChatColor.RESET, ChatColor.YELLOW);

        running = false;
        maze = new Maze(100, 100);
        maze.setStartLocation(mazeStart);

        noJump =  new PotionEffect(PotionEffectType.JUMP, 999999, 128, false, false);
        pm = ProtocolLibrary.getProtocolManager();
        timeLeftBossbar = getServer().createBossBar("Time Left", BarColor.GREEN, BarStyle.SOLID);

        /*pm.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL,
                        PacketType.Play.Server.ENTITY_METADATA) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        // Item packets (id: 0x29)
                        if (event.getPacketType() ==
                                PacketType.Play.Server.ENTITY_METADATA) {
                            //event.setCancelled(true);
                            PacketPlayOutEntityMetadata rawPacket = (PacketPlayOutEntityMetadata) event.getPacket().getHandle();
                            PacketContainer packet = event.getPacket();
                            int entityID = packet.getIntegers().read(0);
                            Player playerInGame = entityIsInGame(entityID);
                            if(entityIsInGame(entityID) != null) {
                                //getLogger().info(String.format("Sent packet for entity ID %d... %s", entityID, list.get(0).b()));
                                DataWatcherObject watcherObject = (DataWatcherObject) packet.getWatchableCollectionModifier().getValues().get(0).get(0).getWatcherObject().getHandle();
                                getLogger().info(String.format("Entity ID: %d... bitmask: %s",
                                        packet.getIntegers().read(0), ));
                            }
                        }
                    }
                });*/

        if(DEV_MODE){
            getCommand("resetgame").setExecutor(new ResetGameCommand(this));
            getCommand("addmetogame").setExecutor(new AddMeToGameCommand(this));
            getCommand("startgame").setExecutor(new StartGameCommand(this));
            getCommand("placeindicator").setExecutor(new PlaceIndicatorCommand(this));

            getServer().getPluginManager().registerEvents(new BlockListener(this), this);

            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                getServer().broadcastMessage(String.format("%s%s== SERVER RUNNING IN DEV MODE ==", ChatColor.RED, ChatColor.BOLD));
            }, 5);
        }

        // GAMERULES
        spawnRoomLocation.getWorld().setGameRule(GameRule.DO_INSOMNIA, false);
        spawnRoomLocation.getWorld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        spawnRoomLocation.getWorld().setGameRule(GameRule.DO_MOB_LOOT, false);
        spawnRoomLocation.getWorld().setGameRule(GameRule.DO_MOB_SPAWNING, false);
        spawnRoomLocation.getWorld().setGameRule(GameRule.MOB_GRIEFING, false);
        spawnRoomLocation.getWorld().setGameRule(GameRule.NATURAL_REGENERATION, false);
        spawnRoomLocation.getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);
        spawnRoomLocation.getWorld().setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);

        resetGame();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new DamageListener(this), this);

        gameScoreboard = getServer().getScoreboardManager().getNewScoreboard();
        gameTeam = gameScoreboard.registerNewTeam("game-team");
        gameTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        gameTeam.setCanSeeFriendlyInvisibles(true);
        gameTeam.setAllowFriendlyFire(false);

        oncePerSecondTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for(Player player : getServer().getOnlinePlayers()){
                player.setFoodLevel(20);
            }
            if(running){
                if(playersAreReleased){
                    currentCountdown--;
                    handleCountdownTime(currentCountdown);
                }
            }
        }, SLOW_GAME_TICK, SLOW_GAME_TICK);

        getLogger().info("MonsterMaze game Started!");
    }

    @Override
    public void onDisable(){
        resetGame();
        HandlerList.unregisterAll(this);
        if(gameTickTask != -1) Bukkit.getScheduler().cancelTask(gameTickTask);
        if(oncePerSecondTask != -1) Bukkit.getScheduler().cancelTask(oncePerSecondTask);
        removeAllEntities();
        if(pm != null)
            pm.removePacketListeners(this);
        if(gameTeam != null)
            gameTeam.unregister();
        getLogger().info("MonsterMaze game Disabled!");
    }

    public void parseMaze(){
        maze.parse();
        platformSpawner = new MazePlatformSpawner(maze.getPlatformAreas());
        placesToSpawn = maze.getBlocksOfType(MazeBlockType.SPAWNER);
    }

    /*

        GAME FLOW

     */

    public void teleportPlayerToSpawnRoom(Player player){
        player.teleport(spawnRoomLocation);
    }

    public boolean playerIsInGame(Player player){
        return playersInGame.contains(player);
    }

    public Player entityIsInGame(int entityID){
        for(Player player : playersInGame){
            if(player.getEntityId() == entityID) return player;
        }
        return null;
    }

    public boolean addPlayer(Player player){
        if(playerIsInGame(player) || running) return false;
        playersInGame.add(player);
        gameTeam.addEntry(player.getDisplayName());
        player.setScoreboard(gameScoreboard);
        return true;
    }

    public int getCountdownTime(int round){
        if(round < 15)
            return 60 - (round * 2);
        else
            return 30;
    }

    public int getCountdownTime(){
        return getCountdownTime(currentRound);
    }

    public void startGame(){
        if(isRunning()){
            Bukkit.broadcastMessage(ChatColor.RED + "Game already started!");
            return;
        }
        if(playersInGame.size() == 0){
            Bukkit.broadcastMessage(ChatColor.RED + "Game needs at least one player to start");
            return;
        }
        running = true;
        playersAreReleased = false;
        currentRound = 1;
        currentCountdown = getCountdownTime();
        dataManager.reset();
        parseMaze();
        int spawns = maze.getBlocksOfType(MazeBlockType.MAZE).size() / 20;
        for(Location location : maze.getInitialSpawnPositions(spawns)){
            if(canSummonEntity()) // IDK we might hit the limit initially...?
                summonEntity(location);
        }
        startPlatform = maze.createPlatform(
                Math.round(((float)maze.getWidth() / 2f) - 7.5f),
                Math.round(((float)maze.getHeight() / 2f) - 7.5f), 15, 15);
        startPlatform.setIndicator(cage);
        startPlatform.setLifeSpan(35000);
        startPlatform.setPlatformHeightOffset(1);
        startPlatform.placePlatform();
        startPlatform.placeIndicator();
        platforms.add(startPlatform);
        removeEntitiesOnPlatforms();

        // Set up all players
        for(Player player : playersInGame) {
            setupPlayer(player);
        }

        // Make all non-players specs
        for(Player player : Bukkit.getOnlinePlayers()){
            if(!playerIsInGame(player))
                makePlayerSpectator(player);
        }
        lastUpdateTime = System.currentTimeMillis();
        startGameTick();

        spawnNextPlatform();
        releasePlayersTask = Bukkit.getScheduler().scheduleSyncDelayedTask(this, this::releasePlayers, 150);
        displayGameTitle(String.format("%s%sMonster Maze", ChatColor.GREEN, ChatColor.BOLD), "Get to the safe pad!");

        Bukkit.broadcastMessage("\n");
        Bukkit.broadcastMessage(String.format("     %s%sMonster Maze", ChatColor.GREEN, ChatColor.BOLD));
        if(playersInGame.size() == 1){
            isSingleplayerMode = true;
            Bukkit.broadcastMessage(String.format("     Singleplayer mode%s", isClassicMode ? classicModeString : ""));
        }
        else{
            isSingleplayerMode = false;
            Bukkit.broadcastMessage(String.format("     Game of %s%d%s players%s",
                    ChatColor.GOLD, playersInGame.size(), ChatColor.RESET,
                    isClassicMode ? classicModeString : ""));
        }
        Bukkit.broadcastMessage(String.format("\n     %sGet to the safe pad before", ChatColor.GRAY));
        Bukkit.broadcastMessage(String.format("     %sthe timer expires! Watch out", ChatColor.GRAY));
        Bukkit.broadcastMessage(String.format("     %sfor snowmen. Click items in your", ChatColor.GRAY));
        Bukkit.broadcastMessage(String.format("     %shotbar to use powerups!\n\n", ChatColor.GRAY));

        // Finally, make players semi-visible to one another
        /*if(!isSingleplayerMode){
            for(Player player : playersInGame) {
                for(Player otherPlayer : playersInGame){
                    if(player != otherPlayer){
                        makeInvisibleTo(player, otherPlayer);
                    }
                }
            }
        }*/

    }

    public void releasePlayers(){
        for(Player player : playersInGame){
            timeLeftBossbar.addPlayer(player);
        }
        playersAreReleased = true;
        startPlatform.removeIndicator();
        releasePlayersTask = -1;
    }

    public void resetTitle(){
        displayGameTitle(null, null);
    }

    public void displayGameTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut){
        for(Player player : playersInGame){
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public void displayGameTitle(String title, String subtitle){
        displayGameTitle(title, subtitle, 20, 110, 20);
    }

    public void displayGameTitle(String title){
        displayGameTitle(title, null);
    }


    public void sendToPlayers(String message){
        for(Player player : playersInGame){
            player.sendMessage(message);
        }
    }

    public void spawnNextPlatform(){
        MazeBlock randomPosition = platformSpawner.next();
        if(lastPlatform != null){
            lastPlatform.removeIndicator(); // Just in case
            lastPlatform.restoreOriginal();
            platforms.remove(lastPlatform);
        }
        if(currentPlatform != null){
            lastPlatform = currentPlatform;
            lastPlatform.setLifeSpan(8000);
            lastPlatform.enableAging();
            lastPlatform.removeIndicator();
        }
        currentPlatform = maze.createPlatform(randomPosition.getBlockX() - 2, randomPosition.getBlockY() - 2, 5, 5);
        currentPlatform.placePlatform();
        currentPlatform.disableAging();
        currentPlatform.setIndicator(indicator);
        currentPlatform.placeIndicator();
        platforms.add(currentPlatform);
        removeEntitiesOnPlatforms();
    }

    public void removeEntitiesOnPlatforms(){
        for(MazePlatform platform : platforms){
            for(int i = 0; i < runners.size(); i++){
                MazeRunner runner = runners.get(i);
                if(platform.entityIsOnPlatform(runner.getEntity())){
                    runner.getEntity().remove();
                    runners.remove(i);
                    i--;
                }
            }
        }
    }

    public void killPlayersNotOnPlatform(){
        for(int i = 0; i < playersInGame.size(); i++){
            Player player = playersInGame.get(i);
            if(!currentPlatform.entityIsOnPlatform(player)){
                killPlayer(player, "You weren't on the safe pad!");
                i--;
            }
        }
    }

    public void setPlayerTokens(Player player, int amt){
        player.getInventory().setItem(8, MazePowerup.tokens(mazeItemKey, amt));
    }

    public void setupPlayer(Player player){
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
        player.addPotionEffect(noJump);
        Location startLocation = maze.centerOfMaze();
        startLocation.setY(startLocation.getY() + 0.5);
        player.teleport(startLocation);
        player.setCollidable(false);
        player.setGameMode(GameMode.ADVENTURE); // Just in case
        player.getInventory().clear();
        player.getInventory().setItem(0, MazePowerup.repulse(mazeItemKey));
        PlayerData data = dataManager.createDataFor(player);
        if(!isClassicMode){
            player.getInventory().setItem(1, MazePowerup.oneUp(mazeItemKey));
            player.getInventory().setItem(2, MazePowerup.speedBoost(mazeItemKey));
            setPlayerTokens(player, 0);
            data.setTokens(0);
        }
        else{
            setPlayerTokens(player, 30);
            data.setTokens(30);
        }
    }

    // Skip ahead but only if its truly "ahead"
    public void skipAheadTime(int to){
        if(to <= currentCountdown){
            currentCountdown = to;
        }
    }

    public void handleCountdownTime(int countdownTime){
        timeLeftBossbar.setProgress((double) currentCountdown / (double) getCountdownTime());
        timeLeftBossbar.setTitle(String.format("%s%sRound %d - %d Seconds Left", ChatColor.GREEN, ChatColor.BOLD, currentRound, countdownTime));
        if(countdownTime <= 0){
            killPlayersNotOnPlatform();
            if(playersInGame.size() > 0){
                currentCountdown = getCountdownTime(++currentRound);
                spawnNextPlatform();
                for(int i = 0; i < NEW_RUNNERS_PER_ROUND && canSummonEntity(); i++){
                    MazeBlock block = placesToSpawn.get(random.nextInt(placesToSpawn.size() - 1));
                    summonEntity(new Location(maze.getStartLocation().getWorld(),
                            (double) block.getBlockX() + maze.getStartLocation().getBlockX() + 0.5,
                            maze.getStartLocation().getY() + 1,
                            (double) block.getBlockY() + maze.getStartLocation().getBlockZ() + 0.5));
                }
                displayGameTitle(String.format("%s%sSafe!", ChatColor.GREEN, ChatColor.BOLD), "Get to the next safe pad", 0, 40, 10);
                for(Player player : playersInGame){
                    dataManager.get(player).recordRoundSurvived();
                }
                if(currentRound % 5 == 0) {
                    Bukkit.broadcastMessage(String.format("\n    %s%sRound %d%s\n",
                            ChatColor.GREEN, ChatColor.BOLD, currentRound,
                            isSingleplayerMode ? "" : String.format("\n    %s%d%s players remaining!",
                                    ChatColor.GOLD, playersInGame.size(), ChatColor.GRAY)));
                }
            }
        }
        else if(countdownTime == 10){
            displayGameTitle(String.format("%s%s%d", ChatColor.GREEN, ChatColor.BOLD, countdownTime), null, 0, 40, 20);
        }
        else if(countdownTime <= 3){
            // Display countdown time as title
            displayGameTitle(String.format("%s%s%d", ChatColor.RED, ChatColor.BOLD, countdownTime), null, 0, 15, 5);
        }
    }

    public void healPlayer(Player player, double amount){
        double health = player.getHealth();
        if(health + amount > 20) player.setHealth(20);
        else player.setHealth(health + amount);
    }

    public void makePlayerSpectator(Player player){
        player.setGameMode(GameMode.SPECTATOR);
        spectators.add(player);
        player.sendTitle(String.format("%s%sSpectating", ChatColor.GREEN, ChatColor.BOLD),
                "You are spectating the game", 20, 60, 20);
        Location specLoc = maze.centerOfMaze();
        specLoc.setY(specLoc.getY() + 10);
        player.teleport(specLoc);
    }

    public void killPlayer(Player player, String reason){
        playersInGame.remove(player);
        deadPlayers.add(player);
        makePlayerSpectator(player);
        timeLeftBossbar.removePlayer(player);
        player.sendTitle(String.format("%s%sYou Died!", ChatColor.RED, ChatColor.BOLD), reason, 20, 60, 20);
        // Send summary
        PlayerData data = dataManager.get(player);
        player.sendMessage(String.format("\n    %s%s== SUMMARY ==%s\n     - Rounds Survived: %s%d%s\n" +
                "     - Safe Pad Firsts: %s%d%s\n     - Total Score: %s%d%s\n     - Death: %s\n",
                ChatColor.GREEN, ChatColor.BOLD, ChatColor.RESET,
                ChatColor.GOLD, data.getRoundsSurvived(), ChatColor.RESET,
                ChatColor.GOLD, data.getSafePadsFirst(), ChatColor.RESET,
                ChatColor.GOLD, data.getScore(), ChatColor.RESET,
                reason == null ? "Health loss" : reason));
        if((isSingleplayerMode && playersInGame.size() == 0) ||
                (!isSingleplayerMode && playersInGame.size() <= 1)){
            finishGame();
        }
        else{
            Bukkit.broadcastMessage(String.format("%s%s died! %d players remain",
                    ChatColor.YELLOW, player.getDisplayName(), playersInGame.size()));
        }
        resetPlayer(player);
    }

    public void killPlayer(Player player){
        killPlayer(player, null);
    }

    public void finishGame(){
        running = false;
        Bukkit.broadcastMessage(String.format("\n%s     Game over!%s\n%s     Rounds survived: %s%d\n",
                ChatColor.RED, ChatColor.RESET, ChatColor.GRAY, ChatColor.GOLD, currentRound - 1));
        resetGame();
    }

    public void handlePlayerLeave(Player player){
        if(playerIsInGame(player)){
            resetPlayer(player);
            playersInGame.remove(player);
            if(playersInGame.size() <= 1){
                finishGame();
            }
        }
        if(spectators.contains(player)){
            spectators.remove(player);
        }
    }

    public void resetPlayer(Player player){
        getLogger().info(String.format("Restting player %s...", player.getDisplayName()));
        teleportPlayerToSpawnRoom(player);
        player.setVelocity(new Vector().setX(0).setY(0).setZ(0));
        player.removePotionEffect(PotionEffectType.JUMP);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
        player.setCollidable(false);
        player.setGameMode(GameMode.ADVENTURE);
        timeLeftBossbar.removePlayer(player);
        player.getInventory().clear();
        // Remove invisible effect entirely
        /*for(Player otherPlayer : Bukkit.getOnlinePlayers()){
            if(otherPlayer != player){
                undoInvisibleEffect(otherPlayer, player);
                undoInvisibleEffect(player, otherPlayer);
            }
        }*/
    }

    public void resetGame(){
        for(Player player : getServer().getOnlinePlayers()){
            resetPlayer(player);
        }
        for(MazePlatform platform : platforms){
            platform.restoreOriginal();
            platform.removeIndicator();
        }
        if(releasePlayersTask != -1)
            Bukkit.getScheduler().cancelTask(releasePlayersTask);
        if(gameTickTask != -1)
            Bukkit.getScheduler().cancelTask(gameTickTask);
        platforms.clear();
        lastPlatform = null;
        currentPlatform = null;
        running = false;
        playersAreReleased = false;
        playersInGame.clear();
        dataManager.reset();
        spectators.clear();
        removeAllEntities();
    }

    public boolean canSummonEntity(){
        return runners.size() < MAX_RUNNERS;
    }

    public Entity summonEntity(Location location){
        Snowman snowman = (Snowman) location.getWorld().spawnEntity(location, EntityType.SNOWMAN);
        snowman.setMetadata("special-entity", specialEntity);
        snowman.setAI(false);
        snowman.setCollidable(false);
        snowman.setInvulnerable(true);
        snowman.setGravity(false);
        MazeRunner runner = new MazeRunner(snowman);
        runner.setMaze(maze);
        runners.add(runner);
        return snowman;
    }

    public void removeAllEntities(){
        for(MazeRunner runner : runners){
            runner.getEntity().remove();
        }
        runners.clear();
    }

    public long deltaTime(){
        long newTime = System.currentTimeMillis();
        long delta = newTime - lastUpdateTime;
        lastUpdateTime = newTime;
        return delta;
    }

    public void startGameTick(){
        gameTickTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            long dt = deltaTime();
            for(MazeRunner runner : runners){
                runner.update(dt);
            }
            for(int i = 0; i < platforms.size(); i++){
                MazePlatform platform = platforms.get(i);
                platform.update(dt);
                if(platform.isExpired()){
                    platform.restoreOriginal();
                    platform.removeIndicator();
                    platforms.remove(platform);
                }
            }
            for(int i = 0; i < playersInGame.size(); i++){
                Player player = playersInGame.get(i);
                PlayerData data = dataManager.get(player);
                if(playersAreReleased && currentPlatform.entityIsOnPlatform(player)){
                    if(currentPlatform.isUntouched()){
                        if(!isSingleplayerMode)
                            Bukkit.broadcastMessage(String.format("%s%s%s made it to the safe pad first!",
                                    ChatColor.GREEN, player.getDisplayName(), ChatColor.RESET));
                        skipAheadTime(11);
                        currentPlatform.setUntouched(false);
                        healPlayer(player, 2);
                        data.addTokens(2);
                        data.recordSafePadFirst();
                        data.addScore(2);
                        setPlayerTokens(player, data.getTokens());
                    }
                    if (!currentPlatform.playerHasVisited(player)) {
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        player.sendMessage(String.format("%sYou made it to the safe pad!", ChatColor.GREEN));
                        healPlayer(player, 2);
                        data.addTokens(1);
                        data.addScore(1);
                        setPlayerTokens(player, data.getTokens());
                        currentPlatform.recordPlayerVisit(player);
                        if(currentPlatform.visitCount() >= playersInGame.size()){
                            skipAheadTime(4);
                        }
                    }
                }
                List<Entity> nearby = player.getWorld().getNearbyEntities(
                        player.getLocation(),
                        HORIZONTAL_HIT_DISTANCE, VERTICAL_HIT_DISTANCE, HORIZONTAL_HIT_DISTANCE,
                        (Entity) -> Entity.hasMetadata("special-entity"))
                        .stream().toList();
                if(nearby.size() > 0){
                    if(boopCooldown.testEntity(player))
                        if(boopPlayer(player, nearby.get(0))){
                            i--; // Player died from the boop :(
                            continue;
                        }
                }
                if(player.getLocation().getY() < maze.getStartLocation().getY() - 0.9){
                    killPlayer(player, "You fell off the maze!");
                    i--;
                }
            }
        }, CHECK_DELAY, CHECK_DELAY);
    }

    // Returns whether or not the player was killed by this boop
    public boolean boopPlayer(Player player, Entity entity){
        Vector velocity = new Vector();
        velocity.setY(VERTICAL_BOOP_FORCE);
        double angleBetween = Math.atan2(entity.getLocation().getX() - player.getLocation().getX(),
                entity.getLocation().getZ() - player.getLocation().getZ());
        velocity.setZ(-Math.cos(angleBetween) * HORIZONTAL_BOOP_FORCE);
        velocity.setX(-Math.sin(angleBetween) * HORIZONTAL_BOOP_FORCE);
        player.setVelocity(velocity);
        if(player.getHealth() <= HEALTH_PER_HIT){
            killPlayer(player);
            return true;
        }
        else{
            player.damage(HEALTH_PER_HIT);
            return false;
        }
    }

    public boolean isRunning(){
        return running;
    }

    public void repulse(Player player){
        if(!playerIsInGame(player) || !playersAreReleased) return;
        PlayerData data = dataManager.get(player);
        if(!data.testPowerupCooldown("repulse")) return;
        if(data.getTokens() >= REPULSE_COST){
            data.setTokens(data.getTokens() - REPULSE_COST);
            doRepulseAt(player.getLocation());
            setPlayerTokens(player, data.getTokens());
        }
        else{
            player.sendMessage(ChatColor.RED + "Not enough tokens! Costs " + REPULSE_COST);
        }
    }

    public void doRepulseAt(Location location){
        List<Entity> nearby = location.getWorld().getNearbyEntities(
                        location,
                        REPULSE_RANGE, REPULSE_RANGE, REPULSE_RANGE,
                        (Entity) -> Entity.hasMetadata("special-entity"))
                .stream().toList();
        for(Entity entity : nearby){
            runners.remove(entity);
            entity.remove();
            Firework firework = entity.getWorld().spawn(new Location(
                    entity.getLocation().getWorld(),
                    entity.getLocation().getX(),
                    entity.getLocation().getY() + 1,
                    entity.getLocation().getZ()
            ), Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.setPower(2);
            FireworkEffect effect = FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.BLACK).build();
            meta.addEffect(effect);
            firework.setFireworkMeta(meta);

            firework.detonate();
        }
    }

    public void oneUp(Player player){
        if(!playerIsInGame(player) || !playersAreReleased) return;
        PlayerData data = dataManager.get(player);
        if(!data.testPowerupCooldown("one-up")) return;
        if(data.getTokens() >= ONE_UP_COST){
            data.setTokens(data.getTokens() - ONE_UP_COST);
            healPlayer(player, 6);
            player.sendMessage(ChatColor.GREEN + "You have been healed!");
            setPlayerTokens(player, data.getTokens());
        }
        else{
            player.sendMessage(ChatColor.RED + "Not enough tokens! Costs " + ONE_UP_COST);
        }
    }

    public void speedBoost(Player player){
        if(!playerIsInGame(player) || !playersAreReleased) return;
        PlayerData data = dataManager.get(player);
        if(!data.testPowerupCooldown("speed-boost")) return;
        if(data.getTokens() >= SPEED_BOOST_COST){
            data.setTokens(data.getTokens() - SPEED_BOOST_COST);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, false, false));
            setPlayerTokens(player, data.getTokens());
        }
        else{
            player.sendMessage(ChatColor.RED + "Not enough tokens! Costs " + SPEED_BOOST_COST);
        }
    }

    /*public void makeInvisibleTo(Player viewer, Player invisiblePlayer){
        getLogger().info(String.format("Making %s invisible to %s", invisiblePlayer.getDisplayName(), viewer.getDisplayName()));
        //PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_EFFECT);
        DataWatcher watcher = ((CraftPlayer) invisiblePlayer).getHandle().getDataWatcher();
        watcher.set(new DataWatcherObject<>(0, DataWatcherRegistry.a), (byte)0x20);
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(invisiblePlayer.getEntityId(),
                watcher, false);
        /*PacketContainer packet = PacketContainer.fromPacket(
                new PacketPlayOutEntityEffect(viewer.getEntityId(),
                        new MobEffect(MobEffectList.fromId(14), 99999, 1)));*//*
        //packet.getIntegers().write(0, invisiblePlayer.getEntityId());
        //packet.getBytes().write(0, (byte)(14 & 255));
        //packet.getSpecificModifier(MobEffect.class).write(0, new MobEffect(MobEffectList.fromId(14)));
        //packet.getIntegers().write(1, 1);
        try {
            pm.sendServerPacket(viewer, PacketContainer.fromPacket(packet));
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }*/

    /*public void undoInvisibleEffect(Player viewer, Player invisiblePlayer){
        getLogger().info(String.format("Making %s visible to %s", invisiblePlayer.getDisplayName(), viewer.getDisplayName()));
        //PacketContainer packet = pm.createPacket(PacketType.Play.Server.REMOVE_ENTITY_EFFECT);
        //packet.getIntegers().write(0, invisiblePlayer.getEntityId());
        //packet.getBytes().write(0, (byte) 14);
        //packet.getSpecificModifier(MobEffectList.class).write(0, MobEffectList.fromId(14));

        DataWatcher watcher = ((CraftPlayer) invisiblePlayer).getHandle().getDataWatcher();
        watcher.set(new DataWatcherObject<>(0, DataWatcherRegistry.a), (byte)0);
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(invisiblePlayer.getEntityId(),
                watcher, false);

        try {
            pm.sendServerPacket(viewer, PacketContainer.fromPacket(packet));
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }*/

    public Maze getMaze() {
        return maze;
    }

    public boolean isClassicMode() {
        return isClassicMode;
    }

    public void setClassicMode(boolean classicMode) {
        isClassicMode = classicMode;
    }

    public Template getIndicatorTemplate(){
        return indicator;
    }

    public NamespacedKey getMazeItemKey() {
        return mazeItemKey;
    }
}
