package com.javafx.terminmanagement.Controllers;

import com.javafx.terminmanagement.Model;
import com.javafx.terminmanagement.StartApplication;
import com.javafx.terminmanagement.Task;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.stage.Stage;

public class TaskOverviewController {
    @FXML
    private ListView<Task> taskList;

    public void initialize() {
        Model model = Model.getInstance();
        taskList.itemsProperty().bind(model.taskListAllProperty());
        model.selectedTaskProperty().bind(taskList.selectionModelProperty().get().selectedItemProperty());
    }

    /**
     * Knopf um Aufgabenerstellungsfenster zu laden und anzuzeigen
     */
    @FXML
    protected void onTaskCreateButtonClick() {

        //Hauptstage vom Mastercontroller holen
        Model model = Model.getInstance();
        Stage stage = model.getStage();

        try {
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

    @FXML
    protected void onTaskDeleteButtonClick() {
        Model model = Model.getInstance();
        if (model.selectedTaskProperty().get() == null) {
            //TODO Nutzer Fehler anzeigen mit Label
            System.out.println("keine Aufgabe ausgewählt!");
        } else {
            model.writeDeletedTask();
            //System.out.println(model.selectedTaskProperty().getValue().toString());
        }

    }

    /**
     * Knopf um ausgewählte Aufgabe in Tagesplan einzutragen
     */
    @FXML
    protected void onTaskSignInButtonClick() {

    }

    /**
     * Knopf um Hauptfenster zu laden und anzuzeigen
     */
    @FXML
    protected void onReturnButtonClick() {
        //Hauptstage vom Mastercontroller holen
        Model controller = Model.getInstance();
        Stage stage = controller.getStage();

        try {
            //Die Objekthierarchie aus dem zugehörigen XML Dokument laden
            FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("mainWindow-view.fxml"));
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
