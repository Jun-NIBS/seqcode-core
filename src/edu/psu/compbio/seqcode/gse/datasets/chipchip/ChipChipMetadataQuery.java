/*
 * Created on Mar 21, 2007
 */
package edu.psu.compbio.seqcode.gse.datasets.chipchip;

import edu.psu.compbio.seqcode.gse.datasets.general.Cells;
import edu.psu.compbio.seqcode.gse.datasets.general.Condition;
import edu.psu.compbio.seqcode.gse.datasets.general.Factor;
import edu.psu.compbio.seqcode.gse.datasets.species.Genome;

/**
 * @author tdanford
 */
public class ChipChipMetadataQuery {

    private Genome genome;
    private Cells cells;
    private Condition cond;
    private Factor factor;

    public ChipChipMetadataQuery() {  
        genome = null;
        cells = null;
        cond = null;
        factor = null;
    }

    public Genome getGenome() { return genome; }
    public void setGenome(Genome g) { genome = g; }

    public Cells getCells() { return cells; }
    public void setCells(Cells c) { cells = c; }

    public Condition getCondition() { return cond; }
    public void setCondition(Condition c) { cond = c; }

    public boolean equals(Object o) { 
        if(!(o instanceof ChipChipMetadataQuery)) { return false; }
        return true;
    }

    public int hashCode() { 
        int code = 17;
        code += genome != null ? genome.hashCode() : 0; code *= 37;
        code += cells != null ? cells.hashCode() : 0; code *= 37;
        return code;
    }
}
