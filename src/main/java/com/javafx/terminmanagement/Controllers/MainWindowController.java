package com.javafx.terminmanagement.Controllers;

import com.javafx.terminmanagement.Model;
import com.javafx.terminmanagement.Task;
import javafx.beans.value.ObservableListValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class MainWindowController {
    //List for dailyTab
    @FXML
    private ListView<String> dailyList;

    //List for allTasksTab
    @FXML
    private TableView<Task> allTableView;

    @FXML
    private TableView<Task> dailyTableView;

    @FXML
    private ListView<String> historyList;

    //createTab
    @FXML
    private TextField textFieldName;
    @FXML
    private TextField textFieldCreateRepeat;
    @FXML
    private RadioButton buttonSetCreateRollover;
    @FXML
    private Label validationCreateLabel;

    //changeTab
    @FXML
    private Label labelName;
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

        allTableView.setItems(model.taskListProperty());

        TableColumn<Task, Integer> columnId = new TableColumn<>("Id");
        columnId.setCellValueFactory(new PropertyValueFactory<>("idProp"));
        TableColumn<Task, String> columnName = new TableColumn<>("Name");
        columnId.setCellValueFactory(new PropertyValueFactory<>("nameProp"));
        allTableView.getColumns().addAll(columnId, columnName);
        //allTableView.getColumns().get(0).tableViewProperty().get().itemsProperty().bind(model.taskListProperty());
        //allTableView.getColumns().get(1).tableViewProperty().get().itemsProperty().bind(model.taskListProperty());
        //allMap.itemsProperty();



        //model.taskListProperty().bind(model.taskListProperty());
        model.selectedTaskProperty().bind(allTableView.selectionModelProperty().getValue().selectedItemProperty());

        //dailyList.itemsProperty().bind(model.stringListPlanProperty());
        //model.selectedStringProperty().bind(dailyTableView.selectionModelProperty().getValue().selectedItemProperty());


        //Validierungslabel bereinigen
        model.setNewTaskValidationProperty("");

        //Bindings for createTaskTab
        model.newTaskNameProperty().bindBidirectional(textFieldName.textProperty());
        model.newTaskRepeatProperty().bindBidirectional(textFieldCreateRepeat.textProperty());
        model.newTaskRolloverProperty().bindBidirectional(buttonSetCreateRollover.selectedProperty());
        validationCreateLabel.textProperty().bind(model.newTaskValidationProperty());

        //Bindings for changeTaskTab
        labelName.textProperty().bind(model.newTaskNameProperty());
        model.newTaskRepeatProperty().bindBidirectional(textFieldChangeRepeat.textProperty());
        model.newTaskRolloverProperty().bindBidirectional(buttonSetChangeRolloverOn.selectedProperty());
        validationChangeLabel.textProperty().bind(model.newTaskValidationProperty());

        //historyList.itemsProperty().bind(model.stringListHistoryProperty());
    }

    /**
     * Knopf um ausgewählte Aufgabe aus Tagesplan auszutragen
     */
    @FXML
    public void onTaskSignOutButtonClick() {
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
        Model model = Model.getInstance();
        /*
        if (!model.writeDoneTask()) {
            System.err.println("Aufgabe konnte nicht fertiggestellt werden!");
        }
         */

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

        tabPane.getSelectionModel().select(changeTaskTab);
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
        /*
        if (!model.writeSignInTask()) {
            System.out.println("Fehler beim Eintragen der Aufgabe in den Aufgabenplan");
        }
         */
    }

    /**
     * Knopf um erstellte Aufgabe zu Speichern
     */
    @FXML
    public void onSaveButtonClick() {
        Model model = Model.getInstance();
        if (createTaskTab.isSelected()) {
            if (model.writeNewTask()) tabPane.getSelectionModel().select(allTasksTab);
        } else {
            if (model.writeChangedTask()) tabPane.getSelectionModel().select(allTasksTab);
        }

    }
}