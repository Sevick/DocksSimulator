package com.fbytes.docksimulator.service;

import com.fbytes.docksimulator.model.Cargo;


/**
 * Created by S on 27.08.2016.
 */
public interface Dispatcher {

    public class DispatcherStats {
        public int queueLength=0;
        volatile public long totalCargoRequests=0;
    }


    public Cargo getNextCargo();
    public void addCargoToQueue(Cargo newCargo);
    public int getQueueLength();
    public void getStats(DispatcherStats statsToUpdate);
}
