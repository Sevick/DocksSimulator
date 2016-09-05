package com.fbytes.docksimulator.stats;

import com.fbytes.docksimulator.model.StatsData;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by S on 31.08.2016.
 */
public class DataCollector {

    private Logger log=Logger.getLogger(this.getClass());

    private static List<StatsData> statsCollection=Collections.synchronizedList(new ArrayList());
    private ObservableList unsyncObservableList=FXCollections.observableList(statsCollection);
    private ObservableList observableList=FXCollections.synchronizedObservableList(unsyncObservableList);

    static DataCollector instance;



    private DataCollector() {
    }

    static public DataCollector getInstance(){
        if (instance==null)
            instance=new DataCollector();

        return instance;
    }


    public void addData(LocalDateTime dt, int dockID, int cargoID, int cargoLoad) {
        //log.debug("adding data to stats");
        observableList.add(0,new StatsData(dt,dockID,cargoID,cargoLoad));
    }


    public ObservableList getObservableDataCollection(){
        return observableList;
    }

}
