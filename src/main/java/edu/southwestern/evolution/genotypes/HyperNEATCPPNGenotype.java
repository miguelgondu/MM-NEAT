package edu.southwestern.evolution.genotypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.southwestern.MMNEAT.MMNEAT;
import edu.southwestern.networks.NetworkUtil;
import edu.southwestern.networks.TWEANN;
import edu.southwestern.networks.hyperneat.HyperNEATTask;
import edu.southwestern.networks.hyperneat.HyperNEATUtil;
import edu.southwestern.networks.hyperneat.Substrate;
import edu.southwestern.networks.hyperneat.SubstrateConnectivity;
import edu.southwestern.parameters.CommonConstants;
import edu.southwestern.parameters.Parameters;
import edu.southwestern.util.CartesianGeometricUtilities;
import edu.southwestern.util.datastructures.ArrayUtil;
import edu.southwestern.util.datastructures.Pair;
import edu.southwestern.util.util2D.ILocated2D;
import edu.southwestern.util.util2D.Tuple2D;

/**
 * Genotype for a hyperNEAT CPPN network
 *
 * @author Lauren Gillespie
 *
 */
public class HyperNEATCPPNGenotype extends TWEANNGenotype {

	// For each substrate layer pairing, there can be multiple output neurons in the CPPN
	public static int numCPPNOutputsPerLayerPair = -1; // Set in MMNEAT
	// Number of output neurons needed to designate bias values across all substrates
	//public static int numBiasOutputs = -1; // Set in MMNEAT : Being static meant that all networks had to have the same value.
	// Within each group, the first (index 0) will always specify the link value
	public static final int LINK_INDEX = 0;
	// If a Link Expression Output is used, it will be second (index 1)
	public static final int LEO_INDEX = 1;
	public static final double BIAS = 1.0;// Necessary for most CPPN networks
	// Transient values not saved to XML files
	public transient static boolean constructingNetwork = false;
	public transient int innovationID = 0;// provides unique innovation numbers for links and genes

	// Determines whether node normalization occurs across past activations in substrate networks
	public static boolean normalizedNodeMemory;
	
	/**
	 * Default constructor
	 */
	public HyperNEATCPPNGenotype() {
		// Default archetype index of 0.
		// The 4 extra inputs from substrateLocationInputs are x/y coordinates of both source and target substrates (move code elsewhere?)
		this(HyperNEATUtil.numCPPNInputs(), HyperNEATUtil.numCPPNOutputs(), 0);
	}

	/**
	 * Used by TWEANNCrossover
	 * 
	 * @param nodes new node genes
	 * @param links new link genes
	 * @param neuronsPerModule effectively the number of output neurons
	 * @param archetypeIndex archetype to use
	 */
	public HyperNEATCPPNGenotype(ArrayList<NodeGene> nodes, ArrayList<LinkGene> links, int neuronsPerModule, int archetypeIndex) {
		super(nodes, links, neuronsPerModule, false, archetypeIndex);
	}	

	/**
	 * Constructor for hyperNEATCPPNGenotype. Uses super constructor from
	 * TWEANNGenotype
	 * 
	 * @param archetypeIndex
	 * 			  which archetype to refer to for crossover
	 * @param links
	 *            list of links between genes
	 * @param genes
	 *            list of nodes in genotype
	 * @param outputNeurons
	 *            number of output neurons
	 */
	public HyperNEATCPPNGenotype(int archetypeIndex, ArrayList<LinkGene> links, ArrayList<NodeGene> genes, int outputNeurons) {
		super(genes, links, outputNeurons, false, archetypeIndex);
	}

	/**
	 * Constructor for random hyperNEATCPPNGenotype.
	 * 
	 * @param networkInputs
	 *            number of network inputs
	 * @param networkOutputs
	 *            number of network outputs
	 * @param archetypeIndex
	 *            index of genotype in archetype
	 */
	public HyperNEATCPPNGenotype(int networkInputs, int networkOutputs, int archetypeIndex) {
		// Construct new CPPN with random weights
		super(networkInputs, networkOutputs, archetypeIndex); 
	}

	/**
	 * Returns TWEANN representation of the CPPN encoded by this genotype,
	 * NOT a TWEANN that is indirectly-encoded
	 * @return TWEANN representation of the CPPN
	 */
	public TWEANN getCPPN() {
		return super.getPhenotype();
	}

	/**
	 * Uses CPPN to create a TWEANN controller for the domain. This
	 * created TWEANN is unique only to the instance in which it is used. In a
	 * sense, it's a one-and-done network, which explains the lax use of
	 * innovation numbers. The TWEANN that is returned is indirectly-encoded
	 * by the CPPN.
	 *
	 * @return TWEANN generated by CPPN
	 */
	@Override
	public TWEANN getPhenotype() {
		TWEANNGenotype tg = getSubstrateGenotype((HyperNEATTask) MMNEAT.task) ;
		TWEANN result = tg.getPhenotype();//return call to substrate genotype
		result.passSubstrateInformation(getSubstrateInformation((HyperNEATTask) MMNEAT.task));
		return result;
	}

	/**
	 * Use the CPPN to construct a genotype that encodes the substrate
	 * network, and return that genotype. This genotype can be used to
	 * get the substrate network phenotype, which is actually evaluated
	 * in any given domain.
	 * 
	 * Having the genotype of the substrate genotype allows access to 
	 * network components using methods of the genotype, which are sometimes
	 * more flexible than the methods of the network itself.
	 * 
	 * @param hnt HyperNEAT task that defines a substrate description used here
	 * @return genotype that encodes a substrate network generated by a CPPN
	 */
	public TWEANNGenotype getSubstrateGenotype(HyperNEATTask hnt) {
		constructingNetwork = true; // prevent displaying of substrates
		//long time = System.currentTimeMillis(); // for timing
		TWEANN cppn = getCPPN();// CPPN used to create TWEANN network
		List<Substrate> subs = getSubstrateInformation(hnt);// extract substrate information from domain
		List<SubstrateConnectivity> connections = getSubstrateConnectivity(hnt);// extract substrate connectivity from domain
		
		assert connections.get(0).sourceSubstrateName != null : "How was a null name constructed";
		
		ArrayList<NodeGene> newNodes = null;
		ArrayList<LinkGene> newLinks = null;

		// Total outputs in substrate network
		int phenotypeOutputs = 0;
		// Max number of substrates at the same height/depth
		int layersWidth = 0;
		// Number of layers of substrates including inputs and outputs
		int layersHeight = 0;
		// Figure out number of output neurons
		for (Substrate s : subs) {
			if (s.getStype() == Substrate.OUTPUT_SUBSTRATE) {
				phenotypeOutputs += s.getSize().t1 * s.getSize().t2;
			}
			layersWidth = Math.max(layersWidth, s.getSubLocation().t1); 
			layersHeight = Math.max(layersHeight, s.getSubLocation().t2); // Should depend on output layer 
		}		
		// Coordinates start at 0, so actual width/height is one more than max coordinate in each dimension
		layersWidth++;
		layersHeight++;

		innovationID = 0;// reset each time a phenotype is generated		
		newNodes = createSubstrateNodes(hnt, cppn, subs, layersWidth, layersHeight);

		// Will map substrate names to index in subs List
		HashMap<String, Integer> substrateIndexMapping = new HashMap<String, Integer>();
		for (int i = 0; i < subs.size(); i++) {
			substrateIndexMapping.put(subs.get(i).getName(), i);
		}

		try {
			// loop through connections and add links, based on contents of subs
			newLinks = createNodeLinks(hnt, cppn, connections, subs, substrateIndexMapping, layersWidth, layersHeight);
		}catch(NullPointerException npe) {
			System.out.println("Error in substrate configuration!");
			System.out.println(subs);
			System.out.println(connections);
			npe.printStackTrace();
			System.exit(1);
		}
		constructingNetwork = false;

		// the instantiation of the TWEANNgenotype in question

		// Hard coded to have a single neural output module.
		// May need to fix this down the line.
		// An archetype index of -1 is used. Hopefully this won't cause
		// problems, since the archetype is only needed for mutations and crossover.
		TWEANNGenotype tg = new TWEANNGenotype(newNodes,newLinks, phenotypeOutputs, false, -1);
		return tg;
	}
	
	/**
 	 * Method that returns a list of information about the substrate layers
	 * contained in the network.
	 * @param the HyperNEAT task
	 * @return List of Substrates in order from inputs to hidden to output
	 *         layers
	 */
	public List<Substrate> getSubstrateInformation(HyperNEATTask HNTask) {
		return HNTask.getSubstrateInformation();
		
	}

	/**
	 * @param hntask the HyperNEAT task
	 * @return List of triples that specifies each substrate with the index of each triple being its layer.
	 * 		Each triple looks like (width of layer, width of substrate, height of substrate)
	 */
	public List<SubstrateConnectivity> getSubstrateConnectivity(HyperNEATTask HNTask) {
		return HNTask.getSubstrateConnectivity();
	}

	/**
	 * Used by HyperNEAT-seeded tasks. The HyperNEAT genotype is used to create a (generally large)
	 * substrate network, and a genotype that directly encodes this resulting substrate network
	 * is returned, so that it can then be used as a seed/template for evolving a population of
	 * such large networks.
	 * 
	 * @param hnt HyperNEAT task which contains substrate description information
	 * @return Genotype that directly encodes a substrate network
	 */
	public TWEANNGenotype getSubstrateGenotypeForEvolution(HyperNEATTask hnt) {
		TWEANNGenotype tg = getSubstrateGenotype(hnt);
		tg.archetypeIndex = 0;
		return tg;
	}

	/**
	 * Copies given genotype
	 * 
	 * @return Copy of the CPPN genotype
	 */
	@Override
	public Genotype<TWEANN> copy() {
		int[] temp = moduleUsage; // Schrum: Not sure if keeping moduleUsage is appropriate
		ArrayList<LinkGene> linksCopy = new ArrayList<LinkGene>(this.links.size());
		for (LinkGene lg : this.links) {// needed for a deep copy
			linksCopy.add(newLinkGene(lg.sourceInnovation, lg.targetInnovation, lg.weight, lg.innovation, false));
		}

		ArrayList<NodeGene> genes = new ArrayList<NodeGene>(this.nodes.size());
		for (NodeGene ng : this.nodes) {// needed for a deep copy
			genes.add(newNodeGene(ng.ftype, ng.ntype, ng.innovation, false, ng.getBias(), false)); // CPPN nodes are not normalized
		}
		HyperNEATCPPNGenotype result = new HyperNEATCPPNGenotype(this.archetypeIndex, linksCopy, genes, this.numOut);

		// Schrum: Not sure if keeping moduleUsage is appropriate
		moduleUsage = temp;
		result.moduleUsage = new int[temp.length];
		System.arraycopy(this.moduleUsage, 0, result.moduleUsage, 0, moduleUsage.length);
		return result;
	}


	/**
	 * If bias outputs are used in CPPN, they will appear after all others.
	 * There should be one output group per layer pairing, so the number of
	 * layer pairings is multiplied by the neurons per output group to determine
	 * the index of the first bias output.
	 * @param hnt HyperNEAT task
	 * @return index where first bias output is located, if it exists
	 */
	public int indexFirstBiasOutput(HyperNEATTask hnt) {
		if(CommonConstants.substrateLocationInputs) {
			return HyperNEATCPPNGenotype.numCPPNOutputsPerLayerPair;
		} else {
			return getSubstrateConnectivity(hnt).size() * HyperNEATCPPNGenotype.numCPPNOutputsPerLayerPair;
		}
	}
	
	/**
	 * creates an array list containing all the nodes from all the substrates
	 *
	 * @param cppn
	 *             CPPN that produces phenotype network
	 * @param subs
	 *            list of substrates extracted from domain
	 * @return array list of NodeGenes from substrates
	 */
	public ArrayList<NodeGene> createSubstrateNodes(HyperNEATTask hnt, TWEANN cppn, List<Substrate> subs, int layersWidth, int layersHeight) {

		boolean convolutionWeightSharing = Parameters.parameters.booleanParameter("convolutionWeightSharing");

		int biasIndex = indexFirstBiasOutput(hnt); // first bias index
		ArrayList<NodeGene> newNodes = new ArrayList<NodeGene>();
		// loops through substrate list
		for (Substrate sub: subs) { // for each substrate
			// This loop gets every (x,y) coordinate pair from the substrate.
			for (Pair<Integer,Integer> coord : sub.coordinateList()) {
				int x = coord.t1;
				int y = coord.t2;

				// Substrate types and Neuron types match and use same values
				double bias = 0.0; // default
				// Non-input substrates can have a bias if desired
				if(CommonConstants.evolveHyperNEATBias && sub.getStype() != Substrate.INPUT_SUBSTRATE) {
					// Ask CPPN to generate a bias for each neuron
					ILocated2D scaledTargetCoordinates = MMNEAT.substrateMapping.transformCoordinates(new Tuple2D(x, y), sub.getSize().t1, sub.getSize().t2);
					double[] filteredInputs = hnt.filterCPPNInputs(new double[]{0, 0, scaledTargetCoordinates.getX(), scaledTargetCoordinates.getY(), BIAS});

					assert -1 <= filteredInputs[0] && filteredInputs[0] <= 1 : "CPPN input 0 out of range: " + filteredInputs[0];
					assert -1 <= filteredInputs[1] && filteredInputs[1] <= 1 : "CPPN input 1 out of range: " + filteredInputs[1];
					assert -1 <= filteredInputs[2] && filteredInputs[2] <= 1 : "CPPN input 2 out of range: " + filteredInputs[2];
					// 1D substrates don't have these extra inputs
					assert 3 >= filteredInputs.length || (-1 <= filteredInputs[3] && filteredInputs[3] <= 1) : "CPPN input 3 out of range: " + filteredInputs[3];
					assert 4 >= filteredInputs.length || (-1 <= filteredInputs[4] && filteredInputs[4] <= 1) : "CPPN input 4 out of range: " + filteredInputs[4];

					if(CommonConstants.substrateLocationInputs || convolutionWeightSharing) {
						// In this case, there are 4 extra CPPN inputs, which are x/y coordinates of the actual substrate locations.
						// To define the bias, we only need to know the location of the substrate containing the neuron to be biased (other is 0,0)
						ILocated2D scaledSubstrateCoordinates = MMNEAT.substrateMapping.transformCoordinates(new Tuple2D(sub.getSubLocation().t1, sub.getSubLocation().t2), layersWidth, layersHeight);
						filteredInputs = ArrayUtil.combineArrays(filteredInputs, new double[]{0, 0, scaledSubstrateCoordinates.getX(), scaledSubstrateCoordinates.getY()});

						assert -1 <= filteredInputs[5] && filteredInputs[5] <= 1 : "CPPN input 5 out of range: " + filteredInputs[5];
						assert -1 <= filteredInputs[6] && filteredInputs[6] <= 1 : "CPPN input 6 out of range: " + filteredInputs[6];
						assert -1 <= filteredInputs[7] && filteredInputs[7] <= 1 : "CPPN input 7 out of range: " + filteredInputs[7];
						assert -1 <= filteredInputs[8] && filteredInputs[8] <= 1 : "CPPN input 8 out of range: " + filteredInputs[8];
					}
					// Weight sharing also shares biases. This eliminates unique neuron contents
					if(convolutionWeightSharing) {
						filteredInputs[2] = 0; // No unique neuron x-coordinate
						filteredInputs[3] = 0; // No unique neuron y-coordinate
					}

					double[] result = cppn.process(filteredInputs);

					try{
						bias = result[biasIndex];
					} catch(ArrayIndexOutOfBoundsException e) { 
						// Ok to leave this error checking since it only executes when an
						// exception is thrown.
						System.out.println("result: " + Arrays.toString(result));
						System.out.println("biasIndex: " + biasIndex);
						System.out.println("CommonConstants.evolveHyperNEATBias: " + CommonConstants.evolveHyperNEATBias);
						System.out.println("numCPPNOutputsPerLayerPair: " + numCPPNOutputsPerLayerPair);
						System.out.println("numBiasOutputsNeeded(hnt): " + numBiasOutputsNeeded(hnt));
						System.out.println("cppn.numInputs(): " + cppn.numInputs());
						System.out.println("cppn.numOutputs(): " + cppn.numOutputs());
						System.out.println("cppn.effectiveNumOutputs(): " + cppn.effectiveNumOutputs());
						System.out.println("indexFirstBiasOutput(hnt): " + indexFirstBiasOutput(hnt));
						for(Substrate s: getSubstrateInformation(hnt)) {
							System.out.println(s);
						}
						for(SubstrateConnectivity sc : getSubstrateConnectivity(hnt)) {
							System.out.println(sc);
						}
						System.out.println(cppn);
						throw e;
					}
				}
				//newNodes.add(newNodeGene(sub.getFtype(), sub.getStype(), innovationID++, false, bias, normalizedNodeMemory));
				newNodes.add(newSubstrateNodeGene(sub, bias));
			}

			if(CommonConstants.evolveHyperNEATBias && !CommonConstants.substrateBiasLocationInputs && sub.getStype() != Substrate.INPUT_SUBSTRATE) {
				// Each non-input substrate has its own bias output for generating bias values,
				// unless substrateBiasLocationInputs is true, in which case the CPPN inputs differentiate
				// the output value of a single CPPN output for defining bias values.

				// Move to the next.
				biasIndex++;
			}
		}
		return newNodes;
	}
	
	/**
	 * This method creates a new neuron for a substrate.
	 * Can be overridden to create different types of neurons.
	 * 
	 * @param sub Substrate that neuron is being defined in
	 * @param bias Bias of this particular neuron
	 * @return NodeGene for substrate
	 */
	public NodeGene newSubstrateNodeGene(Substrate sub, double bias) {
		return newNodeGene(sub.getFtype(), sub.getStype(), innovationID++, false, bias, normalizedNodeMemory);
	}

	/**
	 * If HyperNEAT neuron bias values are evolved, then this method determines
	 * how many CPPN outputs are needed to specify them: 1 per non-input substrate layer.
	 * @param hnt HyperNEATTask that specifies substrate connectivity
	 * @return number of bias outputs needed by CPPN
	 */
	public int numBiasOutputsNeeded(HyperNEATTask hnt) {
		// CPPN has no bias outputs if they are not being evolved
		if(!CommonConstants.evolveHyperNEATBias) return 0;
		
		// If substrate coordinates are inputs to the CPPN, then
		// biases on difference substrates can be different based on the
		// inputs rather than having separate outputs for each substrate.
		if(CommonConstants.substrateBiasLocationInputs) return 1;

		List<Substrate> subs = getSubstrateInformation(hnt);
		int count = 0;
		for(Substrate s : subs) {
			if(s.getStype() != Substrate.INPUT_SUBSTRATE) count++;
		}
		return count;
	}
	
	/**
	 * creates an array list of links between substrates as dictated by
	 * connections parameter
	 *
	 * @param cppn
	 *            used to evolve link weight
	 * @param connections
	 *            list of different connections between substrates
	 * @param subs
	 *            list of substrates in question
	 * @param sIMap
	 *            hashmap that maps the substrate in question to its index in
	 *            the substrate list
	 *
	 * @return array list containing all the links between substrates
	 */
	private ArrayList<LinkGene> createNodeLinks(HyperNEATTask hnt, TWEANN cppn, List<SubstrateConnectivity> connections, List<Substrate> subs, HashMap<String, Integer> sIMap, int layersWidth, int layersHeight) {
		ArrayList<LinkGene> result = new ArrayList<LinkGene>();
		for (int i = 0; i < connections.size(); i++) { // For each pair of substrates that are connected
			SubstrateConnectivity currentConnection = connections.get(i);
			int sourceSubstrateIndex = sIMap.get(currentConnection.sourceSubstrateName);
			assert sIMap.get(currentConnection.targetSubstrateName) != null :"null in " + sIMap + "\nat " + connections.get(i).targetSubstrateName + "\nat " + i;
			int targetSubstrateIndex = sIMap.get(currentConnection.targetSubstrateName);
			Substrate sourceSubstrate = subs.get(sourceSubstrateIndex);
			Substrate targetSubstrate = subs.get(targetSubstrateIndex);
			
			// Whether to connect these layers used convolutional structure instead of standard fully connected structure
			boolean convolution = currentConnection.connectivityType == SubstrateConnectivity.CTYPE_CONVOLUTION && CommonConstants.convolution;
			int outputIndex = CommonConstants.substrateLocationInputs ? 0 : i;
			// both options add links from between two substrates to whole list of links
			if(convolution) {
				convolutionalLoopThroughLinks(hnt, result, cppn, outputIndex, sourceSubstrate, targetSubstrate, sourceSubstrateIndex, targetSubstrateIndex,
						subs, layersWidth, layersHeight, currentConnection.receptiveFieldWidth, currentConnection.receptiveFieldHeight);
			} else {
				loopThroughLinks(hnt, result, cppn, outputIndex, sourceSubstrate, targetSubstrate, sourceSubstrateIndex, targetSubstrateIndex, subs, layersWidth, layersHeight);
			}
		}
		return result;
	}
	
	/**
	 * TODO: uncomment
	 * Connect two substrate layers using convolutional link structures
	 * @param hnt HyperNEATTask instance with
	 * @param linksSoFar List of link genes to add to
	 * @param cppn Network generating link weights
	 * @param outputIndex index from cppn outputs to be used as weight in creating link
	 * @param s1 Where links come from
	 * @param s2 Where links go to
	 * @param s1Index Index in substrate list of source substrate
	 * @param s2Index Index in substrate list of target substrate
	 * @param subs List of substrates
	 * @param substrateHorizontalCoordinate Used by global coordinates
	 * @param substrateVerticalCoordinate Used by global coordinates
	 */
//	void convolutionalLoopThroughLinks(HyperNEATTask hnt, ArrayList<LinkGene> linksSoFar, TWEANN cppn, int outputIndex,
//			Substrate s1, Substrate s2, int s1Index, int s2Index,
//			List<Substrate> subs, int substrateHorizontalCoordinate, int substrateVerticalCoordinate) {
//		convolutionalLoopThroughLinks(hnt, linksSoFar, cppn, outputIndex, s1, s2, s1Index, s2Index, subs,
//				substrateHorizontalCoordinate, substrateVerticalCoordinate, 3, 3);
//	}

	/**
	 * Connect two substrate layers using convolutional link structures
	 * @param hnt HyperNEATTask instance with
	 * @param linksSoFar List of link genes to add to
	 * @param cppn Network generating link weights
	 * @param outputIndex index from cppn outputs to be used as weight in creating link
	 * @param s1 Where links come from
	 * @param s2 Where links go to
	 * @param s1Index Index in substrate list of source substrate
	 * @param s2Index Index in substrate list of target substrate
	 * @param subs List of substrates
	 * @param substrateHorizontalCoordinate Used by global coordinates
	 * @param substrateVerticalCoordinate Used by global coordinates
	 * @param receptiveFieldWidth the width of the receptive field window
	 * @param receptiveFieldHeight the hieght of the receptive field window
	 */
	void convolutionalLoopThroughLinks(HyperNEATTask hnt, ArrayList<LinkGene> linksSoFar, TWEANN cppn, int outputIndex,
			Substrate s1, Substrate s2, int s1Index, int s2Index,
			List<Substrate> subs, int substrateHorizontalCoordinate, int substrateVerticalCoordinate, int receptiveFieldWidth, int receptiveFieldHeight) {

		boolean convolutionDeltas = Parameters.parameters.booleanParameter("convolutionDeltas");
		boolean convolutionCoordinates = Parameters.parameters.booleanParameter("convolutionCoordinates");
		boolean convolutionWeightSharing = Parameters.parameters.booleanParameter("convolutionWeightSharing");
		assert receptiveFieldHeight % 2 == 1 : "Receptive field height needs to be odd to be centered: " + receptiveFieldHeight;
		assert receptiveFieldWidth % 2 == 1 : "Receptive field width needs to be odd to be centered: " + receptiveFieldWidth;
		// Need to watch out for links that want to connect out of bounds
		boolean zeroPadding = Parameters.parameters.booleanParameter("zeroPadding");
		int xOffset = receptiveFieldWidth / 2;
		int yOffset = receptiveFieldHeight / 2;
		int xEdgeOffset = zeroPadding ? 0 : xOffset;
		int yEdgeOffset = zeroPadding ? 0 : yOffset;
		
		int stride = Parameters.parameters.integerParameter("stride");

		// Traverse center points of receptive fields
		for(int x = xEdgeOffset; x < s1.getSize().t1 - xEdgeOffset; x += stride) {
			for(int y = yEdgeOffset; y < s1.getSize().t2 - yEdgeOffset; y += stride) {
				// There is a direct correspondence between each receptive field and
				// its target neuron in the next layer
				int targetXIndex = (x - xEdgeOffset) / stride; 
				int targetYIndex = (y - yEdgeOffset) / stride;
				
				assert targetXIndex < s2.getSize().t1 : "X-Coordinate must be within substrate! " + targetXIndex + " not less than " + s2.getSize().t1;
				assert targetYIndex < s2.getSize().t2 : "Y-Coordinate must be within substrate! " + targetYIndex + " not less than " + s2.getSize().t2 + "\n " + "(" + y + " - " + yEdgeOffset +") / " + stride + " = " + targetYIndex;
				// If target neuron is dead, do not continue
				if(!s2.isNeuronDead(targetXIndex, targetYIndex)) {
					// Loop through all neurons in the receptive field
					for(int fX = -xOffset; fX <= xOffset; fX++) {
						// Source neuron is offset from receptive field center
						int fromXIndex = x + fX;
						if(fromXIndex >= 0 && fromXIndex < s1.getSize().t1) {
							for(int fY = -yOffset; fY <= yOffset; fY++) {
								// Source neuron is offset from receptive field center
								int fromYIndex = y + fY;
								if(fromYIndex >= 0 && fromYIndex < s1.getSize().t2) {
									// Do not continue if source neuron is dead
									if(!s1.isNeuronDead(fromXIndex, fromYIndex)) {
										assert targetXIndex < s2.getSize().t1 : "X-Coordinate must be within substrate! " + targetXIndex + " not less than " + s2.getSize().t1;
										assert targetYIndex < s2.getSize().t2 : "Y-Coordinate must be within substrate! " + targetYIndex + " not less than " + s2.getSize().t2;
										// Target coordinates can use the standard substrate mapping, which may not be centered
										ILocated2D scaledTargetCoordinates = MMNEAT.substrateMapping.transformCoordinates(new Tuple2D(targetXIndex, targetYIndex), s2.getSize().t1, s2.getSize().t2);										
										double[] inputs; // Defined below
										// Phillip Verbancsics approach from his paper on Generative Neuro-Evolution for Deep Learning
										if(convolutionDeltas) {
											ILocated2D scaledSourceCoordinates = MMNEAT.substrateMapping.transformCoordinates(new Tuple2D(fromXIndex, fromYIndex), s1.getSize().t1, s1.getSize().t2);
											// inputs to CPPN 
											// First two inputs are deltas between target and source scaled coordinated: (x2 - x1, y2 - y1, x2, y2, 1.0)
											inputs = new double[]{scaledTargetCoordinates.getX() - scaledSourceCoordinates.getX(), scaledTargetCoordinates.getY() - scaledSourceCoordinates.getY(), scaledTargetCoordinates.getX(), scaledTargetCoordinates.getY(), BIAS};
											// These inputs may be outside the [-1,1] range
										} else if(convolutionCoordinates) {
											ILocated2D scaledSourceCoordinates = MMNEAT.substrateMapping.transformCoordinates(new Tuple2D(fromXIndex, fromYIndex), s1.getSize().t1, s1.getSize().t2); 
											inputs = new double[]{scaledSourceCoordinates.getX(), scaledSourceCoordinates.getY(), scaledTargetCoordinates.getX(), scaledTargetCoordinates.getY(), BIAS};
										} else {
											// Receptive field scaling needs to be with respect to the center of the field, regardless of what the mapping for the other coordinates is
											ILocated2D scaledFieldCoordinates = CartesianGeometricUtilities.centerAndScale(new Tuple2D(fX+xOffset, fY+yOffset), receptiveFieldWidth, receptiveFieldHeight);
											// inputs to CPPN 
											// NOTE: filterCPPNInputs call was removed because it doesn't seem to make sense with convolutional inputs
											if(convolutionWeightSharing) {
												// Since weights for each feature are duplicated, inputs defining the target neuron are not needed
												inputs = new double[]{scaledFieldCoordinates.getX(), scaledFieldCoordinates.getY(), 0, 0, BIAS};
											} else {
												inputs = new double[]{scaledFieldCoordinates.getX(), scaledFieldCoordinates.getY(), scaledTargetCoordinates.getX(), scaledTargetCoordinates.getY(), BIAS};
											}
											// This approach maintains all values in the scaled range
											assert -1 <= inputs[0] && inputs[0] <= 1 : "CPPN input 0 out of range: " + inputs[0];
											assert -1 <= inputs[1] && inputs[1] <= 1 : "CPPN input 1 out of range: " + inputs[1];
											assert -1 <= inputs[2] && inputs[2] <= 1 : "CPPN input 2 out of range: " + inputs[2];
											assert -1 <= inputs[3] && inputs[3] <= 1 : "CPPN input 3 out of range: " + inputs[3] + " target:" + (new Tuple2D(targetXIndex, targetYIndex)) + " in " + (new Tuple2D(s2.getSize().t1, s2.getSize().t2)) + " with " + MMNEAT.substrateMapping;
											assert -1 <= inputs[4] && inputs[4] <= 1 : "CPPN input 4 out of range: " + inputs[4];
										}

										// Convolutional weight sharing requires substrate location inputs to prevent all receptive fields across all layers from being the same.
										if(CommonConstants.substrateLocationInputs || convolutionWeightSharing) {
											ILocated2D scaledSubstrate1Coordinates = MMNEAT.substrateMapping.transformCoordinates(new Tuple2D(s1.getSubLocation().t1, s1.getSubLocation().t2), substrateHorizontalCoordinate, substrateVerticalCoordinate);
											ILocated2D scaledSubstrate2Coordinates = MMNEAT.substrateMapping.transformCoordinates(new Tuple2D(s2.getSubLocation().t1, s2.getSubLocation().t2), substrateHorizontalCoordinate, substrateVerticalCoordinate);

											// Phillip Verbancsics approach from his paper on Generative Neuro-Evolution for Deep Learning
											if(convolutionDeltas) {
												assert !convolutionWeightSharing : "Combining convolution deltas with convolutional weight sharing does not make sense";
											// Could force centering in this case, as Verbancsics did, but it didn't seem to help
											//ILocated2D scaledSubstrate1Coordinates = CartesianGeometricUtilities.centerAndScale(new Tuple2D(s1.getSubLocation().t1, s1.getSubLocation().t2), layersWidth, layersHeight);
											//ILocated2D scaledSubstrate2Coordinates = CartesianGeometricUtilities.centerAndScale(new Tuple2D(s2.getSubLocation().t1, s2.getSubLocation().t2), layersWidth, layersHeight);

											// Extra inputs are location of target substrate, and delta between target and source substrates
											// In Verbancsics' scheme: (f2 - f1, z2 - z1, f2, z2)
											inputs = ArrayUtil.combineArrays(inputs, new double[]{scaledSubstrate2Coordinates.getX() - scaledSubstrate1Coordinates.getX(), scaledSubstrate2Coordinates.getY() - scaledSubstrate1Coordinates.getY(), scaledSubstrate2Coordinates.getX(), scaledSubstrate2Coordinates.getY()});											
											// These inputs may be outside the [-1,1] range
											} else {												
												// Extra inputs are locations of the substrates (just x/y coordinates)
												// In Verbancsics' scheme: (f1, z1, f2, z2)
												inputs = ArrayUtil.combineArrays(inputs, new double[]{scaledSubstrate1Coordinates.getX(), scaledSubstrate1Coordinates.getY(), scaledSubstrate2Coordinates.getX(), scaledSubstrate2Coordinates.getY()});

												assert -1 <= inputs[5] && inputs[5] <= 1 : "CPPN input 5 out of range: " + inputs[5];
												assert -1 <= inputs[6] && inputs[6] <= 1 : "CPPN input 6 out of range: " + inputs[6];
												assert -1 <= inputs[7] && inputs[7] <= 1 : "CPPN input 7 out of range: " + inputs[7];
												assert -1 <= inputs[8] && inputs[8] <= 1 : "CPPN input 8 out of range: " + inputs[8];
											}
										}
										conditionalLinkAdd(linksSoFar, cppn, inputs, outputIndex, fromXIndex, fromYIndex, s1Index, targetXIndex, targetYIndex, s2Index, subs, innovationID++);
									}	
								}
							}						
						}
					}
				}
			}		
		}
	}

	/**
	 * a method for looping through all nodes of two substrates to be linked
	 * Link is only created if CPPN output reaches a certain threshold that is
	 * dictated via command line parameter.
	 *
	 * @param linksSoFar
	 * 			  All added links are accumulated in this list
	 * @param cppn
	 *            used to evolve link weight
	 * @param outputIndex
	 *            index from cppn outputs to be used as weight in creating link
	 * @param s1
	 *            first substrate to be linked
	 * @param s2
	 *            second substrate to be linked
	 * @param s1Index
	 *            index of first substrate in substrate list
	 * @param s2Index
	 *            index of second substrate in substrate list
	 * @param subs
	 *            list of substrates
	 *
	 */
	void loopThroughLinks(HyperNEATTask hnt, ArrayList<LinkGene> linksSoFar, TWEANN cppn, int outputIndex, Substrate s1, Substrate s2, int s1Index, int s2Index, List<Substrate> subs, int layersWidth, int layersHeight) {

		// This loop goes through every (x,y) coordinate in Substrate s1: source substrate
		for(Pair<Integer,Integer> src : s1.coordinateList()) {
			int fromXIndex = src.t1;
			int fromYIndex = src.t2;
			// If the neuron in the source substrate is dead, it will not have outputs
			if(!s1.isNeuronDead(fromXIndex, fromYIndex)) {
				// This loop searches through every (x,y) coordinate in Substrate s2: target substrate
				for(Pair<Integer,Integer> target: s2.coordinateList()) {
					int targetXindex = target.t1;
					int targetYIndex = target.t2;
					// If the target neuron is dead, then don't bother with incoming links
					if(!s2.isNeuronDead(targetXindex, targetYIndex)) {
						// CPPN inputs need to be centered and scaled
						ILocated2D scaledSourceCoordinates = MMNEAT.substrateMapping.transformCoordinates(new Tuple2D(fromXIndex, fromYIndex), s1.getSize().t1, s1.getSize().t2);
						ILocated2D scaledTargetCoordinates = MMNEAT.substrateMapping.transformCoordinates(new Tuple2D(targetXindex, targetYIndex), s2.getSize().t1, s2.getSize().t2);
						// inputs to CPPN 
						// These next two lines need to be generalized for different numbers of CPPN inputs
						double[] inputs = hnt.filterCPPNInputs(new double[]{scaledSourceCoordinates.getX(), scaledSourceCoordinates.getY(), scaledTargetCoordinates.getX(), scaledTargetCoordinates.getY(), BIAS});

						assert -1 <= inputs[0] && inputs[0] <= 1 : "CPPN input 0 out of range: " + inputs[0];
						assert -1 <= inputs[1] && inputs[1] <= 1 : "CPPN input 1 out of range: " + inputs[1];
						assert -1 <= inputs[2] && inputs[2] <= 1 : "CPPN input 2 out of range: " + inputs[2];
						// 1D substrates don't have these extra inputs
						assert 3 >= inputs.length || (-1 <= inputs[3] && inputs[3] <= 1) : "CPPN input 3 out of range: " + inputs[3];
						assert 4 >= inputs.length || (-1 <= inputs[4] && inputs[4] <= 1) : "CPPN input 4 out of range: " + inputs[4];

						if(CommonConstants.substrateLocationInputs || Parameters.parameters.booleanParameter("convolutionWeightSharing")) {
							// Extra inputs are locations of the substrates (just x/y coordinates)
							ILocated2D scaledSubstrate1Coordinates = MMNEAT.substrateMapping.transformCoordinates(new Tuple2D(s1.getSubLocation().t1, s1.getSubLocation().t2), layersWidth, layersHeight);
							ILocated2D scaledSubstrate2Coordinates = MMNEAT.substrateMapping.transformCoordinates(new Tuple2D(s2.getSubLocation().t1, s2.getSubLocation().t2), layersWidth, layersHeight);
							inputs = ArrayUtil.combineArrays(inputs, new double[]{scaledSubstrate1Coordinates.getX(), scaledSubstrate1Coordinates.getY(), scaledSubstrate2Coordinates.getX(), scaledSubstrate2Coordinates.getY()});

							assert -1 <= inputs[5] && inputs[5] <= 1 : "CPPN input 5 out of range: " + inputs[5];
							assert -1 <= inputs[6] && inputs[6] <= 1 : "CPPN input 6 out of range: " + inputs[6];
							assert -1 <= inputs[7] && inputs[7] <= 1 : "CPPN input 7 out of range: " + inputs[7];
							assert -1 <= inputs[8] && inputs[8] <= 1 : "CPPN input 8 out of range: " + inputs[8];
						}
						conditionalLinkAdd(linksSoFar, cppn, inputs, outputIndex, fromXIndex, fromYIndex, s1Index, targetXindex, targetYIndex, s2Index, subs, innovationID++); // increment innovation regardless of whether link is added
					}
				}
			}
		}
	}

	/**
	 * If the given inputs to the CPPN indicate that a link should be added, then it is added to the provided list of links with the
	 * appropriate weight.
	 * 
	 * @param linksSoFar List of links to add to
	 * @param cppn Network generating link weights
	 * @param inputs inputs to the CPPN
	 * @param outputIndex index within CPPN outputs to look for weight information
	 * @param fromXIndex x-coordinate of neuron in source substrate
	 * @param fromYIndex y-coordinate of neuron in source substrate
	 * @param s1Index source substrate index in substrate list
	 * @param targetXindex x-coordinate of neuron in target substrate
	 * @param targetYIndex y-coordinate of neuron in target substrate
	 * @param s2Index target substrate index in substrate list
	 * @param subs list of substrates
	 */
	void conditionalLinkAdd(ArrayList<LinkGene> linksSoFar, TWEANN cppn, double[] inputs, int outputIndex, int fromXIndex, int fromYIndex, int s1Index, int targetXindex, int targetYIndex, int s2Index, List<Substrate> subs, long linkInnovationID) {
		double[] outputs = cppn.process(inputs);
		int module = cppn.lastModule();
		boolean expressLink = CommonConstants.leo
				// Specific network output determines link expression
				? outputs[(numCPPNOutputsPerLayerPair * outputIndex) + LEO_INDEX] > CommonConstants.linkExpressionThreshold
						// Output magnitude determines link expression
						: Math.abs(outputs[(numCPPNOutputsPerLayerPair * outputIndex) + LINK_INDEX]) > CommonConstants.linkExpressionThreshold;
						if (expressLink) {
							long sourceID = getInnovationID(fromXIndex, fromYIndex, s1Index, subs);
							long targetID = getInnovationID(targetXindex, targetYIndex, s2Index, subs);
							double weight = CommonConstants.leo
									// LEO takes its weight directly from the designated network output
									? outputs[(numCPPNOutputsPerLayerPair * outputIndex) + LINK_INDEX]
											// Standard HyperNEAT must scale the weight
											: NetworkUtil.calculateWeight(outputs[(numCPPNOutputsPerLayerPair * outputIndex) + LINK_INDEX]);
									linksSoFar.add(newLinkGene(sourceID, targetID, weight, linkInnovationID, true, false, false, module));									
						}
	}

	/**
	 * returns the innovation id of the node in question
	 *
	 * @param x
	 *            x-coordinate of node
	 * @param y
	 *            y-coordinate of node
	 * @param sIndex
	 *            index of substrate in question
	 * @param subs
	 *            list of substrates available
	 *
	 * @return innovationID of link in question
	 */
	public long getInnovationID(int x, int y, int sIndex, List<Substrate> subs) {
		long innovationIDAccumulator = 0;
		for (int i = 0; i < sIndex; i++) {
			Substrate s = subs.get(i);
			innovationIDAccumulator += s.getSize().t1 * s.getSize().t2;
		}
		innovationIDAccumulator += (subs.get(sIndex).getSize().t1 * y) + x;
		return innovationIDAccumulator;
	}

	/**
	 * Creates a new random instance of the hyperNEATCPPNGenotype
	 * @return New genotype for CPPN
	 */
	@Override
	public Genotype<TWEANN> newInstance() {
		HyperNEATCPPNGenotype result = new HyperNEATCPPNGenotype(HyperNEATUtil.numCPPNInputs(), HyperNEATUtil.numCPPNOutputs(), this.archetypeIndex);
		result.moduleUsage = new int[result.numModules];
		return result;
	}

}
