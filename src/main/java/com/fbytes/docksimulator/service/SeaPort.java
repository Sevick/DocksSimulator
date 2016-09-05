package com.fbytes.docksimulator.service;

import com.fbytes.docksimulator.model.Dock;
import com.fbytes.docksimulator.service.dispatcher.CargoDispatcher;
import com.fbytes.docksimulator.service.dispatcher.SynchronizedBlockDispatcher;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

/**
 * Created by S on 09.08.2016.
 */


public class SeaPort {
    final static int MIN_DISCHARGE_RATE = 500;
    final static int MAX_DISCHARGE_RATE = 2000;

    final Logger log = Logger.getLogger(this.getClass());
    static Random rndGen = new Random();

    Dock[] docks;
    Thread[] docksThreads;
    List<Future<String>> docksFuture;
    ScheduledExecutorService scheduledExecutorService;

    volatile int totalCargoRequests = 0;

    //private CargoDispatcher dispatcher=new BlockignQueueDispatcher();
    //private CargoDispatcher dispatcher=new SynchronizedDispatcher();
    private CargoDispatcher dispatcher = new SynchronizedBlockDispatcher();

    public void simulateExceptionInOneDock() {
        int dockNum=rndGen.nextInt(docks.length);
        log.debug("Requesting exception throw from Dock#"+dockNum);
        docks[dockNum].requestException();
    }


    public class SeaPortStats extends CargoDispatcher.DispatcherStats {
        //public int queueLength=0;
        public long totalShipsDischarged = 0;
        public ArrayList<Dock.DockStats> dockStatses = new ArrayList<Dock.DockStats>();
    }

    SeaPortStats seaPortStats = new SeaPortStats();
    ;


    public void initialize(int docksCount, int dischargeDelay) {

        log.info("Initializing SeaPort with docksCount=" + docksCount + "  and dischargeDelay=" + dischargeDelay);

        // create new docks
        docks = new Dock[docksCount];
        docksThreads = new Thread[docksCount];


        // !!!!!!!
        // TO Do : Check for ExecutorCompletionService
        // !!!!!!!

        scheduledExecutorService = Executors.newScheduledThreadPool(docks.length+1);    // +1 for docksMonitor process
        docksFuture = new ArrayList<Future<String>>(docks.length);


        for (int i = 0; i < docks.length; i++) {
            Dock newDock = new Dock(i, dispatcher,
                    MIN_DISCHARGE_RATE + rndGen.nextInt(MAX_DISCHARGE_RATE - MIN_DISCHARGE_RATE),
                    dischargeDelay);
            docks[i] = newDock;

            //Future<?> handle =scheduledExecutorService.submit((Callable<?>) docks[i]);
            docksFuture.add(scheduledExecutorService.submit(docks[i]));

        }
        Runnable docksMonitor = () -> {
            Thread.currentThread().setName("Docks threads monitor");
            do {
                Iterator<Future<String>> docksIter = docksFuture.iterator();
                while (docksIter.hasNext()) {
                    try {
                        docksIter.next().get(50, TimeUnit.MILLISECONDS);
                        docksIter.remove();
                    } catch (InterruptedException e) {
                        docksIter.remove();
                        log.error("docksMonitor receive the InterruptedException:", e);
                    } catch (ExecutionException e) {
                        docksIter.remove();
                        log.error("docksMonitor receive the ExecutionException:", e);
                    } catch (TimeoutException e) {
                        // no nothing - dock is operational
                    }
                }
            } while (docksFuture.size() > 0);
        };
        scheduledExecutorService.submit(docksMonitor);

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

    public CargoDispatcher getDispatcher() {
        return dispatcher;
    }

    public SeaPortStats getSeaPortStats() {
        dispatcher.getStats(seaPortStats);
        seaPortStats.dockStatses.clear();
        seaPortStats.totalShipsDischarged = 0;
        for (Dock curDock : docks) {
            seaPortStats.dockStatses.add(curDock.getStats());
            seaPortStats.totalShipsDischarged += curDock.getStats().totalDischargedShips;

        }
        //seaPortStats.totalShipsDischarged=totalCargoRequests>docks.length ? totalCargoRequests : 0;
        return seaPortStats;
    }

    public void stop() {
        // teminate existed docks

        log.info("SeaPort is shutting down");
        if (scheduledExecutorService != null)
            scheduledExecutorService.shutdownNow();

        /*
        if (docks!=null) {
            for (int i = 0; i < docks.length; i++) {
                docksThreads[i].interrupt();
            }
        }
        */
    }

    public void reset() {
        seaPortStats = new SeaPortStats();
    }
}
