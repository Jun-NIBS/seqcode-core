/*
 * Created on Feb 10, 2006
 */
package org.seqcode.gse.utils.probability.boundaries;

/**
 * @author tdanford
 */
public interface ConstrainedChooser {
    public double logConstrainedChoose(int N, int E, int p0, int s0, boolean optimal);
}
