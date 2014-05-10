package edu.psu.compbio.seqcode.deepseq;

import java.util.ArrayList;
import java.util.List;

/**
 * Read represents the ChIP-Seq tag (fragment) which is generated by a short read
 * sequencer. <br>
 * Each <tt>Read</tt> can map to different locations (<tt>ReadHits</tt>) on
 * the genome (if non-unique hits are supported)
 * 
 * Read and ReadHit are only used as a convenient way to represent multiply mapping reads when reading from certain file formats.
 * @author shaunmahony
 *
 */
public class Read {
	protected List<ReadHit> hits = new ArrayList<ReadHit>();
	protected float numHits=0; //have to store this separately because we can't always trust the size of the hits list
	
	public Read(){
	}
	
	//Accessor
	public double getNumHits(){return (double)hits.size();}
	public void setNumHits(float n){
		numHits=n;
		float w = 1/numHits;
		for(ReadHit x : hits){
			x.setWeight(w);
		}
	}
	public void addHit(ReadHit h){addHit(h, true);}
	public void addHit(ReadHit h, boolean updateWeight){
		//First add the hit
		hits.add(h);
		numHits++;
		
		if(updateWeight){
			//Now propagate the effect of adding the hit to the read weights
			float w = 1/numHits;
			for(ReadHit x : hits){
				x.setWeight(w);
			}
		}
	}
	
	public List<ReadHit> getHits(){return hits;}
	
}