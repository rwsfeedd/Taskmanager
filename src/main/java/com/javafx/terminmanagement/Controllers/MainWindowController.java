package com.javafx.terminmanagement.Controllers;

import com.javafx.terminmanagement.Model;
import com.javafx.terminmanagement.StartApplication;
import com.javafx.terminmanagement.Task;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MainWindowController {
    @FXML
    private ListView<String> dailyList;
    @FXML
    private ListView<Task> allList;
    @FXML
    private ListView<String> historyList;



    @FXML
    private TextField textFieldName;
    @FXML
    private TextField textFieldRepeat;
    @FXML
    private RadioButton buttonSetRollover;
    @FXML
    private Label validationLabel;
    @FXML
    private Button taskSaveButton;


    @FXML
    private TabPane tabPane;
    @FXML
    private Tab previousTab;
    @FXML
    private Tab dailyTab;
    @FXML
    private Tab nextTab;
    @FXML
    private Tab allTasksTab;
    @FXML
    private Tab createTaskTab;

    @FXML
    private Button signOutButton;
    @FXML
    private Button taskDoneButton;
    @FXML
    private Button taskCreateButton;
    @FXML
    private Button taskChangeButton;
    @FXML
    private Button taskDeleteButton;
    @FXML
    private Button signInButton;

    Model model;

    /**
     * Initialisierung des MainWindowViews, jedes Mal, wenn der MainWindowView geladen wird
     */
    public void initialize() {
        model = Model.getInstance();

        Model model = Model.getInstance();
        allList.itemsProperty().bind(model.taskListAllProperty());
        model.selectedTaskProperty().bind(allList.selectionModelProperty().getValue().selectedItemProperty());

        dailyList.itemsProperty().bind(model.stringListPlanProperty());
        model.selectedStringProperty().bind(dailyList.selectionModelProperty().getValue().selectedItemProperty());


        //Validierungslabel bereinigen
        model.setNewTaskValidationProperty("");

        //Binding von Aufgabenname
        model.newTaskNameProperty().bindBidirectional(textFieldName.textProperty());
        //Binding von Aufgabenwiederholung
        model.newTaskRepeatProperty().bindBidirectional(textFieldRepeat.textProperty());
        //Binding von Aufgabenrollover
        model.newTaskRolloverProperty().bindBidirectional(buttonSetRollover.selectedProperty());
        //Binding für Label um Nutzer invalide Aufgabe zu zeigen
        validationLabel.textProperty().bind(model.newTaskValidationProperty());

        historyList.itemsProperty().bind(model.stringListHistoryProperty());
    }

    /**
     * Knopf um Aufgabenübersicht zu laden
     */
    @FXML
    public void onTaskOverviewButtonClick() {

        //Hauptstage vom Mastercontroller holen
        Model controller = Model.getInstance();
        Stage stage = controller.getStage();

        try{
            //Die Objekthierarchie aus dem zugehörigen XML Dokument laden
            FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("taskOverviewView.fxml"));
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
    public void onTaskSignOutButtonClick() {
        Model model = Model.getInstance();
        //System.out.println(model.selectedStringProperty().getValue());
        if (!model.writeSignOutTask()) {
            System.err.println("Aufgabe konnte nicht ausgetragen werden!");
        }

    }

    /**
     * Knopf um ausgewählte Aufgabe als abgearbeitet zu speichern
     */
    @FXML
    public void onTaskDoneButtonClick() {
        Model model = Model.getInstance();
        if (!model.writeDoneTask()) {
            System.err.println("Aufgabe konnte nicht fertiggestellt werden!");
        }
    }

    /**
     * Knopf um Aufgabenerstellungsfenster zu laden und anzuzeigen
     */
    @FXML
    public void onTaskCreateButtonClick() {
        tabPane.getSelectionModel().select(createTaskTab);
        /*
        if(model.createTabClosedProperty().get()) {
            System.out.println("Oufff");
            tabPane.getSelectionModel().select(createTaskTab);
            model.createTabClosedProperty().set(false);
        } else {
            tabPane.getSelectionModel().select(createTaskTab);
        }
        */
    }

    /**
     * Knopf um Aufgabenbearbeitungsfenster zu laden und anzuzeigen
     */
    @FXML
    public void onTaskChangeButtonClick() {

        //Hauptstage vom Model holen
        Model model = Model.getInstance();
        Stage stage = model.getStage();

        if (model.selectedTaskProperty().getValue() == null) {
            return;
        }
        try {
            //Die Objekthierarchie aus dem zugehörigen XML Dokument laden
            FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("taskChangeView.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 640, 480);

            //Stage initialisieren und darstellen
            stage.setTitle("Terminmanagement");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onTaskDeleteButtonClick() {
        Model model = Model.getInstance();
        if (!model.writeDeletedTask()) {
            System.out.println("Fehler beim Löschen der Aufgabe!");
        }

    }

    /**
     * Knopf um ausgewählte Aufgabe in Tagesplan einzutragen
     */
    @FXML
    public void onTaskSignInButtonClick() {
        Model model = Model.getInstance();
        if (!model.writeSignInTask()) {
            System.out.println("Fehler beim Eintragen der Aufgabe in den Aufgabenplan");
        }
    }

    /**
     * Knopf um Hauptfenster zu laden und anzuzeigen
     */
    @FXML
    public void onReturnButtonClick() {
        //Hauptstage vom Model holen
        Model controller = Model.getInstance();
        Stage stage = controller.getStage();

        try {
            //Die Objekthierarchie aus dem zugehörigen XML Dokument laden
            FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("mainWindowView.fxml"));
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
     * Knopf um erstellte Aufgabe zu Speichern
     */
    @FXML
    public void onSaveButtonClick() {
        Model model = Model.getInstance();
        if (model.writeNewTask()) {
            tabPane.getSelectionModel().select(allTasksTab);
        }
    }

    /**
     * Knopf um Aufgabenübersicht anzuzeigen
     */
    @FXML
    public void onCancelButtonClick() {
        Model model = Model.getInstance();

        Stage stage = model.getStage();

        try {
            //Die Objekthierarchie aus dem zugehörigen XML Dokument laden
            FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("taskOverviewView.fxml"));
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