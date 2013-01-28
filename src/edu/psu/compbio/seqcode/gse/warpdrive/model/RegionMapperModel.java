package edu.psu.compbio.seqcode.gse.warpdrive.model;

import edu.psu.compbio.seqcode.gse.datasets.general.Region;
import edu.psu.compbio.seqcode.gse.ewok.verbs.Mapper;

public class RegionMapperModel<OUT> extends MapperModel<Region,OUT> implements RegionModel {
    private Region region;

    public RegionMapperModel(Mapper<Region,OUT> ex) {
        super(ex);
    }

    public void setRegion(Region r) throws NullPointerException {
        if (r == null) {throw new NullPointerException("Region can't be null");}
        region = r;
        setInput(r);
    }
    public Region getRegion() {
        return region;
    }


}
