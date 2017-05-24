package edu.utexas.cs.nn.tasks.interactive.remixesizer;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.utexas.cs.nn.evolution.genotypes.Genotype;
import edu.utexas.cs.nn.networks.Network;
import edu.utexas.cs.nn.parameters.Parameters;
import edu.utexas.cs.nn.tasks.interactive.InteractiveEvolutionTask;
import edu.utexas.cs.nn.util.graphics.DrawingPanel;
import edu.utexas.cs.nn.util.graphics.GraphicsUtil;
import edu.utexas.cs.nn.util.sound.PlayDoubleArray;
import edu.utexas.cs.nn.util.sound.SoundFromCPPNUtil;

public class RemixesizerTask<T extends Network> extends InteractiveEvolutionTask<T> {

	private static final int FREQUENCY_DEFAULT = 440; //default frequency of generated amplitude: A440

	//ideal numbers to initialize AudioFormat; based on obtaining formats of a series of WAV files
	//These numbers are interesting because they conflict with the AudioFormat that is used
	//to save files from a CPPN. (saving files from CPPN uses 16 bit signed format)
	public static final float DEFAULT_SAMPLE_RATE = 11025; //default frame rate is same value
	public static final int DEFAULT_BIT_RATE = 8; 
	public static final int DEFAULT_CHANNEL = 1; 
	public static final int BYTES_PER_FRAME = 1; 


	private static final int TIME_CHECKBOX_INDEX = -25;
	private static final int SINE_OF_TIME_CHECKBOX_INDEX = -26;
	private static final int WAV_CHECKBOX_INDEX = -27;
	private static final int BIAS_CHECKBOX_INDEX = -28;

	private static final int TIME_INPUT_INDEX = 0;
	private static final int SINE_OF_TIME_INPUT_INDEX = 1;
	private static final int WAV_INPUT_INDEX = 2;
	private static final int BIAS_INPUT_INDEX = 3;

	public static final int CPPN_NUM_INPUTS	= 4;
	public static final int CPPN_NUM_OUTPUTS = 1;

	public RemixesizerTask() throws IllegalAccessException {
		super();
		//Checkboxes to control if x, y, distance from center, or bias effects appear on the console
		JCheckBox timeEffect = new JCheckBox("Time", true);
		inputMultipliers[TIME_INPUT_INDEX] = 1.0;
		JCheckBox sineOfTimeEffect = new JCheckBox("Sine(time)", true); //no spaces because of scanner in actionPerformed
		inputMultipliers[SINE_OF_TIME_INPUT_INDEX] = 1.0;
		JCheckBox wavEffect = new JCheckBox("CPPN", true);
		inputMultipliers[WAV_INPUT_INDEX] = 1.0;
		JCheckBox biasEffect = new JCheckBox("Bias", true);
		inputMultipliers[BIAS_INPUT_INDEX] = 1.0;

		timeEffect.setName("" + TIME_CHECKBOX_INDEX);
		sineOfTimeEffect.setName("" + SINE_OF_TIME_CHECKBOX_INDEX);
		wavEffect.setName("" + WAV_CHECKBOX_INDEX);
		biasEffect.setName("" + BIAS_CHECKBOX_INDEX);

		timeEffect.addActionListener(this);
		sineOfTimeEffect.addActionListener(this);
		wavEffect.addActionListener(this);
		biasEffect.addActionListener(this);
	}

	@Override
	public String[] sensorLabels() {
		return new String[] { "Time", "Sine of time", "Wav file input", "bias" };
	}

	@Override
	public String[] outputLabels() {
		return new String[] { "amplitude" };
	}

	@Override
	protected String getWindowTitle() {
		return "Breederemix";
	}

	protected void respondToClick(int itemID) {
		super.respondToClick(itemID);
		// Extra checkboxes specific to Remixesizer
		if(itemID == TIME_CHECKBOX_INDEX){ // If time checkbox is clicked
			setEffectCheckBox(TIME_INPUT_INDEX);
		}else if(itemID == SINE_OF_TIME_CHECKBOX_INDEX){ // If sine of time checkbox is clicked
			setEffectCheckBox(SINE_OF_TIME_INPUT_INDEX);
		}else if(itemID == WAV_CHECKBOX_INDEX){ // If CPPN checkbox is clicked
			setEffectCheckBox(WAV_INPUT_INDEX);
		}else if(itemID == BIAS_CHECKBOX_INDEX){ // If bias checkbox is clicked
			setEffectCheckBox(BIAS_INPUT_INDEX);
		} 
	}

	@Override
	protected void save(int i) {
		//SAVING IMAGE

		// Use of imageHeight and imageWidth allows saving a higher quality image than is on the button
		BufferedImage toSave = getButtonImage((Network)scores.get(i).individual.getPhenotype(), Parameters.parameters.integerParameter("imageWidth"), Parameters.parameters.integerParameter("imageHeight"), inputMultipliers);
		DrawingPanel p = GraphicsUtil.drawImage(toSave, "" + i, toSave.getWidth(), toSave.getHeight());
		JFileChooser chooser = new JFileChooser();//used to get save name 
		chooser.setApproveButtonText("Save");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("BMP Images", "bmp");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(frame);
		if(returnVal == JFileChooser.APPROVE_OPTION) {//if the user decides to save the image
			System.out.println("You chose to call the image: " + chooser.getSelectedFile().getName());
			p.save(chooser.getCurrentDirectory() + "\\" + chooser.getSelectedFile().getName() + (showNetwork ? "network" : "image") + ".bmp");
			System.out.println("image " + chooser.getSelectedFile().getName() + " was saved successfully");
			p.setVisibility(false);
		} else { //else image dumped
			p.setVisibility(false);
			System.out.println("image not saved");
		}

		//SAVING AUDIO

		chooser = new JFileChooser();
		AudioFormat af = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, DEFAULT_SAMPLE_RATE, DEFAULT_BIT_RATE, DEFAULT_CHANNEL, BYTES_PER_FRAME, DEFAULT_SAMPLE_RATE, true);
		chooser.setApproveButtonText("Save");
		FileNameExtensionFilter audioFilter = new FileNameExtensionFilter("WAV audio files", "wav");
		chooser.setFileFilter(audioFilter);
		int audioReturnVal = chooser.showOpenDialog(frame);
		if(audioReturnVal == JFileChooser.APPROVE_OPTION) {//if the user decides to save the image
			System.out.println("You chose to call the file: " + chooser.getSelectedFile().getName());
			try {
				SoundFromCPPNUtil.saveFileFromCPPN(scores.get(i).individual.getPhenotype(), Parameters.parameters.integerParameter("clipLength"), FREQUENCY_DEFAULT, chooser.getSelectedFile().getName() + ".wav", af);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("audio file " + chooser.getSelectedFile().getName() + " was saved successfully");
			p.setVisibility(false);
		} else { //else image dumped
			p.setVisibility(false);
			System.out.println("audio file not saved");
		}	

	}

	@Override
	protected BufferedImage getButtonImage(Network phenotype, int width, int height, double[] inputMultipliers) {
		double[] amplitude = SoundFromCPPNUtil.amplitudeGenerator(phenotype, Parameters.parameters.integerParameter("clipLength"), FREQUENCY_DEFAULT, inputMultipliers);
		BufferedImage wavePlotImage = GraphicsUtil.wavePlotFromDoubleArray(amplitude, height, width);
		return wavePlotImage;
	}

	@Override
	protected void additionalButtonClickAction(Genotype<T> individual) {
		Network phenotype = individual.getPhenotype();
		double[] amplitude = SoundFromCPPNUtil.amplitudeGenerator(phenotype, Parameters.parameters.integerParameter("clipLength"), FREQUENCY_DEFAULT, inputMultipliers);
		PlayDoubleArray.playDoubleArray(amplitude);	

	}

}
