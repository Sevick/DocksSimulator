package com.fbytes.docksimulator.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by S on 02.09.2016.
 */
public class StatsData{
    LocalDateTime dt;
    int dockID;
    int cargoID;
    int cargoLoad;

    public StatsData(LocalDateTime dt, int dockID, int cargoID, int cargoLoad) {
        this.dt = dt;
        this.dockID = dockID;
        this.cargoID = cargoID;
        this.cargoLoad = cargoLoad;
    }

    public String getDtAsString(){
        DateTimeFormatter dtFormat=DateTimeFormatter.ofPattern("HH:mm:ss   dd/MM/yy");
        return dtFormat.format(dt);
    }

    public Integer getDockIDInteger() {
        return new Integer(dockID);
    }

    public Integer getCargoIDInteger() {
        return new Integer(cargoID);
    }

    public Integer getCargoLoadInteger() {
        return new Integer(cargoLoad);
    }
}