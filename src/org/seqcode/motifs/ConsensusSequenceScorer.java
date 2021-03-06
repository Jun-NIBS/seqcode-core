package org.seqcode.motifs;

import org.seqcode.genome.location.Region;
import org.seqcode.genome.location.StrandedRegion;
import org.seqcode.genome.sequence.SequenceGenerator;
import org.seqcode.genome.sequence.SequenceUtils;

public class ConsensusSequenceScorer {
	private ConsensusSequence consensus;
	private SequenceGenerator seqgen;
	
	public ConsensusSequenceScorer(ConsensusSequence cons){
		consensus = cons;
		seqgen = new SequenceGenerator();
	}
	public ConsensusSequenceScorer(ConsensusSequence cons, SequenceGenerator sg){
		consensus = cons;
		seqgen = sg;
	}
	
	public ConsensusSequenceScoreProfile execute(Region r) {return execute(r, '.');}
	public ConsensusSequenceScoreProfile execute(Region r, char watsoncrick) {
        String seq = seqgen.execute(r);
        seq = seq.toUpperCase();
        int[] fscores=null, rscores=null;
        char strand = '.';
        
        if(watsoncrick!='.'){
	        if(r instanceof StrandedRegion){
	        	char rstrand = ((StrandedRegion)r).getStrand(); 
	        	if((watsoncrick=='W' && rstrand=='+') || (watsoncrick=='C' && rstrand=='-'))
	        		strand='+';
	        	else
	        		strand='-';
	        }else
	        	strand = watsoncrick=='W' ? '+' : '-';
        }
        
        try {
        	if(strand=='.' || strand=='+')
        		fscores = score(consensus, seq.toCharArray(), '+');
        	else{
        		fscores = new int[seq.length()];
        		for(int i=0; i<seq.length(); i++)
        			fscores[i] = consensus.getMaxMismatch();
        	}
        	
        	if(strand=='.' || strand=='-'){
        		seq = SequenceUtils.reverseComplement(seq);
        		rscores = score(consensus, seq.toCharArray(), '-');
        	}else{
        		rscores = new int[seq.length()];
        		for(int i=0; i<seq.length(); i++)
        			rscores[i] = consensus.getMaxMismatch();
        	}
        } catch (ArrayIndexOutOfBoundsException e) { 
            e.printStackTrace(System.err);
        }
       	
        return new ConsensusSequenceScoreProfile(consensus, fscores, rscores);
    }
    
    public ConsensusSequenceScoreProfile execute(String seq) {return execute(seq, '.');}
    public ConsensusSequenceScoreProfile execute(String seq, char strand) {
    	seq = seq.toUpperCase();
        int[] fscores = null, rscores = null;
        if(strand=='.' || strand=='+')
    		fscores = score(consensus, seq.toCharArray(), '+');
    	else{
    		fscores = new int[seq.length()];
    		for(int i=0; i<seq.length(); i++)
    			fscores[i] = consensus.getMaxMismatch();
    	}
    	
    	if(strand=='.' || strand=='-'){
    		seq = SequenceUtils.reverseComplement(seq);
    		rscores = score(consensus, seq.toCharArray(), '-');
    	}else{
    		rscores = new int[seq.length()];
    		for(int i=0; i<seq.length(); i++)
    			rscores[i] = consensus.getMaxMismatch();
    	}
        return new ConsensusSequenceScoreProfile(consensus, fscores, rscores);
    }

    public static int[] score(ConsensusSequence cons, char[] sequence, char strand) {
        int[] results = new int[sequence.length];
        /* scan through the sequence */
        int length = cons.getLength();
       	for (int i = 0; i < sequence.length; i++) {
       		results[i] = cons.getMaxMismatch();
       	}
       	if (sequence.length<length)
     		return results;
       	
        for (int i = 0; i <= sequence.length - length; i++) {
            int mismatches=0;
            for (int j = 0; j < length; j++)
                if(!cons.getMatrix()[j][sequence[i+j]])
                	mismatches++;
            
            if(strand=='-')
            	results[sequence.length-length-i] = mismatches;
            else
            	results[i] = mismatches;
        }
        return results;
    }


}
