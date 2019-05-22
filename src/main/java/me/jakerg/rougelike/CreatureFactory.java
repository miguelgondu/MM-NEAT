package me.jakerg.rougelike;

import asciiPanel.AsciiPanel;
import edu.southwestern.tasks.gvgai.zelda.level.Dungeon;

/**
 * Factory class to take care of creating new enemies or a player
 * @author gutierr8
 *
 */
public class CreatureFactory {
    private World world;

    public CreatureFactory(World world){
        this.world = world;
    }
    
    /**
     * Create a new player with Player ai, no dungeon
     * @return Creature with player AI
     */
    public Creature newPlayer(){
        Creature player = new Creature(world, '@', AsciiPanel.brightWhite);
        world.addAtEmptyLocation(player);
        new PlayerAi(player);
        return player;
    }
    
    /**
     * Create an enemy at given coordinates
     * @param x x point to place enemy
     * @param y y point to place enemy
     * @return Creature with enemy ai
     */
    public Creature newEnemey(int x, int y) {
    	Creature enemy = new Creature(world, 'e', AsciiPanel.brightRed, 1, 1, 0);
    	world.addCreatureAt(x, y, enemy);
    	new EnemyAi(enemy);
    	return enemy;
    }
    
    /**
     * Create a creature with a player ai and a dungeon
     * @param dungeon Dungeon for the player to interact with
     * @return Creature with player ai
     */
    public Creature newDungeonPlayer(Dungeon dungeon) {
    	Creature player = new Creature(world, '@', AsciiPanel.brightWhite, 10, 5, 0, dungeon);
        world.addAtEmptyLocation(player);
        new DungeonAi(player);
        return player;
    }
}
