package com.fbytes.docksimulator.service;

/**
 * Created by S on 09.08.2016.
 */
public interface StatsProducer{
    public SeaPort.DispatcherStats getSeaPosrtStat();
    public CargoProducer.CargoProducerStats getCargoProducerStats();
}