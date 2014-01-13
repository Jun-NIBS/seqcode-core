package edu.psu.compbio.seqcode.projects.akshay.bayesments.features;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.psu.compbio.seqcode.gse.datasets.general.Point;
import edu.psu.compbio.seqcode.gse.datasets.general.Region;
import edu.psu.compbio.seqcode.gse.datasets.species.Genome;
import edu.psu.compbio.seqcode.projects.akshay.bayesments.experiments.ExperimentCondition;
import edu.psu.compbio.seqcode.projects.akshay.bayesments.experiments.ExperimentManager;
import edu.psu.compbio.seqcode.projects.akshay.bayesments.experiments.ExperimentSet;
import edu.psu.compbio.seqcode.projects.akshay.bayesments.framework.Config;
import edu.psu.compbio.seqcode.projects.shaun.EventMetaMaker;

public class GenomicLocations {
	
	protected Genome gen;
	protected ExperimentSet experiments;
	protected List<Point> locations = new ArrayList<Point>();
	protected float[][] chromatinCounts;
	protected float[][] factorCounts;
	// 2-d array, rows as points and columns as conditions
	protected Region[][] chromatinRegions;
	protected Region[][] factorRegions;
	protected int numChromCons;
	protected int numFacCons;
	
	public GenomicLocations(ExperimentManager manager, Config config) {
		try{
			File peaksFile = config.getPeaksFile();
			List<Point> points = EventMetaMaker.loadPoints(peaksFile);
			this.experiments = manager.getExperimentSet();
			this.gen = manager.getGenome();
			
			//Filling chromatinRegions
			List<ExperimentCondition> chromConds = manager.getChromatinConditionList();
			this.numChromCons = chromConds.size();
			chromatinRegions = new Region[points.size()][chromConds.size()];
			chromatinCounts = new float[points.size()][chromConds.size()];
			int pointCount=0;
			for(Point p : points){
				int conCount=0;
				for(ExperimentCondition ec : chromConds){
					chromatinRegions[pointCount][conCount] = new Region(p.getGenome(),p.getChrom()
							,p.getLocation()-ec.getWinSize(),p.getLocation()+ec.getWinSize());
					conCount++;
				}
				pointCount++;
			}
			
			//Filling factorRegions
			pointCount=0;
			List<ExperimentCondition> facConds = manager.getFacConditionList();
			this.numFacCons = facConds.size();
			factorRegions = new Region[points.size()][facConds.size()];
			factorCounts = new float[points.size()][facConds.size()];
			for(Point p : points){
				int conCount=0;
				for(ExperimentCondition ec : facConds){
					factorRegions[pointCount][conCount] = new Region(p.getGenome(),p.getChrom()
							, p.getLocation()-ec.getWinSize(), p.getLocation()+ec.getWinSize());
					conCount++;
				}
				pointCount++;
			}
			
			//Filling chromatinCounts
			pointCount=0;
			for(Point p : points){
				int conCount=0;
				for(ExperimentCondition ec : chromConds){
					Region r = chromatinRegions[pointCount][conCount];
					chromatinCounts[pointCount][conCount] = ec.getTotalSignalCountInARegion(r);
					conCount++;
				}
				pointCount++;
			}
			
			//Filling factorCounts
			pointCount=0;
			for(Point p: points){
				int conCount=0;
				for(ExperimentCondition ec : facConds){
					Region r = factorRegions[pointCount][conCount];
					factorCounts[pointCount][conCount] = ec.getTotalSignalCountInARegion(r);
					conCount++;
				}
				pointCount++;
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	//Accessors
	public int getNumChromatinCons(){return this.numChromCons;}
	public int getNumFacCons(){return this.numFacCons;}
	public int getNumTrainingExamples(){return this.locations.size();}
	public float[][] getChromatinCounts(){return this.chromatinCounts;}
	public float[][] getFactorCounts(){return this.factorCounts;}
	
	

}
