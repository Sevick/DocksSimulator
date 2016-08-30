package com.fbytes.docksimulator.service;


import com.fbytes.docksimulator.model.Ship;
import com.fbytes.docksimulator.service.dispatcher.CargoDispatcher;
import org.apache.log4j.Logger;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by S on 09.08.2016.
 */
public class CargoProducer {

    public final static int MIN_DEADWEIGHT=1000;
    public final static int MAX_DEADWEIGHT=10000;
    public final static int MIN_DISCHARGE_RATE=100;
    public final static int MAX_DISCHARGE_RATE=500;
    public final static int MIN_LOAD=50;   // 50%

    Logger log=Logger.getLogger(this.getClass());


    CargoDispatcher dispatcher;
    volatile int cargoNumerator;
    int delay;
    Random rndGen=new Random();

    private ScheduledExecutorService scheduler;

    public class CargoProducerStats{
        public volatile long totalCargoWeightProduced=0;
        public volatile long totalShipsProduced=0;

    }
    CargoProducerStats cargoProducerStats=new CargoProducerStats();


    //---------------------------------------------------------------------------------------
    public void start(){
        cargoProducerStats=new CargoProducerStats();
        scheduler = Executors.newScheduledThreadPool(1);

        Runnable genCargp=()-> {Thread.currentThread().setName("Cargo producer"); log.debug("Sheduled ship generation");dispatcher.addCargoToQueue(getRandomShip());};
        scheduler.scheduleAtFixedRate(genCargp, 0, delay, TimeUnit.MILLISECONDS);
    }
    public void stop() {
        if (scheduler!=null)
            scheduler.shutdown();
    }

    public void reset(){
        cargoProducerStats=new CargoProducerStats();
    }


    public void initialize(CargoDispatcher dispatcher, int delay) {
        this.dispatcher = dispatcher;
        this.delay=delay;
    }

    protected Ship getRandomShip(){
        int newDeadWight=MIN_DEADWEIGHT+rndGen.nextInt(MAX_DEADWEIGHT-MIN_DEADWEIGHT);
        int newDischargeRate=MIN_DISCHARGE_RATE+rndGen.nextInt(MAX_DISCHARGE_RATE-MIN_DISCHARGE_RATE);
        int newCurrentLoad=newDeadWight*MIN_LOAD/100+rndGen.nextInt(newDeadWight-newDeadWight*MIN_LOAD/100);
        cargoProducerStats.totalShipsProduced++;
        cargoProducerStats.totalCargoWeightProduced+=newCurrentLoad;
        Ship newShip=new Ship(cargoNumerator++,newDeadWight,newDischargeRate,newCurrentLoad);
        return newShip;
    }


    public CargoProducerStats getStats(){
        return cargoProducerStats;
    }

}
