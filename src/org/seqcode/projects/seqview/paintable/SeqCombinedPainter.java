package org.seqcode.projects.seqview.paintable;


import java.awt.Graphics2D;

import org.seqcode.projects.seqview.model.SeqDataModel;

public class SeqCombinedPainter extends SeqPainter {

    private SeqBasicOverlapPainter basic;
    private SeqAboveBelowStrandPainter stranded;

    public SeqCombinedPainter (SeqDataModel model) {
        super(model);
        basic = new SeqBasicOverlapPainter(model);
        basic.setProperties(getProperties());
        stranded = new SeqAboveBelowStrandPainter(model);
        stranded.setProperties(getProperties());       
    }

    protected void paintOverlapping(Graphics2D g, 
                                    int x1, int y1, 
                                    int x2, int y2) {
        if (getProperties().Stranded) {
            stranded.paintOverlapping(g,x1,y1,x2,y2);
        } else {
            basic.paintOverlapping(g,x1,y1,x2,y2);
        }
    }
    
    protected void paintNonOverlapping(Graphics2D g, 
                                       int x1, int y1, 
                                       int x2, int y2) {
        if (getProperties().Stranded) {
            stranded.paintNonOverlapping(g,x1,y1,x2,y2);
        } else {
            basic.paintNonOverlapping(g,x1,y1,x2,y2);
        }
    }
}