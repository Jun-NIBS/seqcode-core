package org.seqcode.ml.clustering.kmeans;

import java.util.Collection;
import java.util.Vector;

import org.seqcode.ml.clustering.Cluster;
import org.seqcode.ml.clustering.ClusterRepresentative;
import org.seqcode.ml.clustering.ClusteringMethod;
import org.seqcode.ml.clustering.DefaultCluster;
import org.seqcode.ml.clustering.PairwiseElementMetric;
import org.seqcode.ml.clustering.vectorcluster.VectorClusterElement;
import java.util.Iterator;


/**
 * @author Timothy Danford
 */
public class KMeansClustering<X> implements ClusteringMethod<X> {
	
    private PairwiseElementMetric<X> metric;
    private ClusterRepresentative<X> repr;
    private Vector<X> startMeans;
    private int numClusters;
    private int iterations;
    private Vector<X> elmts;
    private Vector<DefaultCluster<X>> clusters;
    private Vector<X> clusterMeans;
	
    public KMeansClustering(PairwiseElementMetric<X> m, 
                            ClusterRepresentative<X> r, Collection<X> starts) { 
        metric = m;
        repr = r;
        numClusters = starts.size(); 
        clusters = new Vector<DefaultCluster<X>>();
        for(int c=0; c<numClusters; c++){clusters.add(new DefaultCluster<X>());}
        clusterMeans = new Vector<X>(starts);
        startMeans = new Vector<X>(starts);
        iterations = 10;
        elmts = new Vector<X>();
    }
	   
    public void setIterations(int i) { iterations = i; }

    public Collection<Cluster<X>> clusterElements(Collection<X> e) {return(clusterElements(e, 0));}
    public Collection<Cluster<X>> clusterElements(Collection<X> e, double convergenceDifference) {
        init(e);
        boolean converged=false;
        for(int i = 0; i < iterations && !converged; i++) {
        	Vector<X> oldClusterMeans = (Vector<X>) clusterMeans.clone();
        	
        	//K-means
        	assignToClusters();
        	getClusterMeans();
            
            //Check convergence
            double totalDist=0;
            for(int c = 0; c < numClusters; c++) {
            	totalDist+= Math.abs(metric.evaluate(oldClusterMeans.get(c), clusterMeans.get(c)));            	
            }if(totalDist<=convergenceDifference)
            	converged=true;
        }
        return new Vector<Cluster<X>>(clusters);
    }
	
    private void assignToClusters() { 
        for(int i = 0; i < numClusters; i++) { clusters.get(i).clear(); }
        for(int k = 0; k < elmts.size(); k++) { 
            X e = elmts.get(k);
            int minCluster = -1;
            double minDist = 0.0;
            for(int i = 0; i < numClusters; i++) { 
                double clustDist = metric.evaluate(e, clusterMeans.get(i));
                if(minCluster == -1 || clustDist < minDist) { 
                    minDist = clustDist;
                    minCluster = i;
                }
            }
            clusters.get(minCluster).addElement(e);
        }
    }
	
    public Vector<X> getClusterMeans() { 
        for(int i = 0; i < numClusters; i++) { 
            clusterMeans.set(i, repr.getRepresentative(clusters.get(i)));
        }return(clusterMeans);
    }
	
    private void init(Collection<X> e) {
        elmts = new Vector<X>(e);
        for(int i = 0; i < numClusters; i++) { 
            clusters.set(i, new DefaultCluster<X>());
            clusterMeans.set(i, startMeans.get(i));
        }
    }
    
    public double sumOfSquaredDistance(){
    	double totalDist =0;
    	for(int k = 0; k < elmts.size(); k++) { 
            X e = elmts.get(k);
            int minCluster = -1;
            double minDist = 0.0;
            for(int i = 0; i < numClusters; i++) { 
                double clustDist = metric.evaluate(e, clusterMeans.get(i));
                if(minCluster == -1 || clustDist < minDist) { 
                    minDist = clustDist;
                    minCluster = i;
                }
            }totalDist += minDist*minDist;
        }
    	return(totalDist);
    }
    
    public double silhouette(){
    	double sh = 0.0;
    	for(int k=0; k< elmts.size(); k++){
    		X e = elmts.get(k);
    		double[] avgDist = new double[numClusters];
    		int minCluster = -1;
    		double minDist = 0.0;
    		for(int i = 0; i < numClusters; i++) { 
                double clustDist = metric.evaluate(e, clusterMeans.get(i));
                if(minCluster == -1 || clustDist < minDist) { 
                    minDist = clustDist;
                    minCluster = i;
                }
                double currDist = 0.0;
                for(X ve : clusters.get(i).getElements()){
                	currDist = currDist + metric.evaluate(e, ve);
                }
                currDist = currDist/clusters.get(i).size();
                avgDist[i] = currDist;
            }
    		double neighborDistance = Double.MAX_VALUE;
    		for(int i=0; i<numClusters; i++){
    			if(i!=minCluster){ // Not its cluster
    				if(avgDist[i]<neighborDistance){
    					neighborDistance = avgDist[i];
    				}
    			}
    		}
    		
    		sh = sh + (neighborDistance - avgDist[minCluster])/Math.max(neighborDistance, avgDist[minCluster]);
    	}
    	sh = sh/elmts.size();
    	return sh;
    }
}

