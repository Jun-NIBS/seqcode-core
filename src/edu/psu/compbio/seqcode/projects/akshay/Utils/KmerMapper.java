package edu.psu.compbio.seqcode.projects.akshay.Utils;

import java.util.ArrayList;
import java.util.List;

import edu.psu.compbio.seqcode.gse.datasets.general.Point;
import edu.psu.compbio.seqcode.gse.datasets.general.Region;
import edu.psu.compbio.seqcode.gse.datasets.general.ScoredStrandedRegion;
import edu.psu.compbio.seqcode.gse.datasets.general.StrandedPoint;
import edu.psu.compbio.seqcode.gse.datasets.general.StrandedRegion;
import edu.psu.compbio.seqcode.gse.datasets.motifs.WeightMatrix;
import edu.psu.compbio.seqcode.gse.datasets.species.Genome;
import edu.psu.compbio.seqcode.gse.ewok.verbs.SequenceGenerator;
import edu.psu.compbio.seqcode.gse.ewok.verbs.motifs.WeightMatrixScoreProfile;
import edu.psu.compbio.seqcode.gse.ewok.verbs.motifs.WeightMatrixScorer;
import edu.psu.compbio.seqcode.gse.utils.sequence.SequenceUtils;
import edu.psu.compbio.seqcode.projects.shaun.Utilities;

public class KmerMapper {
	
	public List<StrandedPoint> points;
	public List<StrandedRegion> regions;
	public int k;
	public SequenceGenerator<Region> seqgen = new SequenceGenerator<Region>();
	public int[][][] MapMatrix;
	public int winSize;
	public Genome gen;
	public WeightMatrix motif;
	public WeightMatrixScorer scorer;
	public int motifThres;
	
	public KmerMapper(Genome g, int win, int threshold) {
		this.gen=g;
		this.winSize=win;
		this.motifThres = threshold;
	}
	
	//Settors
	
	public void setRegions(String peaksFileName,String SeqPathFile){
		List<Point> tempPoints = Utilities.loadPeaksFromPeakFile(gen, peaksFileName, winSize);
		List<Region> tempRegions = Utilities.loadRegionsFromPeakFile(gen, peaksFileName, winSize);
	
		seqgen.useCache(true);
		seqgen.setGenomePath(SeqPathFile);
		scorer = new WeightMatrixScorer(motif,seqgen);
			
		for(int i=0 ; i<tempRegions.size(); i++){
			Point p= tempPoints.get(i);
			Region r = tempRegions.get(i);
			WeightMatrixScoreProfile profiler = scorer.execute(r);
			int bestMotifIndex = profiler.getMaxIndex();
			double bestMotifScore = profiler.getMaxScore(bestMotifIndex);
			if(bestMotifScore >= motifThres){
				int closestDist = Integer.MAX_VALUE;
				int closestIndex = -1;
				char closestStrand = '\0';
				for(int z=0; z<r.getWidth()-motif.length()+1; z++){
					double currScore= profiler.getMaxScore(z);
					if(currScore>=motifThres){
						int motifCenterCoord = z+(motif.length()/2)+r.getStart();
						int dist = Math.abs(p.getLocation() - motifCenterCoord);
						if(dist<closestDist){
							closestDist = dist;
							closestIndex = z;
							closestStrand = profiler.getMaxStrand(z);
						}
					}
				}
				
				if(closestStrand == '+'){
					Point addP = new Point(gen,r.getChrom(),r.getStart()+closestIndex);
					points.add(new StrandedPoint(addP,closestStrand));
				}else{
					Point addP = new Point(gen,r.getChrom(),r.getStart()+closestIndex+motif.length()-1);
					points.add(new StrandedPoint(addP,closestStrand));
				}
			}
			for(StrandedPoint sp : points){
				regions.add(new StrandedRegion(sp.expand(winSize/2),sp.getStrand()));
			}
			
		}
	}
	
	
	public void setMapMatrix(String SeqPathFile){
		int numk = (int)Math.pow(4, k);
		this.MapMatrix = new int[numk][points.size()][winSize];
		seqgen.useCache(true);
		seqgen.setGenomePath(SeqPathFile);
		
		for(int i=0; i<regions.size(); i++){
			String seq = seqgen.execute(regions.get(i)).toUpperCase();
			if(seq.contains("N"))
				continue;
			String seqOriented = (regions.get(i).getStrand() == '+') ? seq : SequenceUtils.reverseComplement(seq);
			for(int j=0; j<(seq.length()-k+1); j++){
				String currK = seqOriented.substring(j, j+k);
				String revCurrK = SequenceUtils.reverseComplement(currK);
				int currKInt = Utilities.seq2int(currK);
				int revCurrKInt = Utilities.seq2int(revCurrK);
				int kmer = currKInt < revCurrKInt ? currKInt : revCurrKInt;
				MapMatrix[kmer][i][j] = 1;
			}
		}
		
	}
	
	
	// Calculators
	
	public void printInformativeKmersFromSet(String[] kmerSet, int percCutoff){
		for(int i=0; i<kmerSet.length; i++){
			int kmerID = Utilities.seq2int(kmerSet[i]);
			int[][] kmerMap = MapMatrix[kmerID];
			boolean pass = false;
			for(int j=0; j<kmerMap[0].length; j++){ // over all positions
				int rowSum =0;
				for(int k=0; k<kmerMap.length; k++){ // over all peaks
					rowSum = rowSum + kmerMap[j][k];
				}
				if((int)((rowSum*100)/kmerMap.length) > percCutoff){
					pass = true;
				}
			}
			if(pass){
				System.out.println(kmerSet[i]);
			}
		}
	}
	
	public void printInformativeKmers(int percCutoff){
		for(int i=0; i<MapMatrix.length; i++){
			int[][] kmerMap = MapMatrix[i];
			boolean pass = false;
			for(int j=0; j<kmerMap[0].length; j++){
				int rowSum =0;
				for(int k=0; k<kmerMap.length; k++){
					rowSum = rowSum + kmerMap[j][k];
				}
				if((int)((rowSum*100)/kmerMap.length) > percCutoff){
					pass = true;
				}
			}
			if(pass){
				System.out.println(Utilities.int2seq(i, k));
			}
			
		}
	}
	
	public void printInformativeKmerWithinDistance(int percCutoff, int distance){
		for(int i=0; i<MapMatrix.length; i++){
			int[][] kmerMap = MapMatrix[i];
			boolean pass = false;
			int midP = (int)(kmerMap[0].length/2);
			for(int j=(midP-distance); j<(midP+distance); j++){ // over all locations
				int rowSum =0;
				for(int k=0; k<kmerMap.length; k++){ // over all peaks
					rowSum = rowSum + kmerMap[j][k];
				}
				if((int)((rowSum*100)/kmerMap.length) > percCutoff){
					pass = true;
				}
			}
			if(pass){
				System.out.println(Utilities.int2seq(i, k));
			}
		}
		
	}
	
	public void printInformativeKmerWithinDistanceFromSet(String[] kmerSet, int percCutoff, int distance){
		for(int i=0; i<kmerSet.length; i++){
			int kmerID = Utilities.seq2int(kmerSet[i]);
			int[][] kmerMap = MapMatrix[kmerID];
			boolean pass = false;
			int midP = (int)(kmerMap[0].length/2);
			for(int j=(midP-distance); j<(midP+distance); j++){ // over all locations
				int rowSum =0;
				for(int k=0; k<kmerMap.length; k++){ // over all peaks
					rowSum = rowSum + kmerMap[j][k];
				}
				if((int)((rowSum*100)/kmerMap.length) > percCutoff){
					pass = true;
				}
			}
			if(pass){
				System.out.println(Utilities.int2seq(i, k));
			}
		}
	}
	
	/**
	 * excludes k-mers coming from motifs that have been centered
	 * @param percCutoff
	 * @param distance
	 */
	public void printInformativeKmerFlanks(int percCutoff, int distance){
		for(int i=0; i<MapMatrix.length; i++){
			int[][] kmerMap = MapMatrix[i];
			boolean pass = false;
			int midP = (int)(kmerMap[0].length/2);
			for(int j=(midP-distance); j<(midP+distance); j++){ // over all locations
				int rowSum =0;
				for(int k=0; k<kmerMap.length; k++){ // over all peaks
					rowSum = rowSum + kmerMap[j][k];
				}
				if((int)((rowSum*100)/kmerMap.length) > percCutoff && (j<midP || j> (midP+motif.length()-k))){
					pass = true;
				}
			}
			if(pass){
				System.out.println(Utilities.int2seq(i, k));
			}
		}
	}
	
	public void printInformativeKmerFlanksFromSet(String[] kmerSet, int percCutoff, int distance){
		for(int i=0; i<kmerSet.length; i++){
			int kmerID = Utilities.seq2int(kmerSet[i]);
			int[][] kmerMap = MapMatrix[kmerID];
			boolean pass = false;
			int midP = (int)(kmerMap[0].length/2);
			for(int j=(midP-distance); j<(midP+distance); j++){ // over all locations
				int rowSum =0;
				for(int k=0; k<kmerMap.length; k++){ // over all peaks
					rowSum = rowSum + kmerMap[j][k];
				}
				if((int)((rowSum*100)/kmerMap.length) > percCutoff && (j<midP || j> (midP+motif.length()-k))){
					pass = true;
				}
			}
			if(pass){
				System.out.println(Utilities.int2seq(i, k));
			}
		}
	}
	
	public static void main(String[] args){
		
		
	}

}