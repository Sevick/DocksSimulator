package com.fbytes.docksimulator;

import com.fbytes.docksimulator.service.CargoProducer;
import com.fbytes.docksimulator.service.SeaPort;
import com.fbytes.docksimulator.service.StatsProducer;
import org.apache.log4j.Logger;

/**
 * Created by S on 09.08.2016.
 */
public class DocksSimulator implements StatsProducer {


    Logger log=Logger.getLogger(this.getClass());

    private int docksCount=5;
    private int shipDelay=400;
    private int dischargeDelay=200;

    SeaPort seaPort=new SeaPort();
    CargoProducer cargoProducer=new CargoProducer();

    //CargoProducer.CargoProducerStats cargoProducerStats;
    //SeaPort.DispatcherStats seaPortStats;

    @Override
    public SeaPort.SeaPortStats getSeaPortStats() {
        return  seaPort.getSeaPortStats();
    }

    @Override
    public CargoProducer.CargoProducerStats getCargoProducerStats() {
        return cargoProducer.getStats();
    }


    public void initSimulator(int docksCount, int shipDelay, int dischargeDelay){
        this.docksCount=docksCount;
        this.shipDelay=shipDelay;
        this.dischargeDelay=dischargeDelay;
        seaPort.initialize(docksCount, dischargeDelay);
        cargoProducer.initialize(seaPort.getDispatcher(),shipDelay);

    }

    public void stopSimulator(){
        stopShipsProduction();
        seaPort.stop();
    }

    public void stopShipsProduction(){
        cargoProducer.stop();

    }

    public void startShipsProduction(){
        cargoProducer.start();
    }


    public void startSimulator() {
        startShipsProduction();
    }


    public void simulateExceptionInOneDock(){
        if (seaPort!=null)
            seaPort.simulateExceptionInOneDock();
    }


}
