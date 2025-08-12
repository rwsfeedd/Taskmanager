package com.javafx.terminmanagement;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

/**
 * enthält alle wichtigen Funktionen für die Arbeit mit der Mainstage
 */
public class Model {
    private static Model instance;
    private static Stage stage;//static nötig?


    private File dataDir;

    //Property für MainWindowView
    private final SimpleListProperty<Task> currentTasks  = new SimpleListProperty<Task>(FXCollections.observableArrayList());
    //Propertys für TaskCreationWindowView
    private final SimpleStringProperty newTaskNameProperty = new SimpleStringProperty("");
    private final SimpleStringProperty newTaskRepeatProperty = new SimpleStringProperty("1");
    private final SimpleBooleanProperty newTaskRolloverProperty  = new SimpleBooleanProperty(false);
    //private final SimpleBooleanProperty newTaskCheckNeedProperty;

    public Model(Stage stage) {
        Model.stage = stage;
    }

    public void writeJson() {
        try{
            File testdat = new File(new File("data"), "SimpleWriteTest.json");
            //File erstellung bei Endprodukt nicht vergessen
            testdat.createNewFile();
            try(FileWriter fileWriter = new FileWriter(testdat);
                JsonWriter jsonWriter = new JsonWriter(fileWriter)) {
                jsonWriter.setIndent("    ");
                writeTaskArray(jsonWriter);
            }

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void writeTaskArray(JsonWriter jsonWriter) throws Exception{
        jsonWriter.beginArray();
        writeTask(jsonWriter);
        jsonWriter.endArray();
    }

    public void writeTask(JsonWriter jsonWriter) throws Exception{
       jsonWriter.beginObject();
       jsonWriter.name("name").value(newTaskNameProperty.getValue());
       jsonWriter.name("repeat").value(newTaskRepeatProperty.getValue());
       jsonWriter.name("rollover").value(newTaskRolloverProperty.getValue().toString());
       jsonWriter.endObject();
    }

    public boolean writeNewTask() {
        //Validierung
            //ist Name nicht leerer String oder null?
            //ist Aufgabenname schon vorhanden?
            //ist Wiederholung eine Zahl?
            //bei Wiederholung Untergrenze=0 und Obergrenze?


        //neue Liste in ListProperty einlesen
        writeJson();
        return currentTasks.add(new Task(newTaskNameProperty.getValue(), Integer.parseInt(newTaskRepeatProperty.getValue()), newTaskRolloverProperty.getValue()));
    }


    public ArrayList<Task> readJson() throws Exception{
        File testdat = new File(new File("data"), "SimpleTaskTest.json");
        FileReader fileReader = new FileReader(testdat);
        JsonReader jsonReader = new JsonReader(fileReader);

        //try-finally block damit Ressourcen nach Lesen freigegeben werden
        try{
            return readTasksArray(jsonReader);
        }finally {
            jsonReader.close();
            fileReader.close();
        }

    }

    public ArrayList<Task> readTasksArray(JsonReader reader) throws Exception{
        //was passiert bei leeren Array?
        ArrayList<Task> returnArray = new ArrayList<>();

        reader.beginArray();
        while(reader.hasNext()) {
           returnArray.add(readTask(reader));
        }
        reader.endArray();
        return returnArray;
    }

    public Task readTask(JsonReader reader) throws Exception{
        //richtiges File öffnen
        //File einlesen
            //Informationen in Graph speichern(aufpassen das Graph nicht zu groß wird wegen Speicher)

        String name = "";
        int repeat = 0;
        boolean rollover = false;

        try{
            reader.beginObject();

            while(reader.hasNext()) {
                switch(reader.nextName()){
                    case("name"): name = reader.nextString();
                        break;
                    case ("repeat"):
                        repeat = Integer.parseInt(reader.nextString());
                        break;
                    case ("rollover"):
                        rollover = Boolean.parseBoolean(reader.nextString());
                        break;
                }
            }
            reader.endObject();

        }catch(Exception ex) {
            ex.printStackTrace();
        }

        return new Task(name, repeat, rollover);
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

    public SimpleStringProperty newTaskNameProperty() {
        return newTaskNameProperty;
    }

    public SimpleStringProperty newTaskRepeatProperty() {
        return newTaskRepeatProperty;
    }

    public SimpleBooleanProperty newTaskRolloverProperty() {
        return newTaskRolloverProperty;
    }

    /*
    public SimpleBooleanProperty newTaskCheckNeedProperty() {
        return newTaskCheckNeedProperty;
    }
    */
}