package com.javafx.terminmanagement;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

import java.util.LinkedList;

/**
 * enthält alle wichtigen Funktionen für die Arbeit mit der Mainstage
 */
public class Model {
    private static Model instance;
    private static Stage stage;//static nötig?

    private final SimpleListProperty<Task> currentTasks;
    private final SimpleStringProperty newTaskNameProp;
    private final SimpleBooleanProperty newTaskActiveProp;

    public Model(Stage stage) {
        Model.stage = stage;
        //Initialisierung der aktuellen Taskliste
        currentTasks = new SimpleListProperty<Task>(FXCollections.observableArrayList());

        newTaskNameProp = new SimpleStringProperty();
        newTaskNameProp.set("");

        newTaskActiveProp = new SimpleBooleanProperty();
        newTaskActiveProp.set(true);


    }

    public boolean writeNewTask() {
        //Validierung??
        return currentTasks.add(new Task(newTaskNameProp.getValue(), newTaskActiveProp.getValue()));
    }

    public Task readTask(String name) {
        //richtiges File öffnen
        //File einlesen
            //Informationen in Graph speichern(aufpassen das Graph nicht zu groß wird wegen Speicher)

        return new Task("yeye", false);
    }

    /**
     * Gibt die einzige Instanz des Modells weiter
     *
     * @return Singleton Model
     *
     */
    public static Model getInstance() {
        //Fehler wenn keine Hauptstage übergeben wurde
        if(stage == null) {
            System.err.println("Model: Bei Programmstart wurde keine Stage uebergeben!");
            Platform.exit();
        }

        //neue Instanz von Model wird erstellt wenn noch keine Instanzen davon exisiteren
        if (instance == null) {
            instance = new Model(stage);
        }

        return instance;
    }

    public Stage getStage() {
        return stage;
    }

    public SimpleListProperty<Task> getCurrentTasks() {
        return currentTasks;
    }

    public SimpleBooleanProperty getNewTaskActiveProp() {
        return newTaskActiveProp;
    }

    public SimpleStringProperty getNewTaskNameProp() {
        return newTaskNameProp;
    }
}