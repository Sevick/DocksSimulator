package com.fbytes.docksimulator.service.dispatcher;

import com.fbytes.docksimulator.model.Cargo;
import org.apache.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by S on 27.08.2016.
 */
public class BlockignQueueDispatcher implements CargoDispatcher {
    final static int MAX_SHIPS_IN_QUEUE=50;

    Logger log=Logger.getLogger(this.getClass());
    DispatcherStats dispatcherStats=new DispatcherStats();

    BlockingQueue<Cargo> shipQueue=new ArrayBlockingQueue<>(MAX_SHIPS_IN_QUEUE);

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
        log.debug("Thread "+Thread.currentThread().getName()+"  adding cargo to queue");
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

    @Override
    public void resetQueue() {
        shipQueue.clear();
    }
}
