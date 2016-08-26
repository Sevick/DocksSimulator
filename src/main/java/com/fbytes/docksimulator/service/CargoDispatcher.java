package com.fbytes.docksimulator.service;

import com.fbytes.docksimulator.model.Cargo;
import com.fbytes.docksimulator.model.Ship;

/**
 * Created by S on 08.08.2016.
 */
abstract public interface CargoDispatcher {

    abstract public Cargo getNextCargo();
    abstract public void addCargoToQueue(Cargo newCargo);
    abstract public int getQueueLength();
}
