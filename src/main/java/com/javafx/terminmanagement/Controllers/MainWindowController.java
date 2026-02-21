package com.javafx.terminmanagement.Controllers;

import com.javafx.terminmanagement.Model;
import com.javafx.terminmanagement.Task;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;

public class MainWindowController {
    //List for dailyTab
    @FXML
    private ListView<Task> dailyList;

    //List for allTasksTab
    @FXML
    private ListView<Task> allList;

    @FXML
    private ListView<String> historyList;

    //createTab
    @FXML
    private TextField textFieldNameCreate;
    @FXML
    private TextField textFieldCreateRepeat;
    @FXML
    private RadioButton buttonSetCreateRollover;
    @FXML
    private Label validationCreateLabel;

    //changeTab
    @FXML
    private TextField textFieldNameChange;
    @FXML
    private TextField textFieldChangeRepeat;
    @FXML
    private RadioButton buttonSetChangeRolloverOn;
    @FXML
    private RadioButton buttonSetChangeRolloverOff;
    @FXML
    private Label validationChangeLabel;

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
    private Tab changeTaskTab;

    Model model;

    /**
     * Initialisierung des MainWindowViews, jedes Mal, wenn der MainWindowView geladen wird
     */
    public void initialize() {
        model = Model.getInstance();

        tabPane.getSelectionModel().select(dailyTab);

        /*
        createTaskTab.setOnSelectionChanged(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if (createTaskTab.isSelected()) model.resetNewTaskPropertys();
            }
        });
         */

        allList.itemsProperty().bind(model.taskListProperty());
        //model.taskListProperty().bind(model.taskListProperty());
        model.selectedTaskProperty().bind(allList.selectionModelProperty().getValue().selectedItemProperty());

        dailyList.itemsProperty().bind(model.dailyListProperty());

        //Validierungslabel bereinigen
        model.setNewTaskValidationProperty("");

        //Bindings for createTaskTab
        model.newTaskNameProperty().bindBidirectional(textFieldNameCreate.textProperty());
        model.newTaskRepeatProperty().bindBidirectional(textFieldCreateRepeat.textProperty());
        model.newTaskRolloverProperty().bindBidirectional(buttonSetCreateRollover.selectedProperty());
        validationCreateLabel.textProperty().bind(model.newTaskValidationProperty());

        //Bindings for changeTaskTab
        model.newTaskNameProperty().bindBidirectional(textFieldNameChange.textProperty());
        model.newTaskRepeatProperty().bindBidirectional(textFieldChangeRepeat.textProperty());
        model.newTaskRolloverProperty().bindBidirectional(buttonSetChangeRolloverOn.selectedProperty());
        validationChangeLabel.textProperty().bind(model.newTaskValidationProperty());

        //historyList.itemsProperty().bind(model.stringListHistoryProperty());
    }

    /**
     * Knopf um Aufgabenerstellungsfenster zu laden und anzuzeigen
     */
    @FXML
    public void onTaskCreateButtonClick() {
        System.out.println("MainWindowController:onTaskCreateButtonClick() -> button pressed");
        model.resetNewTaskPropertys();
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
        System.out.println("MainWindowController:onTaskChangedButtonClick() -> button pressed");
        //Hauptstage vom Model holen

        if (model.selectedTaskProperty().getValue() == null) {
            return;
        }

        model.loadSelectedTask();

        tabPane.getSelectionModel().select(changeTaskTab);
    }

    @FXML
    public void onTaskDeleteButtonClick() {
        System.out.println("MainWindowController:onTaskDeleteButtonClick() -> button pressed");
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
        System.out.println("(INFO) MainWindowController:onTaskSignInButtonClick() -> button pressed");

        if (!model.signInTask()) {
            System.out.println("(ERR) Model:onTaskSignInButtonClick() Couldn't sign in task!");
        }
    }

    /**
     * Knopf um ausgewählte Aufgabe aus Tagesplan auszutragen
     */
    @FXML
    public void onTaskSignOutButtonClick() {
        System.out.println("MainWindowController:onTaskSignOutButtonClick() -> button pressed");
        Model model = Model.getInstance();
        //System.out.println(model.selectedStringProperty().getValue());
        /*
        if (!model.writeSignOutTask()) {
            System.err.println("Aufgabe konnte nicht ausgetragen werden!");
        }
         */

    }

    /**
     * Knopf um ausgewählte Aufgabe als abgearbeitet zu speichern
     */
    @FXML
    public void onTaskDoneButtonClick() {
        System.out.println("MainWindowController:onTaskDoneButtonClick() -> button pressed");
        Model model = Model.getInstance();
        /*
        if (!model.writeDoneTask()) {
            System.err.println("Aufgabe konnte nicht fertiggestellt werden!");
        }
         */

    }

    /**
     * Knopf um erstellte Aufgabe zu Speichern
     */
    @FXML
    public void onSaveButtonClick() {
        System.out.println("MainWindowController:onSaveButtonClick() -> button pressed");
        if (createTaskTab.isSelected()) {
            if (model.writeNewTask()) tabPane.getSelectionModel().select(allTasksTab);
        } else {
            if (model.writeChangedTask()) tabPane.getSelectionModel().select(allTasksTab);
        }

    }
}