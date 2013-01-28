package edu.psu.compbio.seqcode.projects.shaun;

import java.util.Iterator;

import edu.psu.compbio.seqcode.gse.datasets.general.NamedRegion;
import edu.psu.compbio.seqcode.gse.datasets.general.Region;
import edu.psu.compbio.seqcode.gse.datasets.species.Genome;
import edu.psu.compbio.seqcode.gse.datasets.species.Organism;
import edu.psu.compbio.seqcode.gse.ewok.verbs.ChromRegionIterator;
import edu.psu.compbio.seqcode.gse.utils.ArgParser;
import edu.psu.compbio.seqcode.gse.utils.NotFoundException;

/**
 * 
 * @author Shaun Mahony
 * 
 * TestChIPCorrection: a simple peak caller based on a ChIP-seq error model that 
 * uses the relative likelihood of observed hit counts.
 *
 */
public class TestChIPCorrection {

	private static double DEFT_T = -10.5;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String exptName = "YoungLab_Solexa_Oct4";
//		String exptName = "PPG_Solexa_RAR_ES+2d";
//		String exptName = "PPG_Solexa_RAR_8hr";
//		String exptName = "PPG_Solexa_RAR_2+1";
//		String WCEName = "PPG_Solexa_WCE_ES+2d";
//		String exptName = "PPG_Solexa_WCE_2+1";
		
		ArgParser ap = new ArgParser(args);
        if(!ap.hasKey("species") || !ap.hasKey("genome")||!ap.hasKey("expt") || !ap.hasKey("back")) { 
            System.err.println("Usage:\n" +
                               "TestChIPCorrection" +
                               "--species <organism name> " +
                               "--genome <genome version> "+
                               "--expt <solexa expt> " +
                               "--back <background expt>"+
                               "--t <threshold on peak ll-ratio> " +
                               "--ecdf <expt CDF> " +
                               "--bcdf <back CDF> ");
            return;
        }
        String species = ap.getKeyValue("species");
        String genome = ap.getKeyValue("genome");
        String exptName = ap.getKeyValue("expt");
        String backName = ap.getKeyValue("back");
        double userT = ap.hasKey("t") ? Double.valueOf(ap.getKeyValue("t")) : DEFT_T;
        String ecdfFile = ap.hasKey("ecdf") ? ap.getKeyValue("ecdf") : "NOFILE";
        String bcdfFile = ap.hasKey("bcdf") ? ap.getKeyValue("bcdf") : "NOFILE";
        
		try {
			System.out.println(exptName+"\t"+backName);
			Organism org = Organism.getOrganism(species);
			Genome gen = org.getGenome(genome);
				
			SeqExptHandler IPhandle = new SeqExptHandler(org, gen, exptName);
			SeqExptHandler backhandle = new SeqExptHandler(org, gen, backName);
			
			if(!ecdfFile.equals("NOFILE")){IPhandle.loadCDFFile(ecdfFile);}
			if(!bcdfFile.equals("NOFILE")){backhandle.loadCDFFile(bcdfFile);}
			
			SeqExptLikelihoodRatio selr = new SeqExptLikelihoodRatio(IPhandle, backhandle);
			selr.calcPeaks(userT);
			selr.printPeaks(annots);
			
			
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public static String [] annots = new String[]{
		"refGene" //, "knownGene", "mgcGenes", "ensGene"
	};

}
