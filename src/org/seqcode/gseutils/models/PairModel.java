/*
 * Author: tdanford
 * Date: Nov 24, 2008
 */
package org.seqcode.gseutils.models;

public class PairModel extends Model {
	public Model car, cdr;
	public PairModel(Model c1, Model c2) {  
		car = c1; cdr = c2;
	}
}
