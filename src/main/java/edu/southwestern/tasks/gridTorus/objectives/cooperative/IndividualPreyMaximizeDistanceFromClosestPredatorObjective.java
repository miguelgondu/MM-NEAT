package edu.southwestern.tasks.gridTorus.objectives.cooperative;

import edu.southwestern.evolution.Organism;
import edu.southwestern.gridTorus.TorusAgent;
import edu.southwestern.networks.Network;
import edu.southwestern.tasks.gridTorus.objectives.GridTorusObjective;

/**
 * 
 * @author rollinsa
 * 
 *         reward the prey for being as far from the closest predator as possible at
 *         the end of the game. If the prey is dead then it receives the lowest 
 *         possible score (zero)
 */
public class IndividualPreyMaximizeDistanceFromClosestPredatorObjective<T extends Network> extends GridTorusObjective<T> {

	private int preyIndex = -1;

	/**
	 * Creates the objective for the fitness for the given prey 
	 * @param i index of the prey
	 */
	public IndividualPreyMaximizeDistanceFromClosestPredatorObjective(int i){
		preyIndex = i;
	}

	@Override
	/**
	 *         reward the prey for being as far from the closest predator as possible at
	 *         the end of the game. If the prey is dead then it receives the lowest 
	 *         possible score (zero)
	 */
	public double fitness(Organism<T> individual) {
		TorusAgent prey = game.getPrey()[preyIndex];

		//if the prey is null, it was eaten, give min score of 0
		if(prey == null)
			return 0;
		
		return prey.distance(prey.closestAgent(game.getPredators()));
	}

}
