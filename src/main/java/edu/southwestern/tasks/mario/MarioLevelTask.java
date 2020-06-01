package edu.southwestern.tasks.mario;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.EvaluationOptions;
import edu.southwestern.MMNEAT.MMNEAT;
import edu.southwestern.evolution.GenerationalEA;
import edu.southwestern.evolution.genotypes.Genotype;
import edu.southwestern.evolution.mapelites.Archive;
import edu.southwestern.evolution.mapelites.MAPElites;
import edu.southwestern.parameters.CommonConstants;
import edu.southwestern.parameters.Parameters;
import edu.southwestern.scores.Score;
import edu.southwestern.tasks.NoisyLonerTask;
import edu.southwestern.tasks.mario.level.LevelParser;
import edu.southwestern.tasks.mario.level.MarioLevelUtil;
import edu.southwestern.tasks.mario.level.MarioState;
import edu.southwestern.tasks.mario.level.MarioState.MarioAction;
import edu.southwestern.tasks.mario.level.OldLevelParser;
import edu.southwestern.util.ClassCreation;
import edu.southwestern.util.MiscUtil;
import edu.southwestern.util.datastructures.ArrayUtil;
import edu.southwestern.util.datastructures.Pair;
import edu.southwestern.util.file.FileUtilities;
import edu.southwestern.util.graphics.GraphicsUtil;
import edu.southwestern.util.random.RandomNumbers;
import edu.southwestern.util.search.AStarSearch;
import edu.southwestern.util.search.Search;


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

	private static final int SEGMENT_WIDTH_IN_BLOCKS = 28; // GAN training window
	private static final int PIXEL_BLOCK_WIDTH = 16; // Is this right?

	private Agent agent;
	private int numFitnessFunctions;
	private boolean fitnessRequiresSimulation;
	private boolean segmentFitness;
	private ArrayList<List<Integer>> targetLevel = null;

	public static final int DECORATION_FREQUENCY_STAT_INDEX = 0;
	public static final int LENIENCY_STAT_INDEX = 1;
	public static final int NEGATIVE_SPACE_STAT_INDEX = 2;
	public static final int NUM_SEGMENT_STATS = 3;

	// Calculated in oneEval, so it can be passed on the getBehaviorVector
	private ArrayList<Double> behaviorVector;

	public MarioLevelTask() {
		// Replace this with a command line parameter
		try {
			agent = (Agent) ClassCreation.createObject("marioLevelAgent");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			System.out.println("Could not instantiate Mario agent");
			System.exit(1);
		}

		// Fitness
		numFitnessFunctions = 0;
		fitnessRequiresSimulation = false; // Until proven otherwise
		segmentFitness = false;
		if(Parameters.parameters.booleanParameter("marioProgressPlusJumpsFitness")) {
			// First maximize progress through the level.
			// If the level is cleared, then maximize the duration of the
			// level, which will indicate that it is challenging.
			MMNEAT.registerFitnessFunction("ProgressPlusJumps");
			fitnessRequiresSimulation = true;
			numFitnessFunctions++;
		} 
		if(Parameters.parameters.booleanParameter("marioProgressPlusTimeFitness")) {
			// Levels that take longer must be harder
			MMNEAT.registerFitnessFunction("ProgressPlusTime");
			fitnessRequiresSimulation = true;
			numFitnessFunctions++;
		}
		if(Parameters.parameters.booleanParameter("marioLevelMatchFitness")) {
			MMNEAT.registerFitnessFunction("LevelMatch");
			numFitnessFunctions++;
			// Load level representation from file here
			String levelFileName = Parameters.parameters.stringParameter("marioTargetLevel"); // Does not have a default value yet
			targetLevel = MarioLevelUtil.listLevelFromVGLCFile(levelFileName);

			// View whole dungeon layout
			Level level = Parameters.parameters.booleanParameter("marioGANUsesOriginalEncoding") ? OldLevelParser.createLevelJson(targetLevel) : LevelParser.createLevelJson(targetLevel);			
			BufferedImage image = MarioLevelUtil.getLevelImage(level);
			String saveDir = FileUtilities.getSaveDirectory();
			GraphicsUtil.saveImage(image, saveDir + File.separator + "Target.png");

		}
		// Encourages an alternating pattern of Vanessa's objectives
		if(Parameters.parameters.booleanParameter("marioLevelAlternatingLeniency")) {
			MMNEAT.registerFitnessFunction("AlternatingLeniency");
			segmentFitness = true;
			numFitnessFunctions++;
		}
		if(Parameters.parameters.booleanParameter("marioLevelAlternatingNegativeSpace")) {
			MMNEAT.registerFitnessFunction("AlternatingNegativeSpace");
			segmentFitness = true;
			numFitnessFunctions++;			
		}
		if(Parameters.parameters.booleanParameter("marioLevelAlternatingDecoration")) {
			MMNEAT.registerFitnessFunction("AlternatingDecorationFrequency");
			segmentFitness = true;
			numFitnessFunctions++;
		}
		// Encourages an periodic pattern of Vanessa's objectives
		if(Parameters.parameters.booleanParameter("marioLevelPeriodicLeniency")) {
			MMNEAT.registerFitnessFunction("PeriodicLeniency");
			segmentFitness = true;
			numFitnessFunctions++;
		}
		if(Parameters.parameters.booleanParameter("marioLevelPeriodicNegativeSpace")) {
			MMNEAT.registerFitnessFunction("PeriodicNegativeSpace");
			segmentFitness = true;
			numFitnessFunctions++;			
		}
		if(Parameters.parameters.booleanParameter("marioLevelPeriodicDecoration")) {
			MMNEAT.registerFitnessFunction("PeriodicDecorationFrequency");
			segmentFitness = true;
			numFitnessFunctions++;
		}

		// Encourages a symmetric pattern of Vanessa's objectives
		if(Parameters.parameters.booleanParameter("marioLevelSymmetricLeniency")) {
			MMNEAT.registerFitnessFunction("SymmetricLeniency");
			segmentFitness = true;
			numFitnessFunctions++;			
		}
		if(Parameters.parameters.booleanParameter("marioLevelSymmetricNegativeSpace")) {
			MMNEAT.registerFitnessFunction("SymmetricNegativeSpace");
			segmentFitness = true;
			numFitnessFunctions++;						
		}
		if(Parameters.parameters.booleanParameter("marioLevelSymmetricDecoration")) {
			MMNEAT.registerFitnessFunction("SymmetricDecorationFrequency");
			segmentFitness = true;
			numFitnessFunctions++;			
		}

		if(Parameters.parameters.booleanParameter("marioSimpleAStarDistance")) {
			MMNEAT.registerFitnessFunction("SimpleA*Distance");
			numFitnessFunctions++;			
		}

		if(Parameters.parameters.booleanParameter("marioRandomFitness")) {
			MMNEAT.registerFitnessFunction("Random");
			numFitnessFunctions++;
		}
		if(Parameters.parameters.booleanParameter("marioDistinctSegmentFitness")) {
			MMNEAT.registerFitnessFunction("Random");
			numFitnessFunctions++;
		}
		if(numFitnessFunctions == 0) throw new IllegalStateException("At least one fitness function required to evolve Mario levels");
		// Other scores
		MMNEAT.registerFitnessFunction("Distance", false);
		MMNEAT.registerFitnessFunction("PercentDistance", false);
		MMNEAT.registerFitnessFunction("Time", false);
		MMNEAT.registerFitnessFunction("Jumps", false);
		for(int i=0; i<Parameters.parameters.integerParameter("marioGANLevelChunks"); i++){
			MMNEAT.registerFitnessFunction("DecorationFrequency-"+i,false);
			MMNEAT.registerFitnessFunction("Leniency-"+i,false);
			MMNEAT.registerFitnessFunction("NegativeSpace-"+i,false);
		}

	}

	@Override
	public int numObjectives() {
		return numFitnessFunctions;  
	}

	public int numOtherScores() {
		return 4 + Parameters.parameters.integerParameter("marioGANLevelChunks") * 3; // Distance, Percentage, Time, and Jumps 
		//plus (decorationFrequency, leniency, negativeSpace) per level segment
	}

	@Override
	public double getTimeStamp() {
		return 0; // Not used
	}

	/**
	 * Different level generators use the genotype to generate a level in different ways
	 * @param individual Genotype 
	 * @return List of lists of integers corresponding to tile types
	 */
	public abstract ArrayList<List<Integer>> getMarioLevelListRepresentationFromGenotype(Genotype<T> individual);

	/**
	 * Different level generators generate levels of different lengths
	 * @param info 
	 * @return
	 */
	public abstract double totalPassableDistance(EvaluationInfo info);

	@SuppressWarnings("unchecked")
	@Override
	public Pair<double[], double[]> oneEval(Genotype<T> individual, int num) {
		EvaluationInfo info = null;
		BufferedImage levelImage = null;
		ArrayList<List<Integer>> oneLevel = getMarioLevelListRepresentationFromGenotype(individual);
		Level level = Parameters.parameters.booleanParameter("marioGANUsesOriginalEncoding") ? OldLevelParser.createLevelJson(oneLevel) : LevelParser.createLevelJson(oneLevel);			
		if(fitnessRequiresSimulation || CommonConstants.watch) {
			agent.reset(); // Get ready to play a new level
			EvaluationOptions options = new CmdLineOptions(new String[]{});
			options.setAgent(agent);
			options.setLevel(level);
			options.setMaxFPS(!(agent instanceof ch.idsia.ai.agents.human.HumanKeyboardAgent)); // Run fast when not playing
			options.setVisualization(CommonConstants.watch);

			List<EvaluationInfo> infos = MarioLevelUtil.agentPlaysLevel(options);
			// For now, assume a single evaluation
			info = infos.get(0);
		}
		
		if(MMNEAT.ea instanceof MAPElites || CommonConstants.watch) {
			// View whole dungeon layout
			levelImage = MarioLevelUtil.getLevelImage(level);
			if(segmentFitness) { // Draw lines dividing the segments 
				Graphics2D g = (Graphics2D) levelImage.getGraphics();
				g.setColor(Color.MAGENTA);
				g.setStroke(new BasicStroke(4)); // Thicker line
				for(int i = 1; i < Parameters.parameters.integerParameter("marioGANLevelChunks"); i++) {
					g.drawLine(i*PIXEL_BLOCK_WIDTH*SEGMENT_WIDTH_IN_BLOCKS, 0, i*PIXEL_BLOCK_WIDTH*SEGMENT_WIDTH_IN_BLOCKS, levelImage.getHeight());
				}
			}
			
			// MAP Elites images get saved later, in a different directory
			if(!(MMNEAT.ea instanceof MAPElites)) {
				String saveDir = FileUtilities.getSaveDirectory();
				int currentGen = ((GenerationalEA) MMNEAT.ea).currentGeneration();
				GraphicsUtil.saveImage(levelImage, saveDir + File.separator + (currentGen == 0 ? "initial" : "gen"+ currentGen) + File.separator + "MarioLevel"+individual.getId()+".png");
			}
		}


		double distancePassed = info == null ? 0 : info.lengthOfLevelPassedPhys;
		double percentLevelPassed = info == null ? 0 : distancePassed / totalPassableDistance(info);
		double time = info == null ? 0 : info.timeSpentOnLevel;
		double jumps = info == null ? 0 : info.jumpActionsPerformed;
		int numDistinctSegments;
		//put each segment into a HashSet to see if it's  distinct
		HashSet<ArrayList<List<Integer>>> k = new HashSet<ArrayList<List<Integer>>>();
        ArrayList<ArrayList<List<Integer>>> levelWithParsedSegments = MarioLevelUtil.getSegmentsFromLevel(oneLevel, SEGMENT_WIDTH_IN_BLOCKS);
        //int numSegments = 0;
        for(ArrayList<List<Integer>> segment : levelWithParsedSegments) {
        	k.add(segment);
        	//numSegments++;
        }
//		int numSegments = levelWithParsedSegments.size();
		numDistinctSegments = k.size();

		
		
		double[] otherScores = new double[] {distancePassed, percentLevelPassed, time, jumps};
		// Adds Vanessa's Mario stats: Decoration Frequency, Leniency, Negative Space
		ArrayList<double[]> lastLevelStats = LevelParser.getLevelStats(oneLevel, SEGMENT_WIDTH_IN_BLOCKS);
		for(double[] stats:lastLevelStats){
			otherScores = ArrayUtils.addAll(otherScores, stats);
		}

		ArrayList<Double> fitnesses = new ArrayList<>(numFitnessFunctions);
		if(Parameters.parameters.booleanParameter("marioProgressPlusJumpsFitness")) {
			if(percentLevelPassed < 1.0) {
				fitnesses.add(percentLevelPassed);
			} else { // Level beaten
				fitnesses.add(1.0+jumps);
			}
		} 
		if(Parameters.parameters.booleanParameter("marioProgressPlusTimeFitness")) {
			if(percentLevelPassed < 1.0) {
				fitnesses.add(percentLevelPassed);
			} else { // Level beaten
				fitnesses.add(1.0+time);
			}
		}
		if(Parameters.parameters.booleanParameter("marioLevelMatchFitness")) {
			int diffCount = 0;

			if(oneLevel.size() != targetLevel.size()) {
				System.out.println("Target");
				System.out.println(targetLevel);
				System.out.println("Evolved");
				System.out.println(oneLevel);
				throw new IllegalStateException("Target level and evolved level are not even the same height.");
			}

			// This will hold the target level, except that every location of conflict with the evolved level will
			// be replaced with the blank passable background tile
			ArrayList<List<Integer>> targetDiff = new ArrayList<>();

			// TODO
			// Should this calculation include or eliminate the starting and ending regions we add to Mario levels?
			Iterator<List<Integer>> evolveIterator = oneLevel.iterator();
			Iterator<List<Integer>> targetIterator = targetLevel.iterator();
			while(evolveIterator.hasNext() && targetIterator.hasNext()) {
				Iterator<Integer> evolveRow = evolveIterator.next().iterator();
				Iterator<Integer> targetRow = targetIterator.next().iterator();
				List<Integer> diffRow = new ArrayList<>(targetLevel.get(0).size()); // For visualizing differences
				while(evolveRow.hasNext() && targetRow.hasNext()) {
					Integer nextInTarget = targetRow.next();
					if(!evolveRow.next().equals(nextInTarget)) {
						diffCount++;
						diffRow.add(-100); // An illegal tile. Indicates a conflict
					} else {
						diffRow.add(nextInTarget);
					}
				}
				targetDiff.add(diffRow);
			}
			// More differences = worse fitness
			fitnesses.add(-1.0*diffCount);

			if(CommonConstants.watch) {
				// View whole level layout
				Level diffLevel = Parameters.parameters.booleanParameter("marioGANUsesOriginalEncoding") ? OldLevelParser.createLevelJson(targetDiff) : LevelParser.createLevelJson(targetDiff);			
				BufferedImage image = MarioLevelUtil.getLevelImage(diffLevel);
				String saveDir = FileUtilities.getSaveDirectory();
				int currentGen = ((GenerationalEA) MMNEAT.ea).currentGeneration();
				GraphicsUtil.saveImage(image, saveDir + File.separator + (currentGen == 0 ? "initial" : "gen"+ currentGen) + File.separator + "MarioLevel"+individual.getId()+"TargetDiff.png");
			}
		}

		// Encourages an alternating pattern of Vanessa's objectives
		if(Parameters.parameters.booleanParameter("marioLevelAlternatingLeniency")) {
			fitnesses.add(alternatingStatScore(lastLevelStats, LENIENCY_STAT_INDEX));
		}
		if(Parameters.parameters.booleanParameter("marioLevelAlternatingNegativeSpace")) {
			fitnesses.add(alternatingStatScore(lastLevelStats, NEGATIVE_SPACE_STAT_INDEX));
		}
		if(Parameters.parameters.booleanParameter("marioLevelAlternatingDecoration")) {
			fitnesses.add(alternatingStatScore(lastLevelStats, DECORATION_FREQUENCY_STAT_INDEX));
		}

		// Encourages a periodic pattern of Vanessa's objectives
		if(Parameters.parameters.booleanParameter("marioLevelPeriodicLeniency")) {
			fitnesses.add(periodicStatScore(lastLevelStats, LENIENCY_STAT_INDEX));
		}
		if(Parameters.parameters.booleanParameter("marioLevelPeriodicNegativeSpace")) {
			fitnesses.add(periodicStatScore(lastLevelStats, NEGATIVE_SPACE_STAT_INDEX));
		}
		if(Parameters.parameters.booleanParameter("marioLevelPeriodicDecoration")) {
			fitnesses.add(periodicStatScore(lastLevelStats, DECORATION_FREQUENCY_STAT_INDEX));
		}

		// Encourages a symmetric pattern of Vanessa's objectives
		if(Parameters.parameters.booleanParameter("marioLevelSymmetricLeniency")) {
			fitnesses.add(symmetricStatScore(lastLevelStats, LENIENCY_STAT_INDEX));
		}
		if(Parameters.parameters.booleanParameter("marioLevelSymmetricNegativeSpace")) {
			fitnesses.add(symmetricStatScore(lastLevelStats, NEGATIVE_SPACE_STAT_INDEX));
		}
		if(Parameters.parameters.booleanParameter("marioLevelSymmetricDecoration")) {
			fitnesses.add(symmetricStatScore(lastLevelStats, DECORATION_FREQUENCY_STAT_INDEX));
		}

		double simpleAStarDistance = -1;
		if(Parameters.parameters.booleanParameter("marioSimpleAStarDistance")) {
			MarioState start = new MarioState(MarioState.preprocessLevel(oneLevel));
			Search<MarioAction,MarioState> search = new AStarSearch<>(MarioState.moveRight);
			HashSet<MarioState> mostRecentVisited = null;
			ArrayList<MarioAction> actionSequence = null;
			try{
				actionSequence = ((AStarSearch<MarioAction, MarioState>) search).search(start, true, Parameters.parameters.integerParameter("aStarSearchBudget"));
				if(actionSequence == null) {
					fitnesses.add(-1.0); // failed search 				
				} else {
					simpleAStarDistance = 1.0*actionSequence.size(); // For MAP Elites bin later
					fitnesses.add(1.0*actionSequence.size()); // maximize length of solution
				}
			} catch(IllegalStateException e) {
				// Sometimes this exception occurs from A*. Not sure why, but we can take this to mean the level has a problem and deserves bad fitness.
				fitnesses.add(-1.0); // failed search 				
			} finally {
				mostRecentVisited = ((AStarSearch<MarioAction, MarioState>) search).getVisited();
			}

			if(MMNEAT.ea instanceof MAPElites || (CommonConstants.netio && CommonConstants.watch)) {
				// Add X marks to the original level image, which should exist if since watch saved it above
				if(mostRecentVisited != null) {
					Graphics2D g = (Graphics2D) levelImage.getGraphics();
					g.setColor(Color.BLUE);
					g.setStroke(new BasicStroke(4)); // Thicker line
					for(MarioState s : mostRecentVisited) {
						int x = s.marioX - LevelParser.BUFFER_WIDTH;
						int y = s.marioY;
						g.drawLine(x*PIXEL_BLOCK_WIDTH, y*PIXEL_BLOCK_WIDTH, (x+1)*PIXEL_BLOCK_WIDTH, (y+1)*PIXEL_BLOCK_WIDTH);
						g.drawLine((x+1)*PIXEL_BLOCK_WIDTH, y*PIXEL_BLOCK_WIDTH, x*PIXEL_BLOCK_WIDTH, (y+1)*PIXEL_BLOCK_WIDTH);
					}
					
					if(actionSequence != null) {
						MarioState current = start;
						g.setColor(Color.RED);
						for(MarioAction a : actionSequence) {
							int x = current.marioX - LevelParser.BUFFER_WIDTH;
							int y = current.marioY;
							g.drawLine(x*PIXEL_BLOCK_WIDTH, y*PIXEL_BLOCK_WIDTH, (x+1)*PIXEL_BLOCK_WIDTH, (y+1)*PIXEL_BLOCK_WIDTH);
							g.drawLine((x+1)*PIXEL_BLOCK_WIDTH, y*PIXEL_BLOCK_WIDTH, x*PIXEL_BLOCK_WIDTH, (y+1)*PIXEL_BLOCK_WIDTH);
							current = (MarioState) current.getSuccessor(a);
						}
					}
				}

				if(!(MMNEAT.ea instanceof MAPElites)) {
					// View level with path
					String saveDir = FileUtilities.getSaveDirectory();
					int currentGen = ((GenerationalEA) MMNEAT.ea).currentGeneration();
					GraphicsUtil.saveImage(levelImage, saveDir + File.separator + (currentGen == 0 ? "initial" : "gen"+ currentGen) + File.separator + "MarioLevel"+individual.getId()+"SolutionPath.png");
				}
			}
		}

		if(Parameters.parameters.booleanParameter("marioRandomFitness")) {
			fitnesses.add(RandomNumbers.fullSmallRand());
		}
		if(Parameters.parameters.booleanParameter("marioDistinctSegmentFitness")) {
			fitnesses.add(new Double(numDistinctSegments));
		}
		// Could conceivably also be used for behavioral diversity instead of map elites, but this would be a weird behavior vector from a BD perspective
		if(MMNEAT.ea instanceof MAPElites) {
			// Assign to the behavior vector before using MAP-Elites
			double[] archiveArray;
			//int binIndex;
			int x1,x2,x3;
			double leniencySum = sumStatScore(lastLevelStats, LENIENCY_STAT_INDEX);
			final double DECORATION_SCALE = 3;
			final double NEGATIVE_SPACE_SCALE = 3;
			// Scale scores so that we are less likely to overstep the bounds of the bins
			final int BINS_PER_DIMENSION = Parameters.parameters.integerParameter("marioGANLevelChunks");
			double decorationSum = sumStatScore(lastLevelStats, DECORATION_FREQUENCY_STAT_INDEX);
			double negativeSpaceSum = sumStatScore(lastLevelStats, NEGATIVE_SPACE_STAT_INDEX);
			int leniencySumIndex = Math.min(Math.max((int)((leniencySum*(BINS_PER_DIMENSION/2)+0.5)*BINS_PER_DIMENSION),0), BINS_PER_DIMENSION-1); //LEANIENCY BIN INDEX
			int decorationBinIndex =  Math.min((int)(decorationSum*DECORATION_SCALE*BINS_PER_DIMENSION), BINS_PER_DIMENSION-1); //decorationBinIndex
			int negativeSpaceSumIndex = Math.min((int)(negativeSpaceSum*NEGATIVE_SPACE_SCALE*BINS_PER_DIMENSION), BINS_PER_DIMENSION-1); //negative space index
			double binScore = simpleAStarDistance;
			

			if(((MAPElites<T>) MMNEAT.ea).getBinLabelsClass() instanceof MarioMAPElitesDecorNSAndLeniencyBinLabels) {
				x1 = leniencySumIndex;
				x2 = decorationBinIndex;
				x3 = negativeSpaceSumIndex;

				archiveArray = new double[BINS_PER_DIMENSION*BINS_PER_DIMENSION*BINS_PER_DIMENSION];
				
			}else if(((MAPElites<T>) MMNEAT.ea).getBinLabelsClass() instanceof MarioMAPElitesDistinctChunksNSAndLeniencyBinLabels) {
				//double decorationSum = sumStatScore(lastLevelStats, DECORATION_FREQUENCY_STAT_INDEX);
				x1 = leniencySumIndex;
				x2 = numDistinctSegments; //number of distinct segments
				x3 = negativeSpaceSumIndex;
			
				// Row-major order lookup in 3D archive
				archiveArray = new double[(BINS_PER_DIMENSION+1)*BINS_PER_DIMENSION*BINS_PER_DIMENSION];
			}else if(((MAPElites<T>) MMNEAT.ea).getBinLabelsClass() instanceof MarioMAPElitesDistinctChunksNSAndDecorationBinLabels) {
				double decorationAlternating = alternatingStatScore(lastLevelStats, DECORATION_FREQUENCY_STAT_INDEX);
				double negativeSpaceAlternating = sumStatScore(lastLevelStats, NEGATIVE_SPACE_STAT_INDEX);
				
				decorationBinIndex = Math.min((int)(decorationAlternating*DECORATION_SCALE*BINS_PER_DIMENSION*10), BINS_PER_DIMENSION-1);
				negativeSpaceSumIndex = Math.min((int)(negativeSpaceAlternating*NEGATIVE_SPACE_SCALE*BINS_PER_DIMENSION), BINS_PER_DIMENSION-1);
				
				x1 = decorationBinIndex;
				x2 = numDistinctSegments;
				x3 = negativeSpaceSumIndex;
				archiveArray = new double[(BINS_PER_DIMENSION+1)*BINS_PER_DIMENSION*BINS_PER_DIMENSION];

				
			}
			
			else {
				throw new RuntimeException("A Valid Binning Scheme For Mario Was Not Specified");
			}
			// Row-major order lookup in 3D archive
			setBinsAndSaveMAPElitesImages(individual, levelImage, archiveArray, x1, x2, x3, BINS_PER_DIMENSION, binScore);

		}
		return new Pair<double[],double[]>(ArrayUtil.doubleArrayFromList(fitnesses), otherScores);
		
	}

	@SuppressWarnings("unchecked")
	/**
	 * sets the bins and saves MAPElites images to archive
	 * @param individual the genotype
	 * @param levelImage the buffered image of the level
	 * @param archiveArray the archive array
	 * @param x1 the first bin dimension
	 * @param x2 the second bin dimension
	 * @param x3 the third bin dimension
	 * @param BINS_PER_DIMENSION the bins per dimension
	 * @param binScore the bin score
	 */
	private void setBinsAndSaveMAPElitesImages(Genotype<T> individual, BufferedImage levelImage, double[] archiveArray,
			int x1, int x2, int x3, final int BINS_PER_DIMENSION, double binScore) {
		int binIndex;
		binIndex = (x2*BINS_PER_DIMENSION + x3)*BINS_PER_DIMENSION + x1;
		Arrays.fill(archiveArray, Double.NEGATIVE_INFINITY); // Worst score in all dimensions
		archiveArray[binIndex] = binScore; // Percent rooms traversed

		System.out.println("["+x2+"]["+x3+"]["+x1+"] = "+binScore);

		behaviorVector = ArrayUtil.doubleVectorFromArray(archiveArray);

		// Saving map elites bin images	
		if(CommonConstants.netio) {
			System.out.println("Save archive images");
//				@SuppressWarnings("unchecked")
			Archive<T> archive = ((MAPElites<T>) MMNEAT.ea).getArchive();
			List<String> binLabels = archive.getBinMapping().binLabels();

			// Index in flattened bin array
			Score<T> elite = archive.getElite(binIndex);
			// If the bin is empty, or the candidate is better than the elite for that bin's score
			if(elite == null || binScore > elite.behaviorVector.get(binIndex)) {
				String fileName = String.format("%7.5f", binScore) + "_" + individual.getId() + ".png";
				String binPath = archive.getArchiveDirectory() + File.separator + binLabels.get(binIndex);
				String fullName = binPath + "_" + fileName;
				System.out.println(fullName);
				GraphicsUtil.saveImage(levelImage, fullName);
				
			}
		}
	}

	private double sumStatScore(ArrayList<double[]> levelStats, int statIndex) {
		double total = 0;
		for(int i = 0; i < levelStats.size(); i++) {
			total += levelStats.get(i)[statIndex];
		}
		return total;
	}

	private double periodicStatScore(ArrayList<double[]> levelStats, int statIndex) {
		double evenTotal = 0;
		// even differences
		for(int i = 2; i < levelStats.size(); i += 2) {
			// Differences between even segments
			evenTotal += Math.abs(levelStats.get(i-2)[statIndex] - levelStats.get(i)[statIndex]);
		}
		double oddTotal = 0;
		// odd differences
		for(int i = 3; i < levelStats.size(); i += 2) {
			// Differences between odd segments
			oddTotal += Math.abs(levelStats.get(i-2)[statIndex] - levelStats.get(i)[statIndex]);
		}
		// Negative because differences are discouraged
		return - (evenTotal + oddTotal);
	}

	private double symmetricStatScore(ArrayList<double[]> levelStats, int statIndex) {
		double total = 0;
		for(int i = 0; i < levelStats.size()/2; i++) {
			// Diff between symmetric segments
			total += Math.abs(levelStats.get(i)[statIndex] - levelStats.get(levelStats.size()-1-i)[statIndex]);
		}
		return - total; // Negative: Max symmetry means minimal difference in symmetric segments
	}

	private double alternatingStatScore(ArrayList<double[]> levelStats, int statIndex) {
		double total = 0;
		for(int i = 1; i < levelStats.size(); i++) {
			// Differences between adjacent segments
			total += Math.abs(levelStats.get(i-1)[statIndex] - levelStats.get(i)[statIndex]);
		}
		return total;
	}

	// It is assumed that the data needed to fill this is computed in oneEval, saved globally, and then returned here.
	// This is primarily meant to be used with MAP Elites, so it is an unusual behavior vector. It is really a vector of bins, where
	// the agent's score in each bin is set ... but a given Mario level should really only be in one of the bins.
	public ArrayList<Double> getBehaviorVector() {
		return behaviorVector;
	}

}
