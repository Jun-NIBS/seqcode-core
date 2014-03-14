package edu.psu.compbio.seqcode.projects.akshay.bayesments.bayesnet;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

import edu.psu.compbio.seqcode.gse.utils.probability.NormalDistribution;
import edu.psu.compbio.seqcode.projects.akshay.bayesments.experiments.ExperimentCondition;
import edu.psu.compbio.seqcode.projects.akshay.bayesments.experiments.ExperimentManager;
import edu.psu.compbio.seqcode.projects.akshay.bayesments.features.GenomicLocations;
import edu.psu.compbio.seqcode.projects.akshay.bayesments.features.Sequences;
import edu.psu.compbio.seqcode.projects.akshay.bayesments.framework.Config;
import edu.psu.compbio.seqcode.projects.akshay.bayesments.utils.BayesmentsSandbox;
import edu.psu.compbio.seqcode.projects.akshay.bayesments.utils.Cubic;
import edu.psu.compbio.seqcode.projects.akshay.bayesments.features.Simulate;

/**
 * EMtrain class the initializes and trains the parameters of the Bayesian network using the EM framework
 * @author akshaykakumanu
 *
 */

public class EMtrain {

	protected Config config;
	protected boolean Simulate;
	
	//The following 4 lines are variables needed when the model is in seq state. The should be initiated using a setter method(setSeqMode)
	protected double[][] Xs;
	protected double[][] MUs;
	protected double[][] SIGMAs;
	
	//boolean variable, turns on when the model is in seqState. Once, the model is in seqState it can't go back to non-seq state mode
	// Initially it is false. That is the EM always stars in non-seq state
	protected boolean seqState;
	
	// True if only chromatin features are used. i.e sequence features are never used
	protected boolean onlyChrom;
	
	// 2-d array of chromatin counts, with rows as locations and columns as conditions
	protected float[][] Xc;
	//2-d array of factor countts, with rows as locations and columns as conditions
	protected float[][] Xf;
	//2-d array of means, with rows as chromatin states and columns as conditions
	protected double[][] MUc;
	//2-d array of means, with rows as factor stats and columns as conditions
	protected double[][] MUf;
	//2-d array of variance, with rows as chromatin states and columns as conditions
	protected double[][] SIGMAc;
	//2-d array of variance, with rows as factor stats and columns as conditions
	protected double[][] SIGMAf;
	//2-d array of transition probabilities, with rows as chromatin states and colums as factor states
	protected double[][] Bjk;
	//Expectation of unobserved variables for the E step in EM algorithm
	protected double[][][] Qijk;
	//no of chromatin states
	protected int numChromStates;
	//number of factor states
	protected int numFacBindingStates;
	protected int N; // number of training examples
	protected int C; // number of chromatin conditions
	protected int F; // number of factor conditions (alomost always 1)
	protected int M; // number of motifs to be included as features (this number is initialized when the models enters the seq state mode (setSeqMode))
	// flag to turn on plotting of the parameters as the function of iterations step in EM
	
	//The 3-d arrays store the values of seq parameters over all iterations of EM. They are globally defined unlike other non-seq parameters 
	//because of some technical details
	//These are initializes when the model enters the seq Mode (setSeqMode)
	protected double[][][] trainMUs;
	protected double[][][] trainSIGMAs;
	
	protected double[][][] trainMUc; 
	protected double[][][] trainMUf; 
	protected double[][][] trainSIGMAc;
	protected double[][][] trainSIGMAf;
	protected double[][] trainPIj; 
	protected double[][][] trainBjk;
	
	protected double[] capSIGMAc;
	protected double[] capSIGMAf;
	protected double[] capSIGMAs;
	
	protected double[] WCnorm;
	protected double[] WSnorm;
	
	protected String[] condition_names;
	
	protected double lambda;
	
	protected boolean regularize;
	
	protected boolean printIntialVals;
	
	//Current round of EM. This remembers, if the model was run previously in non-seq mode and hence globally defined
	protected int itr_no=0;
	//Total number of EM iters, both, in seq and non-seq mode (Should be given when the class object is initialized)
	protected int total_itrs=0;
	
	/**
	 * When initializing this class object, it is always in seq off mode
	 * @param config
	 * @param trainingData
	 * @param manager
	 */
	public EMtrain(Config config, GenomicLocations trainingData, ExperimentManager manager, int nChromStates, int nFacStates, boolean Regularize, boolean printVals) {
		this.config = config;
		//this.trainingData = trainingData;
		this.regularize = Regularize;
		if(regularize){
			this.lambda = config.getLambda();
		}
		
		N = trainingData.getNumTrainingExamples();
		C= trainingData.getNumChromatinCons();
		F = trainingData.getNumFacCons();
		this.numChromStates = nChromStates;
		this.numFacBindingStates = nFacStates;
		
		//Initializing and loading X's
		this.Xc = new float[N][C];
		this.Xf = new float[N][F];
		this.Xc = trainingData.getChromatinCounts();
		this.Xf = trainingData.getFactorCounts();
		
		this.condition_names = new String[C];
		int c =0;
		for(ExperimentCondition ec :manager.getChromatinConditionList()){
			this.condition_names[c] = ec.getName();
			c++;
		}
		
		this.printIntialVals = printVals;
		//Initializing the model
		initializeEM(this.printIntialVals);
		this.total_itrs = config.getNumItrs();
		this.onlyChrom = config.runOnlyChrom();
		this.seqState = false;
		this.Simulate = false;
		
	}
	
	public EMtrain(Config config, int nChromStates, int nFacStates, boolean Regularize, boolean printVals) {
		this.config = config;
		//this.trainingData = null;
		this.regularize = Regularize;
		if(regularize){
			this.lambda = config.getLambda();
		}
		Simulate sim = new Simulate();
		sim.simulate();
		this.C = sim.getNumChromCondition();
		this.N = sim.getNumTrainingEgs();
		this.F =1;
		this.numChromStates = nChromStates;
		this.numFacBindingStates = nFacStates;
		this.onlyChrom = true;
		this.seqState = false;
		this.total_itrs = this.config.getNumItrs();
		
		//Initializing and loading X's
		this.Xc = new float[N][C];
		this.Xf = new float[N][F];
		
		this.Xc = sim.getSimXc();
		this.Xf = sim.getSimXf();
		
		this.condition_names = new String[C];
		for(int c=0; c<C; c++){
			this.condition_names[c] = "Sim-"+Integer.toString(c);
		}
		this.printIntialVals = printVals;
		this.initializeEM(this.printIntialVals);
		
		this.total_itrs = config.getNumItrs();
		this.onlyChrom = true; // Simulates always does not include seq features
		this.seqState = false;
		this.Simulate = true;
		
		
	}
	
	/**
	 * Method that initializes all the parameters for the Bayesian network
	 * @param manager
	 */
	private void initializeEM(boolean printVals){
		
		//Initializing mu's
		MUc = new double[numChromStates][C];
		MUf = new double[numFacBindingStates][F];
		
		if(regularize){
			WCnorm = new double[C];
			// Setting all initial weights to 1
			for(int c=0; c<C; c++){
				WCnorm[c] = 0.0;
			}
		}
		
		this.capSIGMAc = new double[C];
		this.capSIGMAf = new double[F];
		
		//Initialization from emperical means
		
		for(int c=0; c< C; c++){
			float[] observedValues = new float[N];
			for(int i=0; i<N; i++){
				observedValues[i] = Xc[i][c];
			}
			double[] means = this.getEmpMeanValues(numChromStates, observedValues);
			for(int j=0; j<numChromStates; j++){
				MUc[j][c] = means[j];
			}
		}
		
		for(int f=0; f< F; f++){
			float[] observedValues = new float[N];
			for(int i=0; i<N; i++){
				observedValues[i] = Xf[i][f];
			}
			double[] means = this.getEmpMeanValues(numFacBindingStates, observedValues);
			for(int k=0; k<numFacBindingStates; k++){
				MUf[k][f] = means[k];
			}
		}
		
		//Printing the initial Mu's
		if(this.printIntialVals){
			System.out.println("----------------------------------Intial MUc and MUf values----------------------------------------");
			BayesmentsSandbox.printArray(MUc, "MUc", "MUc", this.condition_names);
			BayesmentsSandbox.printArray(MUf, "MUf", "MUf", this.condition_names);
			System.out.println("---------------------------------------------------------------------------------------------------");
		}
		//Initializing sigma's
		SIGMAc = new double[numChromStates][C];
		SIGMAf = new double[numFacBindingStates][F];
		
		// Initializing from emperical data... (Max-Min) 
		for(int c=0; c< C; c++){
			double[] observedValues = new double[N];
			for(int i=0; i<N; i++){
				observedValues[i] = Xc[i][c];
			}
			double min = observedValues[this.getPercentileIndex(15.0, observedValues)];
			double max = observedValues[this.getPercentileIndex(85, observedValues)];
			for(int j=0; j<numChromStates; j++){
				SIGMAc[j][c] = (max-min)/(this.numChromStates+config.getBufferSigmaVal());
				this.capSIGMAc[c] = (max-min)/(this.numChromStates+config.getBufferSigmaVal());
			}
		}
		
		for(int f=0; f< F; f++){
			double[] observedValues = new double[N];
			for(int i=0; i<N; i++){
				observedValues[i] = Xf[i][f];
			}
			double min = observedValues[this.getPercentileIndex(15.0, observedValues)];
			double max = observedValues[this.getPercentileIndex(85, observedValues)];
			for(int k=0; k<numFacBindingStates; k++){
				SIGMAf[k][f] = (max-min)/this.numFacBindingStates;
				this.capSIGMAf[f] = (max-min)/this.numFacBindingStates;
			}
		}
		
		//Printing the initial SIGMA's
		//if(this.printIntialVals){
		//	BayesmentsSandbox.printArray(SIGMAc, "SIGMAc", "SIGMAc", this.condition_names);
		//	BayesmentsSandbox.printArray(SIGMAf, "SIGMAf", "SIGMAf", this.condition_names);
		//}
		// Initializing Bjk .. Using random initialization 
		Bjk = new double[numChromStates][numFacBindingStates];
		for(int i=0; i<numChromStates; i++){
			Bjk[i] = this.getRandomList(numFacBindingStates, true);
		}
		
		//printing the initial Bjk's
		//if(this.printIntialVals){
		//	BayesmentsSandbox.printArray(Bjk, "chrom_state", "factor_State", this.condition_names);
		//}
		// Initializing PIj ... Using Uniform initialization
		//PIj = new double[numChromStates];
		//PIj = this.getUniformList(numChromStates);
		
		//Printing the initial PIj's
		//BayesmentsSandbox.printArray(PIj, "chrom_state");
		
		//Initializing the dimensions of Qijk's
		Qijk = new double[N][numChromStates][numFacBindingStates];
		
	}
	
	/**
	 * This is internally called by seqSeqMode setter when the class enters seq mode
	 * The method initializes the parameters for the seq features of the bayesian network
	 */
	private void initializeSeqParams(ExperimentManager manager){
		
		this.MUs = new double[numChromStates][M];
		this.SIGMAs = new double[numChromStates][M];
		
		for(int m=0; m< M; m++){
			float[] observedScores = new float[N];
			for(int i=0; i<N; i++){
				observedScores[i] = (float) Xs[i][m];
			}
			double[] means = this.getEmpMeanValues(numChromStates,  observedScores);
			for(int j=0; j<numChromStates; j++){
				MUs[j][m] = means[j];
			}
		}
		
		//printing params
		BayesmentsSandbox.printArray(MUs, "MUs", "MUs", this.condition_names);
		
		for(int m=0; m< M; m++){
			double[] observedValues = new double[N];
			for(int i=0; i<N; i++){
				observedValues[i] = Xs[i][m];
			}
			double min = observedValues[this.getPercentileIndex(15.0, observedValues)];
			double max = observedValues[this.getPercentileIndex(85.0, observedValues)];
			
			for(int j=0; j<numChromStates; j++){
				SIGMAs[j][m] = (max-min)/(this.numChromStates+config.getBufferSigmaVal());
				this.capSIGMAs[m] = (max-min)/(this.numFacBindingStates+config.getBufferSigmaVal());
			}
		}
		BayesmentsSandbox.printArray(SIGMAs, "SIGMAs", "SIGMAs", this.condition_names);
		
	}
	
	
	/**
	 * Generates n random numbers that sum up to 1 if the boolean prob is TRUE 
	 * @param n
	 * @param prob
	 * @return
	 */
	private double[] getRandomList(int n, boolean prob){
		double[] ret = new double[n];
		double sum=0.0;
		Random ran = new Random();
		for(int i=0; i<n; i++){
			ret[i] = ran.nextDouble()+(double) ran.nextInt(90);
			sum = sum + ret[i];
		}
		if(prob){
			for(int i=0; i<n; i++){
				ret[i] = ret[i]/sum;
			}
			return ret;
		}else{
			return ret;
		}
	}
	
	/**
	 * Generates n values that fall in mu+-0.2*sigma of the the observedValues
	 * @param n
	 * @param observedValues
	 * @return
	 */
	private double[] getEmpMeanValues(int n, float[] observedValues){
		double[] ret =  new double[n];
		double mean = 0.0;
		double std=0.0;
		for(int i=0; i< observedValues.length; i++){
			mean = mean + observedValues[i];
		}
		mean = mean/(double) observedValues.length;
		
		for(int i=0; i<observedValues.length; i++){
			std = std + Math.pow(observedValues[i]-mean, 2.0);
		}
		std = std/(double) observedValues.length;
		
		
		std = Math.sqrt(std);
		Random rn =new Random();
		double range = 0.4*std;
		
		for(int i=0; i<n ; i++){
			double random  = rn.nextDouble()*range+mean-0.2*std;
			ret[i] = random;
		}
		return ret;
	}
	
	/**
	 * Returns n equal numbers that add upto one
	 * @param n
	 * @return
	 */
	private double[] getUniformList(int n){
		double[] ret =  new double[n];
		for(int i=0; i<n; i++){
			ret[i] = 1/(double)n;
		}
		return ret;
	}
	
	/**
	 * Returns the index of the minimum number in the given list of doubles
	 * @param list
	 * @return
	 */
	private int getMinindex(double[] list){
		double val = 100000.0;
		int ret=0;
		for(int i=0; i< list.length; i++){
			if(val > list[i]){
				ret = i;
				val = list[i];
			}
		}
		return ret;
	}
	
	/**
	 * returns the index of the maximum number in the given list of doubles
	 * @param list
	 * @return
	 */
	private int getMaxindex(double[] list){
		double val = -100000.0;
		int ret=0;
		for(int i=0; i< list.length; i++){
			if(list[i] > val){
				ret =i;
				val = list[i];
			}
		}
		return ret;
	}
	
	public int getPercentileIndex(double percentile, double[] list){
		
		Arrays.sort(list);
		int index = (int) percentile*list.length/100;
		return index;
	}	
	
	
	
	/**
	 * Runs the EM algorithm for a given number of iterations and also plots the parameters over the learning rounds if plot parameter is true
	 */
	public void runEM(int itrs, boolean plot){
		
		// Initializing the arrays to store the parameters for all training rounds
		//double[][][] trainMUc = new double[this.total_itrs+1][numChromStates][C]; //initial random params plus itrs 
		//double[][][] trainMUf = new double[this.total_itrs+1][numFacBindingStates][F];
		//double[][][] trainSIGMAc = new double[this.total_itrs+1][numChromStates][C];
		//double[][][] trainSIGMAf = new double[this.total_itrs+1][numFacBindingStates][F];
		//double[][] trainPIj = new double[this.total_itrs+1][numChromStates];
		//double[][][] trainBjk = new double[this.total_itrs+1][numChromStates][numFacBindingStates];
		
		
		for(int t=0; t<itrs; t++){ // training for the given number of iterations
			
			if(itr_no==0){      //Copy the initial set of random parameters. True on the first round of EM
				
				trainMUc = new double[this.total_itrs+1][numChromStates][C];
				trainMUf = new double[this.total_itrs+1][numFacBindingStates][F];
				trainSIGMAc = new double[this.total_itrs+1][numChromStates][C];
				trainSIGMAf = new double[this.total_itrs+1][numFacBindingStates][F];
				trainPIj = new double[this.total_itrs+1][numChromStates];
				trainBjk = new double[this.total_itrs+1][numChromStates][numFacBindingStates];
				
				trainMUc[0] = MUc;
				trainMUf[0] = MUf;
				trainSIGMAc[0] = SIGMAc;
				trainSIGMAf[0] = SIGMAf;
				//trainPIj[0] = PIj;
				trainBjk[0] = Bjk;
				itr_no++;
			}
			if(this.seqState){ //When the model is in seq state
				if(t == 0){  // First EM iteration after the model entered seq state
					for(int p=0; p< this.itr_no-1 ; p++){ // indexe's form 0 to itr_no-2 are all zeros
						for(int j=0; j<this.numChromStates; j++){
							for(int m=0; m<M; m++){
									trainMUs[p][j][m] =0.0; 
							}
						}
					}
					for(int j=0; j<this.numChromStates; j++){ // index no itr_no-1 is initial random seq parameters
						for(int m=0; m<M; m++){
							trainMUs[itr_no-1] = MUs;
							trainSIGMAs[itr_no-1] = SIGMAs;
						}
					}
				}
			}
			
			executeEStep();  //E-Step
			executeMStep(); //M-Step
			
			// Copy the updated parameters
			for(int j=0; j<numChromStates; j++){
				for(int c=0; c<C; c++){
					trainMUc[itr_no][j][c]= MUc[j][c];
				}
			}
			
			for(int k=0; k<numFacBindingStates; k++){
				for(int f=0; f<F; f++){
					trainMUf[itr_no][k][f]= MUf[k][f];
				}
			}
			
			for(int j=0; j<numChromStates; j++){
				for(int c=0; c<C; c++){
					trainSIGMAc[itr_no][j][c]= SIGMAc[j][c];
				}
			}
			
			for(int k=0; k<numFacBindingStates; k++){
				for(int f=0; f<F; f++){
					trainSIGMAf[itr_no][k][f]= SIGMAf[k][f];
				}
			}
			
			//for(int j=0; j<numChromStates; j++){
			//	trainPIj[itr_no][j] = PIj[j];
			//}
			
			for(int j=0; j< numChromStates; j++){
				for(int k=0; k<numFacBindingStates; k++){
					trainBjk[itr_no][j][k] = Bjk[j][k];
				}
			}
			
			if(this.seqState){
				for(int j=0; j<numChromStates; j++){
					for(int m=0; m<M; m++){
						trainMUs[itr_no][j][m]= MUs[j][m];
						//debug
						//System.out.println(Integer.toString(j)+"\t"+Double.toString(MUs[j][m]));
					}
				}
				for(int j=0; j<numChromStates; j++){
					for(int m=0; m<M; m++){
						trainSIGMAs[itr_no][j][m]= SIGMAs[j][m];
					}
				}
			}
			this.itr_no++; // increment the EM iteration number
		}
		
		// Plot if asked for
		if(plot){
			if(this.onlyChrom || this.seqState){
				//Plotting Pi values
				double[][] Xaxes = new double[numChromStates][this.total_itrs+1];  // plus 1 for initial random parameters
				double[][] Yaxes = new double[numChromStates][this.total_itrs+1];
				for(int j=0; j<numChromStates; j++){
					for(int itr =0; itr<this.total_itrs+1; itr++){
						Xaxes[j][itr] = itr;
						Yaxes[j][itr] = trainPIj[itr][j];
					}
				}
				EMIterPlotter piplotter = new EMIterPlotter(this.config, Xaxes, Yaxes, "PI-C");
				piplotter.plot();
				
				//Plotting Mu-c
				Xaxes = new double[numChromStates][this.total_itrs+1];
				Yaxes = new double[numChromStates][this.total_itrs+1];
				
				for(int c=0; c<C; c++){
					for(int j=0; j<numChromStates; j++){
						for(int itr=0; itr<this.total_itrs+1; itr++){
							Xaxes[j][itr] = itr;
							Yaxes[j][itr] = trainMUc[itr][j][c];
						}
					}
					EMIterPlotter MUcPlotter = new EMIterPlotter(this.config, Xaxes, Yaxes, "MU-C_"+Integer.toString(c));
					MUcPlotter.plot();
				}
				//Plotting SIGMAc
				Xaxes = new double[numChromStates][this.total_itrs+1];
				Yaxes = new double[numChromStates][this.total_itrs+1];
				for(int c=0; c<C; c++){
					for(int j=0; j<numChromStates; j++){
						for(int itr=0; itr<this.total_itrs+1; itr++){
							Xaxes[j][itr] = itr;
							Yaxes[j][itr] = trainSIGMAc[itr][j][c];
						}
					}
					EMIterPlotter SIGMACPlotter = new EMIterPlotter(this.config, Xaxes, Yaxes, "SIGMA-C_"+Integer.toString(c));
					SIGMACPlotter.plot();
				}
				
				//Plotting Muf
				Xaxes = new double[numFacBindingStates][this.total_itrs+1];
				Yaxes = new double[numFacBindingStates][this.total_itrs+1];
				for(int f=0; f<F; f++){
					for(int k=0; k<numFacBindingStates; k++){
						for(int itr=0; itr<this.total_itrs+1; itr++){
							Xaxes[k][itr] = itr;
							Yaxes[k][itr] = trainMUf[itr][k][f];
						}
					}
					EMIterPlotter MUfPlotter = new EMIterPlotter(this.config, Xaxes, Yaxes, "MF-F_"+Integer.toString(f));
					MUfPlotter.plot();
				}
				
				
				//Plotting SIGMAf
				
				Xaxes = new double[numFacBindingStates][this.total_itrs+1];
				Yaxes = new double[numFacBindingStates][this.total_itrs+1];
				
				for(int f=0; f<F; f++){
					for(int k=0; k<numFacBindingStates; k++){
						for(int itr=0; itr<this.total_itrs+1; itr++){
							Xaxes[k][itr] = itr;
							Yaxes[k][itr] = trainSIGMAf[itr][k][f];
						}
					}
					EMIterPlotter SIGMAfPlotter = new EMIterPlotter(this.config, Xaxes, Yaxes, "SIGMA-f_"+Integer.toString(f));
					SIGMAfPlotter.plot();
				}
				
				
				//Plotting Bjk
				Xaxes = new double[numFacBindingStates*numChromStates][this.total_itrs+1];
				Yaxes = new double[numFacBindingStates*numChromStates][this.total_itrs+1];
				
				int count=0;
				for(int j=0; j<numChromStates; j++){
					for(int k=0; k<numFacBindingStates; k++){
						for(int itr=0; itr<this.total_itrs+1; itr++){
							Xaxes[count][itr] = itr;
							Yaxes[count][itr] = trainBjk[itr][j][k];
						}
						count++;
					}
				}
				
				EMIterPlotter BjkPlotter = new EMIterPlotter(this.config, Xaxes, Yaxes, "Bjk");
				BjkPlotter.plot();
			}
			if(this.seqState){
				
				double[][] Xaxes = new double[numChromStates][this.total_itrs+1];  // plus 1 for initial random parameters
				double[][] Yaxes = new double[numChromStates][this.total_itrs+1];
				//Plotting Mus
				for(int m=0; m<M; m++){
					for(int j=0; j<numChromStates; j++){
						for(int itr=0; itr<this.total_itrs+1; itr++){
							Xaxes[j][itr] = itr;
							Yaxes[j][itr] = trainMUs[itr][j][m];
						}
					}
					EMIterPlotter MUsPlotter = new EMIterPlotter(this.config, Xaxes, Yaxes, "MU-s_"+Integer.toString(m));
					MUsPlotter.plot();
				}
				
				
				//Plotting SIIGMAs
				Xaxes = new double[numChromStates][this.total_itrs+1];
				Yaxes = new double[numChromStates][this.total_itrs+1];
				for(int m=0; m<M; m++){
					for(int j=0; j<numChromStates; j++){
						for(int itr=0; itr<this.total_itrs+1; itr++){
							Xaxes[j][itr] = itr;
							Yaxes[j][itr] = trainSIGMAs[itr][j][m];
						}
					}
					EMIterPlotter SIGMAsPlotter = new EMIterPlotter(this.config, Xaxes, Yaxes, "SIGMA-s_"+Integer.toString(m));
					SIGMAsPlotter.plot();
				}
				
			}
			
		}
		
		
		
	}
	
	/**
	 * Executes the E step. That is it calculates the Qijk's using the current set of parameters
	 */
	private void executeEStep(){
		
		double den[]= new double[N];
		
		//Calculate the numerator and the denominator for all the Qijk's
		for(int i=0; i<N; i++){    // over the training examples
			for(int j=0; j<numChromStates; j++){   // over the chromatin states
				for(int k=0; k< numFacBindingStates; k++){ //over factor binding states
					double chromGausssianProd=0.0;
					double facGaussianProd = 0.0;
					double seqGaussianProd = 0.0;
					for(int c=0; c<C; c++){
						NormalDistribution gaussian = new NormalDistribution(MUc[j][c],Math.pow(SIGMAc[j][c], 2.0));
						double que_pusher = (this.regularize) ?Math.pow(gaussian.calcProbability((double) Xc[i][c]), 1/(1+WCnorm[c])): gaussian.calcProbability((double) Xc[i][c])  ;
						chromGausssianProd = c==0 ? que_pusher: chromGausssianProd* que_pusher;
					}
					for(int f=0; f< F; f++){
						NormalDistribution gaussian = new NormalDistribution(MUf[k][f],Math.pow(SIGMAf[k][f], 2.0));
						facGaussianProd = (f == 0 ? gaussian.calcProbability((double) Xf[i][f]): facGaussianProd* gaussian.calcProbability((double) Xf[i][f]));
					}
					// Set the guassian products to 0 in case they are NaN
					chromGausssianProd = ( Double.isNaN(chromGausssianProd)) ? 0.0 : chromGausssianProd;
					facGaussianProd = (Double.isNaN(facGaussianProd)) ? 0.0: facGaussianProd;
					
					if(this.seqState){
						for(int m=0; m<M; m++){
							NormalDistribution gaussian = new NormalDistribution(MUs[j][m],Math.pow(SIGMAs[j][m], 2.0));
							double que_pusher = this.regularize ? Math.pow(gaussian.calcProbability((double) Xs[i][m]), 1/(1+WSnorm[m])) : gaussian.calcProbability((double) Xs[i][m]);
							seqGaussianProd = m==0 ? que_pusher: seqGaussianProd* que_pusher;
						}
						seqGaussianProd = (Double.isNaN(seqGaussianProd)) ? 0.0: seqGaussianProd;
					}
					
					if(this.seqState){
						Qijk[i][j][k] = Math.pow(chromGausssianProd, -1*config.getChromWeight())*Bjk[j][k]*facGaussianProd*Math.pow(seqGaussianProd, -1*config.getSeqWeight());
					}else{Qijk[i][j][k] = chromGausssianProd*Bjk[j][k]*facGaussianProd;}		
					den[i] = den[i]+Qijk[i][j][k];
				}
			}
			
		}
		
		//Normalize the numerator by dividing the Qijk's with the denominators
		for(int i=0; i<N; i++){
			for(int j=0; j<numChromStates; j++){
				for(int k=0; k<numFacBindingStates; k++){
					Qijk[i][j][k] = Qijk[i][j][k]/den[i];
					
					Qijk[i][j][k] = ( Double.isNaN(Qijk[i][j][k])) ? 0.0 : Qijk[i][j][k];
				}
			}
		}
		
	}
	
	/**
	 * Executes the M step. Updates all the parameters of the model
	 */
	private void executeMStep(){
		
		//-------------------------PIj update-----------------------------
		
		//Compute
		//double denPIj = 0.0;
		//for(int j=0; j<numChromStates; j++){
		//	for(int i=0; i<N; i++){
		//		for(int k=0; k<numFacBindingStates; k++){
		//			PIj[j] = k==0 && i==0 ? Qijk[i][j][k] : PIj[j]+Qijk[i][j][k];
		//			denPIj = denPIj + Qijk[i][j][k];
		//		}
		//	}
		//}
		
		//Normalize
		//for(int j=0; j<numChromStates; j++){
		//	PIj[j] = PIj[j]/denPIj;
		//}
		
		//Making sure PI-j for any state does not go to zero
		//for(int j=0; j<numChromStates; j++){
		//	if(PIj[j] < 0.01){
		//		PIj[j] = 0.01;
		//	}
		//}
		
		//Re-normalize
		//denPIj = 0.0;
		//for(int j=0; j< numChromStates; j++){
		//	denPIj = denPIj + PIj[j];
		//}
		//for(int j=0; j<numChromStates; j++){
		//	PIj[j] = PIj[j]/denPIj;
		//}
		
		//-----------------------MUc update------------------------------------
		
		//Compute
		double[][] denMUc=new double[numChromStates][C];
		for(int j=0; j<numChromStates; j++){
			for(int c=0; c<C; c++){
				for(int i=0; i<N; i++){
					for(int k=0; k<numFacBindingStates; k++){
						MUc[j][c] = k==0 && i==0 ? Qijk[i][j][k]*Xc[i][c] : MUc[j][c]+ Qijk[i][j][k]*Xc[i][c];
						denMUc[j][c] = denMUc[j][c]+Qijk[i][j][k];
					
					}
				}
			}
		}
		
		//Normalize 
		for(int j=0; j<numChromStates; j++){
			for(int c=0; c<C; c++){
				MUc[j][c] = MUc[j][c]/denMUc[j][c];
			}
		}
		
		
		//-----------------------MUf update --------------------------------------
		
		//Compute
		double[][] denMUf = new double[numFacBindingStates][F];
		for(int k=0; k<numFacBindingStates; k++){
			for(int f=0; f<F; f++){
				for(int i=0; i<N; i++){
					for(int j=0; j<numChromStates; j++){
						MUf[k][f] = j==0 && i==0? Qijk[i][j][k]*Xf[i][f]: MUf[k][f]+Qijk[i][j][k]*Xf[i][f];
						denMUf[k][f] = denMUf[k][f]+Qijk[i][j][k];
					}
				}
			}
		}
		
		//Normalize
		for(int k=0; k<numFacBindingStates; k++){
			for(int f=0; f<F; f++){
				MUf[k][f] = MUf[k][f]/denMUf[k][f];
			}
		}
		
		// -----------------------MUs update, if in seqState----------------------------
		
		//Compute
		if(this.seqState){
			double[][] denMUs = new double[numChromStates][M];
			for(int j=0; j<numChromStates; j++){
				for(int m=0; m<M; m++){
					for(int i=0; i<N; i++){
						for(int k=0; k<numFacBindingStates; k++){
							MUs[j][m] = k==0 && i==0? Qijk[i][j][k]*Xs[i][m]: MUs[j][m]+Qijk[i][j][k]*Xs[i][m];
							denMUs[j][m] = denMUs[j][m]+Qijk[i][j][k];
						}
					}
				}
			}
			
			//Normalize
			for(int j=0; j<numChromStates; j++){
				for(int m=0; m<M; m++){
					MUs[j][m] = MUs[j][m]/denMUs[j][m];
				}
			}
		}
		

		//--------------------SIGMAc update --------------------------------------
		
		//Compute
		//double[][] denSIGMAc = new double[numChromStates][C];
		//for(int j=0; j<numChromStates; j++){
		//	for(int c=0; c<C; c++){
		//		for(int i=0; i<N; i++){
		//			for(int k=0; k<numFacBindingStates; k++){
		//				SIGMAc[j][c] = k==0 && i==0? Qijk[i][j][k]*Math.pow(((double)Xc[i][c] - MUc[j][c]) , 2.0): SIGMAc[j][c]+Qijk[i][j][k]*Math.pow(((double)Xc[i][c] - MUc[j][c]) , 2.0);
		//				denSIGMAc[j][c] = denSIGMAc[j][c]+Qijk[i][j][k];
		//			}
		//		}
		//	}
		//}
		
		//Normalize and taking the square root
		//for(int j=0; j<numChromStates; j++){
		//	for(int c=0; c<C; c++){
		//		SIGMAc[j][c] = Math.sqrt(SIGMAc[j][c]/denSIGMAc[j][c]);
		//	}
		//}
		
		//if(config.capSigma()){
		//	for(int j=0; j<numChromStates; j++){
		//		for(int c=0; c<C; c++){
		//			SIGMAc[j][c] = (SIGMAc[j][c] > this.capSIGMAc[c]) ? this.capSIGMAc[c] : SIGMAc[j][c];
		//		}
		//	}
		//}
		
		//------------------SIGMAf update------------------------------------------
		
		//Compute
		//double[][] denSIGMAf = new double[numFacBindingStates][F];
		//for(int k=0; k<numFacBindingStates; k++){
		//	for(int f=0; f<F; f++){
		//		for(int i=0; i<N; i++){
		//			for(int j=0; j< numChromStates; j++){
		//				SIGMAf[k][f] = j==0 && i==0? Qijk[i][j][k]*Math.pow(((double)Xf[i][f] - MUf[k][f]) , 2.0) : SIGMAf[k][f]+Qijk[i][j][k]*Math.pow(((double)Xf[i][f] - MUf[k][f]) , 2.0);
		//				denSIGMAf[k][f] = denSIGMAf[k][f]+Qijk[i][j][k];
		//			}
		//		}
		//	}
		//}
		
		//Normalize and taking the square root
		//for(int k=0; k<numFacBindingStates; k++){
		//	for(int f=0; f<F; f++){
		//		SIGMAf[k][f] = Math.sqrt(SIGMAf[k][f]/denSIGMAf[k][f]);
		//	}
		//}
		
		//if(config.capSigma()){
		//	for(int k=0; k<numFacBindingStates; k++){
		//		for(int f=0; f<F; f++){
		//			SIGMAf[k][f] = (SIGMAf[k][f] > this.capSIGMAf[f]) ? this.capSIGMAf[f] : SIGMAf[k][f];
		//		}
		//	}
		//}
		
		
		
		//--------------------SIGMAs update, if in seqState ------------------------
		
		//Compute
		//if(this.seqState){
		//	double[][] denSIGMAs = new double[numChromStates][M];
		//	for(int j=0; j<numChromStates; j++){
		//		for(int m=0; m<M; m++){
		//			for(int i=0; i<N; i++){
		//				for(int k=0; k<numFacBindingStates; k++){
		//					SIGMAs[j][m] = k==0 && i==0? Qijk[i][j][k]*Math.pow(((double)Xs[i][m] - MUs[j][m]) , 2.0): SIGMAs[j][m]+Qijk[i][j][k]*Math.pow(((double)Xs[i][m] - MUs[j][m]) , 2.0);
		//					denSIGMAs[j][m] = denSIGMAs[j][m]+Qijk[i][j][k];
		//				}
		//			}
		//		}
		//	}
			
			//Normalize and taking the square root
		//	for(int j=0; j<numChromStates; j++){
		//		for(int m=0; m<M; m++){
		//			SIGMAs[j][m] = Math.sqrt(SIGMAs[j][m]/denSIGMAs[j][m]);
		//		}
		//	}
			
		//	if(config.capSigma()){
		//		for(int j=0; j<numChromStates; j++){
		//			for(int m=0; m<M; m++){
		//				SIGMAs[j][m] = (SIGMAs[j][m] > this.capSIGMAs[m]) ? this.capSIGMAs[m] : SIGMAs[j][m];
		//			}
		//		}
		//	}
		//}
		
		//---------------------Bjk update -------------------------------------------
		
		//Compute
		double[] denBjk = new double[numFacBindingStates];
		for(int k=0; k<numFacBindingStates; k++){
			for(int j=0; j< numChromStates; j++){
				for(int i=0; i<N; i++){
					Bjk[j][k] = i==0 ? Qijk[i][j][k] : Bjk[j][k]+Qijk[i][j][k];
					denBjk[k] = denBjk[k]+Qijk[i][j][k];
				}
			}
		}
		
		//Normalize
		for(int k=0; k<numFacBindingStates; k++){
			for(int j=0; j< numChromStates; j++){
				Bjk[j][k] = Bjk[j][k]/denBjk[k];
			}
		}
		
		// ------------------------ regularization chromatin weights update (if regularization is true) ------------------------------------
		if(this.regularize){
			for(int c=0; c<C; c++){
				double Z=0.0;
				Cubic cube_root_solver = new Cubic();
				for(int i=0; i<N; i++){
					for(int j=0; j<numChromStates; j++){
						for(int k=0; k<numFacBindingStates; k++){
							NormalDistribution gaussian = new NormalDistribution(MUc[j][c],Math.pow(SIGMAc[j][c], 2.0));
							if(!Double.isNaN(gaussian.calcProbability((double)Xc[i][c]))){
								double que_pusher=0.0;
								if(Double.isInfinite(Math.log(gaussian.calcProbability((double)Xc[i][c])))){
									que_pusher = Math.log(Double.MIN_VALUE);
									Z = (i==0 && j==0 && k==0) ? Qijk[i][j][k]*que_pusher : Z+Qijk[i][j][k]*que_pusher;
								}else{
									que_pusher = Math.log(gaussian.calcProbability((double)Xc[i][c]));
									Z =  (i==0 && j==0 && k==0) ? Qijk[i][j][k]*que_pusher : Z+Qijk[i][j][k]*que_pusher;
								}
								}else{
								Z = (i==0 && j==0 && k==0) ? 0.0 : Z+0.0;
							}
						}
					}
				}
				//System.out.println("the value of Z-chrom is: "+Double.toString(Z));
				cube_root_solver.solve(1, 2, 1, Z/this.lambda);
				this.WCnorm[c] = cube_root_solver.x1;
				//System.out.println("Number of roots-chrom: "+Integer.toString(cube_root_solver.nRoots));
				//System.out.println("Value of Highest root is-chrom: "+Double.toString(cube_root_solver.x1));
			}
			if(this.seqState){
				for(int m=0; m<M; m++){
					double Z=0.0;
					Cubic cube_root_solver =  new Cubic();
					for(int i=0; i<N; i++){
						for(int j=0; j<numChromStates; j++){
							for(int k=0; k< this.numFacBindingStates; k++){
								NormalDistribution gaussian = new NormalDistribution(MUs[j][m],Math.pow(SIGMAs[j][m], 2.0));
								if(!Double.isNaN(gaussian.calcProbability((double)Xs[i][m]))){
									Z = (i==0 && j==0 && k==0) ?  Qijk[i][j][k]*Math.log(gaussian.calcProbability((double)Xs[i][m])) : Z+Qijk[i][j][k]*Math.log(gaussian.calcProbability((double)Xs[i][m]));
								}else{
									Z = (i==0 && j==0 && k==0) ? 0.0 : Z+0.0;
								}
							}
						}
					}
					//Debug lines
					//System.out.println("the value of Z-seq is: "+Double.toString(Z));
					cube_root_solver.solve(1, 2, 1, Z/this.lambda);
					this.WSnorm[m] = cube_root_solver.x1;
					//System.out.println("Number of roots-seq: "+Integer.toString(cube_root_solver.nRoots));
					//System.out.println("Value of Highest root is-seq: "+Double.toString(cube_root_solver.x1));
				}
			}
		}
	}
	
	
	//setters
	public void setSeqMode(Sequences seqs, double[][] Xs, ExperimentManager manager){
		this.seqState = true;
		this.setXs(Xs);
		this.M = Xs[0].length;
		System.out.println("2-time "+Integer.toString(M));
		if(this.regularize){
			WSnorm = new double[M];
		}
		this.capSIGMAs = new double[M];
		//this.setSequences(seqs);
		this.setInitialSeqParams(manager);
		this.trainMUs = new double[this.total_itrs+1][this.numChromStates][this.M];
		this.trainSIGMAs = new double[this.total_itrs+1][this.numChromStates][this.M];
		
		
	}
	
	private void setXs(double[][] Xs){this.Xs = Xs;}
	private void setInitialSeqParams(ExperimentManager manager){this.initializeSeqParams(manager);}
	
	//Accessors
	public double[][] getMUc(){return this.MUc;}
	public double[][] getMUf(){return this.MUf;}
	public double[][] getSIGMAc(){return this.SIGMAc;}
	public double[][] getSIGMAf(){return this.SIGMAf;}
	public double[][] getBjk(){return this.Bjk;}
	public double[][] getMUs(){return this.MUs;}
	public double[][] getSIGMAs(){return this.SIGMAs;}
	public int getNumTrainingEgs(){return this.N;}
	public int getNumMotifs(){return this.M;}
	public int getnumChromConds(){return this.C;}
	public int getnumFacConds(){return this.F;}
	public float[][] getXc(){return this.Xc;}
	public float[][] getXf(){return this.Xf;}
	public double[][] getXs(){return this.Xs;}
	public boolean getSeqStateStatus(){return this.seqState;}
	public double[] getChromWeights(){return this.WCnorm;}
	public double[] getSeqWeights(){return this.WSnorm;}
	public String[] getConditionNames(){return this.condition_names;}
	// main method is only for testing puposers
	
	public static void main(String[] args){
		//double[] test = getRandomList(3,true);
		//System.out.println(test[0]);
		//System.out.println(test[1]);
		//System.out.println(test[2]);
		double a = Double.valueOf("9.0e-09");//.longValue();
		
		System.out.println(a);
		
	}
	
}
