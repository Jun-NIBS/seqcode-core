package org.seqcode.genome.sequence.seqfunctions;

import java.util.HashMap;
import java.util.Map;

/**
 * Scores Minor Groove Width (Angstroms) of a sequence using score defined by Remo Rohs' lab.
 * Source: 
 * 		http://rohslab.cmb.usc.edu/DNAshape/
 *		Zhou,,T., Yang,L., Lu,Y., Dror,I., Dantas Machado,A.C., Ghane,T., Di Felice,R. and Rohs,R. 
 *		DNAshape: a method for the high-throughput prediction of DNA structural features on a genomic scale. 
 *		Nucleic Acids Res. (2013) 41, W56-W62.
 *   
 * @author mahony
 *
 */
public class MGWStructureFunction implements SeqFunction{

	//Variables
	final int scoreDimension = 1;
	int scoringOffset = 2;
	int scoreWindowSize = 5;
	boolean isBetweenNucs = false;
	final String[] labels = {"MGW"};
	final String description = "Minor Groove Width (Rohs)";
	
	Map<String, Double> structure = new HashMap<String, Double>();
	
	
	public MGWStructureFunction(){
		loadStructureValues();
	}
	
	public double[][] score(String seq) throws SeqFunctionException {
		if(seq.length()<scoreWindowSize)
			throw new SeqFunctionException("Sequence too short for MGWStructureFunction");
		
		double [][] scores = new double[scoreDimension][seq.length()]; 
		String seqU = seq.toUpperCase();
		for(int i=0; i<seqU.length(); i++)
			scores[0][i]=0;
		for(int i=0; i<seqU.length()-scoreWindowSize+1; i++){
			String kmer = seqU.substring(i, i+scoreWindowSize);
			
			scores[0][i+scoringOffset]=structure.get(kmer);
		}
		
		return scores;
	}

	public int scoreDimension() {
		return scoreDimension;
	}

	public int scoringOffset() {
		return scoringOffset;
	}

	public int scoreWindowSize() {
		return scoreWindowSize;
	}

	public boolean isBetweenNucleotides() {
		return isBetweenNucs;
	}

	public String[] dimensionLabels() {
		return labels;
	}

	public String scoreDescription() {
		return description;
	}


	public double getMaxScore(){return 6.2;}
	public double getMinScore(){return 2.85;}
	
	private void loadStructureValues(){
		structure.put("AAAAA",  3.38);
		structure.put("AAAAT",  3.63);
		structure.put("AAAAG",  3.68);
		structure.put("AAAAC",  4.05);
		structure.put("AAATA",  3.79);
		structure.put("AAATT",  2.85);
		structure.put("AAATG",  3.84);
		structure.put("AAATC",  4.12);
		structure.put("AAAGA",  4.02);
		structure.put("AAAGT",  3.35);
		structure.put("AAAGG",  4.05);
		structure.put("AAAGC",  4.03);
		structure.put("AAACA",  4.65);
		structure.put("AAACT",  3.85);
		structure.put("AAACG",  4.43);
		structure.put("AAACC",  4.06);
		structure.put("AATAA",  5.53);
		structure.put("AATAT",  4.8);
		structure.put("AATAG",  4.65);
		structure.put("AATAC",  5.3);
		structure.put("AATTA",  4.36);
		structure.put("AATTT",  2.85);
		structure.put("AATTG",  4.24);
		structure.put("AATTC",  3.75);
		structure.put("AATGA",  4.46);
		structure.put("AATGT",  4.27);
		structure.put("AATGG",  5.08);
		structure.put("AATGC",  4.89);
		structure.put("AATCA",  4.46);
		structure.put("AATCT",  3.75);
		structure.put("AATCG",  4.56);
		structure.put("AATCC",  4.19);
		structure.put("AAGAA",  4.8);
		structure.put("AAGAT",  3.9);
		structure.put("AAGAG",  4.68);
		structure.put("AAGAC",  4.65);
		structure.put("AAGTA",  4.03);
		structure.put("AAGTT",  3.34);
		structure.put("AAGTG",  4.14);
		structure.put("AAGTC",  3.74);
		structure.put("AAGGA",  4.31);
		structure.put("AAGGT",  3.75);
		structure.put("AAGGG",  4.42);
		structure.put("AAGGC",  4.51);
		structure.put("AAGCA",  4.61);
		structure.put("AAGCT",  4.14);
		structure.put("AAGCG",  4.63);
		structure.put("AAGCC",  4.17);
		structure.put("AACAA",  4.97);
		structure.put("AACAT",  4.3);
		structure.put("AACAG",  4.95);
		structure.put("AACAC",  5.05);
		structure.put("AACTA",  4.24);
		structure.put("AACTT",  3.34);
		structure.put("AACTG",  4.49);
		structure.put("AACTC",  3.95);
		structure.put("AACGA",  4.8);
		structure.put("AACGT",  4.21);
		structure.put("AACGG",  4.62);
		structure.put("AACGC",  4.64);
		structure.put("AACCA",  4.33);
		structure.put("AACCT",  3.64);
		structure.put("AACCG",  4.36);
		structure.put("AACCC",  4.03);
		structure.put("ATAAA",  5.66);
		structure.put("ATAAT",  5.28);
		structure.put("ATAAG",  5.48);
		structure.put("ATAAC",  5.62);
		structure.put("ATATA",  5.76);
		structure.put("ATATT",  4.8);
		structure.put("ATATG",  5.32);
		structure.put("ATATC",  5.4);
		structure.put("ATAGA",  5.69);
		structure.put("ATAGT",  4.86);
		structure.put("ATAGG",  5.6);
		structure.put("ATAGC",  5.37);
		structure.put("ATACA",  5.82);
		structure.put("ATACT",  5.37);
		structure.put("ATACG",  5.53);
		structure.put("ATACC",  5.46);
		structure.put("ATTAA",  5.58);
		structure.put("ATTAT",  5.28);
		structure.put("ATTAG",  5.57);
		structure.put("ATTAC",  5.44);
		structure.put("ATTTA",  4.75);
		structure.put("ATTTT",  3.63);
		structure.put("ATTTG",  4.12);
		structure.put("ATTTC",  4.27);
		structure.put("ATTGA",  5.46);
		structure.put("ATTGT",  5.03);
		structure.put("ATTGG",  5.3);
		structure.put("ATTGC",  5.15);
		structure.put("ATTCA",  5.22);
		structure.put("ATTCT",  4.43);
		structure.put("ATTCG",  4.88);
		structure.put("ATTCC",  4.69);
		structure.put("ATGAA",  5.5);
		structure.put("ATGAT",  4.79);
		structure.put("ATGAG",  5.23);
		structure.put("ATGAC",  5.45);
		structure.put("ATGTA",  5.27);
		structure.put("ATGTT",  4.3);
		structure.put("ATGTG",  5.15);
		structure.put("ATGTC",  5.02);
		structure.put("ATGGA",  5.34);
		structure.put("ATGGT",  4.97);
		structure.put("ATGGG",  5.19);
		structure.put("ATGGC",  5.23);
		structure.put("ATGCA",  5.55);
		structure.put("ATGCT",  5.23);
		structure.put("ATGCG",  5.6);
		structure.put("ATGCC",  5.33);
		structure.put("ATCAA",  5.52);
		structure.put("ATCAT",  4.79);
		structure.put("ATCAG",  5.34);
		structure.put("ATCAC",  5.35);
		structure.put("ATCTA",  4.74);
		structure.put("ATCTT",  3.9);
		structure.put("ATCTG",  4.98);
		structure.put("ATCTC",  4.74);
		structure.put("ATCGA",  5.23);
		structure.put("ATCGT",  4.8);
		structure.put("ATCGG",  5.25);
		structure.put("ATCGC",  5.28);
		structure.put("ATCCA",  4.94);
		structure.put("ATCCT",  4.51);
		structure.put("ATCCG",  4.47);
		structure.put("ATCCC",  4.81);
		structure.put("AGAAA",  4.74);
		structure.put("AGAAT",  4.43);
		structure.put("AGAAG",  4.5);
		structure.put("AGAAC",  4.85);
		structure.put("AGATA",  4.72);
		structure.put("AGATT",  3.75);
		structure.put("AGATG",  4.66);
		structure.put("AGATC",  4.36);
		structure.put("AGAGA",  5.0);
		structure.put("AGAGT",  4.86);
		structure.put("AGAGG",  4.97);
		structure.put("AGAGC",  5.04);
		structure.put("AGACA",  4.99);
		structure.put("AGACT",  4.57);
		structure.put("AGACG",  4.83);
		structure.put("AGACC",  4.73);
		structure.put("AGTAA",  5.37);
		structure.put("AGTAT",  5.37);
		structure.put("AGTAG",  5.2);
		structure.put("AGTAC",  5.54);
		structure.put("AGTTA",  4.67);
		structure.put("AGTTT",  3.85);
		structure.put("AGTTG",  4.38);
		structure.put("AGTTC",  4.25);
		structure.put("AGTGA",  5.22);
		structure.put("AGTGT",  5.1);
		structure.put("AGTGG",  5.31);
		structure.put("AGTGC",  5.3);
		structure.put("AGTCA",  4.89);
		structure.put("AGTCT",  4.57);
		structure.put("AGTCG",  4.59);
		structure.put("AGTCC",  4.51);
		structure.put("AGGAA",  4.76);
		structure.put("AGGAT",  4.51);
		structure.put("AGGAG",  4.63);
		structure.put("AGGAC",  4.81);
		structure.put("AGGTA",  4.33);
		structure.put("AGGTT",  3.64);
		structure.put("AGGTG",  4.42);
		structure.put("AGGTC",  4.18);
		structure.put("AGGGA",  4.68);
		structure.put("AGGGT",  4.29);
		structure.put("AGGGG",  4.62);
		structure.put("AGGGC",  4.73);
		structure.put("AGGCA",  4.82);
		structure.put("AGGCT",  4.33);
		structure.put("AGGCG",  4.77);
		structure.put("AGGCC",  4.72);
		structure.put("AGCAA",  5.08);
		structure.put("AGCAT",  5.23);
		structure.put("AGCAG",  5.16);
		structure.put("AGCAC",  5.43);
		structure.put("AGCTA",  4.88);
		structure.put("AGCTT",  4.14);
		structure.put("AGCTG",  4.8);
		structure.put("AGCTC",  4.63);
		structure.put("AGCGA",  5.19);
		structure.put("AGCGT",  4.86);
		structure.put("AGCGG",  5.15);
		structure.put("AGCGC",  5.18);
		structure.put("AGCCA",  4.53);
		structure.put("AGCCT",  4.33);
		structure.put("AGCCG",  4.83);
		structure.put("AGCCC",  4.61);
		structure.put("ACAAA",  5.21);
		structure.put("ACAAT",  5.03);
		structure.put("ACAAG",  5.19);
		structure.put("ACAAC",  5.58);
		structure.put("ACATA",  5.14);
		structure.put("ACATT",  4.27);
		structure.put("ACATG",  4.99);
		structure.put("ACATC",  4.82);
		structure.put("ACAGA",  5.33);
		structure.put("ACAGT",  5.2);
		structure.put("ACAGG",  5.29);
		structure.put("ACAGC",  5.31);
		structure.put("ACACA",  5.5);
		structure.put("ACACT",  5.1);
		structure.put("ACACG",  5.47);
		structure.put("ACACC",  5.31);
		structure.put("ACTAA",  5.73);
		structure.put("ACTAT",  4.86);
		structure.put("ACTAG",  5.13);
		structure.put("ACTAC",  5.62);
		structure.put("ACTTA",  4.48);
		structure.put("ACTTT",  3.35);
		structure.put("ACTTG",  4.52);
		structure.put("ACTTC",  4.31);
		structure.put("ACTGA",  5.48);
		structure.put("ACTGT",  5.2);
		structure.put("ACTGG",  5.32);
		structure.put("ACTGC",  5.4);
		structure.put("ACTCA",  4.89);
		structure.put("ACTCT",  4.86);
		structure.put("ACTCG",  4.93);
		structure.put("ACTCC",  4.73);
		structure.put("ACGAA",  5.21);
		structure.put("ACGAT",  4.8);
		structure.put("ACGAG",  5.13);
		structure.put("ACGAC",  5.34);
		structure.put("ACGTA",  4.84);
		structure.put("ACGTT",  4.21);
		structure.put("ACGTG",  4.85);
		structure.put("ACGTC",  4.7);
		structure.put("ACGGA",  5.06);
		structure.put("ACGGT",  4.73);
		structure.put("ACGGG",  4.92);
		structure.put("ACGGC",  5.08);
		structure.put("ACGCA",  5.27);
		structure.put("ACGCT",  4.86);
		structure.put("ACGCG",  5.2);
		structure.put("ACGCC",  5.08);
		structure.put("ACCAA",  5.08);
		structure.put("ACCAT",  4.97);
		structure.put("ACCAG",  5.19);
		structure.put("ACCAC",  5.38);
		structure.put("ACCTA",  4.68);
		structure.put("ACCTT",  3.75);
		structure.put("ACCTG",  4.54);
		structure.put("ACCTC",  4.37);
		structure.put("ACCGA",  5.02);
		structure.put("ACCGT",  4.73);
		structure.put("ACCGG",  5.16);
		structure.put("ACCGC",  5.08);
		structure.put("ACCCA",  4.66);
		structure.put("ACCCT",  4.29);
		structure.put("ACCCG",  4.7);
		structure.put("ACCCC",  4.58);
		structure.put("TAAAA",  4.89);
		structure.put("TAAAT",  4.75);
		structure.put("TAAAG",  4.7);
		structure.put("TAAAC",  5.1);
		structure.put("TAATA",  5.11);
		structure.put("TAATT",  4.36);
		structure.put("TAATG",  4.9);
		structure.put("TAATC",  4.81);
		structure.put("TAAGA",  4.93);
		structure.put("TAAGT",  4.48);
		structure.put("TAAGG",  4.81);
		structure.put("TAAGC",  5.01);
		structure.put("TAACA",  5.17);
		structure.put("TAACT",  4.67);
		structure.put("TAACG",  5.15);
		structure.put("TAACC",  4.87);
		structure.put("TATAA",  6.07);
		structure.put("TATAT",  5.76);
		structure.put("TATAG",  5.79);
		structure.put("TATAC",  6.01);
		structure.put("TATTA",  5.11);
		structure.put("TATTT",  3.79);
		structure.put("TATTG",  5.02);
		structure.put("TATTC",  4.51);
		structure.put("TATGA",  5.84);
		structure.put("TATGT",  5.14);
		structure.put("TATGG",  5.88);
		structure.put("TATGC",  5.76);
		structure.put("TATCA",  5.38);
		structure.put("TATCT",  4.72);
		structure.put("TATCG",  5.32);
		structure.put("TATCC",  4.98);
		structure.put("TAGAA",  5.33);
		structure.put("TAGAT",  4.74);
		structure.put("TAGAG",  5.41);
		structure.put("TAGAC",  5.32);
		structure.put("TAGTA",  5.33);
		structure.put("TAGTT",  4.24);
		structure.put("TAGTG",  5.11);
		structure.put("TAGTC",  5.08);
		structure.put("TAGGA",  4.98);
		structure.put("TAGGT",  4.68);
		structure.put("TAGGG",  4.99);
		structure.put("TAGGC",  4.97);
		structure.put("TAGCA",  5.36);
		structure.put("TAGCT",  4.88);
		structure.put("TAGCG",  5.22);
		structure.put("TAGCC",  5.0);
		structure.put("TACAA",  5.89);
		structure.put("TACAT",  5.27);
		structure.put("TACAG",  5.41);
		structure.put("TACAC",  5.74);
		structure.put("TACTA",  5.33);
		structure.put("TACTT",  4.03);
		structure.put("TACTG",  5.13);
		structure.put("TACTC",  4.69);
		structure.put("TACGA",  5.61);
		structure.put("TACGT",  4.84);
		structure.put("TACGG",  5.33);
		structure.put("TACGC",  5.32);
		structure.put("TACCA",  5.13);
		structure.put("TACCT",  4.33);
		structure.put("TACCG",  4.96);
		structure.put("TACCC",  4.71);
		structure.put("TTAAA",  5.73);
		structure.put("TTAAT",  5.58);
		structure.put("TTAAG",  5.58);
		structure.put("TTAAC",  5.85);
		structure.put("TTATA",  6.07);
		structure.put("TTATT",  5.53);
		structure.put("TTATG",  6.02);
		structure.put("TTATC",  5.89);
		structure.put("TTAGA",  6.0);
		structure.put("TTAGT",  5.73);
		structure.put("TTAGG",  5.96);
		structure.put("TTAGC",  5.82);
		structure.put("TTACA",  5.99);
		structure.put("TTACT",  5.37);
		structure.put("TTACG",  6.11);
		structure.put("TTACC",  5.43);
		structure.put("TTTAA",  5.73);
		structure.put("TTTAT",  5.66);
		structure.put("TTTAG",  5.82);
		structure.put("TTTAC",  5.91);
		structure.put("TTTTA",  4.89);
		structure.put("TTTTT",  3.38);
		structure.put("TTTTG",  4.76);
		structure.put("TTTTC",  4.35);
		structure.put("TTTGA",  5.6);
		structure.put("TTTGT",  5.21);
		structure.put("TTTGG",  5.42);
		structure.put("TTTGC",  5.41);
		structure.put("TTTCA",  5.4);
		structure.put("TTTCT",  4.74);
		structure.put("TTTCG",  4.98);
		structure.put("TTTCC",  4.63);
		structure.put("TTGAA",  6.0);
		structure.put("TTGAT",  5.52);
		structure.put("TTGAG",  5.9);
		structure.put("TTGAC",  5.91);
		structure.put("TTGTA",  5.89);
		structure.put("TTGTT",  4.97);
		structure.put("TTGTG",  5.2);
		structure.put("TTGTC",  5.57);
		structure.put("TTGGA",  5.42);
		structure.put("TTGGT",  5.08);
		structure.put("TTGGG",  5.44);
		structure.put("TTGGC",  5.56);
		structure.put("TTGCA",  5.78);
		structure.put("TTGCT",  5.08);
		structure.put("TTGCG",  5.81);
		structure.put("TTGCC",  5.3);
		structure.put("TTCAA",  6.0);
		structure.put("TTCAT",  5.5);
		structure.put("TTCAG",  5.93);
		structure.put("TTCAC",  5.94);
		structure.put("TTCTA",  5.33);
		structure.put("TTCTT",  4.8);
		structure.put("TTCTG",  5.14);
		structure.put("TTCTC",  4.67);
		structure.put("TTCGA",  5.75);
		structure.put("TTCGT",  5.21);
		structure.put("TTCGG",  5.38);
		structure.put("TTCGC",  5.53);
		structure.put("TTCCA",  4.97);
		structure.put("TTCCT",  4.76);
		structure.put("TTCCG",  5.09);
		structure.put("TTCCC",  4.73);
		structure.put("TGAAA",  5.4);
		structure.put("TGAAT",  5.22);
		structure.put("TGAAG",  5.15);
		structure.put("TGAAC",  5.35);
		structure.put("TGATA",  5.38);
		structure.put("TGATT",  4.46);
		structure.put("TGATG",  5.22);
		structure.put("TGATC",  4.77);
		structure.put("TGAGA",  5.29);
		structure.put("TGAGT",  4.89);
		structure.put("TGAGG",  5.3);
		structure.put("TGAGC",  5.4);
		structure.put("TGACA",  5.43);
		structure.put("TGACT",  4.89);
		structure.put("TGACG",  5.39);
		structure.put("TGACC",  4.94);
		structure.put("TGTAA",  5.99);
		structure.put("TGTAT",  5.82);
		structure.put("TGTAG",  5.74);
		structure.put("TGTAC",  6.2);
		structure.put("TGTTA",  5.17);
		structure.put("TGTTT",  4.65);
		structure.put("TGTTG",  5.24);
		structure.put("TGTTC",  4.85);
		structure.put("TGTGA",  5.84);
		structure.put("TGTGT",  5.5);
		structure.put("TGTGG",  5.8);
		structure.put("TGTGC",  5.94);
		structure.put("TGTCA",  5.43);
		structure.put("TGTCT",  4.99);
		structure.put("TGTCG",  5.28);
		structure.put("TGTCC",  5.02);
		structure.put("TGGAA",  4.97);
		structure.put("TGGAT",  4.94);
		structure.put("TGGAG",  5.02);
		structure.put("TGGAC",  5.19);
		structure.put("TGGTA",  5.13);
		structure.put("TGGTT",  4.33);
		structure.put("TGGTG",  4.84);
		structure.put("TGGTC",  4.84);
		structure.put("TGGGA",  4.93);
		structure.put("TGGGT",  4.66);
		structure.put("TGGGG",  4.89);
		structure.put("TGGGC",  4.96);
		structure.put("TGGCA",  5.24);
		structure.put("TGGCT",  4.53);
		structure.put("TGGCG",  5.06);
		structure.put("TGGCC",  4.93);
		structure.put("TGCAA",  5.78);
		structure.put("TGCAT",  5.55);
		structure.put("TGCAG",  5.79);
		structure.put("TGCAC",  5.94);
		structure.put("TGCTA",  5.36);
		structure.put("TGCTT",  4.61);
		structure.put("TGCTG",  5.27);
		structure.put("TGCTC",  5.1);
		structure.put("TGCGA",  5.68);
		structure.put("TGCGT",  5.27);
		structure.put("TGCGG",  5.59);
		structure.put("TGCGC",  5.71);
		structure.put("TGCCA",  5.24);
		structure.put("TGCCT",  4.82);
		structure.put("TGCCG",  5.17);
		structure.put("TGCCC",  5.02);
		structure.put("TCAAA",  5.6);
		structure.put("TCAAT",  5.46);
		structure.put("TCAAG",  5.4);
		structure.put("TCAAC",  5.65);
		structure.put("TCATA",  5.84);
		structure.put("TCATT",  4.46);
		structure.put("TCATG",  5.27);
		structure.put("TCATC",  5.08);
		structure.put("TCAGA",  5.47);
		structure.put("TCAGT",  5.48);
		structure.put("TCAGG",  5.47);
		structure.put("TCAGC",  5.74);
		structure.put("TCACA",  5.84);
		structure.put("TCACT",  5.22);
		structure.put("TCACG",  5.71);
		structure.put("TCACC",  5.4);
		structure.put("TCTAA",  6.0);
		structure.put("TCTAT",  5.69);
		structure.put("TCTAG",  5.84);
		structure.put("TCTAC",  5.76);
		structure.put("TCTTA",  4.93);
		structure.put("TCTTT",  4.02);
		structure.put("TCTTG",  4.92);
		structure.put("TCTTC",  4.52);
		structure.put("TCTGA",  5.47);
		structure.put("TCTGT",  5.33);
		structure.put("TCTGG",  5.56);
		structure.put("TCTGC",  5.75);
		structure.put("TCTCA",  5.29);
		structure.put("TCTCT",  5.0);
		structure.put("TCTCG",  5.17);
		structure.put("TCTCC",  4.92);
		structure.put("TCGAA",  5.75);
		structure.put("TCGAT",  5.23);
		structure.put("TCGAG",  5.42);
		structure.put("TCGAC",  5.64);
		structure.put("TCGTA",  5.61);
		structure.put("TCGTT",  4.8);
		structure.put("TCGTG",  5.29);
		structure.put("TCGTC",  5.16);
		structure.put("TCGGA",  5.36);
		structure.put("TCGGT",  5.02);
		structure.put("TCGGG",  5.22);
		structure.put("TCGGC",  5.43);
		structure.put("TCGCA",  5.68);
		structure.put("TCGCT",  5.19);
		structure.put("TCGCG",  5.42);
		structure.put("TCGCC",  5.4);
		structure.put("TCCAA",  5.42);
		structure.put("TCCAT",  5.34);
		structure.put("TCCAG",  5.31);
		structure.put("TCCAC",  5.58);
		structure.put("TCCTA",  4.98);
		structure.put("TCCTT",  4.31);
		structure.put("TCCTG",  4.92);
		structure.put("TCCTC",  4.8);
		structure.put("TCCGA",  5.36);
		structure.put("TCCGT",  5.06);
		structure.put("TCCGG",  5.39);
		structure.put("TCCGC",  5.27);
		structure.put("TCCCA",  4.93);
		structure.put("TCCCT",  4.68);
		structure.put("TCCCG",  4.95);
		structure.put("TCCCC",  4.76);
		structure.put("GAAAA",  4.35);
		structure.put("GAAAT",  4.27);
		structure.put("GAAAG",  4.36);
		structure.put("GAAAC",  4.74);
		structure.put("GAATA",  4.51);
		structure.put("GAATT",  3.75);
		structure.put("GAATG",  4.39);
		structure.put("GAATC",  4.36);
		structure.put("GAAGA",  4.52);
		structure.put("GAAGT",  4.31);
		structure.put("GAAGG",  4.63);
		structure.put("GAAGC",  4.82);
		structure.put("GAACA",  4.85);
		structure.put("GAACT",  4.25);
		structure.put("GAACG",  4.74);
		structure.put("GAACC",  4.46);
		structure.put("GATAA",  5.89);
		structure.put("GATAT",  5.4);
		structure.put("GATAG",  5.54);
		structure.put("GATAC",  5.65);
		structure.put("GATTA",  4.81);
		structure.put("GATTT",  4.12);
		structure.put("GATTG",  4.81);
		structure.put("GATTC",  4.36);
		structure.put("GATGA",  5.08);
		structure.put("GATGT",  4.82);
		structure.put("GATGG",  5.53);
		structure.put("GATGC",  5.38);
		structure.put("GATCA",  4.77);
		structure.put("GATCT",  4.36);
		structure.put("GATCG",  4.96);
		structure.put("GATCC",  4.63);
		structure.put("GAGAA",  4.67);
		structure.put("GAGAT",  4.74);
		structure.put("GAGAG",  4.93);
		structure.put("GAGAC",  5.04);
		structure.put("GAGTA",  4.69);
		structure.put("GAGTT",  3.95);
		structure.put("GAGTG",  4.78);
		structure.put("GAGTC",  4.53);
		structure.put("GAGGA",  4.8);
		structure.put("GAGGT",  4.37);
		structure.put("GAGGG",  4.78);
		structure.put("GAGGC",  4.85);
		structure.put("GAGCA",  5.1);
		structure.put("GAGCT",  4.63);
		structure.put("GAGCG",  5.02);
		structure.put("GAGCC",  4.67);
		structure.put("GACAA",  5.57);
		structure.put("GACAT",  5.02);
		structure.put("GACAG",  5.26);
		structure.put("GACAC",  5.49);
		structure.put("GACTA",  5.08);
		structure.put("GACTT",  3.74);
		structure.put("GACTG",  5.0);
		structure.put("GACTC",  4.53);
		structure.put("GACGA",  5.16);
		structure.put("GACGT",  4.7);
		structure.put("GACGG",  5.1);
		structure.put("GACGC",  5.09);
		structure.put("GACCA",  4.84);
		structure.put("GACCT",  4.18);
		structure.put("GACCG",  4.69);
		structure.put("GACCC",  4.45);
		structure.put("GTAAA",  5.91);
		structure.put("GTAAT",  5.44);
		structure.put("GTAAG",  5.62);
		structure.put("GTAAC",  5.86);
		structure.put("GTATA",  6.01);
		structure.put("GTATT",  5.3);
		structure.put("GTATG",  5.93);
		structure.put("GTATC",  5.65);
		structure.put("GTAGA",  5.76);
		structure.put("GTAGT",  5.62);
		structure.put("GTAGG",  5.76);
		structure.put("GTAGC",  5.88);
		structure.put("GTACA",  6.2);
		structure.put("GTACT",  5.54);
		structure.put("GTACG",  5.93);
		structure.put("GTACC",  5.84);
		structure.put("GTTAA",  5.85);
		structure.put("GTTAT",  5.62);
		structure.put("GTTAG",  5.8);
		structure.put("GTTAC",  5.86);
		structure.put("GTTTA",  5.1);
		structure.put("GTTTT",  4.05);
		structure.put("GTTTG",  4.95);
		structure.put("GTTTC",  4.74);
		structure.put("GTTGA",  5.65);
		structure.put("GTTGT",  5.58);
		structure.put("GTTGG",  5.42);
		structure.put("GTTGC",  5.7);
		structure.put("GTTCA",  5.35);
		structure.put("GTTCT",  4.85);
		structure.put("GTTCG",  5.1);
		structure.put("GTTCC",  4.84);
		structure.put("GTGAA",  5.94);
		structure.put("GTGAT",  5.35);
		structure.put("GTGAG",  5.8);
		structure.put("GTGAC",  6.01);
		structure.put("GTGTA",  5.74);
		structure.put("GTGTT",  5.05);
		structure.put("GTGTG",  5.73);
		structure.put("GTGTC",  5.49);
		structure.put("GTGGA",  5.58);
		structure.put("GTGGT",  5.38);
		structure.put("GTGGG",  5.41);
		structure.put("GTGGC",  5.46);
		structure.put("GTGCA",  5.94);
		structure.put("GTGCT",  5.43);
		structure.put("GTGCG",  5.88);
		structure.put("GTGCC",  5.66);
		structure.put("GTCAA",  5.91);
		structure.put("GTCAT",  5.45);
		structure.put("GTCAG",  5.67);
		structure.put("GTCAC",  6.01);
		structure.put("GTCTA",  5.32);
		structure.put("GTCTT",  4.65);
		structure.put("GTCTG",  5.22);
		structure.put("GTCTC",  5.04);
		structure.put("GTCGA",  5.64);
		structure.put("GTCGT",  5.34);
		structure.put("GTCGG",  5.54);
		structure.put("GTCGC",  5.56);
		structure.put("GTCCA",  5.19);
		structure.put("GTCCT",  4.81);
		structure.put("GTCCG",  5.08);
		structure.put("GTCCC",  4.9);
		structure.put("GGAAA",  4.63);
		structure.put("GGAAT",  4.69);
		structure.put("GGAAG",  4.63);
		structure.put("GGAAC",  4.84);
		structure.put("GGATA",  4.98);
		structure.put("GGATT",  4.19);
		structure.put("GGATG",  4.84);
		structure.put("GGATC",  4.63);
		structure.put("GGAGA",  4.92);
		structure.put("GGAGT",  4.73);
		structure.put("GGAGG",  4.93);
		structure.put("GGAGC",  5.05);
		structure.put("GGACA",  5.02);
		structure.put("GGACT",  4.51);
		structure.put("GGACG",  5.02);
		structure.put("GGACC",  4.8);
		structure.put("GGTAA",  5.43);
		structure.put("GGTAT",  5.46);
		structure.put("GGTAG",  5.55);
		structure.put("GGTAC",  5.84);
		structure.put("GGTTA",  4.87);
		structure.put("GGTTT",  4.06);
		structure.put("GGTTG",  4.82);
		structure.put("GGTTC",  4.46);
		structure.put("GGTGA",  5.4);
		structure.put("GGTGT",  5.31);
		structure.put("GGTGG",  5.56);
		structure.put("GGTGC",  5.56);
		structure.put("GGTCA",  4.94);
		structure.put("GGTCT",  4.73);
		structure.put("GGTCG",  4.82);
		structure.put("GGTCC",  4.8);
		structure.put("GGGAA",  4.73);
		structure.put("GGGAT",  4.81);
		structure.put("GGGAG",  4.85);
		structure.put("GGGAC",  4.9);
		structure.put("GGGTA",  4.71);
		structure.put("GGGTT",  4.03);
		structure.put("GGGTG",  4.77);
		structure.put("GGGTC",  4.45);
		structure.put("GGGGA",  4.76);
		structure.put("GGGGT",  4.58);
		structure.put("GGGGG",  4.75);
		structure.put("GGGGC",  4.82);
		structure.put("GGGCA",  5.02);
		structure.put("GGGCT",  4.61);
		structure.put("GGGCG",  4.96);
		structure.put("GGGCC",  4.77);
		structure.put("GGCAA",  5.3);
		structure.put("GGCAT",  5.33);
		structure.put("GGCAG",  5.41);
		structure.put("GGCAC",  5.66);
		structure.put("GGCTA",  5.0);
		structure.put("GGCTT",  4.17);
		structure.put("GGCTG",  4.95);
		structure.put("GGCTC",  4.67);
		structure.put("GGCGA",  5.4);
		structure.put("GGCGT",  5.08);
		structure.put("GGCGG",  5.29);
		structure.put("GGCGC",  5.41);
		structure.put("GGCCA",  4.93);
		structure.put("GGCCT",  4.72);
		structure.put("GGCCG",  4.96);
		structure.put("GGCCC",  4.77);
		structure.put("GCAAA",  5.41);
		structure.put("GCAAT",  5.15);
		structure.put("GCAAG",  5.45);
		structure.put("GCAAC",  5.7);
		structure.put("GCATA",  5.76);
		structure.put("GCATT",  4.89);
		structure.put("GCATG",  5.5);
		structure.put("GCATC",  5.38);
		structure.put("GCAGA",  5.75);
		structure.put("GCAGT",  5.4);
		structure.put("GCAGG",  5.52);
		structure.put("GCAGC",  5.67);
		structure.put("GCACA",  5.94);
		structure.put("GCACT",  5.3);
		structure.put("GCACG",  5.74);
		structure.put("GCACC",  5.56);
		structure.put("GCTAA",  5.82);
		structure.put("GCTAT",  5.37);
		structure.put("GCTAG",  5.67);
		structure.put("GCTAC",  5.88);
		structure.put("GCTTA",  5.01);
		structure.put("GCTTT",  4.03);
		structure.put("GCTTG",  4.94);
		structure.put("GCTTC",  4.82);
		structure.put("GCTGA",  5.74);
		structure.put("GCTGT",  5.31);
		structure.put("GCTGG",  5.53);
		structure.put("GCTGC",  5.67);
		structure.put("GCTCA",  5.4);
		structure.put("GCTCT",  5.04);
		structure.put("GCTCG",  5.2);
		structure.put("GCTCC",  5.05);
		structure.put("GCGAA",  5.53);
		structure.put("GCGAT",  5.28);
		structure.put("GCGAG",  5.46);
		structure.put("GCGAC",  5.56);
		structure.put("GCGTA",  5.32);
		structure.put("GCGTT",  4.64);
		structure.put("GCGTG",  5.25);
		structure.put("GCGTC",  5.09);
		structure.put("GCGGA",  5.27);
		structure.put("GCGGT",  5.08);
		structure.put("GCGGG",  5.3);
		structure.put("GCGGC",  5.37);
		structure.put("GCGCA",  5.71);
		structure.put("GCGCT",  5.18);
		structure.put("GCGCG",  5.54);
		structure.put("GCGCC",  5.41);
		structure.put("GCCAA",  5.56);
		structure.put("GCCAT",  5.23);
		structure.put("GCCAG",  5.43);
		structure.put("GCCAC",  5.46);
		structure.put("GCCTA",  4.97);
		structure.put("GCCTT",  4.51);
		structure.put("GCCTG",  4.94);
		structure.put("GCCTC",  4.85);
		structure.put("GCCGA",  5.43);
		structure.put("GCCGT",  5.08);
		structure.put("GCCGG",  5.36);
		structure.put("GCCGC",  5.37);
		structure.put("GCCCA",  4.96);
		structure.put("GCCCT",  4.73);
		structure.put("GCCCG",  4.95);
		structure.put("GCCCC",  4.82);
		structure.put("CAAAA",  4.76);
		structure.put("CAAAT",  4.12);
		structure.put("CAAAG",  4.52);
		structure.put("CAAAC",  4.95);
		structure.put("CAATA",  5.02);
		structure.put("CAATT",  4.24);
		structure.put("CAATG",  4.69);
		structure.put("CAATC",  4.81);
		structure.put("CAAGA",  4.92);
		structure.put("CAAGT",  4.52);
		structure.put("CAAGG",  4.65);
		structure.put("CAAGC",  4.94);
		structure.put("CAACA",  5.24);
		structure.put("CAACT",  4.38);
		structure.put("CAACG",  4.98);
		structure.put("CAACC",  4.82);
		structure.put("CATAA",  6.02);
		structure.put("CATAT",  5.32);
		structure.put("CATAG",  5.53);
		structure.put("CATAC",  5.93);
		structure.put("CATTA",  4.9);
		structure.put("CATTT",  3.84);
		structure.put("CATTG",  4.69);
		structure.put("CATTC",  4.39);
		structure.put("CATGA",  5.27);
		structure.put("CATGT",  4.99);
		structure.put("CATGG",  5.34);
		structure.put("CATGC",  5.5);
		structure.put("CATCA",  5.22);
		structure.put("CATCT",  4.66);
		structure.put("CATCG",  4.94);
		structure.put("CATCC",  4.84);
		structure.put("CAGAA",  5.14);
		structure.put("CAGAT",  4.98);
		structure.put("CAGAG",  5.14);
		structure.put("CAGAC",  5.22);
		structure.put("CAGTA",  5.13);
		structure.put("CAGTT",  4.49);
		structure.put("CAGTG",  4.98);
		structure.put("CAGTC",  5.0);
		structure.put("CAGGA",  4.92);
		structure.put("CAGGT",  4.54);
		structure.put("CAGGG",  4.85);
		structure.put("CAGGC",  4.94);
		structure.put("CAGCA",  5.27);
		structure.put("CAGCT",  4.8);
		structure.put("CAGCG",  5.13);
		structure.put("CAGCC",  4.95);
		structure.put("CACAA",  5.2);
		structure.put("CACAT",  5.15);
		structure.put("CACAG",  5.32);
		structure.put("CACAC",  5.73);
		structure.put("CACTA",  5.11);
		structure.put("CACTT",  4.14);
		structure.put("CACTG",  4.98);
		structure.put("CACTC",  4.78);
		structure.put("CACGA",  5.29);
		structure.put("CACGT",  4.85);
		structure.put("CACGG",  5.1);
		structure.put("CACGC",  5.25);
		structure.put("CACCA",  4.84);
		structure.put("CACCT",  4.42);
		structure.put("CACCG",  4.92);
		structure.put("CACCC",  4.77);
		structure.put("CTAAA",  5.82);
		structure.put("CTAAT",  5.57);
		structure.put("CTAAG",  5.49);
		structure.put("CTAAC",  5.8);
		structure.put("CTATA",  5.79);
		structure.put("CTATT",  4.65);
		structure.put("CTATG",  5.53);
		structure.put("CTATC",  5.54);
		structure.put("CTAGA",  5.84);
		structure.put("CTAGT",  5.13);
		structure.put("CTAGG",  5.69);
		structure.put("CTAGC",  5.67);
		structure.put("CTACA",  5.74);
		structure.put("CTACT",  5.2);
		structure.put("CTACG",  5.77);
		structure.put("CTACC",  5.55);
		structure.put("CTTAA",  5.58);
		structure.put("CTTAT",  5.48);
		structure.put("CTTAG",  5.49);
		structure.put("CTTAC",  5.62);
		structure.put("CTTTA",  4.7);
		structure.put("CTTTT",  3.68);
		structure.put("CTTTG",  4.52);
		structure.put("CTTTC",  4.36);
		structure.put("CTTGA",  5.4);
		structure.put("CTTGT",  5.19);
		structure.put("CTTGG",  5.31);
		structure.put("CTTGC",  5.45);
		structure.put("CTTCA",  5.15);
		structure.put("CTTCT",  4.5);
		structure.put("CTTCG",  4.92);
		structure.put("CTTCC",  4.63);
		structure.put("CTGAA",  5.93);
		structure.put("CTGAT",  5.34);
		structure.put("CTGAG",  5.56);
		structure.put("CTGAC",  5.67);
		structure.put("CTGTA",  5.41);
		structure.put("CTGTT",  4.95);
		structure.put("CTGTG",  5.32);
		structure.put("CTGTC",  5.26);
		structure.put("CTGGA",  5.31);
		structure.put("CTGGT",  5.19);
		structure.put("CTGGG",  5.39);
		structure.put("CTGGC",  5.43);
		structure.put("CTGCA",  5.79);
		structure.put("CTGCT",  5.16);
		structure.put("CTGCG",  5.67);
		structure.put("CTGCC",  5.41);
		structure.put("CTCAA",  5.9);
		structure.put("CTCAT",  5.23);
		structure.put("CTCAG",  5.56);
		structure.put("CTCAC",  5.8);
		structure.put("CTCTA",  5.41);
		structure.put("CTCTT",  4.68);
		structure.put("CTCTG",  5.14);
		structure.put("CTCTC",  4.93);
		structure.put("CTCGA",  5.42);
		structure.put("CTCGT",  5.13);
		structure.put("CTCGG",  5.65);
		structure.put("CTCGC",  5.46);
		structure.put("CTCCA",  5.02);
		structure.put("CTCCT",  4.63);
		structure.put("CTCCG",  5.01);
		structure.put("CTCCC",  4.85);
		structure.put("CGAAA",  4.98);
		structure.put("CGAAT",  4.88);
		structure.put("CGAAG",  4.92);
		structure.put("CGAAC",  5.1);
		structure.put("CGATA",  5.32);
		structure.put("CGATT",  4.56);
		structure.put("CGATG",  4.94);
		structure.put("CGATC",  4.96);
		structure.put("CGAGA",  5.17);
		structure.put("CGAGT",  4.93);
		structure.put("CGAGG",  5.18);
		structure.put("CGAGC",  5.2);
		structure.put("CGACA",  5.28);
		structure.put("CGACT",  4.59);
		structure.put("CGACG",  5.19);
		structure.put("CGACC",  4.82);
		structure.put("CGTAA",  6.11);
		structure.put("CGTAT",  5.53);
		structure.put("CGTAG",  5.77);
		structure.put("CGTAC",  5.93);
		structure.put("CGTTA",  5.15);
		structure.put("CGTTT",  4.43);
		structure.put("CGTTG",  4.98);
		structure.put("CGTTC",  4.74);
		structure.put("CGTGA",  5.71);
		structure.put("CGTGT",  5.47);
		structure.put("CGTGG",  5.64);
		structure.put("CGTGC",  5.74);
		structure.put("CGTCA",  5.39);
		structure.put("CGTCT",  4.83);
		structure.put("CGTCG",  5.19);
		structure.put("CGTCC",  5.02);
		structure.put("CGGAA",  5.09);
		structure.put("CGGAT",  4.47);
		structure.put("CGGAG",  5.01);
		structure.put("CGGAC",  5.08);
		structure.put("CGGTA",  4.96);
		structure.put("CGGTT",  4.36);
		structure.put("CGGTG",  4.92);
		structure.put("CGGTC",  4.69);
		structure.put("CGGGA",  4.95);
		structure.put("CGGGT",  4.7);
		structure.put("CGGGG",  4.94);
		structure.put("CGGGC",  4.95);
		structure.put("CGGCA",  5.17);
		structure.put("CGGCT",  4.83);
		structure.put("CGGCG",  5.09);
		structure.put("CGGCC",  4.96);
		structure.put("CGCAA",  5.81);
		structure.put("CGCAT",  5.6);
		structure.put("CGCAG",  5.67);
		structure.put("CGCAC",  5.88);
		structure.put("CGCTA",  5.22);
		structure.put("CGCTT",  4.63);
		structure.put("CGCTG",  5.13);
		structure.put("CGCTC",  5.02);
		structure.put("CGCGA",  5.42);
		structure.put("CGCGT",  5.2);
		structure.put("CGCGG",  5.51);
		structure.put("CGCGC",  5.54);
		structure.put("CGCCA",  5.06);
		structure.put("CGCCT",  4.77);
		structure.put("CGCCG",  5.09);
		structure.put("CGCCC",  4.96);
		structure.put("CCAAA",  5.42);
		structure.put("CCAAT",  5.3);
		structure.put("CCAAG",  5.31);
		structure.put("CCAAC",  5.42);
		structure.put("CCATA",  5.88);
		structure.put("CCATT",  5.08);
		structure.put("CCATG",  5.34);
		structure.put("CCATC",  5.53);
		structure.put("CCAGA",  5.56);
		structure.put("CCAGT",  5.32);
		structure.put("CCAGG",  5.4);
		structure.put("CCAGC",  5.53);
		structure.put("CCACA",  5.8);
		structure.put("CCACT",  5.31);
		structure.put("CCACG",  5.64);
		structure.put("CCACC",  5.56);
		structure.put("CCTAA",  5.96);
		structure.put("CCTAT",  5.6);
		structure.put("CCTAG",  5.69);
		structure.put("CCTAC",  5.76);
		structure.put("CCTTA",  4.81);
		structure.put("CCTTT",  4.05);
		structure.put("CCTTG",  4.65);
		structure.put("CCTTC",  4.63);
		structure.put("CCTGA",  5.47);
		structure.put("CCTGT",  5.29);
		structure.put("CCTGG",  5.4);
		structure.put("CCTGC",  5.52);
		structure.put("CCTCA",  5.3);
		structure.put("CCTCT",  4.97);
		structure.put("CCTCG",  5.18);
		structure.put("CCTCC",  4.93);
		structure.put("CCGAA",  5.38);
		structure.put("CCGAT",  5.25);
		structure.put("CCGAG",  5.65);
		structure.put("CCGAC",  5.54);
		structure.put("CCGTA",  5.33);
		structure.put("CCGTT",  4.62);
		structure.put("CCGTG",  5.1);
		structure.put("CCGTC",  5.1);
		structure.put("CCGGA",  5.39);
		structure.put("CCGGT",  5.16);
		structure.put("CCGGG",  5.19);
		structure.put("CCGGC",  5.36);
		structure.put("CCGCA",  5.59);
		structure.put("CCGCT",  5.15);
		structure.put("CCGCG",  5.51);
		structure.put("CCGCC",  5.29);
		structure.put("CCCAA",  5.44);
		structure.put("CCCAT",  5.19);
		structure.put("CCCAG",  5.39);
		structure.put("CCCAC",  5.41);
		structure.put("CCCTA",  4.99);
		structure.put("CCCTT",  4.42);
		structure.put("CCCTG",  4.85);
		structure.put("CCCTC",  4.78);
		structure.put("CCCGA",  5.22);
		structure.put("CCCGT",  4.92);
		structure.put("CCCGG",  5.19);
		structure.put("CCCGC",  5.3);
		structure.put("CCCCA",  4.89);
		structure.put("CCCCT",  4.62);
		structure.put("CCCCG",  4.94);
		structure.put("CCCCC",  4.75);
	}
}
