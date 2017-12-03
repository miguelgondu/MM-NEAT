package edu.southwestern.tasks.interactive.mario;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.human.HumanKeyboardAgent;
import ch.idsia.mario.engine.level.Level;
import edu.southwestern.MMNEAT.MMNEAT;
import edu.southwestern.evolution.genotypes.Genotype;
import edu.southwestern.networks.Network;
import edu.southwestern.parameters.Parameters;
import edu.southwestern.tasks.interactive.InteractiveEvolutionTask;
import edu.southwestern.tasks.mario.level.MarioLevelUtil;

public class MarioLevelBreederTask<T extends Network> extends InteractiveEvolutionTask<T> {
	// Should exceed any of the CPPN inputs or other interface buttons
	public static final int PLAY_BUTTON_INDEX = -20; 
	
	private boolean initializationComplete = false;
	protected JSlider levelWidthSlider; // Allows for changing levelWidth
	
	public MarioLevelBreederTask() throws IllegalAccessException {
		super();
		//Construction of JSlider to determine length of generated CPPN amplitude
		// Width ranged from 20 to 200 blocks
		levelWidthSlider = new JSlider(JSlider.HORIZONTAL, 20, 200, Parameters.parameters.integerParameter("marioLevelLength"));
		levelWidthSlider.setMinorTickSpacing(10000);
		levelWidthSlider.setPaintTicks(true);
//		Hashtable<Integer,JLabel> labels = new Hashtable<>();
//		labels.put(20, new JLabel("Shorter clip"));
//		labels.put(200, new JLabel("Longer clip"));
//		levelWidthSlider.setLabelTable(labels);
		levelWidthSlider.setPaintLabels(true);
		levelWidthSlider.setPreferredSize(new Dimension(200, 40));

		/**
		 * Changed level width picture previews
		 */
		levelWidthSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if(!initializationComplete) return;
				// get value
				JSlider source = (JSlider)e.getSource();
				if(!source.getValueIsAdjusting()) {

					int newLength = (int) source.getValue();

					Parameters.parameters.setInteger("marioLevelLength", newLength);
					// reset buttons
					resetButtons(true);
				}
			}
		});
		
		if(!Parameters.parameters.booleanParameter("simplifiedInteractiveInterface")) {
			top.add(levelWidthSlider);	
		}
		
		//Construction of button that lets user plays the level
		JButton play = new JButton("Play");
		// Name is first available numeric label after the input disablers
		play.setName("" + PLAY_BUTTON_INDEX);
		play.addActionListener(this);
		top.add(play);
		initializationComplete = true;
	}

	@Override
	public String[] sensorLabels() {
		// Consider using radial distance from bottom-left
		return new String[] {"x-coordinate","y-coordinate","bias"};
	}

	@Override
	public String[] outputLabels() {
		return new String[] {"Present?", "Rock", "Breakable", "Question", "Coin", "Pipes", "Goomba", "GreenKoopa", "RedKoopa", "Spiky","Winged?"};
	}

	@Override
	protected String getWindowTitle() {
		return "Mario Level Breeder";
	}

	@Override
	protected void save(String file, int i) {
		// TODO Make the representation richer before providing a way to save
		
	}

	@Override
	protected BufferedImage getButtonImage(T phenotype, int width, int height, double[] inputMultipliers) {
		Level level = MarioLevelUtil.generateLevelFromCPPN(phenotype, inputMultipliers, Parameters.parameters.integerParameter("marioLevelLength"));
		BufferedImage image = MarioLevelUtil.getLevelImage(level);
		return image;
	}

	/**
	 * Responds to a button to actually play a selected level
	 */
	protected boolean respondToClick(int itemID) {
		boolean undo = super.respondToClick(itemID);
		if(undo) return true; // Click must have been a bad activation checkbox choice. Skip rest
		// Human plays level
		if(itemID == PLAY_BUTTON_INDEX && selectedCPPNs.size() > 0) {
			Network cppn = scores.get(selectedCPPNs.get(selectedCPPNs.size() - 1)).individual.getPhenotype();
			Level level = MarioLevelUtil.generateLevelFromCPPN(cppn, inputMultipliers, Parameters.parameters.integerParameter("marioLevelLength"));
			Agent agent = new HumanKeyboardAgent();
			// Must launch game in own thread, or won't animate or listen for events
			new Thread() {
				public void run() {
					MarioLevelUtil.agentPlaysLevel(level, agent);
				}
			}.start();
		}
		return false; // no undo: every thing is fine
	}	
	
	@Override
	protected void additionalButtonClickAction(int scoreIndex, Genotype<T> individual) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getFileType() {
		return "Text File";
	}

	@Override
	protected String getFileExtension() {
		return "txt";
	}

	@Override
	public int numCPPNInputs() {
		return this.sensorLabels().length;
	}

	@Override
	public int numCPPNOutputs() {
		return this.outputLabels().length;
	}

	public static void main(String[] args) {
		try {
			MMNEAT.main(new String[]{"runNumber:0","randomSeed:1","trials:1","mu:16","maxGens:500","io:false","netio:false","mating:true","fs:false","task:edu.southwestern.tasks.interactive.mario.MarioLevelBreederTask","allowMultipleFunctions:true","ftype:0","watch:true","netChangeActivationRate:0.3","cleanFrequency:-1","simplifiedInteractiveInterface:false","recurrency:false","saveAllChampions:true","cleanOldNetworks:false","ea:edu.southwestern.evolution.selectiveBreeding.SelectiveBreedingEA","imageWidth:2000","imageHeight:2000","imageSize:200","includeFullSigmoidFunction:true","includeFullGaussFunction:true","includeCosineFunction:true","includeGaussFunction:false","includeIdFunction:true","includeTriangleWaveFunction:true","includeSquareWaveFunction:true","includeFullSawtoothFunction:true","includeSigmoidFunction:false","includeAbsValFunction:false","includeSawtoothFunction:false"});
		} catch (FileNotFoundException | NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
}
