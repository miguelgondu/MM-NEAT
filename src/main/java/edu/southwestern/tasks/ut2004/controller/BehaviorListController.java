package edu.southwestern.tasks.ut2004.controller;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import edu.southwestern.tasks.ut2004.actions.BotAction;
import edu.southwestern.tasks.ut2004.actions.EmptyAction;
import edu.southwestern.tasks.ut2004.controller.behaviors.BehaviorModule;
import java.util.ArrayList;

/**
 * Creates and populates an ArrayList of behavior modules
 * @author Jacob Schrum
 */
public class BehaviorListController implements BotController {

	/**
	 * constructs an ArrayList of the bot's behaviour modules
	 */
	public ArrayList<BehaviorModule> behaviors;
	private String name;
	private String skin;

	/**
	 * initializes the BehaviorListController as a blank ArrayList
	 */
	public BehaviorListController() {
		this(new ArrayList<BehaviorModule>());
	}

	/**
	 * initializes the BehaviorListController with a given ArrayList
	 * @param behaviors (ArrayList of behaviors)
	 */
	public BehaviorListController(ArrayList<BehaviorModule> behaviors) {
		// Default skin and name
		this(behaviors, "BehaviorListController", "Aliens.AlienFemaleA");
	}
	
	public BehaviorListController(ArrayList<BehaviorModule> behaviors, String name, String skin) {
		this.behaviors = behaviors;
		this.name = name;
		this.skin = skin;
	}

	/**
	 * selects an action for the bot to execute
	 * @return returns a controller for the bot
	 */
	public BotAction control(@SuppressWarnings("rawtypes") UT2004BotModuleController bot) {
		for (int i = 0; i < behaviors.size(); i++) {
			BehaviorModule mod = behaviors.get(i);
			if (mod.trigger(bot)) {
				return mod.control(bot);
			}
		}
		return new EmptyAction();
	}

	/**
	 * initializes the controller
	 */
	public void initialize(@SuppressWarnings("rawtypes") UT2004BotModuleController bot) {
		for (int i = 0; i < behaviors.size(); i++) {
			behaviors.get(i).initialize(bot);
		}
		bot.getBot().getBotName().setNameBase(name);
	}

	/**
	 * resets the controller to be reprogrammed
	 */
	public void reset(@SuppressWarnings("rawtypes") UT2004BotModuleController bot) {
		for (int i = 0; i < behaviors.size(); i++) {
			behaviors.get(i).reset(bot);
		}
	}

	@Override
	public String getSkin() {
		return skin;
	}
}
