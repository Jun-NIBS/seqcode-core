package edu.psu.compbio.seqcode.projects.shaun;
/*
 * 
 * 
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import edu.psu.compbio.seqcode.gse.datasets.general.NamedRegion;
import edu.psu.compbio.seqcode.gse.datasets.general.Point;
import edu.psu.compbio.seqcode.gse.datasets.general.Region;
import edu.psu.compbio.seqcode.gse.datasets.general.ScoredStrandedRegion;
import edu.psu.compbio.seqcode.gse.datasets.general.StrandedPoint;
import edu.psu.compbio.seqcode.gse.datasets.motifs.MarkovBackgroundModel;
import edu.psu.compbio.seqcode.gse.datasets.motifs.WMHit;
import edu.psu.compbio.seqcode.gse.datasets.motifs.WeightMatrix;
import edu.psu.compbio.seqcode.gse.datasets.species.Gene;
import edu.psu.compbio.seqcode.gse.datasets.species.Genome;
import edu.psu.compbio.seqcode.gse.datasets.species.Organism;
import edu.psu.compbio.seqcode.gse.ewok.verbs.ChromRegionIterator;
import edu.psu.compbio.seqcode.gse.ewok.verbs.PointParser;
import edu.psu.compbio.seqcode.gse.ewok.verbs.RefGeneGenerator;
import edu.psu.compbio.seqcode.gse.ewok.verbs.RegionParser;
import edu.psu.compbio.seqcode.gse.ewok.verbs.SequenceGenerator;
import edu.psu.compbio.seqcode.gse.ewok.verbs.motifs.FASTALoader;
import edu.psu.compbio.seqcode.gse.ewok.verbs.motifs.WeightMatrixScoreProfile;
import edu.psu.compbio.seqcode.gse.ewok.verbs.motifs.WeightMatrixScorer;
import edu.psu.compbio.seqcode.gse.tools.utils.Args;
import edu.psu.compbio.seqcode.gse.utils.ArgParser;
import edu.psu.compbio.seqcode.gse.utils.NotFoundException;
import edu.psu.compbio.seqcode.gse.utils.Pair;
import edu.psu.compbio.seqcode.gse.utils.io.motifs.BackgroundModelIO;
import edu.psu.compbio.seqcode.gse.utils.sequence.SequenceUtils;
import edu.psu.compbio.seqcode.gse.viz.metagenes.BinningParameters;

public class MotifAnalysisSandbox {
	private Genome gen;
	private WeightMatrix motif;
	private WeightMatrixScorer scorer;
	private SequenceGenerator seqgen;
	private ArrayList<Region> regions=null;
	private ArrayList<Point> peaks=null;
	private ArrayList<String> inFileLines;
	private double motifThres=-100000;
	private int averageRegWidth=0, maxRegWidth=0; 
	private int maxGeneDistance=100000;
	private int motifDensityWinSize=1000000;
	private int motifDensityStepSize=500000;
	
	public static void main(String[] args) throws IOException, ParseException {
		boolean havePeaks=false;
		boolean printHits=false, printBestHits=false, printPeakClosestHits=false, printSeqs=false, printSeqsNoMotifs=false;
		boolean printSeqsWithMotifs=false, printPeakInfo=false, printPeaksWithMotifs=false, printPeaksNoMotifs=false;
		boolean printMotifScore=false, printMotifPerc=false, motifDistHist=false, screenMotifs=false, scoreSeqs=false;
		boolean motifDensity=false, pocc=false, wholeGenome=false, printprofiles=false, motifHisto=false;;
		String seqFile=null;
		String genomeSequencePath=null;
		MotifAnalysisSandbox tools;
		
		ArgParser ap = new ArgParser(args);
        if((!ap.hasKey("species") && !ap.hasKey("geninfo")) || (!ap.hasKey("motiffile") && !ap.hasKey("motifname"))) { 
            System.err.println("Usage:\n " +
                               "MotifAnalysisSandbox " +
                               "--species <organism;genome> OR\n" +
                               "--geninfo <genome info> AND --seq <path to seqs>\n" +
                               "--motiffile <file with motifs> AND --back <background>\nOR\n"+
                               "--motifname <weightmatrix name> AND"+
                               "--motifversion <weightmatrix version> \n"+
                               "--peaks <file containing coordinates of peaks> \n" +
                               "--wholegenome [motif analysis in whole genome]\n"+
                               "--win <window of sequence to take around peaks> "+
                               "--minscore <minimum motif score> " +
                               "--motifthres <file with thresholds> " +
                               "--threslevel <threshold level> " +
                               "\nOPTIONS:\n" +
                               "--printhits " +
                               "--printbesthits " +
                               "--printpeakclosesthits " +
                               "--printseqs " +
                               "--printseqsnomotifs " +
                               "--printseqswithmotifs " +
                               "--screenmotifs " +
                               "--printpeaksnomotifs " +
                               "--printpeakswithmotifs " +
                               "--printpeakinfo " +
                               "--printmotifscore " +
                               "--printmotifperc " +
                               "--motifdensity " +
                               "--motifdisthist \n" +
                               "--motifhisto \n" +
                               "--scoreseqs <seqFile> " +
                               "--pocc <seqFile>\n" +
                               "--printprofiles [motif-profiler style vectors] --bins <num bin for profile> --binsize <binsize for histo>\n");
            return;
        }
        String motifversion=null, motifname=null, motiffile=null, backfile=null;
        boolean loadFromFile=false;
        //Motifs
        if(ap.hasKey("motifname")){
	        if(ap.hasKey("motifversion")){motifversion = ap.getKeyValue("motifversion");}
	        motifname = ap.getKeyValue("motifname");
	        if (motifname.indexOf(';') != -1) {
	            String[] pieces = motifname.split(";");
	            motifname = pieces[0];
	            motifversion = pieces[1];
	        }
        }else if(ap.hasKey("motiffile")){
        	loadFromFile=true;
        	motiffile = ap.getKeyValue("motiffile");
        	backfile = ap.getKeyValue("back");
        }
        havePeaks = ap.hasKey("peaks");
        String posFile = ap.getKeyValue("peaks");
        int win = ap.hasKey("win") ? new Integer(ap.getKeyValue("win")).intValue():-1;
        boolean usingWin= win>0;
        int bins = ap.hasKey("bins") ? new Integer(ap.getKeyValue("bins")).intValue():1; //Used by profile printing
        int binsize = ap.hasKey("binsize") ? new Integer(ap.getKeyValue("binsize")).intValue():10; //Used by motif histogram
        double minscore = (ap.hasKey("minscore")) ? new Double(ap.getKeyValue("minscore")).doubleValue() : -10000;
        genomeSequencePath = ap.hasKey("seq") ? ap.getKeyValue("seq") : null;
        printHits = ap.hasKey("printhits");
        printBestHits = ap.hasKey("printbesthits");
        printPeakClosestHits = ap.hasKey("printpeakclosesthits");
        printSeqs = ap.hasKey("printseqs");
        printSeqsNoMotifs = ap.hasKey("printseqsnomotifs");
        printSeqsWithMotifs = ap.hasKey("printseqswithmotifs");
        printPeaksNoMotifs = ap.hasKey("printpeaksnomotifs");
        printPeaksWithMotifs = ap.hasKey("printpeakswithmotifs");
        printPeakInfo = ap.hasKey("printpeakinfo");
        printMotifScore = ap.hasKey("printmotifscore");
        printMotifPerc = ap.hasKey("printmotifperc");
        motifDistHist= ap.hasKey("motifdisthist");
        motifHisto= ap.hasKey("motifhisto");
        motifDensity= ap.hasKey("motifdensity");
        screenMotifs= ap.hasKey("screenmotifs");
        scoreSeqs = ap.hasKey("scoreseqs");
        wholeGenome = ap.hasKey("wholegenome");
        printprofiles = ap.hasKey("printprofiles");
        String thresFile = ap.hasKey("motifthres") ? ap.getKeyValue("motifthres"):null;
    	double thresLevel = ap.hasKey("threslevel") ? new Double(ap.getKeyValue("threslevel")).doubleValue():0.05;
        pocc = ap.hasKey("pocc");
        if(scoreSeqs)
        	seqFile = ap.getKeyValue("scoreseqs");
        if(pocc)
        	seqFile = ap.getKeyValue("pocc");
        
        ArrayList<Region> posRegs = new ArrayList<Region>();
        ArrayList<Point> posPeaks = new ArrayList<Point>();
        ArrayList<String> posLines= new ArrayList<String>();
        
        
		try {
			Genome currgen=null;
			Organism currorg=null;
			//Load genome
			if(ap.hasKey("species")){
				Pair<Organism, Genome> pair = Args.parseGenome(args);
				if(pair != null){
					currorg = pair.car();
					currgen = pair.cdr();
				}
			}else{
				if(ap.hasKey("geninfo") || ap.hasKey("g")){
					//Make fake genome... chr lengths provided
					String fName = ap.hasKey("geninfo") ? ap.getKeyValue("geninfo") : ap.getKeyValue("g");
					currgen = new Genome("Genome", new File(fName), true);
				}else{
					currgen = null;
				}
			}
			
			
			//Load motif
			WeightMatrix matrix = null;
			if(loadFromFile){
			    matrix = loadMotifFromFile(motiffile, backfile, currgen);
			}else{
				if(currorg!=null){
					int wmid = WeightMatrix.getWeightMatrixID(currorg.getDBID(), motifname, motifversion);
					matrix = WeightMatrix.getWeightMatrix(wmid);
				}
			}
			if(thresFile!=null){
	        	HashMap<String,Double> thres = loadThresholdsFromFile(thresFile, thresLevel);
	        	if(thres.containsKey(matrix.getName()))
	        		minscore = thres.get(matrix.getName());
			}
			
	        //Load the positive hits
	        if(havePeaks){
		        File pFile = new File(posFile);
				if(!pFile.isFile()){System.err.println("Invalid positive file name");System.exit(1);}
	            BufferedReader reader = new BufferedReader(new FileReader(pFile));
	            String line;
	            while ((line = reader.readLine()) != null) {
	                line = line.trim();
                	Point pt=null; Region reg=null;
                	String [] curr = line.split("\\s+");
        			String coord = curr[0];
        			if(!curr[0].contains("#")){
        				if(curr.length>=3 && curr[2].contains(":")){coord = curr[2];}
	        			
	        			if(coord.contains(":")) {
	        				String [] currB = coord.split(":");
	        				String chrom = currB[0]; chrom=chrom.replaceFirst("chr", "");
	        				char strand = '?';
	        				if(currB.length==3)
	        					strand = currB[2].charAt(0);
	        				int location=-1, rstart=-1, rend=-1;
	        				if(currB[1].contains("-")){
	        					String [] currC = currB[1].split("-");
	        					location = (new Integer(currC[0])+new Integer(currC[1]))/2;
	        					rstart = new Integer(currC[0]);
	        					rend = new Integer(currC[1]);
	        				}else{
	        					location = new Integer(currB[1]);
	        					rstart = Math.max(0, location-(win/2));
	        					rend = Math.min(0, location+(win/2));
	        				}
	        				if(strand!='?')
	        					pt = new StrandedPoint(currgen, chrom, location, strand);
	        				else
	        					pt = new Point(currgen, chrom, location);
	        				
	        				if(usingWin)
	        					reg = pt.expand(win/2);
	        				else
	        					reg = new Region(currgen, chrom, rstart, rend);
	        			}
                	
	                    if(pt!=null && reg!=null){
		                	posPeaks.add(pt);
		                	posRegs.add(reg);
		                	posLines.add(line);
		                }	                    
	                }
	            }
	        }else if(wholeGenome){
	        	ChromRegionIterator chroms = new ChromRegionIterator(currgen);
				while(chroms.hasNext()){
					NamedRegion c = chroms.next();
					posRegs.add(c);
				}
	        }
	        
            ////////////////////////////////////////////////////////////////////////
	        tools = new MotifAnalysisSandbox(currgen, matrix, posLines, posPeaks, posRegs, minscore, genomeSequencePath);
	        
	        if(printHits){
	        	if(wholeGenome)
	        		tools.printMotifHitsFullGenome();
	        	else
	        		tools.printMotifHits();
	        }
	        if(printBestHits)
	        	tools.printBestMotifHits();
	        if(printPeakClosestHits)
	        	tools.printPeakClosestMotifHits();
	        if(printSeqs)
	        	tools.printPeakSeqs();
	        if(printSeqsNoMotifs)
	        	tools.printSeqsWithoutMotifHits();
	        if(printSeqsWithMotifs)
	        	tools.printSeqsWithMotifHits();
	        if(printPeaksWithMotifs)
	        	tools.printPeaksWithMotifs();
	        if(printPeaksNoMotifs)
	        	tools.printPeaksWithoutMotifs();
	        if(printPeakInfo)
	        	tools.printPeakInfo();
	        if(printMotifScore)
	        	tools.motifScoreThreshold();
	        if(printMotifPerc)
	        	tools.motifPercThreshold();
	        if(motifDistHist)
	        	tools.peak2ClosestMotifHisto(binsize);
	        if(motifHisto)
	        	tools.peak2motifHisto(binsize);
	        if(screenMotifs)
	        	tools.screenMotifHits();
	        if(scoreSeqs && seqFile !=null){
	        	if(screenMotifs)
	        		tools.screenFastaFile(seqFile);
	        	else
	        		tools.scoreFastaFile(seqFile);
	        }
	        if(motifDensity)
	        	tools.printMotifDensity();
	        if(pocc)
	        	tools.pOccFastaFile(seqFile);
	        if(printprofiles)
	        	tools.printMotifProfiles(win, bins);
	        
	        ////////////////////////////////////////////////////////////////////////
	        
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	//Constructor 
	public MotifAnalysisSandbox(Genome g, WeightMatrix wm, ArrayList<String> inL, ArrayList<Point> pospeak, ArrayList<Region> posreg, double minscore, String genomeSequencePath){
		gen=g;
		motif= wm;
		inFileLines = inL;
		peaks=pospeak;
		regions=posreg;
		averageRegWidth = findAvgWidth(regions);
		maxRegWidth = findMaxWidth(regions);
		motifThres = minscore;
		seqgen = new SequenceGenerator(g);
		if(genomeSequencePath != null){
			seqgen.useCache(true);
			seqgen.useLocalFiles(true);
			seqgen.setGenomePath(genomeSequencePath);
		}
		scorer = new WeightMatrixScorer(motif, seqgen);
	}
	//Load freq matrix
	public static WeightMatrix loadMotifFromFile(String filename, String backname, Genome gen) throws IOException, ParseException {
		FreqMatrixImport motifImport = new FreqMatrixImport();
		MarkovBackgroundModel back = BackgroundModelIO.parseMarkovBackgroundModel(backname, gen);
    	motifImport.setBackground(back);
		return(motifImport.readTransfacMatrices(filename).getFirst());		
	}
	private int findAvgWidth(ArrayList<Region> regs){
		long total=0, num=0;
		for(Region r: regs){
			total+=r.getWidth();
			num++;
		}
		return(num==0 ? -1 :(int)(total/num));
	}
	private int findMaxWidth(ArrayList<Region> regs){
		int max=0;
		for(Region r: regs)
			if(r.getWidth()>max){max = r.getWidth();}
		return(max);
	}
	
	//Find the best motif matches in the regions and report the distances to the peaks
	public void motif2PeakHisto(){		
		int binSize = 20;
		int numBins = (motif.length()+(averageRegWidth/2))/binSize;
		double [] motifDistHisto = new double [numBins+1];
		for(int h=0; h<=numBins; h++){motifDistHisto[h]=0;}
		
		//Add the positive set scores
		for(int i=0; i<regions.size(); i++){
			Region r = regions.get(i);
			Point p = peaks.get(i);
			
			WeightMatrixScoreProfile profiler = scorer.execute(r);
			int bestMotifIndex = profiler.getMaxIndex();
			double bestMotifScore = profiler.getMaxScore(bestMotifIndex);
			
			if(bestMotifScore>=motifThres){
				int peak2motif = Math.abs((p.getLocation()-r.getStart())-bestMotifIndex+(motif.length()/2));
				motifDistHisto[peak2motif/binSize]++;				
				//System.out.println(p.getLocationString()+"\t"+peak2motif+"\t"+bestMotifScore);
			}
		}
		
		System.out.println("\nMotif-to-Peak Histogram\nDistance\tMotifs");
		for(int h=0; h<=numBins; h++){
			int bin =h*binSize; 
			System.out.println(bin+"\t"+motifDistHisto[h]);
		}
		
	}
	
	//Find the closest motif match to the peak (that's over the threshold) 
	public void peak2ClosestMotifHisto(int binS){
		int binSize = binS;
		int numBins = (motif.length()+(maxRegWidth/2))/binSize;
		double [] motifDistHisto = new double [numBins+1];
		for(int h=0; h<=numBins; h++){motifDistHisto[h]=0;}
		
		//Add the positive set scores
		for(int i=0; i<regions.size(); i++){
			Region r = regions.get(i);
			Point p = peaks.get(i);
			
			WeightMatrixScoreProfile profiler = scorer.execute(r);
			int closeDist=1000000;
			boolean goodMotif=false;
			for(int z=0; z<r.getWidth(); z++){
				int mpos = z+(motif.length()/2);
				double currScore= profiler.getMaxScore(z);
				if(currScore>=motifThres && Math.abs((p.getLocation()-r.getStart())-mpos)<Math.abs(closeDist)){
					closeDist = (p.getLocation()-r.getStart())-mpos;
					goodMotif=true;
				}
			}
			if(goodMotif){
				//System.out.println(p.getLocationString()+"\t"+closeDist);
				motifDistHisto[(Math.abs(closeDist))/binSize]++;
			}
		}
		
		System.out.println("\nPeak-to-ClosestMotif Histogram\nDistance\tMotifs");
		for(int h=0; h<=numBins; h++){
			int bin =h*binSize; 
			System.out.println(bin+"\t"+motifDistHisto[h]);
		}
	}

	//Histogram of all matches to the motif vs distance to peak 
	public void peak2motifHisto(int binSize){
		int numBins = (motif.length()+(maxRegWidth/2))/binSize;
		double [] motifDistHisto = new double [numBins+1];
		for(int h=0; h<=numBins; h++){motifDistHisto[h]=0;}
		
		//Add the positive set scores
		for(int i=0; i<regions.size(); i++){
			Region r = regions.get(i);
			Point p = peaks.get(i);
			
			WeightMatrixScoreProfile profiler = scorer.execute(r);
			for(int z=0; z<r.getWidth(); z++){
				double currScore= profiler.getMaxScore(z);
				if(currScore>=motifThres){
					int dist = (p.getLocation()-r.getStart())-z;
					motifDistHisto[(Math.abs(dist))/binSize]++;
				}
			}
		}
		
		System.out.println("\nPeak-to-Motif Histogram\nDistance\tMotifCounts\tMotifCountsNorm");
		for(int h=0; h<=numBins; h++){
			int bin =h*binSize; 
			System.out.println(bin+"\t"+motifDistHisto[h]+"\t"+motifDistHisto[h]/(double)regions.size());
		}
	}
	//Simple count of hits
	public void countHits(){
		//System.out.println("Counting hits");
		int numHits=0, peaksWithHits=0, totalPeaks=0;
		for(int i=0; i<regions.size(); i++){
			totalPeaks++;
			Region r = regions.get(i);
			Point p = peaks.get(i);
			
			WeightMatrixScoreProfile profiler = scorer.execute(r);
			boolean goodMotif=false;
			for(int z=0; z<r.getWidth(); z++){
				double currScore= profiler.getMaxScore(z);
				if(currScore>=motifThres){
					numHits++;
					goodMotif=true;
				}
			}
			if(goodMotif){
				peaksWithHits++;
			}
		}
		System.out.println(numHits+" hits in "+peaksWithHits+" peaks from "+totalPeaks+" total peaks.");
	}
	//printing the hit sequences
	public void printMotifHits(){
		int numHits=0, peaksWithHits=0, totalPeaks=0;
		ArrayList<ScoredStrandedRegion> hits = new ArrayList<ScoredStrandedRegion>();
		ArrayList<String> hitseqs = new ArrayList<String>();
		
		for(int i=0; i<regions.size(); i++){
			totalPeaks++;
			Region r = regions.get(i);
			String seq = seqgen.execute(r);
			
			WeightMatrixScoreProfile profiler = scorer.execute(r);
			boolean goodMotif=false;
			for(int z=0; z<r.getWidth()-motif.length()+1; z++){
				double currScore= profiler.getMaxScore(z);
				if(currScore>=motifThres){
					numHits++;
					goodMotif=true;
					String subseq = seq.substring(z, z+motif.length());
					Region hitreg =new Region(gen, r.getChrom(), r.getStart()+z, r.getStart()+z+motif.length()-1);
					hits.add(new ScoredStrandedRegion(gen, r.getChrom(), hitreg.getStart(), hitreg.getEnd(), currScore, profiler.getMaxStrand(z)));
					if(profiler.getMaxStrand(z)=='+'){hitseqs.add(subseq);
					}else{hitseqs.add(SequenceUtils.reverseComplement(subseq));}
				}
			}
			if(goodMotif){
				peaksWithHits++;
			}
        }
		double perc = (double)peaksWithHits/(double)totalPeaks;
		System.out.println(motif.name+" hits: "+numHits+" hits in "+peaksWithHits+" regions from "+totalPeaks+" total peaks ("+perc+").");
		for(int h=0; h<hits.size(); h++){
			//System.out.println(hits.get(h)+"\t"+hitseqs.get(h)+"\t"+SequenceUtils.reverseComplement(hitseqs.get(h)));
			System.out.println(hits.get(h).toTabString()+"\t"+hitseqs.get(h));
		}
	}
	//printing the best hit sequences
	public void printBestMotifHits(){
		int numHits=0, peaksWithHits=0, totalPeaks=0;
		ArrayList<ScoredStrandedRegion> hits = new ArrayList<ScoredStrandedRegion>();
		ArrayList<String> hitseqs = new ArrayList<String>();
		
		for(int i=0; i<regions.size(); i++){
			totalPeaks++;
			Region r = regions.get(i);
			String seq = seqgen.execute(r);
			
			WeightMatrixScoreProfile profiler = scorer.execute(r);
			int bestMotifIndex = profiler.getMaxIndex();
			double bestMotifScore = profiler.getMaxScore(bestMotifIndex);
			if(bestMotifScore>=motifThres){
				numHits++;
				String subseq = seq.substring(bestMotifIndex, bestMotifIndex+motif.length());
				Region hitreg =new Region(gen, r.getChrom(), r.getStart()+bestMotifIndex, r.getStart()+bestMotifIndex+motif.length()-1);
				hits.add(new ScoredStrandedRegion(gen, r.getChrom(), hitreg.getStart(), hitreg.getEnd(), bestMotifScore, profiler.getMaxStrand(bestMotifIndex)));
				if(profiler.getMaxStrand(bestMotifIndex)=='+'){hitseqs.add(subseq);
				}else{hitseqs.add(SequenceUtils.reverseComplement(subseq));}
				peaksWithHits++;				
			}
        }
		double perc = (double)peaksWithHits/(double)totalPeaks;
		System.out.println(motif.name+" hits: "+numHits+" hits in "+peaksWithHits+" regions from "+totalPeaks+" total peaks ("+perc+").");
		for(int h=0; h<hits.size(); h++){
			//System.out.println(hits.get(h)+"\t"+hitseqs.get(h)+"\t"+SequenceUtils.reverseComplement(hitseqs.get(h)));
			System.out.println(hits.get(h).toTabString()+"\t"+hitseqs.get(h));
		}
	}
	//printing the hit closest to the peak
	public void printPeakClosestMotifHits(){
		int numHits=0, peaksWithHits=0, totalPeaks=0;
		ArrayList<ScoredStrandedRegion> hits = new ArrayList<ScoredStrandedRegion>();
		ArrayList<String> hitseqs = new ArrayList<String>();
		
		for(int i=0; i<regions.size(); i++){
			totalPeaks++;
			Region r = regions.get(i);
			Point p = peaks.get(i);
			String seq = seqgen.execute(r);
			
			WeightMatrixScoreProfile profiler = scorer.execute(r);
			int bestMotifIndex = profiler.getMaxIndex();
			double bestMotifScore = profiler.getMaxScore(bestMotifIndex);
			
			if(bestMotifScore>=motifThres){
				int closestDist = Integer.MAX_VALUE;
				int closestIndex = -1;
				double closestScore=0.0;
				for(int z=0; z<r.getWidth()-motif.length()+1; z++){
					double currScore= profiler.getMaxScore(z);
					if(currScore>=motifThres){
						int motifCenterCoord = z+(motif.length()/2)+r.getStart();
						int dist = Math.abs(p.getLocation() - motifCenterCoord);
						if(dist<closestDist){
							closestDist = dist;
							closestIndex = z;
							closestScore = currScore;
						}
					}
				}
				numHits++;
				String subseq = seq.substring(closestIndex, closestIndex+motif.length());
				Region hitreg =new Region(gen, r.getChrom(), r.getStart()+closestIndex, r.getStart()+closestIndex+motif.length()-1);
				hits.add(new ScoredStrandedRegion(gen, r.getChrom(), hitreg.getStart(), hitreg.getEnd(), closestScore, profiler.getMaxStrand(closestIndex)));
				if(profiler.getMaxStrand(closestIndex)=='+'){hitseqs.add(subseq);
				}else{hitseqs.add(SequenceUtils.reverseComplement(subseq));}
				peaksWithHits++;				
			}
        }
		double perc = (double)peaksWithHits/(double)totalPeaks;
		System.out.println("#"+motif.name+" hits: "+numHits+" hits in "+peaksWithHits+" regions from "+totalPeaks+" total peaks ("+perc+").");
		for(int h=0; h<hits.size(); h++){
			//System.out.println(hits.get(h)+"\t"+hitseqs.get(h)+"\t"+SequenceUtils.reverseComplement(hitseqs.get(h)));
			System.out.println(hits.get(h).toTabString()+"\t"+hitseqs.get(h));
		}
	}
	//printing the hit sequences
	public void printMotifHitsFullGenome(){
		int numHits=0, peaksWithHits=0, totalPeaks=0;
		ArrayList<ScoredStrandedRegion> hits = new ArrayList<ScoredStrandedRegion>();
		ArrayList<String> hitseqs = new ArrayList<String>();
		
		for(int i=0; i<regions.size(); i++){
			totalPeaks++;
			Region r = regions.get(i);
			
			int rstart = r.getStart();
            int chunksize = 10000000;
            int length = motif.length();
            // work over the target region in pieces
            while (rstart < r.getEnd()) {
                int rend = rstart + chunksize;
                if (rend > r.getEnd()) {
                    rend = r.getEnd();
                }if (rend - rstart < length) {break;}
				
                Region cr = new Region(r.getGenome(), r.getChrom(), rstart, rend);
				String seq = seqgen.execute(cr);
				
				WeightMatrixScoreProfile profiler = scorer.execute(cr);
				boolean goodMotif=false;
				for(int z=0; z<cr.getWidth(); z++){
					double currScore= profiler.getMaxScore(z);
					if(currScore>=motifThres){
						numHits++;
						goodMotif=true;
						String subseq = seq.substring(z, z+motif.length());
						Region hitreg =new Region(gen, r.getChrom(), cr.getStart()+z, cr.getStart()+z+motif.length()-1);
						ScoredStrandedRegion ssr = new ScoredStrandedRegion(gen, r.getChrom(), hitreg.getStart(), hitreg.getEnd(), currScore, profiler.getMaxStrand(z));
						String currseq = null;
						if(profiler.getMaxStrand(z)=='+'){currseq = subseq;
						}else{currseq = SequenceUtils.reverseComplement(subseq);}
						System.out.println(ssr.toTabString()+"\t"+currseq);
					}
				}
				if(goodMotif){
					peaksWithHits++;
				}
				rstart = rend - length + 1;
            }
		}
		double perc = (double)peaksWithHits/(double)totalPeaks;
		System.out.println(motif.name+" hits: "+numHits+" hits in "+peaksWithHits+" regions from "+totalPeaks+" total peaks ("+perc+").");		
	}
	//printing the hit sequences
	public void printMotifHitsAndPoints(){
		int numHits=0, peaksWithHits=0, totalPeaks=0;
		ArrayList<ScoredStrandedRegion> hits = new ArrayList<ScoredStrandedRegion>();
		ArrayList<Point> hitPoints = new ArrayList<Point>();
		ArrayList<String> hitseqs = new ArrayList<String>();
		
		for(int i=0; i<regions.size(); i++){
			totalPeaks++;
			Region r = regions.get(i);
			String seq = seqgen.execute(r);
			
			WeightMatrixScoreProfile profiler = scorer.execute(r);
			boolean goodMotif=false;
			for(int z=0; z<r.getWidth(); z++){
				double currScore= profiler.getMaxScore(z);
				if(currScore>=motifThres){
					numHits++;
					goodMotif=true;
					String subseq = seq.substring(z, z+motif.length());
					Region hitreg =new Region(gen, r.getChrom(), r.getStart()+z, r.getStart()+z+motif.length()-1);
					hits.add(new ScoredStrandedRegion(gen, r.getChrom(), hitreg.getStart(), hitreg.getEnd(), currScore, profiler.getMaxStrand(z)));
					hitPoints.add(peaks.get(i));
					if(profiler.getMaxStrand(z)=='+'){hitseqs.add(subseq);
					}else{hitseqs.add(SequenceUtils.reverseComplement(subseq));}
				}
			}
			if(goodMotif){
				peaksWithHits++;
			}
		}
		double perc = (double)peaksWithHits/(double)totalPeaks;
		System.out.println(motif.name+" hits: "+numHits+" hits in "+peaksWithHits+" peaks from "+totalPeaks+" total peaks ("+perc+").");
		for(int h=0; h<hits.size(); h++){
			System.out.println(hitPoints.get(h)+"\t"+hits.get(h).toTabString()+"\t"+hitseqs.get(h)+"\t"+SequenceUtils.reverseComplement(hitseqs.get(h)));
			//System.out.println(hits.get(h)+"\t"+hitseqs.get(h));
		}
	}
	
	//Simple printing of peak lines that contain the motif
	public void printPeaksWithMotifs(){
		for(int i=0; i<regions.size(); i++){
			Region r = regions.get(i);
			WeightMatrixScoreProfile profiler = scorer.execute(r);
			boolean goodMotif=false;
			for(int z=0; z<r.getWidth(); z++){
				double currScore= profiler.getMaxScore(z);
				if(currScore>=motifThres)
					goodMotif=true;				
			}
			if(goodMotif)
				System.out.println(inFileLines.get(i));
		}
	}
	
	//Simple printing of peak lines that don't contain the motif
	public void printPeaksWithoutMotifs(){
		for(int i=0; i<regions.size(); i++){
			Region r = regions.get(i);
			WeightMatrixScoreProfile profiler = scorer.execute(r);
			boolean goodMotif=false;
			for(int z=0; z<r.getWidth(); z++){
				double currScore= profiler.getMaxScore(z);
				if(currScore>=motifThres)
					goodMotif=true;				
			}
			if(!goodMotif)
				System.out.println(inFileLines.get(i));
		}
	}
	
	//Print the sequences for each peak 
	public void printPeakSeqs(){
		for(int i=0; i<regions.size(); i++){
			Region r = regions.get(i);
			String seq = seqgen.execute(r);
			seq = seq.toLowerCase();
			System.out.println(">"+r.toString()+"\n"+seq);
		}
	}
	//Screen out sequences without motifs occurences 
	public void printSeqsWithMotifHits(){
		for(int i=0; i<regions.size(); i++){
			Region r = regions.get(i);
			
			String seq = seqgen.execute(r);
			seq = seq.toLowerCase();
			WeightMatrixScoreProfile profiler = scorer.execute(r);
			boolean goodMotif=false;
			for(int z=0; z<r.getWidth(); z++){
				double currScore= profiler.getMaxScore(z);
				if(currScore>=motifThres)
				{	goodMotif=true;
					String mmatch = seq.substring(z, z+motif.length());
					seq = seq.replaceAll(mmatch, mmatch.toUpperCase());
				}
			}
			if(goodMotif)
				System.out.println(">"+r.toString()+"\n"+seq);
		}
	}
	//Screen out good motif hits from the input sequence 
	public void printSeqsWithoutMotifHits(){
		for(int i=0; i<regions.size(); i++){
			Region r = regions.get(i);
			
			String seq = seqgen.execute(r);
			seq = seq.toLowerCase();
			WeightMatrixScoreProfile profiler = scorer.execute(r);
			boolean goodMotif=false;
			for(int z=0; z<r.getWidth(); z++){
				double currScore= profiler.getMaxScore(z);
				if(currScore>=motifThres)
					goodMotif=true;				
			}
			if(!goodMotif)
				System.out.println(">"+r.toString()+"\n"+seq);
		}
	}
	
	//Screen out sequences containing good motif hits from the input sequence 
	public void screenMotifHits(){
		for(int i=0; i<regions.size(); i++){
			Region r = regions.get(i);
			
			char [] seq = seqgen.execute(r).toCharArray();
			boolean [] covered = motifCoverage(r);
			
			StringBuilder outseq = new StringBuilder();
			for(int j=0; j<r.getWidth(); j++){
				if(!covered[j]){
					outseq.append(seq[j]);
				}else{
					outseq.append('N');
				}
			}
			System.out.println(">"+r.toString()+"\n"+outseq.toString());
		}
	}
	
	//Print score if > threshold 
	public void motifScoreThreshold(){
		for(int i=0; i<regions.size(); i++){
			Region r = regions.get(i);
			
			String seq = seqgen.execute(r);
			WeightMatrixScoreProfile profiler = scorer.execute(seq);
			boolean goodMotif=false;
			double maxScore= profiler.getMaxScore();
			if(maxScore>=motifThres)
				goodMotif=true;				
			
			int index = profiler.getMaxIndex();
			String bestSeq = seq.substring(index, index+motif.length());
			if(profiler.getMaxStrand()=='-'){
				bestSeq = SequenceUtils.reverseComplement(bestSeq);
			}
			
			String[] words = inFileLines.get(i).split("\\s+");
			
			if(!goodMotif)
				System.out.println(words[0]+"\t"+words[1]+"\t"+words[2]+"\t"+words[3]+"\t"+motif.getMinScore()+"\t"+bestSeq);
			else
				System.out.println(words[0]+"\t"+words[1]+"\t"+words[2]+"\t"+words[3]+"\t"+maxScore+"\t"+bestSeq);
		}
	}
	
	//Print score if > threshold 
	public void motifPercThreshold(){
		for(int i=0; i<regions.size(); i++){
			Region r = regions.get(i);
			
			WeightMatrixScoreProfile profiler = scorer.execute(r);
			boolean goodMotif=false;
			double maxScore= profiler.getMaxScore();
			if(maxScore>=motifThres)
				goodMotif=true;
			else
				maxScore = motif.getMinScore();
			
			String[] words = inFileLines.get(i).split("\\s+");
			
			double perc = (maxScore-motif.getMinScore())/(motif.getMaxScore()-motif.getMinScore()); 
			System.out.println(words[0]+"\t"+words[1]+"\t"+words[2]+"\t"+words[3]+"\t"+perc);
		}
	}
	
	//Print representations of the motif-scoring landscape over each sequence
	//Watch out for the minimum motif score in this; if the motif threshold is below zero, the default will look funky
	public void printMotifScoreLandscape(){
		for(int i=0; i<regions.size(); i++){
			Region r = regions.get(i);
			double [] scores = new double[r.getWidth()];
			for(int j=0; i<r.getWidth(); j++){scores[j]=0;}
			
			WeightMatrixScoreProfile profiler = scorer.execute(r);
			for(int z=0; z<r.getWidth(); z++){
				double currScore= profiler.getMaxScore(z);
				if(currScore>=motifThres){
					for(int j=0; j<motif.length() && z+j<r.getWidth(); j++){
						if(currScore>=scores[z+j]){
							scores[z+j]=currScore;
						}
					}
				}
			}
			System.out.print(r.toString());
			for(int z=0; z<r.getWidth(); z++){
				System.out.print("\t"+scores[z]);
			}System.out.print("\n");
		}
	}
	
	//Returns the hits to a motif in a region
	private ArrayList<ScoredStrandedRegion> getMotifHits(Region r){
		ArrayList<ScoredStrandedRegion> hits = new ArrayList<ScoredStrandedRegion>();
		ArrayList<String> hitseqs = new ArrayList<String>();
		
		String seq = seqgen.execute(r);
		
		WeightMatrixScoreProfile profiler = scorer.execute(r);
		boolean goodMotif=false;
		for(int z=0; z<r.getWidth(); z++){
			double currScore= profiler.getMaxScore(z);
			if(currScore>=motifThres){
				goodMotif=true;
				String subseq = seq.substring(z, z+motif.length());
				Region hitreg =new Region(gen, r.getChrom(), r.getStart()+z, r.getStart()+z+motif.length()-1);
				hits.add(new ScoredStrandedRegion(gen, r.getChrom(), hitreg.getStart(), hitreg.getEnd(), currScore, profiler.getMaxStrand(z)));
				if(profiler.getMaxStrand(z)=='+'){hitseqs.add(subseq);
				}else{hitseqs.add(SequenceUtils.reverseComplement(subseq));}
			}
		}
		return(hits);
	}
	
	//returns the peaks overlapping a region
	private ArrayList<Point> getOverlappingPeaks(Region r){
		ArrayList<Point> overlapping = new ArrayList<Point>();
		for(Point p : peaks){
			if(r.contains(p)){
				overlapping.add(p);
			}
		}return(overlapping);
	}
	//return an indicator array for covered/uncovered by good motif
	private boolean[] motifCoverage(Region r){
		boolean [] covered = new boolean [r.getWidth()];
		for(int i=0; i<r.getWidth(); i++){covered[i]=false;}
		WeightMatrixScoreProfile profiler = scorer.execute(r);
		for(int z=0; z<r.getWidth(); z++){
			double currScore= profiler.getMaxScore(z);
			if(currScore>=motifThres){
				for(int j=0; j<motif.length() && z+j<r.getWidth(); j++){
					covered[z+j]=true;
				}
			}
		}
		return(covered);
	}
	//return an indicator array for covered/uncovered by good motif
	private boolean[] motifCoverage(String seq){
		boolean [] covered = new boolean [seq.length()];
		for(int i=0; i<seq.length(); i++){covered[i]=false;}
		WeightMatrixScoreProfile profiler = scorer.execute(seq);
		for(int z=0; z<seq.length(); z++){
			double currScore= profiler.getMaxScore(z);
			if(currScore>=motifThres){
				for(int j=0; j<motif.length() && z+j<seq.length(); j++){
					covered[z+j]=true;
				}
			}
		}
		return(covered);
	}
	
	public void screenFastaFile(String inFile){
		FASTALoader loader = new FASTALoader();
		File f = new File(inFile);
		Iterator<Pair<String, String>> it = loader.execute(f);
		while(it.hasNext()){
			Pair<String, String> p = it.next();
			String name = p.car();
			String sequence = p.cdr();
			char [] seq = sequence.toCharArray();
			boolean [] covered = motifCoverage(sequence);
			
			StringBuilder outseq = new StringBuilder();
			for(int j=0; j<seq.length; j++){
				if(!covered[j]){
					outseq.append(seq[j]);
				}else{
					outseq.append('N');
				}
			}
			System.out.println(">"+name+"\n"+outseq.toString());
		}
				
	}
	
	public void scoreFastaFile(String inFile){
		FASTALoader loader = new FASTALoader();
		File f = new File(inFile);
		int numHits=0, seqsWithHits=0, totalSeqs=0;
		Iterator<Pair<String, String>> it = loader.execute(f);
		while(it.hasNext()){
			Pair<String, String> p = it.next();
			String name = p.car();
			String seq = p.cdr();
			WeightMatrixScoreProfile profiler = scorer.execute(seq);
			boolean goodMotif=false; 
			for(int z=0; z<seq.length()-motif.length()+1; z++){
				double currScore= profiler.getMaxScore(z);
				if(currScore>=motifThres){
					char maxStrand = profiler.getMaxStrand(z);
					numHits++;
					goodMotif=true;
					String subseq = seq.substring(z, z+motif.length());
					String revseq=SequenceUtils.reverseComplement(subseq);
					
					if(maxStrand == '+')
						System.out.println(name+"\t"+z+":+"+"\t"+currScore+"\t"+subseq+"\t"+revseq);
					else
						System.out.println(name+"\t"+z+":-"+"\t"+currScore+"\t"+revseq+"\t"+subseq);
				}
			}
			if(goodMotif){
				seqsWithHits++;
			}
			totalSeqs++;
		}
		double perc = (double)seqsWithHits/(double)totalSeqs;
		System.out.println(motif.name+" hits: "+numHits+" hits in "+seqsWithHits+" peaks from "+totalSeqs+" total peaks ("+perc+").");		
	}
	//Same as above except returning the probability of occupancy for each sequence
	public void pOccFastaFile(String inFile){
		double conc = Math.exp(-1*motif.getMaxScore());
		FASTALoader loader = new FASTALoader();
		File f = new File(inFile);
		int numHits=0, seqsWithHits=0, totalSeqs=0;
		
		Iterator<Pair<String, String>> it = loader.execute(f);
		while(it.hasNext()){
			Pair<String, String> p = it.next();
			String name = p.car();
			String seq = p.cdr();
			WeightMatrixScoreProfile profiler = scorer.execute(seq);
			
			double pOcc = profiler2ProbOcc(profiler, conc);
			System.out.println(name+"\t"+pOcc);
		}				
	}
	
	public void printPeakInfo() {
		String [] refs = new String[]{"refGene"};
		ArrayList<Gene> closestGenes = findClosestGenes(refs); 
		for(int i=0; i<peaks.size(); i++){
			Point p = peaks.get(i);
			Region r = regions.get(i);
			//String seq = seqgen.execute(r);
			Gene closestGene = closestGenes.get(i);
			int dist2Gene = closestGene.getName().equals("NONE") ? maxGeneDistance : Math.abs(p.getLocation() - closestGene.getFivePrime()); 
			String[] words = inFileLines.get(i).split("\\s+");
			System.out.println(words[0]+"\t"+words[1]+"\t"+words[2]+"\t"+words[3]+"\t"+closestGene.getName()+"\t"+closestGene.getID()+"\t"+dist2Gene);
			//System.out.println(words[0]+"\t"+words[1]+"\t"+words[2]+"\t"+words[3]+"\t"+seq);
		}
		
	}
	//Arguments: a list of enriched peaks and an array of annotation source names
	private ArrayList<Gene> findClosestGenes(String [] geneAnnots){
		ArrayList<Gene> closest = new ArrayList<Gene>();
		if(geneAnnots!=null && geneAnnots.length>0){
			for(int g=0; g<geneAnnots.length; g++){
				RefGeneGenerator<Region> rgg = new RefGeneGenerator<Region>(gen, geneAnnots[g]);
                for (Point p : peaks) {
                	int distToGene = maxGeneDistance+1;
                	Gene closestGene=null;
                    Region query = p.expand(maxGeneDistance);
                    Iterator<Gene> geneIter = rgg.execute(query);
                    while (geneIter.hasNext()) {
                        Gene gene = geneIter.next();
                        int distance = Math.abs(p.getLocation() - gene.getFivePrime());
                        if (distance < distToGene) {
                            distToGene = distance;
                            closestGene = gene;
                        }   
                    }
                    if(distToGene<maxGeneDistance)
                    	closest.add(closestGene);
                    else{
                    	Gene none = new Gene(gen, "NONE", 1, 2, "NONE", "NONE", '+', "NONE");
                    	closest.add(none);
                    }
                    	
                }
			}
		}
		return(closest);
	}
	
	//Print the motif density alongside the peaks density
	private void printMotifDensity(){
		
		Iterator<NamedRegion> chroms = new ChromRegionIterator(gen);
		while (chroms.hasNext()) {
			NamedRegion currentChrom = chroms.next();
			for(int x=currentChrom.getStart(); x<=currentChrom.getEnd(); x+=motifDensityStepSize){
				int y = x+motifDensityWinSize-1; 
				if(y>=currentChrom.getEnd()){y=currentChrom.getEnd()-1;}
				Region currRegion = new Region(gen, currentChrom.getChrom(), x, y);
				
				//Get the number of motif hits in the region
				ArrayList<ScoredStrandedRegion> motifHits = getMotifHits(currRegion);
				
				//Get the number of peaks in the region
				ArrayList<Point> overlapPeaks = getOverlappingPeaks(currRegion);
				
				double motifDensity = (double)(motifHits.size())/(double)currRegion.getWidth();
				double peakDensity = (double)(overlapPeaks.size())/(double)currRegion.getWidth();
				
				System.out.println(currRegion.getLocationString()+"\t"+motifDensity+"\t"+peakDensity);
			}
		}		
	}
	
	//Convert a WeightMatrix score to a probability
	private double score2Prob(double x, double conc){
		double kd = Math.exp(-1*x);
		return(conc/(kd+conc));
	}
	//Convert a WeightMatrixProfile into a probability of occupancy 
	public double profiler2ProbOcc(WeightMatrixScoreProfile profile, double conc){
		double prod = 1;
		for(int i=0; i<profile.length(); i++){
			double currScore = profile.getMaxScore(i);
			if(!Double.isNaN(currScore) && !Double.isInfinite(currScore))
				prod*=(1-score2Prob(currScore, conc));
		}
		return(1-prod);
	}

	//Load thresholds
	public static HashMap<String,Double> loadThresholdsFromFile(String filename, double level){
		HashMap<String,Double> motifThresholds = new HashMap<String,Double>();
		if(filename == null){System.err.println("No threshold file specified");}
		else{
			int thresIndex=3;
			try{
				File bFile = new File(filename);
				if(bFile.isFile()){
					BufferedReader reader;
					reader = new BufferedReader(new FileReader(bFile));
					String firstLine = reader.readLine();
					String [] tokens = firstLine.split("[\\s*\\t\\r\\n\\f]");
					for(int i=3; i<tokens.length; i++){
						String t = tokens[i];
						if(t.startsWith("Thres")){
							double val = new Double(t.replaceAll("Thres", "")).doubleValue();
							if(val==level){
								thresIndex=i;
							}
						}
					}
					String line;
					while((line= reader.readLine())!=null){
						tokens = line.split("[\\s*\\t\\r\\n\\f]");
						String name = tokens[0];
						double v = new Double(tokens[thresIndex]);
						motifThresholds.put(name, v);
					}
					reader.close();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		return(motifThresholds);
	}
	
	//Print motif-profiler style vectors -- percent-scores at binwidth resolution
	public void printMotifProfiles(int winLen, int bins){
		BinningParameters params = new BinningParameters(winLen, bins);
		for(int x=0; x<peaks.size(); x++){
			Point a = peaks.get(x);
			double[] array = new double[params.getNumBins()];
			for(int i = 0; i < array.length; i++) { array[i] = 0; }
			
			int window = params.getWindowSize();
			int left = window/2;
			int right = window-left-1;
			
			int start = Math.max(0, a.getLocation()-left);
			int end = Math.min(a.getLocation()+right, a.getGenome().getChromLength(a.getChrom())-1);
			Region query = new Region(gen, a.getChrom(), start, end);
			boolean strand = (a instanceof StrandedPoint) ? 
					((StrandedPoint)a).getStrand() == '+' : true;
			
			String seq = seqgen.execute(query);
			WeightMatrixScoreProfile profiler = scorer.execute(seq);
			for(int i=query.getStart(); i<query.getEnd(); i+=params.getBinSize()){
				double maxScore=Double.MIN_VALUE;
				int maxPos=0;
				for(int j=i; j<i+params.getBinSize() && j<query.getEnd(); j++){
					int offset = j-query.getStart();
					
					if(profiler.getMaxScore(offset)>maxScore){
						maxScore= profiler.getMaxScore(offset); 
						maxPos=offset;					
					}
				}
				if(maxScore>=motifThres){
					int startbin, stopbin;

					startbin = params.findBin(maxPos);
					stopbin = params.findBin(maxPos+motif.length()-1);
					
					if(!strand) { 
						int tmp = (params.getNumBins()-stopbin)-1;
						stopbin = (params.getNumBins()-startbin)-1;
						startbin = tmp;
					}
					double percScore = maxScore / motif.getMaxScore();
					for(int k = startbin; k <= stopbin; k++) { 
						array[k] = Math.max(array[k],percScore);
					}
				}
			}
			//Print the vector
			System.out.print(a);
			for(int z=0; z<array.length; z++){
				System.out.print("\t"+array[z]);
			}System.out.print("\n");
		}
	}
}

