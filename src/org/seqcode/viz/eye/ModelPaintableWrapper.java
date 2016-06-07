/*
 * Author: tdanford
 * Date: Nov 14, 2008
 */
package org.seqcode.viz.eye;

import java.awt.Graphics;
import java.util.Iterator;

import org.seqcode.gseutils.models.Model;
import org.seqcode.viz.paintable.Paintable;


/**
 * Turns a Paintable into a ModelPaintable. 
 * 
 * @author tdanford
 */
public class ModelPaintableWrapper extends AbstractModelPaintable {

	private Paintable inner;
	
	public ModelPaintableWrapper(Paintable p) { 
		inner = p;
		inner.addPaintableChangedListener(this);
	}

	public void addModel(Model m) {
	}

	public void addModels(Iterator<? extends Model> itr) {
	}

	public void clearModels() {
	}

	public void paintItem(Graphics g, int x1, int y1, int x2, int y2) {
		inner.paintItem(g, x1, y1, x2, y2);
	}
}
