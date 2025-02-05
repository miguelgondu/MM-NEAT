package edu.southwestern.tasks.motests;

import edu.southwestern.evolution.Organism;
import edu.southwestern.evolution.fitness.FitnessFunction;
import edu.southwestern.evolution.genotypes.Genotype;
import edu.southwestern.evolution.genotypes.RealValuedGenotype;
import edu.southwestern.MMNEAT.MMNEAT;
import edu.southwestern.scores.MultiObjectiveScore;
import edu.southwestern.scores.Score;
import edu.southwestern.tasks.LonerTask;
import edu.southwestern.tasks.motests.testfunctions.FunctionOptimizationSet;
import java.util.ArrayList;

/**
 *
 * @author Jacob Schrum
 */
public class FunctionOptimization extends LonerTask<ArrayList<Double>> {

	@SuppressWarnings("rawtypes")
	FitnessFunction[] functions;
	double sign;
	OptimizationDisplay display;
	/**
	 * Used to make sure that only parent point updates are shown *
	 */
	boolean parent = true;
	boolean addToFront = false;
	int frontSamples = 300;

	@SuppressWarnings("rawtypes")
	public FunctionOptimization(FitnessFunction[] functions, double sign) {
		this.functions = functions;
		for (FitnessFunction f : functions) {
			MMNEAT.registerFitnessFunction(f.getClass().getSimpleName());
		}
		this.sign = Math.signum(sign);
		this.display = new OptimizationDisplay();
	}

	public FunctionOptimization(FunctionOptimizationSet fos, double sign) {
		this(fos.getFitnessFunctions(), sign);

		// Generate the true Pareto front using expert knowledge
		addToFront = true;
		try {
			double[] bounds = fos.frontDecisionValuesBoundsOfFirst();
			double start = bounds[0];
			double step = (bounds[1] - bounds[0]) / frontSamples;
			for (int i = 0; i <= frontSamples; i++) {
				double x1 = start + (i * step);
				double[] xs = fos.frontDecisionValuesInTermsOfFirst(x1);
				RealValuedGenotype individual = new RealValuedGenotype(xs);
				evaluate(individual);
			}
		} catch (UnsupportedOperationException e) {
			System.out.println("The true Pareto front is not known");
		}
		addToFront = false;
	}

	@Override
	public ArrayList<Score<ArrayList<Double>>> evaluateAll(ArrayList<Genotype<ArrayList<Double>>> population) {
		if (parent) {
			display.clear();
		}
		ArrayList<Score<ArrayList<Double>>> result = super.evaluateAll(population);
		parent = !parent;
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Score<ArrayList<Double>> evaluate(Genotype<ArrayList<Double>> individual) {
		// System.out.println(individual);
		double[] scores = new double[numObjectives()];
		for (int i = 0; i < functions.length; i++) {
			scores[i] = sign * functions[i].fitness(new Organism<ArrayList<Double>>(individual));
		}
		// Update individual point positions
		// System.out.println("score:" + Arrays.toString(scores));
		if (parent) {
			display.addPoint(sign * scores[0], sign * scores[1], addToFront);
		}

		return new MultiObjectiveScore<ArrayList<Double>>(individual, scores, null);
	}

	public int numObjectives() {
		return functions.length;
	}

	public double getTimeStamp() {
		return 0;
	}
}
