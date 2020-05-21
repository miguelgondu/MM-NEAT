package edu.southwestern.evolution.genotypes;

import java.util.ArrayList;

import edu.southwestern.evolution.mutation.tweann.ConvertCPPN2GANtoDirect2GANMutation;
import edu.southwestern.networks.TWEANN;
import edu.southwestern.parameters.Parameters;

/**
 * Allows to switch back and forth randomly between a CPPN to GAN 
 * or Direct to Gan genotype
 * 
 *
 */
public class CPPNOrDirectToGANGenotype extends EitherOrGenotype<TWEANN,ArrayList<Double>> {

	/**
	 * default is TWEANN
	 */
	public CPPNOrDirectToGANGenotype() {
		this(new TWEANNGenotype(), true);
	}	
	
	/**
	 * constructor that allows for changing from the default
	 * TWEANN
	 * @param genotype the genotype
	 * @param firstForm whether or not it is a TWEANN
	 */
	public CPPNOrDirectToGANGenotype(Genotype genotype, boolean firstForm) {
		super(genotype, firstForm);
	}
	
	@Override
	/**
	 * Has a chance of mutating to change to CPPN
	 */
	public void mutate() {
		super.firstForm=false;
		StringBuilder sb = new StringBuilder();
		sb.append(this.getId());
		sb.append(" ");
		
		new ConvertCPPN2GANtoDirect2GANMutation().go(current, sb);
		// new WhateverMutationOpIsCalled().go( params )
		// TODO: Small chance of transitioning from CPPN to Direct
		// Put into the mutation operation: super.firstForm = false;
		

		super.mutate();
	}
	

}
