package edu.southwestern.tasks.mario;

import java.util.List;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.EvaluationOptions;
import competition.cig.robinbaumgarten.AStarAgent;
import edu.southwestern.MMNEAT.MMNEAT;
import edu.southwestern.evolution.genotypes.Genotype;
import edu.southwestern.parameters.CommonConstants;
import edu.southwestern.tasks.NoisyLonerTask;
import edu.southwestern.tasks.mario.level.MarioLevelUtil;
import edu.southwestern.util.datastructures.Pair;

/**
 * 
 * Evolve Mario levels using an agent,
 * like the Mario A* Agent, as a means of evaluating.
 * Levels can be generated by CPPNs or a GAN, but this is
 * done in child classes.
 * 
 * @author Jacob Schrum
 *
 * @param <T>
 */
public abstract class MarioLevelTask<T> extends NoisyLonerTask<T> {
	public static boolean useProgressPlusJumpsFitness = true;
	
	private Agent agent;
	
	public MarioLevelTask() {
		// Replace this with a command line parameter
		agent = new AStarAgent();
		
		// Fitness
		if(useProgressPlusJumpsFitness) {
			MMNEAT.registerFitnessFunction("ProgressPlusJumps");
		} else {
			MMNEAT.registerFitnessFunction("ProgressPlusTime");
		}
        // Other scores
        MMNEAT.registerFitnessFunction("Distance", false);
        MMNEAT.registerFitnessFunction("PercentDistance", false);
        MMNEAT.registerFitnessFunction("Time", false);
        MMNEAT.registerFitnessFunction("Jumps", false);

	}
	
	@Override
	public int numObjectives() {
		// First maximize progress through the level.
		// If the level is cleared, then maximize the duration of the
		// level, which will indicate that it is challenging.
		return 1;  
	}
	
	public int numOtherScores() {
		return 4; // Distance, Percentage, Time, and Jumps
	}

	@Override
	public double getTimeStamp() {
		return 0; // Not used
	}

	/**
	 * Different level generators use the genotype to generate a level in different ways
	 * @param individual
	 * @return
	 */
	public abstract Level getMarioLevelFromGenotype(Genotype<T> individual);
	
	/**
	 * Different level generators generate levels of different lengths
	 * @param info 
	 * @return
	 */
	public abstract double totalPassableDistance(EvaluationInfo info);
	
	@Override
	public Pair<double[], double[]> oneEval(Genotype<T> individual, int num) {
		Level level = getMarioLevelFromGenotype(individual);
		agent.reset(); // Get ready to play a new level
		EvaluationOptions options = new CmdLineOptions(new String[]{});
		options.setAgent(agent);
        options.setLevel(level);
        options.setMaxFPS(true);
        options.setVisualization(CommonConstants.watch);
		List<EvaluationInfo> infos = MarioLevelUtil.agentPlaysLevel(options);
		// For now, assume a single evaluation
		EvaluationInfo info = infos.get(0);
		double distancePassed = info.lengthOfLevelPassedPhys;
		double percentLevelPassed = distancePassed / totalPassableDistance(info);
		double time = info.timeSpentOnLevel;
		double jumps = info.jumpActionsPerformed;

		double[] otherScores = new double[] {distancePassed, percentLevelPassed, time, jumps};
		
		if(percentLevelPassed < 1.0) {
			//System.out.println(distancePassed + " < " + (totalDistanceInLevel - SPACE_AT_LEVEL_END));
			// If level is not completed, score the amount of distance covered.
			// This is true whether time or jumps are added.
			return new Pair<double[],double[]>(new double[]{percentLevelPassed}, otherScores);
		} else { // Level beaten
			//System.out.println("BEAT LEVEL");
			if(useProgressPlusJumpsFitness) { // Add Jumps to favor harder levels requiring more jumping
				return new Pair<double[],double[]>(new double[]{1.0+jumps}, otherScores);
			} else { // Add in the time so that more complicated, challenging levels will be favored
				return new Pair<double[],double[]>(new double[]{1.0+time}, otherScores);
			}
		}
	}
	
}
