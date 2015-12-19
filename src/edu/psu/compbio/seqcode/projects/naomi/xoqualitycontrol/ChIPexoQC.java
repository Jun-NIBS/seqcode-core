package edu.psu.compbio.seqcode.projects.naomi.xoqualitycontrol;

import edu.psu.compbio.seqcode.deepseq.experiments.ControlledExperiment;
import edu.psu.compbio.seqcode.deepseq.experiments.ExperimentCondition;
import edu.psu.compbio.seqcode.deepseq.experiments.ExperimentManager;
import edu.psu.compbio.seqcode.deepseq.experiments.ExptConfig;
import edu.psu.compbio.seqcode.deepseq.experiments.Sample;
import edu.psu.compbio.seqcode.genome.GenomeConfig;

public class ChIPexoQC {
	
	protected GenomeConfig gconfig;
	protected ExptConfig econfig;
	
	public ChIPexoQC(GenomeConfig gcon, ExptConfig econ){	
		gconfig = gcon;
		econfig = econ;
	}
	 
	public void printQCMetrics(){
		
		double ncis, signalHits, controlHits;
		double IPstrength=0;
		
		ExperimentManager manager = new ExperimentManager(econfig);
				
		for(ExperimentCondition exptCond: manager.getConditions()){
			for(ControlledExperiment rep : exptCond.getReplicates()){
				ncis = rep.getControlScaling();
				signalHits = rep.getSignal().getHitCount();
				controlHits = rep.getControl().getHitCount();
				IPstrength = 1-(ncis/(signalHits/controlHits));
				if (IPstrength<0)
					IPstrength=0;
				System.out.println("Condition:"+rep.getCondName()+"\tSignal:"+signalHits+"\tControl:"+controlHits+"\tScalingFactor:"+ncis+"\tIPstrength: "+IPstrength);
			}
		}
	}
		
	public static void main(String[] args) {
		
		GenomeConfig gconf = new GenomeConfig(args);
		ExptConfig  econf = new ExptConfig(gconf.getGenome(), args);
		ChIPexoQC exoQC = new ChIPexoQC(gconf, econf); 
		exoQC.printQCMetrics();
	}

}
