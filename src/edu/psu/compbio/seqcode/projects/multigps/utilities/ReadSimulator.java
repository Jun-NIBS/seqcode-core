package edu.psu.compbio.seqcode.projects.multigps.utilities; 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import edu.psu.compbio.seqcode.gse.datasets.general.Point;
import edu.psu.compbio.seqcode.gse.datasets.species.Genome;
import edu.psu.compbio.seqcode.gse.tools.utils.Args;
import edu.psu.compbio.seqcode.gse.utils.Pair;
import edu.psu.compbio.seqcode.projects.multigps.framework.BindingModel;
import edu.psu.compbio.seqcode.projects.multigps.framework.ReadHit;

/**
 * Simulates reads using BindingModels. <br> 
 * 
 * The two most basic files that should be loaded are: <tt>binding_model_file</tt> 
 * and <tt>sites_file</tt>. <br>
 * The <tt>binding_model_file</tt> should be in the form expected by BindingModel
 * </pre>
 * Each entry of the <tt>sites_file</tt> should have the form: <br>
 * <pre> 
 * position_of_event1	strength_of_event1
 * position_of_event2	strength_of_event2
 * ...
 * </pre> 
 * E.g. <br>
 * <pre>
 *   50 20
 *  150 20
 *  300 10
 *   </pre>
 * 
 * @author Shaun Mahony
 *
 */
public class ReadSimulator {

	public final int DEFAULT_PEAK_LOCATION = 500;
	
	private BindingModel model;
	private int numReads;
	
	private List<ReadHit> reads;
	private Genome fakeGen;
	private int[] chromLens;
	private HashMap<String, Integer> chromOffsets = new HashMap<String, Integer>();
	private List<Pair<Point, Double>> events; //Pair : Position / Strength(read count)
	private double[] forProbLand;
	private double[] revProbLand;
	private double[] forProbCumul;
	private double[] revProbCumul;
	private int rLen=32;
	private int genomeLength=-1;
	private int numNoisyReads;
	
	/**
	 * This proportion of reads will be generated entirely randomly (Poisson) <br>
	 * The higher the <tt>noiseProbability</tt>, the most likely the read is to
	 * be generated by a Poisson distribution.
	 */
	private double noiseProbability=0.5;
	
	public ReadSimulator(BindingModel m, File sFile, Genome g){
		model=m;
		reads = new ArrayList<ReadHit>();
		fakeGen = g;
		genomeLength = (int)fakeGen.getGenomeLength();
		chromLens = new int[fakeGen.getChromList().size()];
		
		int c=0; int offset=0;
		for(String chr : fakeGen.getChromList()){
			chromLens[c]=fakeGen.getChromLength(chr); 
			chromOffsets.put(chr, offset);
			offset+=chromLens[c];
			c++;
		}
				
		//Load the file
		try {
			events = new LinkedList<Pair<Point,Double>>(); 			
			BufferedReader reader = new BufferedReader(new FileReader(sFile));
	        String line;

	        numNoisyReads = 0;
	        while ((line = reader.readLine()) != null) {
	            line = line.trim();
	            String[] words = line.split("\\s+");
	            if(words.length >= 2){
	            	String chr = words[0]; chr = chr.replaceFirst("chr", "");
	            	int pos = Integer.parseInt(words[1]);
	            	double strength =100;
	            	if(words.length >= 2)
	            		strength = Double.parseDouble(words[2]);
	            	Point pt = new Point(fakeGen, chr, pos);
	            	Pair<Point,Double> p = new Pair<Point,Double>(pt, strength);
	            	events.add(p);
	            }
	        }
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		initProbabilities();
	}
		
	private void initProbabilities(){
		//Initialize the probability landscape
		forProbLand=new double[genomeLength]; revProbLand=new double[genomeLength];
		forProbCumul=new double[genomeLength]; revProbCumul=new double[genomeLength];
		for(int i=0; i<genomeLength; i++){
			forProbLand[i]=0; revProbLand[i]=0;
			forProbCumul[i]=0; revProbCumul[i]=0;
		}
		
		int modelRange = Math.max(Math.abs(model.getMin()), Math.abs(model.getMax()));
		//Impose the binding events on the probability landscape
		for(Pair<Point,Double> e : events){
			Point pt =  e.car();		// read position
			String chr = pt.getChrom();
			int pos = pt.getLocation(); 
			double strength = e.cdr();		// event strength
			int chromOff = chromOffsets.get(chr); 
			int winStart = chromOff+pos-modelRange; 
			int winEnd = chromOff+pos+modelRange;	

			for(int i=winStart; i<winEnd; i++){
				int forDist  = i-(chromOff+pos);
				int revDist  = (chromOff+pos)-i;
				forProbLand[i]+=strength*model.probability(forDist);
				revProbLand[i]+=strength*model.probability(revDist);
			}
		}
		
		//Set the cumulative scores
		double fTotal=0, rTotal=0;
		for(int i=0; i<genomeLength; i++){
			fTotal+=forProbLand[i];
			rTotal+=revProbLand[i];
			forProbCumul[i]=fTotal;
			revProbCumul[i]=rTotal;
		}
		//Normalize
		for(int i=0; i<genomeLength; i++){
			forProbLand[i]=forProbLand[i]/fTotal;
			revProbLand[i]=revProbLand[i]/rTotal;
			forProbCumul[i]=forProbCumul[i]/fTotal;
			revProbCumul[i]=revProbCumul[i]/rTotal;
		}
	}
	
	//Accessors
	public void setNoiseProb(double p){
		if(p < 0.0 || p > 1.0) { throw new IllegalArgumentException("p has to be a number between 0.0 and 1.0"); }
		noiseProbability = p;
	}
	
	//Simulate reads
	public List<ReadHit> simulateBothStrands(){
		simulate(numReads/2, true);
		simulate(numReads-numReads/2, false);
		return(reads);
	}
	public List<ReadHit> simulateBothStrands(int numReads){
		simulate(numReads/2, true);
		simulate(numReads-numReads/2, false);
		return(reads);
	}
	public List<ReadHit> simulate(int numReads)	{
		return(simulate(numReads, true));
	}
	public List<ReadHit> simulate(int numReads, boolean forwardStrand){
		
		Random generator = new Random();
		Random noiseGenerator = new Random();
		
		ReadHit r=null;
		for(int i=0; i<numReads; i++){
			double rand = generator.nextDouble();
			double noiserand = noiseGenerator.nextDouble();
			
			if(noiserand < noiseProbability){
				// The read comes from the background (noise) model
				int pos = (int)(rand*(genomeLength));
				
				//Translate from pos to chromosome name and start
				int c=0; int offset=0;
				String chr = fakeGen.getChromList().get(0);
				while(pos>(offset+chromLens[c]) && c<fakeGen.getChromList().size()){
					offset +=chromLens[c];
					c++;
					chr = fakeGen.getChromList().get(c);
				}
				int start = pos-offset;
				
				//Add the ReadHit
				if (forwardStrand)
					r = new ReadHit(chr, start, start+rLen-1, '+');
				else
					r = new ReadHit(chr, Math.max(1, start-rLen+1), start, '-');
				
				numNoisyReads++;
			}else{
				// The read is being generated by the event
				int pos=0;
				//Find the probability interval
				if(forwardStrand){
					for(int j=0; j<=genomeLength; j++){
						if(forProbCumul[j] > rand){
							pos=j;
							break;
						}
					}

					//Translate from pos to chromosome name and start
					int c=0; int offset=0;
					String chr = fakeGen.getChromList().get(0);
					while(pos>(offset+chromLens[c]) && c<fakeGen.getChromList().size()){
						offset +=chromLens[c];
						c++;
						chr = fakeGen.getChromList().get(c);
					}
					int fivePrimeEnd = pos-offset;
					
					//Make the ReadHit
					r = new ReadHit(chr, fivePrimeEnd, fivePrimeEnd+rLen-1, '+');			
				}else{
					for(int j=genomeLength-1; j>=0; j--){
						if(revProbCumul[j] < rand){
							pos=j+1;
							break;
						}
					}
					//Translate from pos to chromosome name and start
					int c=0; int offset=0;
					String chr = fakeGen.getChromList().get(0);
					while(pos>(offset+chromLens[c]) && c<fakeGen.getChromList().size()){
						offset +=chromLens[c];
						c++;
						chr = fakeGen.getChromList().get(c);
					}
					int fivePrimeEnd = pos-offset;
					
					//Make the ReadHit
					r = new ReadHit(chr, Math.max(1, fivePrimeEnd-rLen+1), fivePrimeEnd, '-');
				}
			}
			reads.add(r);
		}
		return(reads);
	}
	// clean up
	public void restart(){
		reads.clear();
	}
	
	/**
	 * Write an output file of read positions in BED format
	 */
	public void printToBED(String filename){
		try {
			FileWriter fout = new FileWriter(filename);
			for(ReadHit r : reads){
				fout.write(r.getChrom()+"\t"+r.getStart()+"\t"+r.getEnd()+"\tU\t0\t"+r.getStrand()+"\n");
			}
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Write an output file of read positions in IDX format
	 */
	public void printToIDX(String filename){
		try {
			FileWriter fout = new FileWriter(filename);
			fout.write("chrom\tindex\tforward\treverse\tvalue\n");
			for(String chr : fakeGen.getChromList()){
				int chrLen = fakeGen.getChromLength(chr);
				int[] posCounts = new int[chrLen];
				int[] negCounts = new int[chrLen];
				for(int x=0; x<chrLen; x++){ posCounts[x]=0; negCounts[x]=0;}
				
				for(ReadHit r : reads){
					if(r.getChrom().equals(chr)){ 
						if(r.getStrand()=='+')
							posCounts[r.getStart()]++;
						else
							negCounts[r.getEnd()]++;
					}
				}
				
				for(int x=0; x<chrLen; x++){
					if(posCounts[x]>0 || negCounts[x]>0)
						fout.write(chr+"\t"+x+"\t"+posCounts[x]+"\t"+negCounts[x]+"\t"+(posCounts[x]+negCounts[x])+"\n");
				}
			}
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<Pair<Point, Double>> getEvents() { return events; }
	
	
	/**
	 * The arguments should have the form:										<br>
	 * --model <tt>binding_model_file</tt>										<br>
     * --sites <tt>binding_events_file</tt>										<br>
	 * --out <tt>output_path</tt>												<br>
     * [--randSeed <tt>read_random_seed_value</tt>]								<br>
	 * [--noiseRandSeed <tt>noise_random_seed_value</tt>]						<br>
	 * [--noise <tt>noise_probability</tt>]									    <br>
	 * [--matlab]																<br>
	 * [--eland]																<br>
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(Args.parseArgs(args).contains("model") && Args.parseArgs(args).contains("sites") && Args.parseArgs(args).contains("geninfo")){
			Genome gen = null;
			String bmfile      = Args.parseString(args, "model", null);
			String sitefile    = Args.parseString(args, "sites", null);
			double noiseProb   = Args.parseDouble(args, "noise", 0.5);
			int numReads       = Args.parseInteger(args, "numreads", 1000);
			String outPath     = Args.parseString(args, "out", "out");
			boolean printIDX = Args.parseFlags(args).contains("idx");
			
			if(Args.parseArgs(args).contains("geninfo")){
				//Make fake genome... chr lengths provided
				String fName = Args.parseString(args, "geninfo", null);
				gen = new Genome("Genome", new File(fName), true);
			}
			
			File mFile = new File(bmfile);
			if(!mFile.isFile()){System.err.println("Invalid file name");System.exit(1);}
			
			File sFile = new File(sitefile);
			if(!sFile.isFile()){System.err.println("Invalid file name");System.exit(1);}
	        
			//File loaded, make a BindingModel
	        BindingModel bm = new BindingModel(mFile);
	        
	        //Initialize the ReadSimulator
	        ReadSimulator sim = new ReadSimulator(bm, sFile, gen);
	        sim.setNoiseProb(noiseProb);
	        
	        //Run the simulator
	        System.err.println("Simulating "+numReads+" reads.");
	        sim.simulateBothStrands(numReads);
	        System.err.println("DONE");
	        
	        if(printIDX)
	        	sim.printToIDX(outPath + "_reads" + ".idx");
	        else
	        	sim.printToBED(outPath + "_reads" + ".bed");
		}else{
			System.err.println("Usage: ReadSimulator \n--model bindingmodel \n--sites file \n--numreads numReads"+
								"\n--out outfile\n--noise noiseProb\n--geninfo <genome info file>" +
								"\n--idx [flag to print output in idx format]\n");
		}
	}
}
