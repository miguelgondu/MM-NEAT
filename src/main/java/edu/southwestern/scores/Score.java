package edu.southwestern.scores;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.southwestern.evolution.ScoreHistory;
import edu.southwestern.evolution.genotypes.Genotype;
import edu.southwestern.parameters.CommonConstants;
import edu.southwestern.util.ClassCreation;
import edu.southwestern.util.datastructures.ArrayUtil;
import edu.southwestern.util.datastructures.Pair;
import edu.southwestern.util.stats.Statistic;

/**
 * This is a class that keeps track of an agent's score, the number of
 * evaluations performed on it and the behaviors available to the agent. It has
 * multiple getter methods and actions to be performed on the score.
 *
 * @author Jacob Schrum
 */
public class Score<T> {
	
	// Use a scoreHistoryStat aggregation when averaging scores across whole score history
	static {
		if(CommonConstants.averageScoreHistory) {
			try {
				scoreHistoryStat = (Statistic) ClassCreation.createObject("noisyTaskStat");
			} catch (NoSuchMethodException ex) {
				ex.printStackTrace();
				System.exit(1);
			}	
		} else scoreHistoryStat = null;
	}
	private static Statistic scoreHistoryStat;
	
	// number of evals performed to determine this agent's score
	public int evals;
	// Array of scores in each objective
	public double[] scores;
	// array of other stats from domain pertinent to performance
	public double[] otherStats;
	// Measure of time the genotype was evaluated for across all evals
	public double totalEvalTime = -1;
	// Average time genotype was evaluated for per eval
	public double averageEvalTime = -1;
	// the genotype of the individual in question
	public Genotype<T> individual;
	// A behavior characterization optionally used with Behavioral Diversity
	private ArrayList<Double> behaviorVector;
	// If assigned, then can be used instead of the wasteful behavior vector
	private Pair<Integer, Double> oneMAPEliteBinIndexScorePair;

	/**
	 * Allows for more efficient MAP Elites implementation, because a large behavior
	 * vector of mostly empty values is not needed.
	 * @param individual Genotype of individual
	 * @param scores Fitness scores
	 * @param indexMAPEliteBin index in MAP Elites archive corresponding to behavior 
	 * @param score for the behavior slot in the MAP Elites bin
	 */
	public Score(Genotype<T> individual, double[] scores, int indexMAPEliteBin, double score) {
		this(individual, scores, null, new double[0]);
		oneMAPEliteBinIndexScorePair = new Pair<Integer,Double>(indexMAPEliteBin, score);
	}
	
	/**
	 * Default constructor for Score object.
	 * 
	 * @param individual:
	 *            Genotype of the individual in question
	 * @param scores:
	 *            array of all other scores of similar agents in the domain
	 * @param behaviorVector:
	 *            an ArrayList of possible behaviors of the agent
	 */
	public Score(Genotype<T> individual, double[] scores, ArrayList<Double> behaviorVector) {
		this(individual, scores, behaviorVector, new double[0]);
	}

	/**
	 * Default constructor for Score object if other stats are known.
	 * 
	 * @param individual:
	 *            Genotype of the individual in question
	 * @param scores:
	 *            array of all other scores of similar agents in the domain
	 * @param behaviorVector:
	 *            an ArrayList of possible behaviors of the agent
	 * @param otherStats:
	 *            a double array containing other stats from the domain that are
	 *            relevant to the score.
	 */
	public Score(Genotype<T> individual, double[] scores, ArrayList<Double> behaviorVector, double[] otherStats) {
		this(individual, scores, behaviorVector, otherStats, 1);
	}

	/**
	 * Constructor for Score object if all parameters known.
	 * 
	 * @param individual:
	 *            Genotype of the individual in question
	 * @param scores:
	 *            array of all other scores of similar agents in the domain
	 * @param behaviorVector:
	 *            an ArrayList of possible behaviors of the agent
	 * @param otherStats:
	 *            a double array containing other stats from the domain that are
	 *            relevant to the score.
	 * @param evals:
	 *            number of evaluations of the score to be performed.
	 */

	public Score(Genotype<T> individual, double[] scores, ArrayList<Double> behaviorVector, double[] otherStats, int evals) {
		this.evals = evals;
		this.individual = individual;
		this.scores = scores;
		this.otherStats = otherStats;
		this.behaviorVector = behaviorVector;
		
		// This technique is based on LEEA by G. Morse and K. Stanley:
		// http://eplex.cs.ucf.edu/papers/morse_gecco16.pdf
		// However, the original method proposed seems to cause fitness
		// scores to grow without bound, so I've adjusted it.
		if(CommonConstants.inheritFitness) {
			List<Long> parentIDs = individual.getParentIDs();
			// Get and average all parent scores if there are any
			double[] adjustedLEEAScores = new double[scores.length];
			for(Long id: parentIDs) {
				double[] parentScores = ScoreHistory.getLast(id);
				// Calculate sum
				for(int i = 0; i < adjustedLEEAScores.length; i++) {
					adjustedLEEAScores[i] += parentScores[i];
				}
			}
			// divide for average, and incorporate inherited fitness
			for(int i = 0; i < adjustedLEEAScores.length; i++) {
				if(parentIDs.size() > 0) {
					adjustedLEEAScores[i] /= parentIDs.size(); // average
					adjustedLEEAScores[i] *= CommonConstants.inheritProportion; // decayed
				}
				//adjustedLEEAScores[i] += scores[i]; // original LEEA?
				adjustedLEEAScores[i] += (1 - CommonConstants.inheritProportion)*scores[i]; // weighted average
			}
			// Save adjusted fitness
			ScoreHistory.add(individual.getId(), adjustedLEEAScores);
			this.scores = adjustedLEEAScores;
		}
		
		// Do not use both inheritFitness and averageScoreHistory
		assert !(CommonConstants.inheritFitness && CommonConstants.averageScoreHistory) :
			    "Do not use both inheritFitness and averageScoreHistory";
		
		// My mu/lambda EAs re-evaluate the parents on every
		// generation, which is actually generally not done.
		// This is useful though because most domains in MM-NEAT
		// have noisy evaluations. However, this setting takes
		// further advantage by averaging fitnesses across all
		// evaluations from subsequent generations.
		if(CommonConstants.averageScoreHistory) {
			// Add the raw scores to the history
			ScoreHistory.add(individual.getId(), scores);
			// Get aggregation (default average) across all scores
			this.scores = ScoreHistory.applyStat(individual.getId(), scoreHistoryStat);
		}
	}

	/**
	 * Get behavior characterization score associated with particular index.
	 * Mainly intended for use with MAP Elites, where the behavior vector is
	 * weirdly used to define the bin index. However, the oneMAPEliteBinIndexScorePair
	 * provides a more efficient alternative, and also provides error checking
	 * on index checks.
	 * 
	 * @param index Should be the one MAP Elites archive index corresponding to the behavior of the genotype
	 * @return Fitness/behavior score associated with that bin
	 */
	public double behaviorIndexScore(int index) {
		if(oneMAPEliteBinIndexScorePair != null) {
			if(oneMAPEliteBinIndexScorePair.t1 != index)
				throw new IllegalArgumentException("Should not ask for score associated with MAP Elites bin index that does not match: " + index + " != " + oneMAPEliteBinIndexScorePair.t1);
			return oneMAPEliteBinIndexScorePair.t2;
		} else {
			// Requires storing large, mostly empty ArrayLists
			return behaviorVector.get(index);
		}
	}
	
	/**
	 * Given two Score instances from the same task, add the scores and other
	 * stats of other to the scores and other stats of this score instance to
	 * create a new Score instance (with this Genotype) which is returned.
	 *
	 * @param other
	 *            other Score instance from same task with same scores and
	 *            otherStats
	 * @return sum Score instance
	 */
	public Score<T> add(Score<T> other) {
		if (other == null) {
			return this.copy();
		}

		assert(this.scores.length == other.scores.length);
		assert(this.otherStats.length == other.otherStats.length);

		Score<T> result = new Score<T>(individual, ArrayUtil.zipAdd(scores, other.scores), behaviorVector, ArrayUtil.zipAdd(otherStats, other.otherStats));
		result.evals = this.evals + other.evals;
		return result;
	}

	/**
	 * Divides the score by a value, x.
	 * 
	 * @param x:
	 *            the double by which the score is divided. A unique math
	 *            'trick' was used to make this work, by dividing 1 by x and
	 *            then multiplying the score in each index of the score array by
	 *            that fraction to prevent code-crashing errors, such as
	 *            dividing by 0
	 * @return: returns the score after dividing it
	 */
	public Score<T> divide(double x) {
		Score<T> result = new Score<T>(individual, ArrayUtil.scale(scores, 1.0 / x), behaviorVector, ArrayUtil.scale(otherStats, 1.0 / x));
		return result;
	}

	/**
	 * Average another Score instance with this score instance, using the evals
	 * variable to appropriately weight the contribution of this and other to
	 * form a common average.
	 *
	 * @param other
	 *            Other score instance, possibly null (treated as zero eval)
	 * @return new Score that is the average of this and other
	 */
	public Score<T> incrementalAverage(Score<T> other) {
		if (other == null) {
			return this.copy();
		}
		assert(this.scores.length == other.scores.length);
		assert(this.otherStats.length == other.otherStats.length);

		double[] thisWeightedScores = ArrayUtil.scale(this.scores, (this.evals * 1.0) / (this.evals + other.evals));
		double[] otherWeightedScores = ArrayUtil.scale(other.scores, (other.evals * 1.0) / (this.evals + other.evals));
		double[] scoresAvg = ArrayUtil.zipAdd(thisWeightedScores, otherWeightedScores);

		double[] thisWeightedOtherStats = ArrayUtil.scale(this.otherStats, (this.evals * 1.0) / (this.evals + other.evals));
		double[] otherWeightedOtherStats = ArrayUtil.scale(other.otherStats, (other.evals * 1.0) / (this.evals + other.evals));
		double[] otherStatsAvg = ArrayUtil.zipAdd(thisWeightedOtherStats, otherWeightedOtherStats);

		Score<T> result = new Score<T>(individual, scoresAvg, behaviorVector, otherStatsAvg);
		result.evals = this.evals + other.evals;
		return result;
	}

	// Copies the score.
	@SuppressWarnings("unchecked")
	public Score<T> copy() {
		return new Score<T>(individual, Arrays.copyOf(scores, scores.length), (ArrayList<Double>) behaviorVector.clone(), Arrays.copyOf(otherStats, otherStats.length));
	}

	// Getter method for number of previous scores calculated for agent.
	public int numObjectives() {
		return scores.length;
	}

	// Determines if agent score is better than other.
	public boolean isBetter(Score<T> other) {
		return scores[0] > other.scores[0];
	}

	// Determines if agent score is better or if equal.
	public boolean isAtLeastAsGood(Score<T> other) {
		return scores[0] >= other.scores[0];
	}

	// Determines if agent score is worse than other.
	public boolean isWorse(Score<T> other) {
		return scores[0] < other.scores[0];
	}

	// Adds a new score to score array.
	public void extraScore(double score) {
		double[] newScores = new double[scores.length + 1];
		System.arraycopy(scores, 0, newScores, 0, scores.length);
		newScores[scores.length] = score;
		scores = newScores;
	}

	/**
	 * Delete the score from the last objective from the list of scores, which
	 * also decreases the number of objectives
	 */
	public void dropLastScore() {
		double[] newScores = new double[scores.length - 1];
		System.arraycopy(scores, 0, newScores, 0, newScores.length);
		scores = newScores;
	}

	// Prints the contents of the agent's data to the console.
	public String toString() {
		return (individual == null ? "NULL" : individual.getId()) + ":N=" + evals + ":" + Arrays.toString(scores) + (otherStats != null && otherStats.length > 0 ? Arrays.toString(otherStats) : "");
	}

	// allows behaviorVector to be printed and then set to a new behavoirVector
	public void giveBehaviorVector(ArrayList<Double> behaviorVector) {
		if (behaviorVector != null) {
			System.out.println("Behavior ArrayList: " + behaviorVector);
		}
		this.behaviorVector = behaviorVector;
	}
	
	/**
	 * This is either a domain-specific behavior characterization, or the
	 * vector for the inefficient MAP Elites approach (though this approach is
	 * also required by MAP Elites when using an Innovation Engine).
	 * @return behavior vector.
	 */
	public ArrayList<Double> getTraditionalDomainSpecificBehaviorVector() {
		return this.behaviorVector;
	}

	/**
	 * maxScores finds the largest score and other stats of both agents. If one
	 * of the scores is null, it simply returns the other score. Else, it
	 * returns a new Score object that contains the biggest score and otherStats
	 * between the two agents. Finally, it incrememnts the number of evals
	 * performed on the specific agent.
	 * 
	 * @param other:
	 *            The score of another agent.
	 * @return:The largest of the scores and other stats between the two scores.
	 */
	public Score<T> maxScores(Score<T> other) {
		if (other == null) {
			return this.copy();
		}
		assert(this.scores.length == other.scores.length);
		assert(this.otherStats.length == other.otherStats.length);

		Score<T> result = new Score<T>(individual, ArrayUtil.zipMax(this.scores, other.scores), behaviorVector, ArrayUtil.zipMax(this.otherStats, other.otherStats));
		result.evals = this.evals + other.evals;
		return result;
	}
	
	/**
	 * replaces old scores with new scores
	 * @param newScores
	 */
	public void replaceScores(double[] newScores){
		scores = newScores;
	}

	/**
	 * Individual was not successfully assigned a behavior vector and/or MAP Elites bin index
	 * @return
	 */
	public boolean undefinedBehaviorBin() {
		return behaviorVector == null && oneMAPEliteBinIndexScorePair == null;
	}
}
