package edu.southwestern.tasks.interactive.gvgai;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import edu.southwestern.MMNEAT.MMNEAT;
import edu.southwestern.parameters.Parameters;
import edu.southwestern.tasks.gvgai.GVGAIUtil;
import edu.southwestern.tasks.gvgai.GVGAIUtil.GameBundle;
import edu.southwestern.tasks.gvgai.zelda.ZeldaGANUtil;
import edu.southwestern.tasks.gvgai.zelda.ZeldaVGLCUtil;
import edu.southwestern.tasks.gvgai.zelda.dungeon.Dungeon;
import edu.southwestern.tasks.gvgai.zelda.dungeon.DungeonUtil;
import edu.southwestern.tasks.gvgai.zelda.dungeon.GraphDungeon;
import edu.southwestern.tasks.gvgai.zelda.dungeon.SimpleDungeon;
import edu.southwestern.tasks.gvgai.zelda.dungeon.ZeldaDungeon;
import edu.southwestern.tasks.gvgai.zelda.dungeon.ZeldaDungeon.Level;
import edu.southwestern.tasks.gvgai.zelda.level.ZeldaLevelUtil;
import edu.southwestern.tasks.interactive.InteractiveGANLevelEvolutionTask;
import edu.southwestern.tasks.mario.gan.GANProcess;
import edu.southwestern.util.datastructures.ArrayUtil;
import edu.southwestern.util.datastructures.Pair;
import gvgai.core.game.BasicGame;
import gvgai.core.game.Game;
import gvgai.core.vgdl.VGDLFactory;
import gvgai.core.vgdl.VGDLParser;
import gvgai.core.vgdl.VGDLRegistry;
import gvgai.tracks.singlePlayer.tools.human.Agent;

/**
 * Evolve Zelda rooms using a GAN
 * 
 * @author Jacob Schrum
 */
public class ZeldaGANLevelBreederTask extends InteractiveGANLevelEvolutionTask {

	private static final int DUNGEONIZE_BUTTON_INDEX = -19;
	
	// Change GAME_FILE to zeldacopy "enhanced" version of original GVGAI version to test dungeon
	private static final String GAME_FILE = "zeldacopy";
	private static final String FULL_GAME_FILE = LevelBreederTask.GAMES_PATH + GAME_FILE + ".txt";

	private ZeldaDungeon<ArrayList<Double>> sd;
	
	/**
	 * Initializes the InteractiveGANLevelEvolutionTask and everything required for GVG-AI
	 * @throws IllegalAccessException
	 */
	public ZeldaGANLevelBreederTask() throws IllegalAccessException {
		// false: Has Dungeonize instead of ability to play one room.
		super(false); // Initialize InteractiveGANLevelEvolutionTask
		
		sd = new GraphDungeon();
		
		JButton dungeonize = new JButton("Dungeonize");
		dungeonize.setName("" + DUNGEONIZE_BUTTON_INDEX);
		dungeonize.addActionListener(this);
		top.add(dungeonize);
		
		VGDLFactory.GetInstance().init(); // Get an instant of VGDL Factor and initialize the characters cache
		VGDLRegistry.GetInstance().init(); // Get an instance of VGDL Registry and initialize the sprite factory
	}

	/**
	 * Override to set the window title to associate with our ZeldaGAN
	 * @return String for title of window
	 */
	@Override
	protected String getWindowTitle() {
		return "ZeldaGAN Level Breeder";
	}

	/**
	 * Take the latent vector and use the ZeldaGAN to create a level,
	 * and then a GameBundle used for playing the game.
	 * @param phenotype Latent vector
	 * @return GameBundle for playing GVG-AI game
	 */
	public static GameBundle setUpGameWithLevelFromLatentVector(ArrayList<Double> phenotype) {
		double[] latentVector = ArrayUtil.doubleArrayFromList(phenotype);
		String[] level = ZeldaGANUtil.generateGVGAILevelFromGAN(latentVector, new Point(8,8));
		int seed = 0; // TODO: Use parameter?
		Agent agent = new Agent();
		agent.setup(null, seed, true); // null = no log, true = human 
		Game game = new VGDLParser().parseGame(FULL_GAME_FILE); // Initialize the game	

		return new GameBundle(game, level, agent, seed, 0);
	}
	
	/**
	 * Like setUpGameWithLevelFromLatentVector but accepts a 2D list of integers to generate a game bundle
	 * @param arrayList - 2D list of integers
	 * @return GameBundle for player GVG-AI game
	 */
	public static GameBundle setUpGameWithLevelFromList(List<List<Integer>> arrayList) {
		String[] stringLevel = ZeldaVGLCUtil.convertZeldaRoomListtoGVGAI(arrayList, new Point(8, 8));
		int seed = 0; // TODO: Use parameter?
		Agent agent = new Agent();
		agent.setup(null, seed, true); // null = no log, true = human 
		Game game = new VGDLParser().parseGame(FULL_GAME_FILE); // Initialize the game	

		return new GameBundle(game, stringLevel, agent, seed, 0);
	}
	
	public static GameBundle setUpGameWithDungeon(Dungeon dungeon) {
		String[] stringLevel = dungeon.getCurrentlevel().level.getStringLevel(new Point(5, 8));
		int seed = 0; // TODO: Use parameter?
		Agent agent = new Agent();
		agent.setup(null, seed, true); // null = no log, true = human 
		Game game = new VGDLParser().parseGame(FULL_GAME_FILE); // Initialize the game	

		return new GameBundle(game, stringLevel, agent, seed, 0);
	}

	/**
	 * Creates a BufferedImage that represents the level on the button
	 * @param phenotype Latent vector
	 * @param width Width of image in pixels
	 * @param height Height of image in pixels
	 * @param inputMultipliers Determines whether CPPN is on or off, not used in function
	 * @returns BufferedImage image of the level on the button
	 */
	@Override
	protected BufferedImage getButtonImage(ArrayList<Double> phenotype, int width, int height, double[] inputMultipliers) {
		if(!Parameters.parameters.booleanParameter("gvgAIForZeldaGAN")) {
			Dungeon dummy = new Dungeon();
			List<List<Integer>> ints = ZeldaGANUtil.generateOneRoomListRepresentationFromGAN(ArrayUtil.doubleArrayFromList(phenotype));
			for(List<Integer> row : ints) {
				for(Integer i : row) {
					System.out.print(i + ", ");
				}
				System.out.println();
			}
			Level level = new Level(ints);
			Dungeon.Node n = null;
			try {
				n = dummy.newNode("ASDF", level);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dummy.setCurrentLevel("ASDF");
			return DungeonUtil.getLevelImage(n, dummy);
		} else {
			GameBundle bundle = setUpGameWithLevelFromLatentVector(phenotype); // Use the above function to build our ZeldaGAN
			BufferedImage levelImage = GVGAIUtil.getLevelImage(((BasicGame) bundle.game), bundle.level, (Agent) bundle.agent, width, height, bundle.randomSeed); // Make image of zelda level
			return levelImage;
		}
		
	}

	/**
	 * Set the GAN Process to type ZELDA
	 */
	@Override
	public void configureGAN() {
		GANProcess.type = GANProcess.GAN_TYPE.ZELDA;
	}

	/**
	 * Function to get the file name of the Zelda GAN Model
	 * @returns String the file name of the GAN Model
	 */
	@Override
	public String getGANModelParameterName() {
		return "zeldaGANModel";
	}

	/**
	 * 
	 * @param model Name of the model to reconfigure
	 * @returns Pair of the old latent vector and the net latent vector
	 */
	@Override
	public Pair<Integer, Integer> resetAndReLaunchGAN(String model) {
		int oldLength = GANProcess.latentVectorLength(); // for old model
		// Need to parse the model name to find out the latent vector size
		String dropDataSource = model.substring(model.indexOf("_")+1);
		String dropEpochs = dropDataSource.substring(dropDataSource.indexOf("_")+1);
		String latentSize = dropEpochs.substring(0,dropEpochs.indexOf("."));
		int size = Integer.parseInt(latentSize);
		Parameters.parameters.setInteger("GANInputSize", size);

		boolean fixed = model.startsWith("ZeldaFixed");
		Parameters.parameters.setBoolean("zeldaGANUsesOriginalEncoding", !fixed);
		
		GANProcess.terminateGANProcess();
		// Because Python process was terminated, latentVectorLength will reinitialize with the new params
		int newLength = GANProcess.latentVectorLength(); // new model
		return new Pair<>(oldLength, newLength);
	}

	/**
	 * Set the path of the Zelda GAN Model
	 * @returns String path to GAN model
	 */
	@Override
	public String getGANModelDirectory() {
		return "src"+File.separator+"main"+File.separator+"python"+File.separator+"GAN"+File.separator+"ZeldaGAN";
	}

	/**
	 * Called from window to play the selected Zelda level
	 * @param phenotype Latent vector of the Zelda level to be played
	 */
	@Override
	public void playLevel(ArrayList<Double> phenotype) {
		GameBundle bundle = setUpGameWithLevelFromLatentVector(phenotype);
		// Must launch game in own thread, or won't animate or listen for events
		new Thread() {
			public void run() {
				// True is to watch the game being played
				GVGAIUtil.runOneGame(bundle, true);
			}
		}.start();
	}
	
	@Override
	protected void save(String file, int i) {
		ArrayList<Double> phenotype = scores.get(i).individual.getPhenotype();
		double[] latentVector = ArrayUtil.doubleArrayFromList(phenotype);
		List<List<Integer>> level = ZeldaGANUtil.generateOneRoomListRepresentationFromGAN(latentVector);
		int[][] levelArray = ZeldaLevelUtil.listToArray(level);
		int distance = ZeldaLevelUtil.findMaxDistanceOfLevel(levelArray, 5, 7);

		/**
		 * Rather than save a text representation of the level, I simply save
		 * the latent vector and the model name, which are sufficient to
		 * recreate any level
		 */
		try {
			PrintStream ps = new PrintStream(new File(file));
			// Write String array to text file 
			ps.println(Parameters.parameters.stringParameter(getGANModelParameterName()));
			ps.println(phenotype);
			for(List<Integer> row : level) {
				for(Integer tile : row) {
					ps.print(tile + " ");
				}
				ps.println();
			}
			ps.println("Max Distance : " + distance);
			ps.close();
		} catch (FileNotFoundException e) {
			System.out.println("Could not save file: " + file);
			e.printStackTrace();
			return;
		}
	}

	protected boolean respondToClick(int itemID) {
		boolean undo = super.respondToClick(itemID);
		if (undo) return true;
		if(itemID == DUNGEONIZE_BUTTON_INDEX && selectedItems.size() > 0) {
			ArrayList<ArrayList<Double>> phenotypes = new ArrayList<>();
			for(Integer i : selectedItems) {
				phenotypes.add(scores.get(i).individual.getPhenotype());
			}
			
			try {
				// TODO: Fix: This 5 seems to be completely ignored by both Simple and Graph Dungeon
				sd.showDungeon(phenotypes, 5);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		try {
			// Run the MMNeat Main method with parameters specifying that we want to run the Zedla GAN 
			//MMNEAT.main(new String[]{"runNumber:0","randomSeed:1","showKLOptions:false","allowInteractiveEvolution:false","trials:1","mu:16","zeldaGANModel:ZeldaFixedDungeonsAll_5000_10.pth","maxGens:500","io:false","netio:false","GANInputSize:10","mating:true","fs:false","task:edu.southwestern.tasks.interactive.gvgai.ZeldaGANLevelBreederTask","genotype:edu.southwestern.evolution.genotypes.BoundedRealValuedGenotype","watch:false","cleanFrequency:-1","simplifiedInteractiveInterface:false","saveAllChampions:true","cleanOldNetworks:false","ea:edu.southwestern.evolution.selectiveBreeding.SelectiveBreedingEA","imageWidth:2000","imageHeight:2000","imageSize:200", "zeldaGANUsesOriginalEncoding:false"});
			//MMNEAT.main(new String[]{"runNumber:0","randomSeed:1","showKLOptions:false","showLatentSpaceOptions:false","trials:1","mu:16","zeldaGANModel:ZeldaFixedDungeonsAll_5000_10.pth","maxGens:500","io:false","netio:false","GANInputSize:10","mating:true","fs:false","task:edu.southwestern.tasks.interactive.gvgai.ZeldaGANLevelBreederTask","genotype:edu.southwestern.evolution.genotypes.BoundedRealValuedGenotype","watch:false","cleanFrequency:-1","simplifiedInteractiveInterface:false","saveAllChampions:true","cleanOldNetworks:false","ea:edu.southwestern.evolution.selectiveBreeding.SelectiveBreedingEA","imageWidth:2000","imageHeight:2000","imageSize:200", "zeldaGANUsesOriginalEncoding:false"});
			MMNEAT.main(new String[]{"runNumber:0","randomSeed:1","showKLOptions:false","trials:1","mu:16","zeldaGANModel:ZeldaFixedDungeonsAll_5000_10.pth","maxGens:500","io:false","netio:false","GANInputSize:10","mating:true","fs:false","task:edu.southwestern.tasks.interactive.gvgai.ZeldaGANLevelBreederTask","genotype:edu.southwestern.evolution.genotypes.BoundedRealValuedGenotype","watch:false","cleanFrequency:-1","simplifiedInteractiveInterface:false","saveAllChampions:true","cleanOldNetworks:false","ea:edu.southwestern.evolution.selectiveBreeding.SelectiveBreedingEA","imageWidth:2000","imageHeight:2000","imageSize:200", "zeldaGANUsesOriginalEncoding:false"});
		} catch (FileNotFoundException | NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Transform latent vector into a 2D list representing a Zelda level
	 * @param latentVector 1D double array of the latent vector
	 * @returns List<List<Integer>> 2D list of integers representing tiles of the Zelda level
	 */
	@Override
	public List<List<Integer>> levelListRepresentation(double[] latentVector) {
		return ZeldaGANUtil.generateOneRoomListRepresentationFromGAN(latentVector);
	}
}
