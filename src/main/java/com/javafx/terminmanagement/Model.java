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
import java.util.Date;
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

    //Propertys für die Auswahl von einem Element aus einem ListView
    private final SimpleObjectProperty<Task> selectedTaskProperty = new SimpleObjectProperty<>();

    private final SimpleListProperty<String> stringListPlanProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
    //Property für MainWindowView
    private final SimpleListProperty<String> stringListTodoProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final SimpleListProperty<Task> taskListAllProperty = new SimpleListProperty<>(FXCollections.observableArrayList());

    //Propertys für TaskCreationWindowView
    private final SimpleStringProperty newTaskNameProperty = new SimpleStringProperty("");
    private final SimpleStringProperty newTaskRepeatProperty = new SimpleStringProperty("0");
    private final SimpleBooleanProperty newTaskRolloverProperty = new SimpleBooleanProperty(false);
    private final SimpleStringProperty newTaskValidationProperty = new SimpleStringProperty();
    //private final SimpleBooleanProperty newTaskCheckNeedProperty;

    public Model(Stage stage) {
        Model.stage = stage;
        instance = this;

        //Ordner für Dateiarbeit erstellen, falls noch nicht vorhanden
        if (!dataDir.exists()) {
            if (dataDir.mkdir()) {
                System.out.println("Data-Verzeichnis wurde erstellt!");
            } else {
                System.err.println("Model Initialisierung: Data-Verzeichnis existiert nicht und konnte auch nicht erstellt werden!");
                Platform.exit();
            }
        }

        //Dateien für Dateiarbeit erstellen, falls noch nicht vorhanden
        //falls dies nicht möglich ist, wird das Programm beendet
        if (!fileTasks.exists()) {
            try {
                if (fileTasks.createNewFile()) {
                    System.out.println(fileTasks.toString() + " wurde erstellt.");
                } else {
                    System.err.println(fileTasks.toString() + " konnte nicht erstellt werden und Programm wird beendet!");
                    Platform.exit();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (!filePlanning.exists()) {
            try {
                if (filePlanning.createNewFile()) {
                    System.out.println(filePlanning.toString() + " wurde erstellt.");
                } else {
                    System.err.println(filePlanning.toString() + " konnte nicht erstellt werden und Programm wird beendet!");
                    Platform.exit();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        //Fähigkeit zum Schreiben und Lesen der Datendateien testen
        if (!fileTasks.canRead()) {
            System.err.println(fileTasks + " kann nicht gelesen werden, weshalb das Programm beendet wird!");
            Platform.exit();
        }
        if (!fileTasks.canWrite()) {
            System.err.println(fileTasks + " kann nicht geschrieben werden, weshalb das Programm beendet wird!");
            Platform.exit();
        }
        if (!filePlanning.canRead()) {
            System.err.println(filePlanning + " kann nicht gelesen werden, weshalb das Programm beendet wird!");
            Platform.exit();
        }
        if (!filePlanning.canWrite()) {
            System.err.println(filePlanning + " kann nicht geschrieben werden, weshalb das Programm beendet wird!");
            Platform.exit();
        }

        //Einlesen der Aufgabenliste bei Programmstart
        //TODO: Verhalten bei Fehler verbessern, sodass Nutzer diese sieht ohne Commandline
        try {
            //vollständige Taskliste mit Werten füllen
            taskListAllProperty().addAll(readTasksJson(fileTasks));

            //Stringliste für Tagesplan und noch zu machende Aufgaben mit Werten füllen
            readPlanningJson(filePlanning);

            //noch zu machende Aufgaben mit vollständiger Taskliste abgleichen
            //TODO: mit sortierter Liste könnte eine sehr viel elegantere Lösung gefunden werden
            //TODO: String löschen, wenn keine dazupassende Aufgabe gefunden wird
            boolean exists;
            for (String stringTodo : stringListTodoProperty) {
                exists = false;
                for (int i = 0; i < taskListAllProperty().getValue().size(); i++) {
                    if (stringTodo.equals(taskListAllProperty().getValue().get(i).getName())) {
                        exists = true;
                        break;
                    }
                }
                if (exists == false) {
                    System.err.println("Aufgabe " + stringTodo + " in Todo exisitiert nicht!");
                }
            }
            //Tagesplan Namen mit noch zu machenden Aufgaben abgleichen
            for (String stringPlan : stringListPlanProperty) {
                exists = false;
                for (String stringTask : stringListTodoProperty) {
                    if (stringPlan.equals(stringTask)) {
                        exists = true;
                        break;
                    }
                }
                if (exists == false) {
                    System.out.println("Aufgabe " + stringPlan + " in Aufgabenplan exisitiert nicht!");
                }
            }

        } catch (IOException iOEx) {
            iOEx.printStackTrace();
        }

    }

    /**
     * Gibt die einzige Instanz des Modells weiter
     *
     * @return Singleton Model
     */
    public static Model getInstance() {
        //Fehler, wenn keine Hauptstage übergeben wurde
        if (stage == null) {
            System.err.println("Model: Bei Programmstart wurde keine Stage uebergeben!");
            Platform.exit();
        }

        //neue Instanz von Model wird erstellt wenn noch keine Instanzen davon exisiteren
        if (instance == null) {
            instance = new Model(stage);
        }

        return instance;
    }

    /** Neue Aufgabe aus newTask...Property's lesen, in File schreiben und taskListAllProperty aktualisieren
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

        //neue Aufgabenliste erstellen, die geschrieben werden soll
        List<Task> listNew = new ArrayList<>();
        //alle vorhandenen Aufgaben aus Aufgabenfile einlesen und in neue Liste schreiben
        try {
            listNew.addAll(readTasksJson(fileTasks));
        } catch (IOException ex) {
            System.out.println("In der writeNewTask-Methode vom Model konnte die Aufgabenliste nicht eingelesen werden!");
            ex.printStackTrace();
        }
        //neue Aufgabe in neue Liste schreiben
        listNew.add(new Task(name, repeat, newTaskRolloverProperty.getValue()));

        //Aufgaben in File schreiben,und falls dies nicht funktioniert false zurückgeben
        if (!writeTasksJson(fileTasks, listNew)) return false;
        //nach erfolgreichem Schreiben taskListAllProperty neu
        taskListAllProperty().setAll(listNew);
        //taskListAllProperty().getValue().addAll(listNew);
        return true;
    }

    //Aufgabe aus selectedTaskProperty lesen, aus Aufgabenfile löschen und taskListAllProperty aktualisieren
    public boolean writeDeletedTask() {
        if (selectedTaskProperty().getValue() == null) return false;
        Task deletedTask = selectedTaskProperty().getValue();

        //Aufgabe aus Aufgabenliste löschen
        ArrayList<Task> listNew = new ArrayList<>(taskListAllProperty());
        listNew.remove(deletedTask);
        //neue Aufgabenliste schreiben
        if (!writeTasksJson(fileTasks, listNew)) {
            return false;
        }
        taskListAllProperty().setAll(listNew);

        //Aufgabe aus TodoListe und planListe löschen und in Datei schreiben, bei vorhandensein
        if (stringListTodoProperty().contains(deletedTask.getName())) {
            ArrayList<String> todoListNew = new ArrayList<>(stringListTodoProperty());
            ArrayList<String> planListNew = new ArrayList<>(stringListPlanProperty());
            todoListNew.remove(deletedTask.getName());
            if (stringListPlanProperty().contains(deletedTask.getName())) {
                planListNew.remove(deletedTask.getName());
            }
            if (!writePlanningJson(filePlanning, todoListNew, planListNew)) {
                return false;
            }
            stringListPlanProperty().setAll(planListNew);
            stringListTodoProperty().setAll(todoListNew);
        }

        return true;
    }

    public boolean writeSignInPlan() {
        //Test, ob eine Aufgabe ausgewählt wurde
        if (selectedTaskProperty().getValue() == null) {
            return false;
        }

        Task taskToSignIn = selectedTaskProperty().getValue();
        ArrayList<String> newList = new ArrayList<>(stringListPlanProperty());

        //Test, ob Aufgabe schon in Plan eingetragen wurde
        if (newList.contains(taskToSignIn.getName())) {
            return true;
        } else {
            newList.add(taskToSignIn.getName());
        }
        //Aufgabe in filePlan schreiben und bei Erfolg in Plannungsliste eintragen
        if (writePlanningJson(filePlanning, newList, stringListTodoProperty())) {
            stringListPlanProperty().getValue().add(taskToSignIn.getName());
        } else {
            return false;
        }

        return true;
    }
    /*
    public boolean writeNewTodoString()
    public boolean writeDeleteTodoString()
    writeNewPlanString()
    writeDeletePlanString()
     */

    private boolean writePlanningJson(File filePlanning, List<String> listPlan, List<String> listTodo) {
        try (FileWriter fileWriter = new FileWriter(filePlanning);
             JsonWriter jsonWriter = new JsonWriter(fileWriter)) {
            jsonWriter.setIndent("    ");
            jsonWriter.beginObject();

            //writeDate()
            writePlanArray(jsonWriter, listPlan);
            writeTodoArray(jsonWriter, listTodo);

            jsonWriter.endObject();

            //Alle gepufferten Daten fertig schreiben
            jsonWriter.flush();
            fileWriter.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        return true;
    }

    private void writeTodoArray(JsonWriter jsonWriter, List<String> listTodo) throws IOException {
        jsonWriter.name("todo");
        jsonWriter.beginArray();

        for (String stringTodo : listTodo) {
            jsonWriter.value(stringTodo);
        }

        jsonWriter.endArray();
    }

    private void writePlanArray(JsonWriter jsonWriter, List<String> listPlan) throws IOException {
        jsonWriter.name("plan");
        jsonWriter.beginArray();

        for (String stringPlan : listPlan) {
            jsonWriter.value(stringPlan);
        }

        jsonWriter.endArray();
    }

    private void writeDate(JsonWriter jsonWriter, Date date) {

    }


    //alle Listen und Datum einlesen in die zugehörigen Propertys
    private boolean readPlanningJson(File filePlanning) {
        if (filePlanning.length() == 0) return true;
        try (FileReader fileReader = new FileReader(filePlanning);
             JsonReader jsonReader = new JsonReader(fileReader)) {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "date":
                        jsonReader.nextString();
                        break;
                    case "plan":
                        stringListPlanProperty.addAll(readPlanArray(jsonReader));
                        break;
                    case "todo":
                        stringListTodoProperty.addAll(readTodoArray(jsonReader));
                        break;
                    default:
                        System.err.println("Unbekannter Name in Datei: " + filePlanning);
                        break;
                }
            }
            jsonReader.endObject();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //TODO: Rückgabewert überdenken, da bie Fehlerfall Exception gethrowed wird
        return true;
    }

    public List<String> readTodoArray(JsonReader jsonReader) throws IOException {
        ArrayList<String> retArray = new ArrayList<>();

        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            retArray.add(jsonReader.nextString());
        }
        jsonReader.endArray();

        return retArray;
    }

    public List<String> readPlanArray(JsonReader jsonReader) throws IOException {
        ArrayList<String> retArray = new ArrayList<>();

        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            retArray.add(jsonReader.nextString());
        }
        jsonReader.endArray();

        return retArray;
    }

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

                //Alle Streams fertig schreiben
                jsonWriter.flush();
                fileWriter.flush();
            }
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            return false;
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
     * @throws IOException
     */
    public ArrayList<Task> readTasksJson(File fileTasks) throws IOException {
        if (fileTasks.length() == 0) return new ArrayList<Task>();
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



    public Stage getStage() {
        return stage;
    }

    public SimpleObjectProperty<Task> selectedTaskProperty() {
        return selectedTaskProperty;
    }

    public SimpleListProperty<String> stringListPlanProperty() {
       return stringListPlanProperty;
    }

    public SimpleListProperty<String> stringListTodoProperty() {
        return stringListTodoProperty;
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