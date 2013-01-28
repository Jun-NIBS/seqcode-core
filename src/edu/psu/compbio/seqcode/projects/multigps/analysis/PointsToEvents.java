package edu.psu.compbio.seqcode.projects.multigps.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.psu.compbio.seqcode.gse.datasets.general.Point;
import edu.psu.compbio.seqcode.gse.datasets.general.Region;
import edu.psu.compbio.seqcode.projects.multigps.experiments.ControlledExperiment;
import edu.psu.compbio.seqcode.projects.multigps.experiments.ExperimentCondition;
import edu.psu.compbio.seqcode.projects.multigps.experiments.ExperimentManager;
import edu.psu.compbio.seqcode.projects.multigps.features.BindingEvent;
import edu.psu.compbio.seqcode.projects.multigps.framework.BindingModel;
import edu.psu.compbio.seqcode.projects.multigps.framework.Config;
import edu.psu.compbio.seqcode.projects.multigps.framework.StrandedBaseCount;

/**
 * PointsToEvents: this class converts a list of points into pseudo-events. 
 * It is only meant as a debugging tool and should not be used in analyses.
 * 
 * @author mahony
 *
 */
public class PointsToEvents {
	private Config config;
	private ExperimentManager manager;
	private List<Point> points;
	private int regionWin;
	
	/**
	 * Constructor
	 * @param c
	 * @param man
	 * @param p
	 * @param win
	 */
	public PointsToEvents(Config c, ExperimentManager man, List<Point> p, int win){
		config = c;
		manager = man;
		points = p;
		regionWin = win;
	}
	
	/**
	 * Convert points to pseudo-events
	 * @return
	 */
	public List<BindingEvent> execute(){
		List<BindingEvent> events = new ArrayList<BindingEvent>();
		BindingEvent.setExperimentSet(manager.getExperimentSet());
		BindingEvent.setConfig(config);
		BindingEvent.setSortingCond(manager.getExperimentSet().getConditions().get(0));
		
		//For each point
		for(Point p : points){
			//Expand point into region
			Region potentialReg = p.expand(regionWin/2);
			
			//Initialize binding event
			BindingEvent e = new BindingEvent(p,potentialReg);
			
			//for each condition
			for(ExperimentCondition c : manager.getExperimentSet().getConditions()){
				boolean[] addedToCond = new boolean[manager.getExperimentSet().getSamples().size()];
				for(int a=0; a<manager.getExperimentSet().getSamples().size(); a++)
					addedToCond[a]=false;

				//Signal hits for each replicate
				for(ControlledExperiment r : c.getReplicates()){
					//Get the hits for this replicate in this region
					List<StrandedBaseCount> sigHits = r.getSignal().getUnstrandedBases(potentialReg);
					List<StrandedBaseCount> ctrlHits = r.getControl().getUnstrandedBases(potentialReg);
					
					//Scan region with binding distribution to find ML position
					Point maxSigPoint = findMaxWithBindingModel(sigHits, potentialReg, r.getBindingModel());
					Point maxCtrlPoint = findMaxWithBindingModel(ctrlHits, potentialReg, r.getBindingModel());
					
					//ML assign reads (single binding event)
					double sigResp = assignReadsSingleEvent(sigHits, maxSigPoint, r.getBindingModel());
					double ctrlResp = assignReadsSingleEvent(ctrlHits, maxCtrlPoint, r.getBindingModel()); 
					
					//Set the replicate responsibilities
					e.setRepSigHits(r, sigResp);
					e.setRepCtrlHits(r, ctrlResp);
					
					//Increment the condition responsibilities
					//TODO: Is this the right way to integrate replicates?
					if(!addedToCond[r.getSignal().getIndex()])
						e.setCondSigHits(c, e.getCondSigHits(c)+sigResp);
					addedToCond[r.getSignal().getIndex()]=true;
					
					if(!addedToCond[r.getControl().getIndex()])
						e.setCondCtrlHits(c, e.getCondCtrlHits(c)+ctrlResp);
					addedToCond[r.getControl().getIndex()]=true;
				}
			}
			if(config.isAddingAnnotations())
				e.addClosestGenes();
			
			events.add(e);
		}
		return(events);
	}
	
	
	/* Find exact peak using a BindingModel */
	protected Point findMaxWithBindingModel(List<StrandedBaseCount> hits, Region coords, BindingModel model){
		int maxPos=0; double maxScore=0;
		double [] p = new double[coords.getWidth()+1];
		for(int k=0; k<=coords.getWidth(); k++){p[k]=0;}
		for(StrandedBaseCount x : hits){
			int readStart = x.getCoordinate();
			if(readStart>=coords.getStart()-model.getMax() && readStart<=coords.getEnd()+model.getMax()){
				int offset = readStart-coords.getStart();
				if(x.getStrand()=='+')
					for(int i=Math.max(model.getMin()+offset, 0); i<=Math.min(coords.getWidth(), offset+model.getMax()); i++)
						p[i]+=model.probability(i-offset) * x.getCount();
				else
					for(int i=Math.max(offset-model.getMax(), 0); i<=Math.min(coords.getWidth(), offset-model.getMin()); i++)
						p[i]+=model.probability(offset-i) * x.getCount();
			}
		}
		for(int k=0; k<=coords.getWidth(); k++)
			if(p[k]>maxScore){maxScore=p[k]; maxPos=k;}
			
		Point pt = new Point(coords.getGenome(), coords.getChrom(), coords.getStart()+maxPos);
		return(pt);
	}
	
	/**
	 * Assign all reads in the list that are within the binding model boundaries to the event at point
	 * @param hits
	 * @param point
	 * @param model
	 * @return
	 */
	protected double assignReadsSingleEvent(List<StrandedBaseCount> hits, Point point, BindingModel model){
		double count =0;
		for(StrandedBaseCount x : hits){
			int readStart = x.getCoordinate();
			if(readStart>=point.getLocation()-model.getMax() && readStart<=point.getLocation()+model.getMax()){
				count += x.getCount();
			}
		}
		return count;
	}
}
