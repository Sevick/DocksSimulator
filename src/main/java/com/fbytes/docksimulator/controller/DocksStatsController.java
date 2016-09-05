package com.fbytes.docksimulator.controller;

import com.fbytes.docksimulator.model.StatsData;
import com.fbytes.docksimulator.stats.DataCollector;
import com.fbytes.docksimulator.stats.StatsAgg;
import com.fbytes.docksimulator.stats.StatsAggCollector;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

/**
 * Created by S on 01.09.2016.
 */
public class DocksStatsController implements Initializable {

    private Logger log=Logger.getLogger(this.getClass());
    public int uiUpdateDelay = 500;
    private Thread uiUpdateThread;
    private DataCollector dataCollector=DataCollector.getInstance();

    private SimpleStringProperty recordsCountProperty=new SimpleStringProperty(new String("-"));

    @FXML
    private TableView tableDocksStats;

    @FXML
    private Button buttonGroupByCargo;

    @FXML
    private Label labelRecordsCount;


    public void showStatsViewer() {
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/stats.fxml"));
            root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Docks stats");
            stage.setScene(new Scene(root, 450, 450));
            stage.show();
            startUIupdates();

            //hide this current window (if this is whant you want
            //((Node)(event.getSource())).getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void startUIupdates() {
        log.debug("Starting UI updates service");

        if (labelRecordsCount == null)
            log.error("labelRecordsCount was not injected check FXML");
        else
            log.debug("labelRecordsCount was injected");

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


    protected  void updateStats(){
        Platform.runLater(() -> {
            try {
                String recCountStr = String.valueOf(dataCollector.getObservableDataCollection().size());
                //labelRecordsCount.setText(recCountStr);
                recordsCountProperty.set(recCountStr);
            }
            catch (Exception e){
                log.error("Exception in DockStatsController UI update thread",e);
            }
        });
    }


    @FXML
    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert tableDocksStats != null : "fx:id=\"tableDocksStats\" was not injected: check your FXML file 'view/stats.fxml'.";

        log.debug("initializing");

        buttonGroupByCargo.setOnAction((event)->{
            log.debug("buttonGroupByCargo pressed");
            Scene scene = new Scene(new Group(),800,600);
            Stage stage = new Stage();
            stage.setTitle("Group by cargo");

            Map<Integer,StatsAgg> cargoMap = (Map<Integer,StatsAgg>) dataCollector.getObservableDataCollection().stream().
                    collect(Collectors.groupingBy(StatsData::getCargoIDInteger,new StatsAggCollector<StatsData>()
                    ));

            ObservableList<Map.Entry<Integer, StatsAgg>> items = FXCollections.observableArrayList(cargoMap.entrySet());

            Label labelTitle=new Label();
            labelTitle.setText("Aggregation by cargo");

            Label footerTitle=new Label();
            footerTitle.setText("Footer");


            TableView<Map.Entry<Integer, StatsAgg>> table = new TableView<Map.Entry<Integer, StatsAgg>>();

            TableColumn aggCol = new TableColumn("Cargo");
            aggCol.setMinWidth(100);

            aggCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<Integer, StatsAgg>, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<Integer, StatsAgg>, String> p) {
                    return new SimpleStringProperty(String.valueOf(p.getValue().getKey()));
                }
            });

            TableColumn countCol = new TableColumn("DischargesCount");
            countCol.setMinWidth(100);
            countCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<Integer, StatsAgg>, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<Integer, StatsAgg>, String> p) {
                    return new SimpleStringProperty(String.valueOf(p.getValue().getValue().getCount()));
                }
            });

            TableColumn sumCol = new TableColumn("DischargesSum");
            sumCol.setMinWidth(100);
            sumCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<Integer, StatsAgg>, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<Integer, StatsAgg>, String> p) {
                    return new SimpleStringProperty(String.valueOf(p.getValue().getValue().getSum()));
                }
            });

            TableColumn avgCol = new TableColumn("DischargesAvg");
            avgCol.setMinWidth(100);
            avgCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<Integer, StatsAgg>, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<Integer, StatsAgg>, String> p) {
                    return new SimpleStringProperty(String.valueOf(p.getValue().getValue().getSum()/p.getValue().getValue().getCount()));
                }
            });

            table.setItems(items);
            table.getColumns().addAll(aggCol,countCol,sumCol,avgCol);

            final VBox vbox = new VBox();
            vbox.setSpacing(5);
            vbox.setPadding(new Insets(10, 0, 0, 10));
            vbox.getChildren().addAll(labelTitle, table, footerTitle);

            ((Group) scene.getRoot()).getChildren().addAll(vbox);
            stage.setScene(scene);
            stage.show();
        });



        ((TableColumn)tableDocksStats.getColumns().get(0)).setCellValueFactory(new PropertyValueFactory<StatsData,Integer>("dtAsString"));
        ((TableColumn)tableDocksStats.getColumns().get(1)).setCellValueFactory(new PropertyValueFactory<StatsData,Integer>("dockIDInteger"));
        ((TableColumn)tableDocksStats.getColumns().get(2)).setCellValueFactory(new PropertyValueFactory<StatsData,Integer>("cargoIDInteger"));
        ((TableColumn)tableDocksStats.getColumns().get(3)).setCellValueFactory(new PropertyValueFactory<StatsData,Integer>("cargoLoadInteger"));


        ((TableColumn)tableDocksStats.getColumns().get(0)).setStyle( "-fx-alignment: CENTER; -fx-padding: 2;");
        ((TableColumn)tableDocksStats.getColumns().get(1)).setStyle( "-fx-alignment: CENTER-RIGHT; -fx-padding: 2;");
        ((TableColumn)tableDocksStats.getColumns().get(2)).setStyle( "-fx-alignment: CENTER-RIGHT; -fx-padding: 2;");
        ((TableColumn)tableDocksStats.getColumns().get(3)).setStyle( "-fx-alignment: CENTER-RIGHT; -fx-padding: 2;");

        tableDocksStats.setItems(dataCollector.getObservableDataCollection());


        labelRecordsCount.setText("-");
        labelRecordsCount.textProperty().bindBidirectional(recordsCountProperty);
        log.debug("initialized");

        startUIupdates();
    }
}
