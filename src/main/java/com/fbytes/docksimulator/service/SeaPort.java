package com.fbytes.docksimulator.service;

import com.fbytes.docksimulator.model.Cargo;
import com.fbytes.docksimulator.model.Dock;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

/**
 * Created by S on 09.08.2016.
 */


public class SeaPort implements CargoDispatcher {
    final static int MIN_DISCHARGE_RATE=500;
    final static int MAX_DISCHARGE_RATE=2000;

    Logger log=Logger.getLogger(this.getClass());
    static Random rndGen=new Random();

    Dock[] docks;
    Thread[] docksThreads;
    List<Future<String>> docksFuture;
    ScheduledExecutorService scheduledExecutorService;

    volatile int totalCargoRequests=0;

    private Dispatcher dispatcher=new BlockignQueueDispatcher();


    public class SeaPortStats extends Dispatcher.DispatcherStats {
        //public int queueLength=0;
        public long totalShipsDischarged=0;
        public ArrayList<Dock.DockStats> dockStatses=new ArrayList<Dock.DockStats>();
    }
    SeaPortStats seaPortStats =new SeaPortStats();



    public void initialize(int docksCount, int dischargeDelay){

        log.info("Initializing SeaPort with docksCount="+docksCount+"  and dischargeDelay="+dischargeDelay);
        seaPortStats.totalShipsDischarged=0;

        // create new docks
        docks=new Dock[docksCount];
        docksThreads=new Thread[docksCount];

        scheduledExecutorService =  Executors.newScheduledThreadPool(docks.length);
        docksFuture =new ArrayList<Future<String>>(docks.length);

        for (int i=0; i<docks.length; i++) {
            Dock newDock=new Dock(i,this,
                    MIN_DISCHARGE_RATE+rndGen.nextInt(MAX_DISCHARGE_RATE-MIN_DISCHARGE_RATE),
                    dischargeDelay);
            docks[i] = newDock;
            docksFuture.add(scheduledExecutorService.submit(docks[i]));
            //scheduledExecutorService.submit(docks[i]);
        }


//   Start threads manually
/*
        for (int i=0; i<docks.length; i++){
            FutureTask<String> futureTask = new FutureTask<>(docks[i]);
            Thread t=new Thread(futureTask);
            docksThreads[i]=t;
            t.start();
        }
*/
    }



    @Override
    public Cargo getNextCargo() {
        totalCargoRequests++;
        Cargo nextCargo=dispatcher.getNextCargo();
        return nextCargo;
    }


    @Override
    public void addCargoToQueue(Cargo newCargo) {
        dispatcher.addCargoToQueue(newCargo);
    }


    @Override
    public int getQueueLength() {
        return  dispatcher.getQueueLength();
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

        log.info("SeaPort is shutting down");
        if (scheduledExecutorService!=null)
            scheduledExecutorService.shutdownNow();

        /*
        if (docks!=null) {
            for (int i = 0; i < docks.length; i++) {
                docksThreads[i].interrupt();
            }
        }
        */
    }

}
