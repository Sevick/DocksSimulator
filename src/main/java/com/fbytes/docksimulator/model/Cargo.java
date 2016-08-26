package com.fbytes.docksimulator.model;

/**
 * Created by S on 08.08.2016.
 */
public interface Cargo {

    public int getId();
    public boolean discharge(int dischargeAmount) throws Exception;
    public int getCurrentLoad();
    public void setCurrentLoad(int currentLoad);
    public int getMaxLoad();
    public void setMaxLoad(int maxLoad) ;
    public int getDischargePerformanceLimit();
    public void setDischargePerformanceLimit(int dischargePerformanceLimit);
}
