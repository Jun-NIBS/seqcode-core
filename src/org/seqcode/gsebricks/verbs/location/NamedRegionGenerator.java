package org.seqcode.gsebricks.verbs.location;

import java.util.*;
import java.sql.*;

import org.seqcode.data.connections.DatabaseException;
import org.seqcode.genome.Genome;
import org.seqcode.genome.location.NamedRegion;
import org.seqcode.genome.location.Region;
import org.seqcode.gsebricks.verbs.Expander;


/* Generator that returns NamedRegion objects from the specified table. 
   The table must have columns: chrom, chromStart, chromEnd.
   The regions are returned sorted by ascending chromStart */

public class NamedRegionGenerator<X extends Region> implements Expander<X,NamedRegion> {

    private Genome genome;
    private String tablename;

    public NamedRegionGenerator(Genome g, String t) {
        genome = g;
        tablename = t;
    }

    public Iterator<NamedRegion> execute(X region) {
        try {
            java.sql.Connection cxn =
                genome.getAnnotationDBConnection();
            PreparedStatement ps = cxn.prepareStatement("select name, chromStart, chromEnd from " + tablename + " where chrom = ? and " +
                                                        "((chromStart <= ? and chromEnd >= ?) or (chromStart >= ? and chromStart <= ?)) order by chromStart");
            String chr = region.getChrom();
            if (!chr.matches("^(chr|scaffold).*")) {
                chr = "chr" + chr;
            }
            ps.setString(1,chr);
            ps.setInt(2,region.getStart());
            ps.setInt(3,region.getStart());
            ps.setInt(4,region.getStart());
            ps.setInt(5,region.getEnd());
            ResultSet rs = ps.executeQuery();
            ArrayList<NamedRegion> results = new ArrayList<NamedRegion>();
            while (rs.next()) {
                results.add(new NamedRegion(genome,
                                            region.getChrom(),
                                            rs.getInt(2),
                                            rs.getInt(3),
                                            rs.getString(1)));
                                     
            }
            rs.close();
            ps.close();
            cxn.close();
            return results.iterator();
        } catch (SQLException ex) {
            throw new DatabaseException("Couldn't get UCSC RefGenes",ex);
        }

    }


}
