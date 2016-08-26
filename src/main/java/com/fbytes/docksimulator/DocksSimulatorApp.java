package com.fbytes.docksimulator;

import com.fbytes.docksimulator.controller.DocksGraphController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class DocksSimulatorApp extends Application{

    DocksSimulator docksSimulator=new DocksSimulator();
    DocksGraphController graphController;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/docks.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Seaport simulation");
        Scene mainScene=new Scene(root, 1024, 800);
        primaryStage.setScene(mainScene);


        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                graphController.stopUIupdates();
                docksSimulator.stopSimulator();
                System.exit(0);
            }
        });

        primaryStage.show();

        graphController = (DocksGraphController)loader.getController();
        graphController.setDocksSimulator(docksSimulator);

    }


    public static void main(String[] args) {
        DocksSimulator docksSimulator=new DocksSimulator();
        launch(args);
    }
}
