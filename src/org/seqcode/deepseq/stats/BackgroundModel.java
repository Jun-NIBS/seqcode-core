package org.seqcode.deepseq.stats;

import org.seqcode.genome.location.Region;

/**
 * BackgroundModel: General background for defining thresholds 
 * 
 * 	Model Types: 
 * 			Genome-wide = -1
 * 			CurrentRegion = 0
 * 			Local window size = positive integer
 * 
 * @author Shaun Mahony
 * @version	%I%, %G%
 */
public abstract class BackgroundModel {
	protected int modelType; //-1 for genome-wide, 0 for current region, positive integer for local window size  
	protected double logConfidence;
	protected double confThreshold;
	protected double totalReads, regionLength, mappableRegion, binWidth;
	protected float expectedCount=0;
	protected int countThreshold=0;
	protected char strand='.';
	protected boolean useThisExpt=true;
	protected double scaling=1;
	
	public BackgroundModel(int mtype, double lc, double r, double rl, double mr, double bw, char str){this(mtype, lc, r, rl, mr, bw, str, 1, true);}
	public BackgroundModel(int mtype, double lc, double r, double rl, double mr, double bw, char str, double sc, boolean ute){
		modelType = mtype;
		logConfidence=lc;
		confThreshold = Math.pow(10,logConfidence);
		totalReads=r;
		regionLength=rl;
		mappableRegion=mr;
		binWidth=bw;
		strand=str;
		scaling = sc;
		useThisExpt = ute;
		
		countThreshold = calcCountThreshold();
		expectedCount = calcExpectedCount();
	}
	
	//Accessors
	public char getStrand(){return strand;}
	public boolean isGenomeWide(){return modelType==-1 ? true : false;}
	public float getExpectedCount(){ return expectedCount;}
	public int getThreshold(){return countThreshold;}
	
	//Required
	public abstract boolean passesThreshold(int count);
	public abstract boolean underThreshold(int count);
	protected abstract int calcCountThreshold();
	protected abstract float calcExpectedCount();
	
	//Update the threshold (depends on the type of model... local, etc)
	public void updateModel(Region currReg, int currOffset, float [] thisExptHitCounts, float [] otherExptHitCounts, float hitCountBinStep){
		float [] hitCounts = useThisExpt ? thisExptHitCounts : otherExptHitCounts; 
		if(hitCounts!=null){
			if(modelType==-1){//Genome-wide
				if(countThreshold==0){
					countThreshold = calcCountThreshold();
					expectedCount = calcExpectedCount();
				}
			}else if(modelType==0){//Current region
				float sum=1;//pseudo count
				for(int i=0; i<hitCounts.length; i++)
					sum+=hitCounts[i];
				totalReads = scaling*sum;
				regionLength = currReg.getWidth();
				mappableRegion=1.0;//Big assumption
				countThreshold = calcCountThreshold();
				expectedCount = calcExpectedCount();
			}else{//Window around current position
				int win = modelType;
				int istart = currOffset-(win/2)<0 ? 0 :currOffset-(win/2);
				int istop = currOffset+(win/2)>=currReg.getWidth() ? currReg.getWidth()-1 :currOffset+(win/2);
				int istartbin = (int)(istart/hitCountBinStep);
				int istopbin = (int)(istop/hitCountBinStep);
				float sum=1;//pseudo count
				for(int i=istartbin; i<=istopbin; i++){
					sum+=hitCounts[i]; 
				}
				totalReads = scaling*sum;
				regionLength=istop-istart+1;
				//mappableRegion=1.0; //any need for this assumption?
				countThreshold = calcCountThreshold();
				expectedCount = calcExpectedCount();
			}
		}
	}
}
