package org.seqcode.projects.seqview.paintable;

import java.io.File;
import java.awt.*;
import java.util.*;

import org.seqcode.data.seqdata.SeqHit;
import org.seqcode.genome.location.Region;
import org.seqcode.gseutils.*;
import org.seqcode.projects.seqview.model.SeqDataModel;
import org.seqcode.projects.seqview.model.SeqScaleModel;
import org.seqcode.viz.DynamicAttribute;


/**
 *  Paints Sequencing data across a genomic region. 
 *  
 */
public abstract class SeqPainter extends RegionPaintable  {

    protected SeqDataModel model;
    protected SeqScaleModel scale;
    protected DynamicAttribute attrib;
    protected Vector<Region> totalLayoutHits = new Vector<Region>();
    protected NonOverlappingLayout<Region> totalLayout = new NonOverlappingLayout<Region>();

    protected static java.util.List configurationFields = null;
    private SeqProperties props;

    public SeqPainter(SeqDataModel model) {
        super();
        this.model = model;
        props = new SeqProperties();
        scale = new SeqScaleModel(model);
        model.addEventListener(this);
        attrib = DynamicAttribute.getGlobalAttributes();
    }
    public SeqProperties getProperties () {return props;}
    public void setProperties(SeqProperties p) {props = p;}
    public void savePropsInDir(File dir) {
        super.savePropsInDir(dir);
        saveModelPropsInDir(dir,model);
    }
    public void loadPropsInDir(File dir) {
        super.loadPropsInDir(dir);
        loadModelPropsInDir(dir,model);
    }    
    public void cleanup() { 
        super.cleanup();
        model.removeEventListener(this);
    }

    public void setScaleModel(SeqScaleModel s) {
        scale = s;
    }
    public SeqScaleModel getScaleModel() {return scale;}

    public java.util.List<String> configurationKeyOrder() {return configurationFields;}

    protected static int xcoord(int base, int x1, int baseStart, double scale) { 
        return x1 + (int)Math.round((double)(base - baseStart) * scale);
    }

        
    protected int calcX(int bp, int rstart, int rend, int x1, int x2) { 
        double frac = (double)(bp - rstart) / (rend - rstart);
        return x1 + (int)Math.round(frac * (double)(x2 - x1));
    }
    
    protected int calcY(int over, int minover, int maxover, int y1, int y2) { 
        double frac = (double)(over - minover) / (double)(maxover - minover);
        return y2 - (int)Math.round(frac * (double)(y2 - y1));
    }
    
    protected void drawStep(Graphics2D g, int px, int py, int pover, int x, int y, int over) { 
        if(over > pover) { drawStepUp(g, px, py, x, y); }
        if(over < pover) { drawStepDown(g, px, py, x, y); }
        if(over==pover) { g.drawLine(px, py, x, y); }
    }
    
    protected void drawStepUp(Graphics2D g, int px, int py, int x, int y) { 
        g.drawLine(px, py, x, py);
        g.drawLine(x, py, x, y);
    }

    protected void drawStepDown(Graphics2D g, int px, int py, int x, int y) { 
        g.drawLine(px, py, px, y);
        g.drawLine(px, y, x, y);
    }

    public synchronized void eventRegistered(EventObject e) {        
        if ((e.getSource() == model) || (e.getSource() == scale) &&
            model.isReady() &&
            ((scale == null) || scale.isReady())) {
            setCanPaint(true);
            setWantsPaint(true);
            totalLayoutHits = null;
            setLayoutHits();
            notifyListeners();
        }
    }

    public void removeEventListener(Listener<EventObject> l) {
        super.removeEventListener(l);
        if (!hasListeners()) {
            model.removeEventListener(this);
        }
    }
    
    
    protected void setLayoutHits() {
        if (canPaint() && totalLayoutHits == null) {
            Iterator<SeqHit> itr = model.getResults();
            totalLayoutHits = new Vector<Region>();
            while(itr.hasNext()) { 
            	SeqHit hit = itr.next();
            	totalLayoutHits.add(hit); 
            }
            totalLayout.setRegions(totalLayoutHits); 
        }
    }
    
    public void paintItem(Graphics2D g, 
            int x1, int y1, 
            int x2, int y2) {
    	
        if (!canPaint()) {
            return;
        }

        if(!model.isReady()) { return; }

    	if (getProperties().Overlapping) {
    		paintOverlapping(g, x1, y1, x2, y2);
    	}
    	else {
    		paintNonOverlapping(g, x1, y1, x2, y2);
    	}
        /* draw the track label */
        int width = x2 - x1;
        int height = y2 - y1;
    	if(model.isDataError()){
    		g.setFont(attrib.getLargeLabelFont(width,height));
			g.setColor(Color.RED);
			g.drawString("ReadDB Data Error: " +getLabel(),x1 + g.getFont().getSize()*2,y1 + g.getFont().getSize()*2);
		}else if (getProperties().DrawTrackLabel) {
            g.setFont(attrib.getLargeLabelFont(width,height));
            g.setColor(Color.BLACK);
            g.drawString(getLabel(),x1,y1 + g.getFont().getSize() * 2);
        }

    }
    
    
    protected abstract void paintOverlapping(Graphics2D g, 
            int x1, int y1, 
            int x2, int y2);
    
    protected abstract void paintNonOverlapping(Graphics2D g, 
            int x1, int y1, 
            int x2, int y2);
    
}

