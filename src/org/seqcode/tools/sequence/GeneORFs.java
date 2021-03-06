package org.seqcode.tools.sequence;

import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import org.seqcode.data.io.FASTAWriter;
import org.seqcode.genome.Genome;
import org.seqcode.genome.location.Gene;
import org.seqcode.genome.location.NamedStrandedRegion;
import org.seqcode.genome.location.Region;
import org.seqcode.gsebricks.verbs.location.ChromRegionIterator;
import org.seqcode.gsebricks.verbs.location.RefGeneGenerator;
import org.seqcode.gseutils.Args;
import org.seqcode.gseutils.NotFoundException;


/**
 * Returns the sequences of named gene ORFs
 * 
 * cat gene_names.txt | java GeneORFs --species "$SC;Sigmav7" --genes s288cMapped 
 * cat gene_names.txt | java GeneORFs --species "$SC;Sigmav7" --genes s288cMapped --fasta
 * java GeneORFs --species "$SC;Sigmav7" --genes s288cMapped  --fasta --allgenes
 *
 *
 */

public class GeneORFs {

    private int upstream, downstream;
    private List<RefGeneGenerator> geneGenerators;
    private Genome genome;
    private boolean allGenes, toFasta;
    private FASTAWriter<NamedStrandedRegion> fwriter;

    public static void main(String args[]) throws Exception {
        GeneORFs gp = new GeneORFs();
        gp.parseArgs(args);        
        gp.run();
    }
    public GeneORFs() {}
    public void parseArgs(String args[]) throws NotFoundException {
        geneGenerators = Args.parseGenes(args);
        for (RefGeneGenerator r : geneGenerators) {
            r.retrieveExons(false);
        }
        genome = Args.parseGenome(args).getLast();        
        allGenes = Args.parseFlags(args).contains("allgenes");
        toFasta = Args.parseFlags(args).contains("fasta");
        if (toFasta) {
            fwriter = new FASTAWriter<NamedStrandedRegion>(System.out);
        }
    }
    public void run() throws IOException {
        if (allGenes) {
            ChromRegionIterator chroms = new ChromRegionIterator(genome);
            while (chroms.hasNext()) {
                Region chrom = chroms.next();
                /* we'll use all the gene generators provided but don't want to output duplicate regions.
                   seen is keyed on 5' and contains a list of 3' ends that have already been output.
                */
                for (RefGeneGenerator refgene : geneGenerators) {
                    Iterator<Gene> iter = refgene.execute(chrom);
                    while (iter.hasNext()) {
                        Gene g = iter.next();
                        output(g);
                    }
                }
            }
        } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            while ((line = reader.readLine()) != null) {
                Gene g = null;
                String pieces[] = line.split("\\t");
                int i = 0;
                while (i < pieces.length && g == null) {
                    for (RefGeneGenerator refgene : geneGenerators) {
                        Iterator<Gene> iter = refgene.byName(pieces[i]);
                        while (iter.hasNext()) {
                            if (g == null) {
                                g = iter.next();
                            } else {
                                iter.next();
                            }

                        }
                        if (g != null) {
                            break;
                        }                        
                    }
                    i++;
                }
                if (g != null) {
                    g.setName(line);
                    output(g); 
                } else {
                    System.err.println("Couldn't find " + line);
                }

            }
        }
    }
    public void output(NamedStrandedRegion r) {
        if (toFasta) {
            fwriter.consume(r);
        } else {
            System.out.println(String.format("%s\t%s:%d-%d:%s",
                                             r.toString(),
                                             r.getChrom(), r.getStart(), r.getEnd(), r.getStrand()));
        }
    }

}