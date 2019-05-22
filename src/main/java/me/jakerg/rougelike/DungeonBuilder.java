package me.jakerg.rougelike;

import java.util.HashMap;
import java.util.Map.Entry;

import edu.southwestern.tasks.gvgai.zelda.level.*;
import edu.southwestern.tasks.gvgai.zelda.level.Dungeon.Node;
import edu.southwestern.tasks.gvgai.zelda.level.ZeldaDungeon.Level;

/**
 * Dungeon builder keeps track of the different rooms of the dungeon
 * @author gutierr8
 *
 */
public class DungeonBuilder {
	private Tile[][] tiles;
	private Dungeon dungeon;
	private HashMap<String, World> levelWorlds;
	
	/**
	 * Create a new dungeon builder and convert the worlds
	 * @param dungeon
	 */
	public DungeonBuilder(Dungeon dungeon) {
	    this.dungeon = dungeon;
	    createWorlds();
	}
	
	/**
	 * Create the worlds into something that we can understand
	 */
	private void createWorlds() {
		levelWorlds = new HashMap<>();
		HashMap<String, Node> map = dungeon.getLevels();
		for(Entry<String, Node> entry : map.entrySet()) {
			String name = entry.getKey();
			Level level = entry.getValue().level;
			World w = TileUtil.makeWorld(level.getLevel());
			levelWorlds.put(name, w);
		}
	}

	/**
	 * Get the world based on the name
	 * @param n name of world
	 * @return World with given name
	 */
	public World getWorld(String n) {
		return levelWorlds.get(n);
	}
	
	/**
	 * Get the current world (where the player is)
	 * @return World where the player is
	 */
	public World getCurrentWorld() {
		String n = dungeon.getCurrentlevel().name;
		return getWorld(n);
	}
	
	/**
	 * Set the world
	 * @param n Name of world
	 * @param w World instance
	 */
	public void setWorld(String n, World w) {
		levelWorlds.put(n, w);
	}
}
