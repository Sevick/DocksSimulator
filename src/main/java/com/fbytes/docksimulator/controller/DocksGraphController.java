package com.fbytes.docksimulator.controller;

import com.fbytes.docksimulator.DocksSimulator;
import com.fbytes.docksimulator.model.Dock;
import com.fbytes.docksimulator.service.CargoProducer;
import com.fbytes.docksimulator.service.SeaPort;
import com.fbytes.docksimulator.service.StatsProducer;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;

import static java.lang.Thread.sleep;


public class DocksGraphController implements Initializable {

    public int uiUpdateDelay = 500;

    private Logger log=Logger.getLogger(this.getClass());
    StatsProducer statsProducer;
    DocksSimulator docksSimulator;

    private Thread uiUpdateThread;

    private ObservableList<XYChart.Series<Integer, Integer>> lineChartData = FXCollections.observableArrayList();
    private LineChart.Series<Integer, Integer> seriesTotalShipsDischarged = new LineChart.Series<>();
    private LineChart.Series<Integer, Integer> seriesTotalShipsProduced = new LineChart.Series<>();
    private LineChart.Series<Integer, Integer> seriesQueueLength = new LineChart.Series<>();
    private LocalTime startTime;


    @FXML
    private Button exitButton; // injected by FXMLLoader fx:id="exitButton"
    @FXML
    private Button runSimulationButton;
    @FXML
    private Button stopSimulationButton;

    @FXML
    private ToggleButton toggleShipProduction;


    @FXML
    private Slider docksCountSelector;
    @FXML
    private Label docksCountLabel;
    @FXML
    private Slider shipDelaySelector;
    @FXML
    private Label shipDelayLabel;
    @FXML
    private Slider dischargeDelaySelector;
    @FXML
    private Label dischargeDelayLabel;


    @FXML
    private Label shipsInQueueLabel;
    @FXML
    private BarChart docksChart;
    @FXML
    private LineChart seaPortChart;

    @FXML
    private LineChart shipsInQueueChart;


    @FXML
    private Button buttonDockException;

    @FXML
    private Button buttonShowStats;




    public DocksGraphController() {
        startTime = LocalTime.now();
    }

    protected void disableParameterChanges(){
        docksCountSelector.setDisable(true);
        shipDelaySelector.setDisable(true);
        dischargeDelaySelector.setDisable(true);
    }

    protected void enableParameterChanges(){
        docksCountSelector.setDisable(false);
        shipDelaySelector.setDisable(false);
        dischargeDelaySelector.setDisable(false);
    }


    @FXML
    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert exitButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'view/docks.fxml'.";

        exitButton.setOnAction((event) -> {
            System.out.println("exitButton button pressed");
            System.exit(0);
        });


        runSimulationButton.setOnAction((event) -> {
            log.debug("runSimulationButton button pressed");
            docksSimulator.initSimulator(getDockCount(), getShipDelay(), getDischargeDelay());
            docksSimulator.startSimulator();
            setStatsProducer(docksSimulator);
            disableParameterChanges();
            //reset();
            startUIupdates();
        });


        stopSimulationButton.setOnAction((event) -> {
            log.debug("stopSimulationButton button pressed");
            docksSimulator.stopSimulator();
            enableParameterChanges();
            stopUIupdates();
        });


        buttonShowStats.setOnAction((event)->{
            log.debug("buttonShowStats button pressed");
            //DocksStatsController docksStatsController=new DocksStatsController();
            //docksStatsController.showStatsViewer();
            showStatsViewer();
        });


        toggleShipProduction.setOnAction((event) -> {
            log.debug("toggleShipProduction button pressed");
            if (toggleShipProduction.isSelected()) {
                docksSimulator.startShipsProduction();
            } else {
                docksSimulator.stopShipsProduction();
            }
        });


        buttonDockException.setOnAction((event) -> {
            log.debug("buttonDockException button pressed");
            docksSimulator.simulateExceptionInOneDock();
        });


        docksCountSelector.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            log.debug("docksCountSelector:" + old_val.intValue() + "->" + new_val.intValue());
            docksCountLabel.setText(String.valueOf(new_val.intValue()));
        });

        shipDelaySelector.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            log.debug("shipDelaySelector: " + old_val.intValue() + "->" + new_val.intValue());
            shipDelayLabel.setText(String.valueOf(new_val.intValue()));
        });

        dischargeDelaySelector.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            log.debug("dischargeDelaySelector: " + old_val.intValue() + "->" + new_val.intValue());
            dischargeDelayLabel.setText(String.valueOf(new_val.intValue()));
        });

        shipsInQueueLabel.setText("-");
        docksChart.getYAxis().setAutoRanging(false);
        ((NumberAxis) docksChart.getYAxis()).setUpperBound(CargoProducer.MAX_DEADWEIGHT);
        docksChart.setCache(true);

        seriesTotalShipsDischarged.setName("totalShipsDischarged");
        seriesTotalShipsProduced.setName("totalShipsProduced");
        seaPortChart.getData().setAll(seriesTotalShipsDischarged,seriesTotalShipsProduced);


        seaPortChart.setCreateSymbols(false);
        seaPortChart.setCache(true);

        shipsInQueueChart.getData().setAll(seriesQueueLength);
        ((NumberAxis)shipsInQueueChart.getYAxis()).setForceZeroInRange(false);

/*
        DropShadow ds = new DropShadow();
        ds.setOffsetY(3.0);
        ds.setOffsetX(3.0);
        ds.setColor(Color.GRAY);
        seaPortChart.setEffect(ds);
*/
    }

    public void showStatsViewer() {
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stats.fxml"));
            root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Docks stats");
            stage.setScene(new Scene(root, 450, 450));
            stage.show();

            //hide this current window (if this is whant you want
            //((Node)(event.getSource())).getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected void reset(){
        seriesTotalShipsDischarged.getData().clear();
        seriesTotalShipsProduced.getData().clear();
    }


    public int getShipDelay() {
        return (int) shipDelaySelector.getValue();
    }

    public int getDockCount() {
        return (int) docksCountSelector.getValue();
    }

    public int getDischargeDelay(){
        return (int) dischargeDelaySelector.getValue();
    }



    public void updateStats() {
        try {
            CargoProducer.CargoProducerStats cargoProducerStats = statsProducer.getCargoProducerStats();
            SeaPort.SeaPortStats seaPortStats = statsProducer.getSeaPortStats();

            Platform.runLater(()->{
                    shipsInQueueLabel.setText(String.valueOf(seaPortStats.queueLength));

                    XYChart.Series seriesGraphDischargeRate = new XYChart.Series();
                    seriesGraphDischargeRate.setName("DischargeRate");

                    XYChart.Series seriesGraphDischargeLeft = new XYChart.Series();
                    seriesGraphDischargeLeft.setName("DischargeLeft");

                    for (Dock.DockStats curDockStat : seaPortStats.dockStatses) {
                        seriesGraphDischargeRate.getData().add(new XYChart.Data("Dock#" + curDockStat.id, curDockStat.currentDischargeRate));
                        seriesGraphDischargeLeft.getData().add(new XYChart.Data("Dock#" + curDockStat.id, curDockStat.currentShipWeightLeft));
                    }
                    docksChart.getData().setAll(seriesGraphDischargeRate, seriesGraphDischargeLeft);

                    int timeDelta = (int) java.time.Duration.between(startTime, LocalTime.now()).getSeconds();
                    seriesTotalShipsDischarged.getData().add(new XYChart.Data(String.valueOf(timeDelta), (int) seaPortStats.totalShipsDischarged));
                    seriesTotalShipsProduced.getData().add(new XYChart.Data(String.valueOf(timeDelta), (int) cargoProducerStats.totalShipsProduced));

                    if (seriesQueueLength.getData().size()>50)
                        seriesQueueLength.getData().remove(0);
                    seriesQueueLength.getData().add(new XYChart.Data(String.valueOf(timeDelta), (int) seaPortStats.queueLength));

                    //log.debug("totalShipsDischarged=" + dispatcherStats.totalShipsDischarged);
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
        log.debug("UI updated");
    }


    public void startUIupdates() {
        log.debug("Starting UI updates service");
        Task uiUpdateTask=new Task<Void>() {
                protected Void call() {
                    Thread.currentThread().setName(this.getClass().getName()+"UIupdate");
                    while(true) {
                        updateStats();
                        try {
                            sleep(uiUpdateDelay);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                    return null;
                }
        };

        uiUpdateThread=new Thread(uiUpdateTask);
        uiUpdateThread.start();
        log.info(this.getClass().getName()+"UIupdate thread started");
    }


    public void stopUIupdates(){
        if (uiUpdateThread!=null)
            uiUpdateThread.interrupt();
    }


    public void setStatsProducer(StatsProducer statsProducer) {
        this.statsProducer = statsProducer;
    }

    public DocksSimulator getDocksSimulator() {
        return docksSimulator;
    }

    public void setDocksSimulator(DocksSimulator docksSimulator) {
        this.docksSimulator = docksSimulator;
    }
}
