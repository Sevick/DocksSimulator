package com.fbytes.docksimulator.model;

import com.fbytes.docksimulator.service.CargoDispatcher;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

import static java.lang.Thread.sleep;
import static java.lang.Thread.yield;

/**
 * Created by S on 08.08.2016.
 */
public class Dock implements Callable<String>{

    Logger log=Logger.getLogger(this.getClass());

    private int id;
    private int currentDischargeRate=0;
    private int dischargePerformance;

    private int dischargeDelay;
    private CargoDispatcher cargoDispatcher;
    private Cargo currentCargo;

    public class DockStats{
        public int id;
        public int totalDischargedWeight=0;
        public int totalDischargedShips=0;
        public int currentShipWeightLeft=0;
        public int currentDischargeRate=0;
        public int dischargePerformance=0;
    }
    DockStats dockStats=new DockStats();


    public DockStats getStats(){
        if (currentCargo!=null) {
            dockStats.currentShipWeightLeft = currentCargo.getCurrentLoad();
            dockStats.currentShipWeightLeft = currentCargo.getCurrentLoad();
        }
        else {
            dockStats.currentShipWeightLeft = 0;
            dockStats.currentShipWeightLeft=0;
        }

        dockStats.id=id;
        dockStats.currentDischargeRate=currentDischargeRate;
        dockStats.dischargePerformance=dischargePerformance;
        return  dockStats;
    }


    public Dock(int id, CargoDispatcher cargoDispatcher, int dischargePerformance, int dischargeDelay) {
        this.dischargePerformance = dischargePerformance;
        this.cargoDispatcher = cargoDispatcher;
        this.id=id;
        this.dischargeDelay=dischargeDelay;


    }


    @Override
    public String call() throws Exception {

        Thread dockThread = Thread.currentThread();
        dockThread.setName("Dock#"+id);

        log.info("Dock#"+id+" is running with dischargeDelay="+dischargeDelay);
        while (true) {
            if (currentCargo==null) {
                log.debug("Dock#"+id+" is requesting next cargo from dispatcher");
                currentCargo = cargoDispatcher.getNextCargo();
                currentDischargeRate = currentCargo.getDischargePerformanceLimit() < dischargePerformance ? currentCargo.getDischargePerformanceLimit() : dischargePerformance;
            }
            try {
                sleep(dischargeDelay);
            } catch (InterruptedException e) {
                log.info("Dock#"+id+" stopped");
                break;
            }
            log.debug("Dock#"+id+" is discharging the ship#"+currentCargo.getId()+"   "+currentCargo.getCurrentLoad()+"/"+currentCargo.getMaxLoad()+ "  discharge rate="+currentDischargeRate+"("+dischargePerformance+")");
            if (!currentCargo.discharge(currentDischargeRate)) {
                // Ship is done. Request for next ship
                log.debug("Dock#"+id+" discharged the ship#"+currentCargo.getId());
                dockStats.totalDischargedShips++;
                currentCargo=null;
                currentDischargeRate=0;
            }
            dockStats.totalDischargedWeight+=currentDischargeRate;
            yield();
        }
        return "";
    }

    public int getDischargeDelay(){
        return dischargeDelay;
    }


}
