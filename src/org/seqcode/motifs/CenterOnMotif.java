package org.seqcode.motifs;
import java.util.*;
import java.io.*;

import org.seqcode.data.connections.DatabaseException;
import org.seqcode.data.connections.DatabaseFactory;
import org.seqcode.data.connections.UnknownRoleException;
import org.seqcode.data.motifdb.*;
import org.seqcode.genome.Genome;
import org.seqcode.genome.location.Region;
import org.seqcode.genome.sequence.SequenceGenerator;
import org.seqcode.gsebricks.verbs.*;
import org.seqcode.gseutils.*;



public class CenterOnMotif {

    private Genome genome;
    private ArrayList<WeightMatrix> matrices;  // these are the matrices to scan for
    private int expand;
    private double cutoffpercent;

    public void parseArgs(String args[]) throws Exception {
        genome = Args.parseGenome(args).cdr();
        cutoffpercent = Args.parseDouble(args,"cutoff",.5);
        expand = Args.parseInteger(args,"expand",0);
        MarkovBackgroundModel bgModel = null;
        String bgmodelname = Args.parseString(args,"bgmodel","whole genome zero order");
        BackgroundModelMetadata md = BackgroundModelLoader.getBackgroundModel(bgmodelname,
                                                                              1,
                                                                              "MARKOV",
                                                                              Args.parseGenome(args).cdr().getDBID());

        if (md != null) {
            bgModel = BackgroundModelLoader.getMarkovModel(md);
        } else {
            System.err.println("Couldn't get metadata for " + bgmodelname);
        }
                
        matrices = new ArrayList<WeightMatrix>();
        matrices.addAll(Args.parseWeightMatrices(args));
        if (bgModel == null) {
            for (WeightMatrix m : matrices) {
                m.toLogOdds();
            }            
        } else {
            for (WeightMatrix m : matrices) {
                m.toLogOdds(bgModel);
            }            
        }

    }
    public void run() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        SequenceGenerator seqgen = new SequenceGenerator();
        String line = null;
        while ((line = reader.readLine()) != null) {
            Region region = Region.fromString(genome, line);
            if (region == null) {
                continue;
            }
            region = region.expand(expand,expand);
            char[] seq = seqgen.execute(region).toCharArray();
            int bestpos = -1;
            double bestscore = 0;
            String beststrand = "";
            for (WeightMatrix m : matrices) {
                float cut = (float)(cutoffpercent * m.getMaxScore());
                List<WMHit> hits = WeightMatrixScanner.scanSequence(m,
                                                                    cut,
                                                                    seq);
                for (WMHit h : hits) {
                    if (h.getScore() > bestscore) {
                        bestscore = h.getScore();
                        bestpos = (h.getStart() + h.getEnd()) / 2;
                        beststrand = h.getStrand();
                    }
                }
            }
            if (bestscore > 0) {
                int p = region.getStart() + bestpos;
                System.out.println(region.getChrom() + ":" + p + "-" + p + ":" + beststrand);
            } 
        }
    }
    public static void main(String args[]) throws Exception {
        CenterOnMotif center = new CenterOnMotif();
        center.parseArgs(args);
        center.run();
    }
}