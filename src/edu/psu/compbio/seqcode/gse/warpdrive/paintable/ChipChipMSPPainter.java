package edu.psu.compbio.seqcode.gse.warpdrive.paintable;

import java.awt.*;
import java.util.*;

import edu.psu.compbio.seqcode.gse.datasets.*;
import edu.psu.compbio.seqcode.gse.datasets.chipchip.ChipChipMSP;
import edu.psu.compbio.seqcode.gse.utils.*;
import edu.psu.compbio.seqcode.gse.warpdrive.model.ChipChipDataModel;

public class ChipChipMSPPainter extends TimChipChipPainter {
    private ChipChipMSP data;

    public ChipChipMSPPainter (ChipChipMSP data,  ChipChipDataModel model) {
        super(data,model);
        this.data = data;
    }    

    public void paintDataPointAt(Graphics2D g, int x,int y,int i,int j) {
        
        Color pc = g.getColor();
        
        int squaresize = 4;
        if (data.getPval3(i) < .001) {
            squaresize = 8;
        } else if (data.getPval3(i) < .01) {
            squaresize = 6;
        }
        
        //g.setColor(Color.white);
        //g.fillRect(x-squaresize/2,y-squaresize/2,squaresize,squaresize);
        
        //g.setColor(pc);
        //g.drawRect(x-squaresize/2,y-squaresize/2,squaresize,squaresize);
    }
}

