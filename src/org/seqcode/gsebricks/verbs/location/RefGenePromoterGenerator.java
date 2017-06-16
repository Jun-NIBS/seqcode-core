package org.seqcode.gsebricks.verbs.location;

import java.util.*;
import java.sql.*;

import org.seqcode.genome.Genome;
import org.seqcode.genome.location.Gene;
import org.seqcode.genome.location.Region;

/**
 * Generator that returns Gene objects from the refGene table (or table with a
 * similar structure, eg sgdGene) in a UCSC annotation database. The size
 * parameter determines the size of the upstream promoter region to use. Any
 * gene whose promoter region overlaps the input region is returned
 */

public class RefGenePromoterGenerator extends RefGeneGenerator {
	private int up, down;

	public RefGenePromoterGenerator(Genome g, int size) {
		super(g);
		this.up = size;
		this.down = size;
	}

	public RefGenePromoterGenerator(Genome g, String t, int size) {
		super(g, t);
		this.up = size;
		this.down = size;
	}

	public RefGenePromoterGenerator(Genome g, int up, int down) {
		super(g);
		this.up = up;
		this.down = down;
	}

	public RefGenePromoterGenerator(Genome g, String t, int up, int down) {
		super(g, t);
		this.up = up;
		this.down = down;
	}

	public Iterator<Gene> execute(Region region) {
		try {
			java.sql.Connection cxn = getGenome().getAnnotationDBConnection();
			String query = "select name, chrom, strand, txStart, txEnd "
					+ (isRetrievingExons() ? ",exonCount, exonStarts, exonEnds" : "") + " from " + getTable()
					+ " where chrom = ? and " + "((strand = '+' and ((txStart - " + up + "  <= ? and txStart + " + down
					+ " >= ?) or (txStart - " + up + " >= ? and txStart - " + up + "<= ?))) or "
					+ " (strand = '-' and ((txEnd - " + down + "  <= ? and txEnd + " + up + " >= ?) or (txEnd - " + down
					+ " >= ? and txEnd - " + down + "<= ?)))) " + "order by txStart";
			PreparedStatement ps = cxn.prepareStatement(query);
			String chr = region.getChrom();
			if (!chr.matches("^(chr|scaffold).*")) {
				chr = "chr" + chr;
			}
			ps.setString(1, chr);
			ps.setInt(2, region.getStart());
			ps.setInt(3, region.getStart());
			ps.setInt(4, region.getStart());
			ps.setInt(5, region.getEnd());
			ps.setInt(6, region.getStart());
			ps.setInt(7, region.getStart());
			ps.setInt(8, region.getStart());
			ps.setInt(9, region.getEnd());

			Iterator<Gene> results = parseResults(ps);
			cxn.close();
			return results;
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new org.seqcode.data.connections.DatabaseException("Couldn't get UCSC RefGenes", ex);
		}
	}
}
