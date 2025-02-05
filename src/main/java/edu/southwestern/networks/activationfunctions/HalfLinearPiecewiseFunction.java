package edu.southwestern.networks.activationfunctions;

import org.nd4j.linalg.activations.Activation;

public class HalfLinearPiecewiseFunction implements ActivationFunction {
	/**
	 * Linear function that returns x within the bounds of 0 < x < 1
	 *
	 * @param x Function parameter
	 * @return linear x within 0 and 1
	 */
	@Override
	public double f(double x) {
		return halfLinear(x);
	}
	
	/**
	 * static version for easy outside access
	 * @param x
	 * @return
	 */
	public static double halfLinear(double x) {
		return Math.max(0, Math.min(1, x));
	}
	
	@Override
	public Activation equivalentDL4JFunction() {
		throw new UnsupportedOperationException("No corresponding DL4J function for " + name());
	}

	@Override
	public String name() {
		return "piecewise-half"; //"Half Piecewise";
	}
}
