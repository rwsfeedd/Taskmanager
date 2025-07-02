package com.javafx.terminmanagement.Controllers;

import com.javafx.terminmanagement.StartApplication;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class MainWindowController {
    @FXML
    private ListView<String> taskList;

    /**
     * initialisierung der Aufgabenliste
     */
    public void initialize() {
        taskList.setItems(FXCollections.observableArrayList("Jeff", "John"));
    }

    /**
     * Aufgabenerstellungsfenster laden und anzeigen
     */
    @FXML
    protected void onTaskCreateButtonClick(){

        //Hauptstage vom Mastercontroller holen
        MasterController controller = MasterController.getInstance();
        Stage stage = controller.getStage();

        //Die Objekthierarchie aus dem zugehörigen XML Dokument laden
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("taskCreationWindow-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //Stage initialisieren und darstellen
        stage.setTitle("Terminmanagement");
        stage.setScene(scene);
        stage.show();
    }
}