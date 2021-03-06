package org.seqcode.projects.seqview.model;

import java.util.*;

import org.seqcode.gseutils.*;



public abstract class SeqViewModel implements Model {
    private HashSet<Listener<EventObject>> listeners;
    private boolean keepRunning;
    private ModelProperties props;
    protected boolean dataError=false;
    
    public SeqViewModel() {
        listeners = new HashSet<Listener<EventObject>>();
        keepRunning = true;
        props = new ModelProperties();
    }

    public ModelProperties getProperties() {return props;}

    public boolean keepRunning() {return keepRunning;}
    public boolean isDataError(){return dataError;}
    public void stopRunning() {
        keepRunning = false;
        synchronized(this) {
            this.notifyAll();
        }
    }
    public synchronized void notifyListeners() {
        for (Listener<EventObject> l : listeners) {
            l.eventRegistered(new EventObject(this));
        }
    }
    public void addEventListener(Listener<EventObject> l) {
        listeners.add(l);
    }
    public void removeEventListener(Listener<EventObject> l) {
        listeners.remove(l);
        if (!hasListeners()) {
            stopRunning();
        }
    }
    public boolean hasListeners() {return listeners.size() > 0;}
}
