package org.seqcode.projects.seqview.paintable;

/**
 * ExperimentPaintableProperties are for tracks that can show different
 * experiments.  Doesn't load the TrackName from the default properties
 * file.  While loading the trackname might make sense for, eg AT content,
 * it doesn't make sense for a painter than can show sequencing data.
 */

public class ExperimentPaintableProperties extends PaintableProperties {
    public void loadDefaults () {
        // don't load the track label from the defaults, since it varies by experiment.
        String origTrackLabel = TrackLabel;
        super.loadDefaults();
        TrackLabel = origTrackLabel;
    }
}