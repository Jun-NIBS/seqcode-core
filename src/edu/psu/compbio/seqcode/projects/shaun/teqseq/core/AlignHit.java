package edu.psu.compbio.seqcode.projects.shaun.teqseq.core;

import java.util.List;

import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.SAMRecord;

import edu.psu.compbio.seqcode.gse.datasets.general.Region;
import edu.psu.compbio.seqcode.gse.datasets.general.StrandedPoint;
import edu.psu.compbio.seqcode.gse.datasets.general.StrandedRegion;
import edu.psu.compbio.seqcode.gse.datasets.species.Genome;

public class AlignHit extends StrandedRegion{
	
	protected double weight;
	protected AlignBlock[] blocks;
	protected boolean isPaired=false;
	protected StrandedPoint pairedRead=null;
	
	/**
	 * Convert a SAMRecord to an AlignHit. Assumes zero-based genome coordinates. 
	 * @param rec SAMRecord denoting read alignment
	 * @param g Genome
	 * @param weight double weighting applied in addition to the weighting from the number of hits
	 */
	public AlignHit(SAMRecord rec, Genome g, double weighting){
		super(new StrandedRegion(g, rec.getReferenceName().replaceFirst("chr", ""), rec.getAlignmentStart()-1, rec.getAlignmentEnd()-1, rec.getReadNegativeStrandFlag() ? '-' : '+'));
		//Assumes that SAM is a one-based genome coordinate.
		//TODO: Check when Genome is defined from fasta files or the BAM index
		weight = weighting*(1/(float)rec.getIntegerAttribute("NH"));
		
		List<AlignmentBlock> recBlocks = rec.getAlignmentBlocks();
		blocks = new AlignBlock[recBlocks.size()];
	    for(int a=0; a<recBlocks.size(); a++){ //Iterate over alignment blocks
	    	AlignmentBlock currBlock = recBlocks.get(a);
	    	blocks[a] = new AlignBlock(currBlock.getLength(), currBlock.getReferenceStart());
	    }
	    
	    if(rec.getReadPairedFlag() && !rec.getMateUnmappedFlag() && rec.getProperPairFlag()){
	    	isPaired=true;
	    	pairedRead = new StrandedPoint(g, rec.getMateReferenceName().replaceFirst("chr", ""), rec.getMateAlignmentStart()-1, rec.getReadNegativeStrandFlag() ? '-' : '+');
	    }
	}public AlignHit(SAMRecord rec, Genome g){this(rec, g, 1.0);}
	
	//Accessors
	public double getWeight(){return weight;}
	public AlignBlock[] getAlignmentBlocks(){return blocks;}
	public StrandedPoint getPairedRead(){return (isPaired ? pairedRead : null);}
	
	public boolean blockOverlaps(Region reg){
        boolean oLap=false;
        for(AlignBlock b : blocks){
        	oLap = oLap|b.overlaps(reg);
        }
        return(oLap);
	}
}
