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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * enthält alle wichtigen Funktionen für die Arbeit mit der Mainstage
 */
public class Model {
    private static Model instance;
    private static Stage stage;//static nötig?

    //Ordner für Datenarbeit
    private final File dataDir = new File("data");

    //Dateien für Datenarbeit
    private final File fileTasks = new File(dataDir, "SimpleWriteTest.json");
    private final File filePlanning = new File(dataDir, "planning.json");

    private final SimpleListProperty<String> stringListPlanProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
    //Property für MainWindowView
    private final SimpleListProperty<String> taskListTodoProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final SimpleListProperty<Task> taskListAllProperty = new SimpleListProperty<Task>(FXCollections.observableArrayList());

    //Propertys für TaskCreationWindowView
    private final SimpleStringProperty newTaskNameProperty = new SimpleStringProperty("");
    private final SimpleStringProperty newTaskRepeatProperty = new SimpleStringProperty("0");
    private final SimpleBooleanProperty newTaskRolloverProperty = new SimpleBooleanProperty(false);
    private final SimpleStringProperty newTaskValidationProperty = new SimpleStringProperty();
    //private final SimpleBooleanProperty newTaskCheckNeedProperty;

    public Model(Stage stage) {
        Model.stage = stage;

        //Ordner für Datenspeicherung erstellen, falls noch nicht vorhanden
        if (!dataDir.exists()) {
            if (dataDir.mkdir()) {
                System.out.println("Data-Verzeichnis wurde neu erstellt!");
            } else {
                System.err.println("Data-Verzeichnis konnte nicht erstellt werden!");
            }
        }

        //Einlesen der Aufgabenliste bei Programmstart
        //TODO: Verhalten bei Fehler verbessern, sodass Nutzer diese sieht ohne Commandline
        try {
            taskListAllProperty().addAll(readTasksJson(fileTasks));
        } catch (IOException iOEx) {
            iOEx.printStackTrace();
        }

    }

    /**
     * @return Rückgabe von "true", nur wenn Aufgabe erfolgreich geschrieben werden konnte
     */
    public boolean writeNewTask() {

        //Validierung der Eingabeparameter
        boolean isValid = true;
        StringBuilder stringInvalid = new StringBuilder();
        //Validierung des Namens
        String name = newTaskNameProperty().getValue();
        //Test, ob Aufgabenname leer ist
        if (name.isEmpty()) {
            stringInvalid.append("Aufgabenname ist leer! \n");
            isValid = false;
        } else {
            //Test, ob Aufgabenname einzigartig ist
            for (Task task : taskListAllProperty.getValue()) {
                if (name.equals(task.getName())) {
                    stringInvalid.append("Aufgabenname ist schon vorhanden! \n");
                    isValid = false;
                }
            }
        }
        //Validierung Aufgabenwiederholung
        int repeat = -1;
        if (newTaskRepeatProperty().getValue().isEmpty()) {//Test ob in Wiederholung etwas geschrieben wurde
            stringInvalid.append("Wiederholung ist leer! \n");
            isValid = false;
        } else {
            //Test, ob Aufgabenwiederholung einem Integer entspricht
            try{
                repeat = Integer.parseInt(newTaskRepeatProperty().getValue());
                //Test, ob Wiederholung in akzeptablen Bereich ist
                if(repeat < 0 | repeat > 100) {
                    stringInvalid.append("Wiederholung außerhalb des Bereichs 0 - 99 \n");
                    isValid = false;
                }
            }catch(NumberFormatException numEx) {
                stringInvalid.append("Wiederholung ist keine Nummer! \n");
                isValid = false;
            }
        }

        //Rückgabe von "false", wenn Aufgabenparameter nicht valide sind
        if (isValid == false) {
            this.setNewTaskValidationProperty(stringInvalid.toString());
            return false;
        }

        //neue Liste in ListProperty einlesen
        List<Task> listNew = new ArrayList<>(taskListAllProperty.getValue());

        Task task = new Task(name, repeat, newTaskRolloverProperty.getValue());

        listNew.add(task);

        writeTasksJson(fileTasks, listNew);
        taskListAllProperty().getValue().add(task);
        return true;
    }

    //alle Listen und Datum einlesen
    public boolean readPlanningJson(File planningFile) {

    }

    public List<String> readTodoArray(JsonReader jsonReader) {

    }

    public List<String> readToday

    /**
     *
     * @param fileTasks
     * @param listTasks
     * @return Rückgabe von true, wenn alle Aufgaben erfolgreich in die Datei geschrieben wurden
     */
    public boolean writeTasksJson(File fileTasks, List<Task> listTasks) {
        try{

            if (!fileTasks.exists()) {
                if (!fileTasks.createNewFile()) {
                    System.err.println("Datenfile konnte nicht erstellt werden!");
                }
            }

            try (FileWriter fileWriter = new FileWriter(fileTasks);
                JsonWriter jsonWriter = new JsonWriter(fileWriter)) {
                jsonWriter.setIndent("    ");
                writeTaskArray(jsonWriter, listTasks);
                jsonWriter.flush();
            }
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
        return true;
    }

    public void writeTaskArray(JsonWriter jsonWriter, List<Task> listTasks) throws IOException {
        jsonWriter.beginArray();
        for (Task task : listTasks) {
            writeTask(jsonWriter, task);
        }
        jsonWriter.endArray();
    }

    public void writeTask(JsonWriter jsonWriter, Task task) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("name").value(task.getName());
        jsonWriter.name("repeat").value(Integer.toString(task.getRepeat()));
        jsonWriter.name("rollover").value(Boolean.toString(task.isRollover()));
        jsonWriter.endObject();
    }

    /**
     *
     * @param fileTasks
     * @return
     * @throws Exception
     */
    public ArrayList<Task> readTasksJson(File fileTasks) throws IOException {
        try (FileReader fileReader = new FileReader(fileTasks);
             JsonReader jsonReader = new JsonReader(fileReader)) {
            return readTasksArray(jsonReader);
        }

        //Leserechte für Datei sicherstellen sonst Fehlermeldung
    }

    public ArrayList<Task> readTasksArray(JsonReader reader) throws IOException{
        //was passiert bei leeren Array?
        ArrayList<Task> returnArray = new ArrayList<>();

        reader.beginArray();
        while(reader.hasNext()) {
           returnArray.add(readTask(reader));
        }
        reader.endArray();
        return returnArray;
    }

    public Task readTask(JsonReader reader) throws IOException{
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
                    default:
                        System.err.println("Model: Unbekanntes Aufgabenattribut bei lesen von JSON-File: " +
                                fileTasks + "!");
                        reader.nextString();
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

    public SimpleListProperty<String> stringListTodayProperty() {
       return stringListPlanProperty;
    }

    public SimpleListProperty<String> taskListTodoProperty() {
        return taskListTodoProperty;
    }

    public SimpleListProperty<Task> taskListAllProperty() {
        return taskListAllProperty;
    }

    public SimpleStringProperty newTaskNameProperty() {
        return newTaskNameProperty;
    }

    public SimpleStringProperty newTaskRepeatProperty() {
        return newTaskRepeatProperty;
    }

    public SimpleBooleanProperty newTaskActiveProperty() {
        return newTaskRolloverProperty;
    }

    public void setNewTaskValidationProperty(String stringInvalid) {
        newTaskValidationProperty().setValue(stringInvalid);
    }

    public SimpleStringProperty newTaskValidationProperty() {
        return newTaskValidationProperty;
    }

    /*
    public SimpleBooleanProperty newTaskCheckNeedProperty() {
        return newTaskCheckNeedProperty;
    }
    */
}