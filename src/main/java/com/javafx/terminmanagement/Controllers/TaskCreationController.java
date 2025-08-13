package com.javafx.terminmanagement.Controllers;

import com.javafx.terminmanagement.Model;
import com.javafx.terminmanagement.StartApplication;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class TaskCreationController {
    @FXML
    TextField textfieldName;
    @FXML
    TextField textfieldRepeat;
    @FXML
    RadioButton buttonSetTrue;

    /**
     * Initialisierung des Aufgabenerstellungsfensters
     */
    public void initialize() {
        Model model = Model.getInstance();
        //Binding von Aufgabenname
        model.newTaskNameProperty().bindBidirectional(textfieldName.textProperty());
        //Binding von Aufgabenwiederholung
        model.newTaskRepeatProperty().bindBidirectional(textfieldRepeat.textProperty());
        //Binding von Aufgabenrollover
        model.newTaskActiveProperty().bindBidirectional(buttonSetTrue.selectedProperty());
    }

    /**
     * Knopf um erstellte Aufgabe zu Speichern
     */
    @FXML
    protected void onSaveButtonClick() {
        Model model = Model.getInstance();
        model.writeNewTask();
    }

    /**
     * Knopf um Aufgabenübersicht anzuzeigen
     */
    @FXML
    protected void onCancelButtonClick(){

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
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}