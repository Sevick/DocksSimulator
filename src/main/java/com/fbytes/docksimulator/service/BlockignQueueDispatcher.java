package com.fbytes.docksimulator.service;

import com.fbytes.docksimulator.model.Cargo;
import com.fbytes.docksimulator.model.Dock;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;

/**
 * Created by S on 27.08.2016.
 */
public class BlockignQueueDispatcher implements Dispatcher {

    Logger log=Logger.getLogger(this.getClass());
    DispatcherStats dispatcherStats;

    BlockingQueue<Cargo> shipQueue;

    @Override
    public Cargo getNextCargo() {
        log.debug("Thread "+Thread.currentThread().getName()+"  requested getNextCargo");
        dispatcherStats.totalCargoRequests++;
        Cargo nextCargo=null;

        try {
            nextCargo=shipQueue.take();
        } catch (InterruptedException e) {
            log.warn(e.getStackTrace());
        }
        return nextCargo;
    }

    @Override
    public void addCargoToQueue(Cargo newCargo) {
        try {
            shipQueue.put(newCargo);
        } catch (InterruptedException e) {
            log.warn(e.getStackTrace());
        }
    }

    @Override
    public int getQueueLength() {
        return shipQueue.size();
    }

    @Override
    public void getStats(DispatcherStats statsToUpdate) {
        statsToUpdate.queueLength=shipQueue.size();
    }
}
