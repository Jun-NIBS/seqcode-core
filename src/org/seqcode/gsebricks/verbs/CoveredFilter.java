package org.seqcode.gsebricks.verbs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import org.seqcode.genome.Genome;
import org.seqcode.genome.location.Region;
import org.seqcode.gseutils.NotFoundException;


public class CoveredFilter<X extends Region> implements Filter<X, X> {

	private LinkedList<Region> regions;
	private File file;
	private Genome genome;
	
	public CoveredFilter(File file) {
		try { 
			genome = Genome.findGenome("mm8");
		} catch(NotFoundException nfe) { 
			throw new IllegalStateException();
		}
		this.file = file;
		this.regions = new LinkedList<Region>();
		parse();
	}
	
	private void parse() {
		try { 
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = null;
			String[] split;
			while((line = br.readLine()) != null) { 
				split = line.split("\t");
				String chrom = split[0];
				int start = Integer.parseInt(split[1]);
				int end = Integer.parseInt(split[2]);
				Region r = new Region(genome, chrom, start, end);
				regions.add(r);
			}
			br.close();
		} catch(IOException ie) { 
			ie.printStackTrace(System.err);
		}
	}

	public X execute(X a) {
		for (Region r : regions) {
			if (r.contains(a)) {
				return a;
			}
		}
		return null;
	}

}
