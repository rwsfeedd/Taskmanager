package com.javafx.terminmanagement.Controllers;

import com.javafx.terminmanagement.Model;
import com.javafx.terminmanagement.StartApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class MainWindowController {
    @FXML
    private ListView<String> taskList;

    /**
     * Initialisierung des MainWindowViews, jedes Mal, wenn der MainWindowView geladen wird
     */
    public void initialize() {
        Model model = Model.getInstance();
        taskList.itemsProperty().bind(model.stringListTodayProperty());
    }

    /**
     * Knopf um Aufgabenübersicht zu laden
     */
    @FXML
    protected void onTaskOverviewButtonClick(){

        //Hauptstage vom Mastercontroller holen
        Model controller = Model.getInstance();
        Stage stage = controller.getStage();

        try{
            //Die Objekthierarchie aus dem zugehörigen XML Dokument laden
            FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("taskOverview-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 640, 480);

            //Stage initialisieren und darstellen
            stage.setTitle("Terminmanagement");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Knopf um ausgewählte Aufgabe aus Tagesplan auszutragen
     */
    @FXML
    protected void onTaskSignOutButtonClick() {

    }

    /**
     * Knopf um ausgewählte Aufgabe als abgearbeitet zu speichern
     */
    @FXML
    protected void onTaskDoneButtonClick() {

    }
}