package edu.psu.compbio.seqcode.projects.naomi.shapealign;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.psu.compbio.seqcode.genome.GenomeConfig;
import edu.psu.compbio.seqcode.genome.location.Point;
import edu.psu.compbio.seqcode.genome.location.Region;
import edu.psu.compbio.seqcode.genome.location.StrandedPoint;
import edu.psu.compbio.seqcode.genome.location.StrandedRegion;
import edu.psu.compbio.seqcode.gse.gsebricks.verbs.sequence.SequenceGenerator;
import edu.psu.compbio.seqcode.gse.tools.utils.Args;
import edu.psu.compbio.seqcode.gse.utils.ArgParser;
import edu.psu.compbio.seqcode.gse.utils.io.RegionFileUtilities;
import edu.psu.compbio.seqcode.gse.utils.io.parsing.PWMParser;

/**
 * MEME algorithm to find a motif given a prior, meant to be integrated with shapeEM. 
 * @author naomi yamada
 */

public class MotifEM {
	protected GenomeConfig gconfig;	
	protected List<StrandedPoint> strandedPoints;
	protected int window;
	protected List<String> sequences;
	
	int q = 10 ; // number of iterations
	int N; // number of sequences
	int W = 3; // length of the motif
	int L; // length of each sequences
	double[][][] z; // estimate after q iterations of EM of the probabilities that the site begins at position j in sequence i given the model and the data.  
	double[][][] p; // estimate after q iterations of EM of the probabilities of letter l appearing in position k of the motif
	double[][] po; // estimate after q iterations of EM of the base frequencies of outside of motif
	double[][][] Y; // indicator variable that equals if the site starts at pos j in sequence i, and 0 otherwise
	
	public MotifEM(GenomeConfig gcon, List<StrandedPoint> spoints, int win, double[][] pwm){
		gconfig = gcon;	
		strandedPoints = spoints;
		window = win;	
		N = strandedPoints.size();
		L = window+1;
		
		z = new double[N][L-W+1][q];
		p = new double[4][W][q];
		po = new double [4][q];	
		Y = new double[4][L][N];
		
		for (int i = 0; i <pwm.length; i++)
			for (int j = 0; j <pwm[0].length; j++)
				System.out.print(pwm[i][j]);
			System.out.println();
		
		
		
		// initialize all the matrix
		for (int i = 0; i <N ; i++)
			for (int j = 0; j <= L-W; j ++)
				for (int itr = 0 ; itr <q ; itr++)
					z[i][j][itr] = 0;		
		for (int base = 0 ; base < 4; base++){
			for (int w = 0; w <W ; w++){
				p[base][w][0] = pwm[base][w];
				for (int itr = 1 ; itr <q ; itr++)
					p[base][w][itr] = 0;
			}
		}
		for (int base = 0; base <4; base++)
			for (int itr = 0; itr <q ; itr++)
				po[base][itr] = 0;
		for (int base = 0; base <4; base++)
			for (int l = 0; l <L; l++){}
		
	}
	
	public void loadSequencesFromRegions(){
		// converting stranded points to stranded regions
		List<StrandedRegion> regionList = new ArrayList<StrandedRegion>();
		for(Point p: strandedPoints){		
			int start = Math.max(1, p.getLocation() - window/2 );
			int end = Math.min(p.getLocation() + window/2, p.getGenome().getChromLength(p.getChrom()));				
			StrandedRegion strandedReg = new StrandedRegion(p.getGenome(), p.getChrom(), start, end, p.getStrand());					
			regionList.add(strandedReg);
		}
		
		// get sequences from regions
		SequenceGenerator<Region> seqgen = new SequenceGenerator<Region>(gconfig.getGenome());		
		for (StrandedRegion reg : regionList){
			String seq = seqgen.execute(reg);
			sequences.add(seq.toUpperCase());
		}	
	}
	
	public void loadTestSequence(){
		String seq1 = "TATTTG";
		String seq2 = "TTATTC";
		String seq3 = "GATTT";
		sequences.add(seq1.toUpperCase());
		sequences.add(seq2.toUpperCase());
		sequences.add(seq3.toUpperCase());
		
		System.out.println("printing test sequences");
		for (String seq : sequences){
			System.out.println(seq);
		}
		
		System.out.println("checking how pwm 2d matrix is arranged");
		for (int base = 0 ; base < p.length; base++ ){
			for (int w = 0; w <W ; w++)
				System.out.print(p[base][w][0]+"\t");
			System.out.println();
		}	
	}
	
	public void runMotifEM(){		
		int round = 0;
		boolean converged = false;
 //       while (!converged || round <q){
        	
        	updatePositions(round);
        	
        	updateMotifFrequencies(round);
        	
        	updateBackgroundBaseFrequencies(round);
        	
        	round ++;
 //       }	
        	
	}
	
	public void updatePositions(int round){	// make sure this is correct
		double[] z_n = new double[L-W+1];
		for (int j = 0 ; j <= L-W; j ++)
			z_n[j] = 1;
		double z_d = 0;
		int n = 0;
		for (String seq : sequences){ // for each sequence
			for (int j = 0; j <= L-W; j++){
				for(int w = 0; w < W; w++)
					z_n[j] *= p[getBaseIndex(seq,j+w)][w][round];
				z_d += z_n[j];
			}
			for (int j = 0 ; j <= L-W; j++)
				z[n][j][round] = z_n[j]/z_d;
			n++;
		}
		
		// printing for test
		System.out.println("printing z");
		for (int i = 0; i <z.length; i++){
			for (int j = 0; j < z[0].length; j++){
				System.out.print(z[i][j][0]);
			}
			System.out.println();
		}		
	}
	
	public void updateMotifFrequencies(int round){
		double[][] epsilon = new double [4][W];
		for (int base = 0; base <4; base++)
			for (int w = 0 ; w <W ; w++)
				epsilon[base][w] = 0;
		int n = 0;
		for (String seq : sequences){ // for each sequence
			for (int j = 0; j <= L-W; j++){
				for(int w = 0; w < W; w++){
					epsilon[getBaseIndex(seq,j+w)][w] += z[n][j][round]; // make sure that this is correct
				}
			}			
			n++;
		}
		for (int base = 0; base < 4; base++){
			for (int w = 0; w <W ; w++)
				p[base][w][round] = epsilon[base][w]/sequences.size();
		}
		
		//printing for test
		System.out.println("printing p");
		for (int i = 0; i <p.length; i++){
			for (int j = 0; j < p[0].length; j++){
				System.out.print(p[i][j][0]);
			}
			System.out.println();
		}
		
	}
	
	public void updateBackgroundBaseFrequencies(int round){
		double[] epsilon_o = new double[4];
		for (int base = 0; base <4; base++)
			epsilon_o[base] = 0;
		int n = 0;
		for (String seq : sequences){
			double[] totalBaseCounts = new double[4];
			for (int base = 0; base <4 ; base++)
				totalBaseCounts[base] = 0;
			for (int l = 0; l < L ; l++)
				totalBaseCounts[getBaseIndex(seq,l)] ++;  //total base counts for one sequence
			for (int j = 0; j <= L-W ; j ++){ // subtract bases for the window size W each time for j times
				double[] seqBaseCounts = new double[4];
				System.arraycopy(totalBaseCounts, 0, seqBaseCounts, 0, totalBaseCounts.length);
				for (int w = 0; w < W; w++){
					seqBaseCounts[getBaseIndex(seq,w+j)]--;
				}
				for (int base = 0; base <4; base++){
					epsilon_o[base] += seqBaseCounts[base]*z[n][j][round];
				}
			}
			n++;			
		}
		for (int base = 0; base <4 ; base++)
			po[base][q] = epsilon_o[base]/(sequences.size()*(L-W)); // po total should add up to 1 !!
		
		//printing for test
		System.out.println("printing po");
		for (int i = 0; i <po.length; i++){
			System.out.println(po[i]);
		}
	}
	
	public int getBaseIndex(String seq, int j){
		int basePos = -1;
		if (seq.charAt(j) == 'A'){
			basePos = 0;
		}else if (seq.charAt(j) == 'C'){
			basePos = 1;
		}else if (seq.charAt(j) == 'G'){
			basePos = 2;
		}else if (seq.charAt(j) == 'T'){
			basePos = 3;
		}else{
			System.err.println("only include ACGT bases");
			System.exit(0);
		}
		return basePos;		
	}
	

	// method test
	public static void main(String[] args) throws IOException, ParseException{
		ArgParser ap = new ArgParser(args);		
        if((!ap.hasKey("peaks") && !ap.hasKey("regions")) ) { 
            System.err.println("Usage:\n " +
                               "MotifEM " +
                               "--species <organism;genome> OR\n" +
                               "--geninfo <genome info> AND --seq <path to seqs>\n" +
                               "--peaks <file containing coordinates of peaks> \n" +
                               "--win <window of sequence to take around peaks> OR\n" +
                               "--regions \n" +
                               "");
            System.exit(0);
        }
        
		GenomeConfig gconf = new GenomeConfig(args);
		
		// parsing command line arguments	
		int win = Args.parseInteger(args, "win", 100);
		List<StrandedPoint> strandedPoints = RegionFileUtilities.loadStrandedPointsFromMotifFile(gconf.getGenome(), ap.getKeyValue("peaks"), win);
		
	    String aLine = " a  0.167 0.500 0.166"; 
	    String cLine = " c  0.167 0.167 0.167"; 
	    String gLine = " g  0.166 0.167 0.167";
	    String tLine = " t  0.500 0.166 0.500"; 
		
	    DenseDoubleMatrix2D pwm  = PWMParser.parsePWM(3, aLine, cLine, gLine, tLine);
	    System.out.println(pwm.toString());

	    
		MotifEM motifEM = new MotifEM(gconf, strandedPoints, win, pwm.toArray());
		motifEM.loadTestSequence();
		motifEM.runMotifEM();
	}

}