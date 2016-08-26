package com.fbytes.docksimulator.service;

import com.fbytes.docksimulator.model.Cargo;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by S on 27.08.2016.
 */
public class SyncronizedDispatcher implements Dispatcher {

    Logger log=Logger.getLogger(this.getClass());
    DispatcherStats dispatcherStats;

    Queue<Cargo> shipQueue=new LinkedList<Cargo>();

    @Override
    public Cargo getNextCargo() {
        log.debug("Thread "+Thread.currentThread().getName()+"  requested getNextCargo");
        dispatcherStats.totalCargoRequests++;
        Cargo nextCargo=null;

        synchronized (shipQueue) {
            log.debug("Thread "+Thread.currentThread().getName()+"  polling shipQueue");
            nextCargo = shipQueue.poll();
            while (nextCargo == null) {
                log.debug("Thread "+Thread.currentThread().getName()+"  got null nextCargo");
                // wait for next ship arrival
                try {
                    log.debug("Thread "+Thread.currentThread().getName()+"  is now waiting on shipQueue");
                    shipQueue.wait();
                    log.debug("Thread "+Thread.currentThread().getName()+"  is exits from wait on shipQueue");
                    nextCargo = shipQueue.poll();
                } catch (InterruptedException e) {
                    log.debug("Wait interrupted");
                    break;
                }
            }
        }

        return nextCargo;
    }

    @Override
    public void addCargoToQueue(Cargo newCargo) {
        synchronized (shipQueue) {
            log.debug("Adding cargo to queue");
            shipQueue.add(newCargo);
            shipQueue.notifyAll();
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
