package edu.southwestern.tasks.zelda;

import edu.southwestern.evolution.genotypes.CPPNOrDirectToGANGenotype;
import edu.southwestern.evolution.genotypes.Genotype;
import edu.southwestern.networks.Network;
import edu.southwestern.parameters.Parameters;
import edu.southwestern.tasks.gvgai.zelda.dungeon.Dungeon;
import edu.southwestern.tasks.gvgai.zelda.dungeon.DungeonUtil;
import edu.southwestern.tasks.interactive.gvgai.ZeldaCPPNtoGANLevelBreederTask;
import edu.southwestern.tasks.mario.gan.GANProcess;
import edu.southwestern.util.datastructures.ArrayUtil;

@SuppressWarnings("rawtypes")
public class ZeldaCPPNOrDirectToGANDungeonTask extends ZeldaDungeonTask {

	private int segmentLength;

	public ZeldaCPPNOrDirectToGANDungeonTask() {
		super();
		// These dungeons are generated by CPPN, not grammar
		DungeonUtil.NO_GRAMMAR_AT_ALL = true;
		GANProcess.type = GANProcess.GAN_TYPE.ZELDA;
		segmentLength = GANProcess.latentVectorLength()+ZeldaCPPNtoGANLevelBreederTask.numberOfNonLatentVariables();
	}
	
	@Override
	public Dungeon getZeldaDungeonFromGenotype(Genotype individual) {
		CPPNOrDirectToGANGenotype m = (CPPNOrDirectToGANGenotype) individual;
		if(m.getFirstForm()) {
			System.out.println();

			System.out.println("First form");

			System.out.println();
			return ZeldaCPPNtoGANLevelBreederTask.cppnToDungeon((Network) individual.getPhenotype(), Parameters.parameters.integerParameter("zeldaGANLevelWidthChunks"), Parameters.parameters.integerParameter("zeldaGANLevelHeightChunks"), ArrayUtil.doubleOnes(ZeldaCPPNtoGANLevelBreederTask.SENSOR_LABELS.length));
		}else {
			System.out.println();

			System.out.println("Second form");

			System.out.println();

			// TODO: Call ZeldaGANDungeonTask.getZeldaDungeonFromDirectArrayListGenotype
			return null; // TODO: Change
		}
	}

}
