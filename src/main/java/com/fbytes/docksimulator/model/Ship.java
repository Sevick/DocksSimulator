package com.fbytes.docksimulator.model;

/**
 * Created by S on 08.08.2016.
 */


public class Ship implements Cargo{
    private int id;
    private int maxLoad;
    private int currentLoad;
    private int dischargePerformanceLimit;

    public Ship(int id,int maxLoad, int dischargePerformanceLimit, int currentLoad) {
        this.maxLoad = maxLoad;
        this.dischargePerformanceLimit = dischargePerformanceLimit;
        this.currentLoad = currentLoad;
        this.id=id;
    }

    public int getId() {
        return id;
    }

    public boolean discharge(int dischargeAmount) throws Exception {
        if (dischargeAmount>dischargePerformanceLimit)
            throw new Exception("Unable to exceed ship's dischargePerformanceLimit. Ship limit "+dischargePerformanceLimit+"  requested: "+dischargeAmount);

        currentLoad-=currentLoad>=dischargeAmount ? dischargeAmount : currentLoad;
        if (currentLoad>0)
            return true;
        else
            return false;
    }

    public int getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentLoad(int currentLoad) {
        this.currentLoad = currentLoad;
    }

    public int getMaxLoad() {
        return maxLoad;
    }

    public void setMaxLoad(int maxLoad) {
        this.maxLoad = maxLoad;
    }


    public int getDischargePerformanceLimit() {
        return dischargePerformanceLimit;
    }

    public void setDischargePerformanceLimit(int dischargePerformanceLimit) {
        this.dischargePerformanceLimit = dischargePerformanceLimit;
    }


}
