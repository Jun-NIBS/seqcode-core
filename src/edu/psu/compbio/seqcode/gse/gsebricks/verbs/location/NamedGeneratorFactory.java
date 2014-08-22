/*
 * Created on Sep 28, 2006
 */
package edu.psu.compbio.seqcode.gse.gsebricks.verbs.location;

import edu.psu.compbio.seqcode.genome.Genome;
import edu.psu.compbio.seqcode.genome.location.NamedRegion;
import edu.psu.compbio.seqcode.genome.location.Region;
import edu.psu.compbio.seqcode.gse.gsebricks.RegionExpanderFactory;
import edu.psu.compbio.seqcode.gse.gsebricks.verbs.Expander;

public class NamedGeneratorFactory implements RegionExpanderFactory<NamedRegion> {
    
    private String tableName;
    
    public NamedGeneratorFactory() {
        tableName = "sgdOther";
    }
    
    public NamedGeneratorFactory(String t) { 
        tableName = t;
    }

    public String getProduct() {return "NamedRegion";}

    public Expander<Region, NamedRegion> getExpander(Genome g, String table) {
        if (table == null) {
            throw new NullPointerException("NamedGenerator must have a tablename");
        } else {
            return new NamedRegionGenerator<Region>(g, table);
        }
    }

    public Expander<Region, NamedRegion> getExpander(Genome g) {
        return getExpander(g, tableName);
    }

    public String getType() {
        return tableName;
    }

    public void setType(String type) {
        tableName = type;
    }
}