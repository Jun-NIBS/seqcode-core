package org.seqcode.data.motifdb;

import java.util.Comparator;

public class WMHitStartComparator implements Comparator<WMHit> {

	public int compare(WMHit a, WMHit b) {
		return a.getStart() - b.getStart();
	}

	public boolean equals(Object o) {
		return o instanceof WMHitStartComparator;
	}

}