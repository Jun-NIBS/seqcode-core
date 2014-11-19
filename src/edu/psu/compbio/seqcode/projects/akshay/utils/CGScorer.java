package edu.psu.compbio.seqcode.projects.akshay.utils;

import edu.psu.compbio.seqcode.genome.location.Region;
import edu.psu.compbio.seqcode.gse.gsebricks.verbs.sequence.SequenceGenerator;
import edu.psu.compbio.seqcode.gse.viz.metaprofile.BinningParameters;

public class CGScorer {
	private SequenceGenerator seqgen;
	private BinningParameters params;
	
	
	 public CGScorer(BinningParameters p, SequenceGenerator sq) {
		 params=p;
		 seqgen=sq;
	}
	
	public CGScoreProfile execute(Region r){
		String seq = seqgen.execute(r);
		seq =seq.toUpperCase();
		
		int[] cgscore= new int[params.getNumBins()];
		double[] cgperc = new double[params.getNumBins()];
		for(int i=0; i<seq.length()-1; i++){
			if(seq.substring(i, i+2).equals("CG")){
				cgscore[params.findBin(i)]++;
			}
		}
		int MaxCG = (int)params.getBinSize()/2;
		for(int i=0; i<cgscore.length;i++){
			cgperc[i] = (double)cgscore[i]/MaxCG;
		}
		
		CGScoreProfile profile = new CGScoreProfile(cgscore,cgperc);
		
		return profile;
		
		
	}
	
	
	
}
