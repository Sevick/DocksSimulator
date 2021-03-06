package com.fbytes.docksimulator.service.dispatcher;

import com.fbytes.docksimulator.model.Cargo;
import org.apache.log4j.Logger;

import java.io.ObjectStreamConstants;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by S on 27.08.2016.
 */
public class SynchronizedDispatcher implements CargoDispatcher {
    Logger log=Logger.getLogger(this.getClass());
    DispatcherStats dispatcherStats=new DispatcherStats();

    Queue<Cargo> shipQueue=new LinkedList<Cargo>();
    Object queueLocker=new Object();

    @Override
    synchronized public Cargo getNextCargo() {
        log.debug("Thread "+Thread.currentThread().getName()+"  requested getNextCargo");
        dispatcherStats.totalCargoRequests++;
        Cargo nextCargo=null;


            log.debug("Thread " + Thread.currentThread().getName() + "  polling shipQueue");
            nextCargo = shipQueue.poll();
            log.debug("Thread " + Thread.currentThread().getName() + " first poll returns nextCargo=" + nextCargo);
            //shipQueue.notifyAll();


            while (nextCargo == null) {
                log.debug("Thread " + Thread.currentThread().getName() + "  got null nextCargo");
                // wait for next ship arrival
                try {
                    log.debug("Thread " + Thread.currentThread().getName() + "  is now waiting on shipQueue");
                    wait();
                    log.debug("Thread " + Thread.currentThread().getName() + "  exits from wait on shipQueue");
//                    synchronized (shipQueue) {
                        nextCargo = shipQueue.poll();
                        //shipQueue.notifyAll();
//                    }

                } catch (InterruptedException e) {
                    log.debug("Wait interrupted");
                    break;
                }
            }

        return nextCargo;
    }

    @Override
    synchronized public void addCargoToQueue(Cargo newCargo) {
        log.debug("Add cargo request from thread: "+Thread.currentThread().getName());
            shipQueue.add(newCargo);
            log.debug("Cargo added to queue");
            notifyAll();
            log.debug("shipQueue notifyed");
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
