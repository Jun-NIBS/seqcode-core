package edu.psu.compbio.seqcode.projects.naomi;

//import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Collections;
//import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.psu.compbio.seqcode.deepseq.StrandedBaseCount;
import edu.psu.compbio.seqcode.deepseq.experiments.ExperimentManager;
import edu.psu.compbio.seqcode.deepseq.experiments.ExptConfig;
import edu.psu.compbio.seqcode.deepseq.experiments.Sample;
import edu.psu.compbio.seqcode.genome.Genome;
import edu.psu.compbio.seqcode.genome.GenomeConfig;
import edu.psu.compbio.seqcode.genome.location.Region;
import edu.psu.compbio.seqcode.gse.gsebricks.verbs.location.ChromosomeGenerator;
//import edu.psu.compbio.seqcode.gse.utils.ArgParser;

public class CrossContaminationEstimator {
	
	protected GenomeConfig gconfig;
	protected ExptConfig econfig;
	
	public CrossContaminationEstimator(GenomeConfig gcon, ExptConfig econ){	
		gconfig = gcon;
		econfig = econ;
	}
	
	public void printDataPoints(){
		
		ExperimentManager manager = new ExperimentManager(econfig);

		Genome genome = gconfig.getGenome();
//		List<String> chromNames = genome.getChromList();		
//		int maxchromSize= 0;
//		for (String chrom : chromNames){
//			if (genome.getChromLength(chrom)> maxchromSize){
//				maxchromSize = genome.getChromLength(chrom);
//			}
//		}

		int sampleSize = manager.getSamples().size();
		
//		float [][] sampleCounts = new float [(int) genome.getGenomeLength()][sampleSize];
//		for (int i = 0; i< genome.getGenomeLength(); i++){
//			for (int j = 0; j < sampleSize; j++){
//				sampleCounts[i][j] = 0;
//			}
//		}
		
		// this is the static array that can store all genome positions
		float[] [] dataPoints = new float [(int) genome.getGenomeLength()] [3];		
		for (int i = 0; i< genome.getGenomeLength(); i ++){
			for (int j = 0; j <3; j ++){
				dataPoints[i][j] = 0;
			}
		}
		
//		float[][] bpCounts = new float [maxchromSize][sampleSize];
//		for (int i = 0; i< maxchromSize;i++){
//			for (int j = 0; j<sampleSize;j ++){
//				bpCounts[i][j] = 0;
//			}
//		}
		
		int dataPointsIndex = 0;

		Iterator<Region> chroms = new ChromosomeGenerator<Genome>().execute(genome);
		//iterating each chromosome; each chromosome is a region.
		while (chroms.hasNext()) {
			
			Region currChrom = chroms.next();
			
			//get base pair counts for each chromosome
			Map<Sample, List<StrandedBaseCount>> sampleCountsMap = new HashMap<Sample, List<StrandedBaseCount>>();
			for (Sample sample : manager.getSamples()){
				sampleCountsMap.put(sample,sample.getBases(currChrom)); 
			}
			
			int currchromSize= currChrom.getWidth()+1;
			
			System.out.println("currentchromSize is: "+currchromSize);
			
			float[][] bpCounts = new float [currchromSize][sampleSize];
			for (int i = 0; i<currchromSize;i++){
				for (int j = 0; j<sampleSize; j++){
					bpCounts[i][j] = 0;
				}
			}
			
			int sampleCounter=0;

			//maybe this is fishy
			for (Sample sample : manager.getSamples()){
				List<StrandedBaseCount> currentCounts = sampleCountsMap.get(sample);
				for (StrandedBaseCount hits: currentCounts){
					bpCounts[hits.getCoordinate()][sampleCounter]=hits.getCount();	
				}
				sampleCounter++;
			}
			
			System.out.println("printing bpCounts");
			for (int i = 0;i <100; i++){
				for (int s = 0; s<sampleSize;s++){
					System.out.println(bpCounts[i][s]);
				}
			}

			int maxIndex = 0;
			float maxcounts = 0;
			float restSum = 0;
			
			//iterating bpCounts 
			//for each position in the chromosome, find the max among samples and copy it to dataPoints
			for (int i = 0; i<currchromSize;i++){
				// iterating one position among different samples
				for (int samp = 0; samp<sampleSize;samp++){
					if (bpCounts[i][samp]>maxcounts){
						maxcounts = bpCounts[i][samp];
						maxIndex = samp;
					}					
				}
				for (int samp = 0; samp<sampleSize;samp++){
					if (bpCounts[i][samp] != maxcounts){
						restSum = restSum + bpCounts[i][samp];
					}
				}
				if(maxcounts!=0){

					dataPoints[dataPointsIndex][0] = maxcounts;
					dataPoints[dataPointsIndex][1] = restSum;
					dataPoints[dataPointsIndex][2] = maxIndex;	
					
					dataPointsIndex++;
				}
				System.out.println("printing maxcounts, restSum"+maxcounts+"\t"+restSum+"\t"+ maxIndex);
				maxIndex = 0;
				maxcounts = 0;
				restSum = 0;
			}
			
			//end of chromosome iteration
			
			//clearing values in bpCounts
//			for (int i = 0; i<maxchromSize; i++){
//				for (int j = 0; j <sampleSize; j++){
//					bpCounts[i][j] = 0;
//				}
//			}
			bpCounts = null;
			
			// remove all mapping 
			sampleCountsMap.clear();		
		}
		
		System.out.println("#max tag number\tsum of other sample's tag\tsample identifier");

		//printing datapoints
		for (int i = 0; i<(int) genome.getGenomeLength(); i++){
			if (dataPoints[i][0] != 0){
				System.out.println(dataPoints[i][0]+"\t"+dataPoints[i][1]+"\t"+dataPoints[i][2]);
			}
		}
	}								
	
	public static void main(String[] args){
		
		GenomeConfig gconf = new GenomeConfig(args);
		ExptConfig  econf = new ExptConfig(gconf.getGenome(), args);
		
//		ArgParser ap = new ArgParser(args);
		
		CrossContaminationEstimator estimator = new CrossContaminationEstimator (gconf, econf);
		estimator.printDataPoints();	
	}
}
