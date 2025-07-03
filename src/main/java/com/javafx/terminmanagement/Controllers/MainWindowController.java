package com.javafx.terminmanagement.Controllers;

import com.javafx.terminmanagement.Model;
import com.javafx.terminmanagement.StartApplication;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

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
        Model controller = Model.getInstance();
        Stage stage = controller.getStage();

        try{
            //Die Objekthierarchie aus dem zugehörigen XML Dokument laden
            FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("taskCreationWindow-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 640, 480);

            //Stage initialisieren und darstellen
            stage.setTitle("Terminmanagement");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}