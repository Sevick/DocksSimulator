package com.fbytes.docksimulator.service;

import com.fbytes.docksimulator.model.Cargo;
import com.fbytes.docksimulator.model.Dock;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

/**
 * Created by S on 09.08.2016.
 */


public class SeaPort implements CargoDispatcher {
    final static int MIN_DISCHARGE_RATE=500;
    final static int MAX_DISCHARGE_RATE=2000;
    final static int MAX_SHIPS_IN_QUEUE=50;


    Logger log=Logger.getLogger(this.getClass());

    static Random rndGen=new Random();

    //ConcurrentLinkedQueue<Cargo> shipQueue=new ConcurrentLinkedQueue();
    //Queue<Cargo> shipQueue;
    BlockingQueue<Cargo> shipQueue;

    Dock[] docks;
    Thread[] docksThreads;

    volatile int totalCargoRequests=0;

    Dispatcher dispatcher=new BlockignQueueDispatcher();


    public class SeaPortStats extends Dispatcher.DispatcherStats {
        //public int queueLength=0;
        public long totalShipsDischarged=0;
        public ArrayList<Dock.DockStats> dockStatses=new ArrayList<Dock.DockStats>();
    }
    SeaPortStats seaPortStats =new SeaPortStats();



    public void initialize(int docksCount, int dischargeDelay){

        seaPortStats.totalShipsDischarged=0;
        //dispatcherStats.queueLength=0;

        shipQueue=new ArrayBlockingQueue<Cargo>(MAX_SHIPS_IN_QUEUE);
        //shipQueue=new LinkedList<Cargo>();

        // create new docks
        docks=new Dock[docksCount];
        docksThreads=new Thread[docksCount];
        for (int i=0; i<docks.length; i++) {
            Dock newDock=new Dock(i,this,
                    MIN_DISCHARGE_RATE+rndGen.nextInt(MAX_DISCHARGE_RATE-MIN_DISCHARGE_RATE),
                    dischargeDelay);
            docks[i] = newDock;
        }

        ScheduledExecutorService scheduledExecutorService =  Executors.newScheduledThreadPool(docks.length);

        for (int i=0; i<docks.length; i++){
            FutureTask<String> futureTask = new FutureTask<>(docks[i]);
            Thread t=new Thread(futureTask);
            docksThreads[i]=t;
            t.start();
        }

/*
        for (Dock curDock: docks){
            FutureTask<String> futureTask = new FutureTask<>(curDock);
            Thread t=new Thread(futureTask);
            t.start();
        }
*/

        //scheduledExecutorService.shutdown();

    }



    @Override
    public Cargo getNextCargo() {
        log.debug("Thread "+Thread.currentThread().getName()+"  requested getNextCargo");
        totalCargoRequests++;
        Cargo nextCargo=null;


/*        synchronized (shipQueue) {
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
        }*/


/*        do{
            nextCargo = shipQueue.poll();
            if (nextCargo==null)
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        } while (nextCargo == null);*/

        try {
            nextCargo=shipQueue.take();
        } catch (InterruptedException e) {
            log.warn(e.getStackTrace());
        }


        return nextCargo;
    }


    @Override
    public void addCargoToQueue(Cargo newCargo) {

/*
        synchronized (shipQueue) {
            log.debug("Adding cargo to queue");
            shipQueue.add(newCargo);
            shipQueue.notifyAll();
        }
*/
        try {
            shipQueue.put(newCargo);
        } catch (InterruptedException e) {
            log.warn(e.getStackTrace());
        }


    }


    @Override
    public int getQueueLength() {
        return  shipQueue.size();
    }


    public SeaPortStats getSeaPortStats(){
        dispatcher.getStats(seaPortStats);
        seaPortStats.dockStatses.clear();
        seaPortStats.totalShipsDischarged=0;
        for (Dock curDock : docks){
            seaPortStats.dockStatses.add(curDock.getStats());
            seaPortStats.totalShipsDischarged+=curDock.getStats().totalDischargedShips;

        }
        seaPortStats.totalShipsDischarged=totalCargoRequests>docks.length ? totalCargoRequests : 0;
        return seaPortStats;
    }

    public void stop(){
        // teminate existed docks
        if (docks!=null) {
            for (int i = 0; i < docks.length; i++) {
                docksThreads[i].interrupt();
            }
        }
    }

}
