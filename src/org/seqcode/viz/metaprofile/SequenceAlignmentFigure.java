package org.seqcode.viz.metaprofile;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.seqcode.data.io.FASTALoader;
import org.seqcode.data.io.RegionFileUtilities;
import org.seqcode.genome.Genome;
import org.seqcode.genome.Species;
import org.seqcode.genome.location.StrandedRegion;
import org.seqcode.genome.sequence.SequenceGenerator;
import org.seqcode.gseutils.ArgParser;
import org.seqcode.gseutils.Args;
import org.seqcode.gseutils.NotFoundException;
import org.seqcode.gseutils.Pair;


public class SequenceAlignmentFigure {

	private static Color AColor = Color.RED;
	private static Color CColor = Color.BLUE;
	private static Color GColor = Color.ORANGE;
	private static Color TColor = Color.GREEN;
	private static Color GapColor = Color.WHITE;
	private static Color NColor = Color.GRAY;
	
	
	public SequenceAlignmentFigure(){}
	
	public static void setColors(Color a, Color c, Color g, Color t){
		AColor = a;
		CColor = c;
		GColor = g;
		TColor = t;
	}
	/**
	 * Visualize sequences as color pixels
	 * @param seqs, raw sequences or FASTA sequences
	 * @param width, width of each base, in pixel
	 * @param height, height of each base, in pixel
	 * @param f, output file
	 */
	public static void visualizeSequences(List<String> seqs, int width, int height, File f){
		if (seqs.size()==0)
			return;
		
		int pixheight = 0;
		int maxLen = 0;
		for (String s:seqs){
        	if (s.length()!=0 && s.charAt(0)!='>')	{		// ignore header line of FASTA file
        		pixheight += height;
        		if (maxLen < s.length())
        			maxLen = s.length();
        	}
		}
		int pixwidth = maxLen*width;
		
		System.setProperty("java.awt.headless", "true");
		BufferedImage im = new BufferedImage(pixwidth, pixheight,BufferedImage.TYPE_INT_ARGB);
        Graphics g = im.getGraphics();
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(Color.WHITE);
        g2.fillRect(0,0,pixwidth, pixheight);
        
        int count = 0;
        for (String s:seqs){
        	if (s.charAt(0)=='>')			// ignore header line of FASTA file
        		continue;
        	char[] letters = s.toCharArray();
        	for (int j=0;j<letters.length;j++){
        		switch(letters[j]){
        		case 'A':
        		case 'a':
        			g.setColor(AColor);
        			break;
        		case 'C':
        		case 'c':
                    g.setColor(CColor);
        			break;
        		case 'G':
        		case 'g':
                    g.setColor(GColor);
        			break;
        		case 'T':
        		case 't':
                    g.setColor(TColor);
        			break;
        		case '-':
                    g.setColor(GapColor);
        			break;
                default:
                	g.setColor(NColor);
        		}
                g.fillRect(j*width, count*height, width, height);
        	}
            count++;
        }
        try {
            ImageIO.write(im,"png",f);
        }  catch (IOException ex) {
            ex.printStackTrace();
        }
	}
	
	public static void main(String[] args) {
		ArgParser ap = new ArgParser(args);
		Color AColor = Color.RED;
		Color CColor = Color.BLUE;
		Color GColor = Color.ORANGE;
		Color TColor = Color.GREEN;
        
        try {
        	String outFile = ap.hasKey("out") ? ap.getKeyValue("out") : "out.png";
        	String seqOutFile = ap.hasKey("seqout") ? ap.getKeyValue("seqout") : null;
        	int win = ap.hasKey("win") ? new Integer(ap.getKeyValue("win")).intValue():-1;
			List<String> seqs = null;
			
			//color options
			if(ap.hasKey("wscolor")){
				CColor = new Color(128,100,128);
				GColor = new Color(128,100,128);
				AColor = Color.YELLOW;
				TColor = Color.YELLOW;
			}
    	
        	if(ap.hasKey("species") && ap.hasKey("gen") && (ap.hasKey("peaks") || ap.hasKey("bed"))){
	        	Pair<Species, Genome> pair = Args.parseGenome(args);
	        	Genome currgen = pair.cdr();
	        	String genomeSequencePath = ap.hasKey("gen") ? ap.getKeyValue("gen") : null;
        		SequenceGenerator seqgen = new SequenceGenerator(currgen);
        		if(genomeSequencePath != null){
        			seqgen.useCache(true);
        			seqgen.useLocalFiles(true);
        			seqgen.setGenomePath(genomeSequencePath);
        		}
        		List<StrandedRegion> regions = null;
        		if(ap.hasKey("peaks")){
        			String peaksFile = ap.getKeyValue("peaks");
        			regions = RegionFileUtilities.loadStrandedRegionsFromMotifFile(currgen, peaksFile, win);
        		}else if(ap.hasKey("bed")){
        			String bedFile = ap.getKeyValue("bed");
        			regions = RegionFileUtilities.loadStrandedRegionsFromBEDFile(currgen, bedFile, win);
        		}
        		
        		seqs = RegionFileUtilities.getSequencesForStrandedRegions(regions, seqgen);
        	}else if(ap.hasKey("seq")){
        		String seqsFile = ap.getKeyValue("seq");
        		FASTALoader loader = new FASTALoader();
        		File f = new File(seqsFile);
        		seqs = new ArrayList<String>();
        		Iterator<Pair<String, String>> it = loader.execute(f);
        		while(it.hasNext()){
        			Pair<String, String> p = it.next();
        			String name = p.car();
        			String seq = p.cdr();
        			seqs.add(seq);
        		}
        	}else{
        		SequenceAlignmentFigure.printHelp();
        	}
        	
        	if(seqs !=null){
		    	SequenceAlignmentFigure fig = new SequenceAlignmentFigure();
		    	fig.setColors(AColor, CColor, GColor, TColor);
		    	fig.visualizeSequences(seqs, 3, 1, new File(outFile));
		    	
		    	if(seqOutFile != null){
		    		FileWriter fout = new FileWriter(seqOutFile);
		    		for(String s: seqs){
		    			fout.write(String.format("%s\n", s));
		    		}
		    		fout.close();
		    	}
        	}
	    	
        } catch (NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void printHelp(){
		System.err.println("Usage:\n " +
                "SequenceAlignmentFigure \n" +
                " Required: \n" +
                "  --species <species;version> AND --gen <sequence directory>\n AND" +
                "  (--peaks <file containing stranded coordinates> OR --bed <bed file>)\n\tOR\n" +
                "  --seq <FASTA file>\n" +
                " Optional:\n" +
                "  --win <window of sequence around peaks> \n"+
                "  --out <output filename>\n" +
                "  --seqout <sequence output filename>\n" +
                "  --wscolor [color bases according to Weak & Strong]" +
                "");
		return;
	}
}
