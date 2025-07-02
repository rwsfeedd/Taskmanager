package com.javafx.terminmanagement.Controllers;

import com.javafx.terminmanagement.StartApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class TaskCreationController {

    @FXML
    protected void onTaskCreateButtonClick() {

    }

    /**
     * Hauptfenster laden und anzeigen
     * @throws IOException
     */
    @FXML
    protected void onCancelButtonClick() throws IOException {

        //Hauptstage vom Mastercontroller holen
        MasterController controller = MasterController.getInstance();
        Stage stage = controller.getStage();

        //Die Objekthierarchie aus dem zugehörigen XML Dokument laden
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("mainWindow-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        }catch(Exception e) {
            e.printStackTrace();
        }


        //Stage initialisieren und darstellen
        stage.setTitle("Terminmanagement");
        stage.setScene(scene);
        stage.show();

    }
}