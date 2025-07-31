package com.javafx.terminmanagement;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;

/**
 * enthält alle wichtigen Funktionen für die Arbeit mit der Mainstage
 */
public class Model {
    private static Model instance;
    private static Stage stage;//static nötig?


    private File dataDir;

    //Property für MainWindowView
    private final SimpleListProperty<Task> currentTasks;
    //Propertys für TaskCreationWindowView
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
        //Unit-Test in dem Task geschrieben wird und danach gelesen wird und die Tasks miteinander verglichen werden
        //Validierung??
        return currentTasks.add(new Task(newTaskNameProp.getValue(), newTaskActiveProp.getValue()));
    }

    public Task readTask() {
        //richtiges File öffnen
        //File einlesen
            //Informationen in Graph speichern(aufpassen das Graph nicht zu groß wird wegen Speicher)
        File testdat = new File(new File("data"), "SimpleTaskTest.json");

        String name = "";
        boolean active = false;

        try{
            FileReader reader = new FileReader(testdat);
            JsonReader jread = new JsonReader(reader);
            jread.beginObject();

            while(jread.hasNext()) {
                switch(jread.nextName()){
                    case("name"): name = jread.nextString();
                        break;
                    case("active"): active = Boolean.valueOf(jread.nextString());
                        break;
                }
            }

        }catch(Exception ex) {
            ex.printStackTrace();
        }

        return new Task(name, active);
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