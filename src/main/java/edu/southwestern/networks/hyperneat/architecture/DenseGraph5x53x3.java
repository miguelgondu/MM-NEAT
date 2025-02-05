package edu.southwestern.networks.hyperneat.architecture;

import java.util.ArrayList;
import java.util.List;

import edu.southwestern.networks.hyperneat.HyperNEATTask;
import edu.southwestern.networks.hyperneat.SubstrateConnectivity;
import edu.southwestern.util.datastructures.Pair;
import edu.southwestern.util.datastructures.Triple;

/**
 * D2W1 Convolutional architecture where each hidden substrate is connected to each input and output. The inputs
 * are connected to the first hidden layer by a 5x5 receptive field and the first hidden layer is connected to the second hidden layer
 * by a 3x3 receptive field.
 * @author Devon Fulcher
 */
public class DenseGraph5x53x3 implements SubstrateArchitectureDefinition{

	@Override
	public List<Triple<Integer, Integer, Integer>> getNetworkHiddenArchitecture() {
		List<Triple<Integer, Integer, Integer>> networkHiddenArchitecture = new ArrayList<Triple<Integer, Integer, Integer>>();
		networkHiddenArchitecture.add(new Triple<Integer, Integer, Integer>(1, 6, 16));
		networkHiddenArchitecture.add(new Triple<Integer, Integer, Integer>(1, 4, 14));
		return networkHiddenArchitecture;
	}

	@Override
	public List<SubstrateConnectivity> getSubstrateConnectivity(HyperNEATTask hnt) {
		List<SubstrateConnectivity> substrateConnectivity = new ArrayList<SubstrateConnectivity>();
		Pair<List<String>, List<String>> io = FlexibleSubstrateArchitecture.getInputAndOutputNames(hnt);
		List<Triple<Integer, Integer, Integer>> networkHiddenArchitecture = getNetworkHiddenArchitecture();
		FlexibleSubstrateArchitecture.connectInputToFirstHidden(substrateConnectivity, io.t1, networkHiddenArchitecture, 5, 5);
		FlexibleSubstrateArchitecture.connectAllAdjacentHiddenLayers(substrateConnectivity, networkHiddenArchitecture, 3, 3);
		FlexibleSubstrateArchitecture.connectInputToHidden(substrateConnectivity, io.t1, networkHiddenArchitecture, 7, 7, 1);
		FlexibleSubstrateArchitecture.connectLastHiddenToOutput(substrateConnectivity, io.t2, networkHiddenArchitecture, SubstrateConnectivity.CTYPE_FULL);
		FlexibleSubstrateArchitecture.connectHiddenToOutput(substrateConnectivity, io.t2, networkHiddenArchitecture, -1, -1, 0);
		return substrateConnectivity;
	}

}
