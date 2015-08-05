package edu.psu.compbio.seqcode.projects.akshay.regulatorydomains;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.psu.compbio.seqcode.genome.Genome;
import edu.psu.compbio.seqcode.genome.GenomeConfig;
import edu.psu.compbio.seqcode.genome.location.Point;
import edu.psu.compbio.seqcode.genome.location.Region;
import edu.psu.compbio.seqcode.gse.tools.utils.Args;
import edu.psu.compbio.seqcode.gse.utils.ArgParser;
import edu.psu.compbio.seqcode.gse.utils.io.RegionFileUtilities;


/**
 * Collection of static methods relating to peaks and genes 
 * @author akshaykakumanu
 *
 */
public class ReguDomainsToGenesSandbox {
	static List<Point> peaks = new ArrayList<Point>();
	Genome gen;
	
	
	public ReguDomainsToGenesSandbox() {
		// TODO Auto-generated constructor stub
	}
	
	// Settors
	public void setPeaks(List<Point> ps){peaks = ps;}
	public void setGenome(Genome g){gen=g;}
	
	
	
	public static void printNoOverlapWith(List<Point> locations, int win){
		HashMap<String, List<Point>> byChr = ReguDomainsToGenesSandbox.hashByChr(locations);
		for(Point p: peaks){
			Region extPeak = p.expand(win/2);
			int noOverlap = 0;
			if(byChr.containsKey(p.getChrom())){
				for(Point l: byChr.get(p.getChrom())){
					if(extPeak.contains(l))
						noOverlap++;
				}
			}
			System.out.println(p.getLocationString()+"\t"+Integer.toString(noOverlap));
		}
	}
	
	
	public static  HashMap<String, List<Point>> hashByChr(List<Point> ps){
		HashMap<String, List<Point>> byChr = new HashMap<String, List<Point>>();
		for(Point p : ps){
			if(!byChr.containsKey(p.getChrom()))
				byChr.put(p.getChrom(), new ArrayList<Point>());
			byChr.get(p.getChrom()).add(p);
		}
		return byChr;
	}
	
	
	public static void main(String[] args){
		GenomeConfig gcon = new GenomeConfig(args);
		int win = Args.parseInteger(args, "win", 150);
		ArgParser ap = new ArgParser(args);
		
		ReguDomainsToGenesSandbox runner = new ReguDomainsToGenesSandbox();
		runner.setGenome(gcon.getGenome());
		
		String peaksString = ap.getKeyValue("Peaks");
		List<Point> peaks = RegionFileUtilities.loadPeaksFromPeakFile(gcon.getGenome(), peaksString, win);
		runner.setPeaks(peaks);
		
		String locationsString = ap.getKeyValue("locations");
		List<Point> locations = RegionFileUtilities.loadPeaksFromPeakFile(gcon.getGenome(), locationsString, win);
		
		if(ap.hasKey("printNoOverlap"))
			ReguDomainsToGenesSandbox.printNoOverlapWith(locations, win);
		
		
	}
}
