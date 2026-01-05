package com.javafx.terminmanagement;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * enthält alle wichtigen Funktionen für die Arbeit mit der Mainstage
 */
public class Model {
    private static Model instance;
    private static Stage stage;

    //internationales Datumsformat nach ISO 8601, mit dem alle lese- und schreibvorgänge von Datumsangaben getätigt werden
    public static final DateTimeFormatter dateFormat = DateTimeFormatter.ISO_LOCAL_DATE;
    //Datum, für das der Aufgabenplan gemacht wurde. wird bei Programmstart aktualisiert
    private LocalDate planDate;

    //Ordner für Datenarbeit
    private final File dataDir = new File("data");

    //Dateien für Datenarbeit
    private final File fileTasks = new File(dataDir, "tasks.json");
    private final File filePlanning = new File(dataDir, "planning.json");

    //Property für die Auswahl von einem Element aus ListView von mainWindow
    private final SimpleObjectProperty<String> selectedStringProperty = new SimpleObjectProperty<>();
    //Property für die Auswahl von einem Element aus ListView von taskOverview
    private final SimpleObjectProperty<Task> selectedTaskProperty = new SimpleObjectProperty<>();

    private final SimpleListProperty<String> stringListPlanProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
    //Property für MainWindowView
    private final SimpleListProperty<String> stringListTodoProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final SimpleListProperty<Task> taskListAllProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final SimpleListProperty<String> stringListHistoryProperty = new SimpleListProperty<>(FXCollections.observableArrayList());

    //Propertys for TaskCreationTab and TaskChangeTab
    private final SimpleStringProperty newTaskNameProperty = new SimpleStringProperty("");
    private final SimpleStringProperty newTaskRepeatProperty = new SimpleStringProperty("0");
    private final SimpleBooleanProperty newTaskRolloverProperty = new SimpleBooleanProperty(false);
    private final SimpleStringProperty newTaskValidationProperty = new SimpleStringProperty();

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

        //Einlesen der Aufgabenliste und der Stringlisten bei Programmstart
        try {
            //vollständige Taskliste mit Werten füllen
            taskListAllProperty().addAll(readTasksJson(fileTasks));

            //Stringliste für Tagesplan, noch zu machende Aufgaben und Datum mit Werten füllen
            readPlanningJson(filePlanning);

            //planDate mit aktuellem Datum initialisieren, falls dieses nicht in dem planningFile eingelesen wurde
            if (planDate == null) planDate = LocalDate.now();

            //Aufgaben aus Taskliste mit Informationen aus planning.json anreichern
            boolean exists;
            HashSet<String> stringRemove = new HashSet<>();
            for (String stringTodo : stringListTodoProperty().getValue()) {
                exists = false;
                for (int i = 0; i < taskListAllProperty().getValue().size(); i++) {
                    if (stringTodo.equals(taskListAllProperty().getValue().get(i).getName())) {
                        taskListAllProperty().getValue().get(i).setTodo(true);
                        exists = true;
                        break;
                    }
                }
                if (exists == false) {
                    //Aufgabe ist nicht in taskList aber in todoarray in planning.json
                    stringRemove.add(stringTodo);
                    System.err.println("Aufgabe " + stringTodo + " aus Todo existiert nicht!");
                }
            }
            for (String stringPlan : stringListPlanProperty().getValue()) {
                exists = false;
                for (int i = 0; i < taskListAllProperty().getValue().size(); i++) {
                    if (stringPlan.equals(taskListAllProperty().getValue().get(i).getName())) {
                        taskListAllProperty().getValue().get(i).setPlanned(true);
                        exists = true;
                        break;
                    }
                }
                if (exists == false) {
                    //Aufgabe ist nicht in taskList aber in planarray in planning.json
                    stringRemove.add(stringPlan);
                    System.err.println("Aufgabe " + stringPlan + " aus Aufgabenplan existiert nicht!");
                }
            }
            //Aufgaben die in planning.json existieren, aber keine dahinterliegende Aufgabe besitzen, löschen
            if (!stringRemove.isEmpty()) {
                ArrayList<String> planListNew = new ArrayList<>(stringListPlanProperty().getValue());
                ArrayList<String> todoListNew = new ArrayList<>(stringListPlanProperty().getValue());
                planListNew.removeAll(stringRemove);
                todoListNew.removeAll(stringRemove);
                if (writePlanningJson(filePlanning, planDate, planListNew, todoListNew)) {
                    stringListPlanProperty().setAll(planListNew);
                    stringListTodoProperty().setAll(todoListNew);
                } else {
                    System.out.println("Aufgaben aus dem Aufgabenplan, die nicht in der Aufgabenliste existieren, konnten nicht aus dem Planfile gelöscht werden!");
                }
            }

            //Testen, ob der Tag des Aufgabenplans noch aktuell ist und wenn nicht Aufgabenliste updaten
            LocalDate currentDate = LocalDate.now();
            if (planDate.until(currentDate, ChronoUnit.DAYS) > 0) {
                ArrayList<Task> taskListNew = new ArrayList<>(taskListAllProperty());

                //temporäre Aufgabenliste aktualisieren
                Task currentTask; //temporäre Variable um Zugriffe auf taskListNew zu verringern
                for (int i = 0; i < taskListNew.size(); i++) {
                    //aktueller Aufgabe in temporäre Variable lesen
                    currentTask = taskListNew.get(i);

                    //geplante Aufgaben in temporärer Aufgabenliste updaten
                    if (currentTask.isPlanned() && !currentTask.isRollover()) {
                        taskListNew.get(i).setPlanned(false);
                    }

                    //check, ob Aufgabe eine fällige Wiederholungsaufgabe ist und wenn ja, in temporärem Aufgabenplan updaten
                    if (currentTask.getRepeat() < 1) continue;
                    if (currentTask.isTodo()) continue;
                    if (currentTask.getDateLastDone() != null) {
                        if (currentTask.getDateLastDone().until(currentDate, ChronoUnit.DAYS) < currentTask.getRepeat())
                            continue;
                    }
                    taskListNew.get(i).setTodo(true);
                    taskListNew.get(i).setPlanned(true);
                }

                //temporären Aufgabenplan und Todoliste initialisieren
                ArrayList<String> todoListNew = new ArrayList<>();
                ArrayList<String> planListNew = new ArrayList<>();
                //temporären Aufgabenplan und Todoliste mit Werten füllen
                for (Task task : taskListNew) {
                    if (task.isTodo()) todoListNew.add(task.getName());
                    if (task.isPlanned()) planListNew.add(task.getName());
                }

                //planning.json aktualisieren mit aktuellem Datum und aktuellem Aufgabenplan
                if (writePlanningJson(filePlanning, currentDate, planListNew, todoListNew)) {
                    //in Model hinterlegtes Datum updaten
                    planDate = currentDate;
                    //Propertylisten von Aufgabenplan, Aufgabenliste und Todoliste updaten
                    taskListAllProperty().getValue().setAll(taskListNew);
                    stringListTodoProperty().getValue().setAll(todoListNew);
                    stringListPlanProperty().getValue().setAll(planListNew);
                } else {
                    System.out.println("Aufgabenplan konnte nicht aktualisiert werden!");
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
        StringBuilder stringInvalid = new StringBuilder();
        //Validierung des Namens
        String name = newTaskNameProperty().getValue();
        //Test, ob Aufgabenname leer ist
        if (name.isEmpty()) {
            stringInvalid.append("Aufgabenname ist leer! \n");
        } else {
            //Test, ob Aufgabenname einzigartig ist
            for (Task task : taskListAllProperty().getValue()) {
                if (name.equals(task.getName())) {
                    stringInvalid.append("Aufgabenname ist schon vorhanden! \n");
                }
            }
        }
        //Validierung Aufgabenwiederholung
        int repeat = -1;
        if (newTaskRepeatProperty().getValue().isEmpty()) {//Test ob in Wiederholung etwas geschrieben wurde
            stringInvalid.append("Wiederholung ist leer! \n");
        } else {
            //Test, ob Aufgabenwiederholung einem Integer entspricht
            try{
                repeat = Integer.parseInt(newTaskRepeatProperty().getValue());
                //Test, ob Wiederholung in akzeptablen Bereich ist
                if (repeat < 0 | repeat > 200) {
                    stringInvalid.append("Wiederholung ist keine valide Nummer \n");
                }
            }catch(NumberFormatException numEx) {
                stringInvalid.append("Wiederholung ist keine Nummer! \n");
            }
        }
        //Rückgabe von "false", wenn Aufgabenparameter nicht valide sind
        if (!stringInvalid.isEmpty()) {
            this.setNewTaskValidationProperty(stringInvalid.toString());
            return false;
        }

        //neue Aufgabenliste erstellen mit allen Aufgaben
        List<Task> listNew = new ArrayList<>(taskListAllProperty());

        //neue Aufgabe in neue Liste schreiben
        Task newTask = new Task(name, repeat, newTaskRolloverProperty().getValue());
        listNew.add(newTask);

        //Aufgaben in File schreiben,und falls dies nicht funktioniert false zurückgeben
        if (!writeTasksJson(fileTasks, listNew)) return false;
        //nach erfolgreichem Schreiben taskListAllProperty neu populieren
        taskListAllProperty().setAll(listNew);

        //Wiederholungsaufgabe in todo und plan schreiben
        if (repeat > 0) {
            ArrayList<String> newTodoList = new ArrayList<>(stringListPlanProperty().get());
            ArrayList<String> newPlanList = new ArrayList<>(stringListPlanProperty().get());
            newTodoList.add(name);
            newPlanList.add(name);
            if (writePlanningJson(filePlanning, planDate, newPlanList, newTodoList)) {
                stringListPlanProperty().setAll(newPlanList);
                stringListTodoProperty().setAll(newTodoList);
                int taskIndex = taskListAllProperty().get().indexOf(newTask);
                taskListAllProperty().getValue().get(taskIndex).setPlanned(true);
                taskListAllProperty().getValue().get(taskIndex).setTodo(true);
            } else {
                System.out.println("Wiederholungsaufgabe konnte nicht im filePlanning gespeichert werden!");
                return false;
            }
        }
        return true;
    }

    public void loadChangeTask() {
        Task changeTask = selectedTaskProperty().getValue();
        newTaskNameProperty().setValue(changeTask.getName());
        newTaskRepeatProperty().setValue(Integer.toString(changeTask.getRepeat()));
        newTaskRolloverProperty().set(changeTask.isRollover());
    }

    /**
     * @return true, wenn Aufgabe nicht verändert wurde oder die Aufgabe geändert wurde und erfolgreich geschrieben wurde
     * <br> false, wenn die Aufgabe nicht geschrieben werden konnte
     */
    public boolean writeChangedTask() {
        //Validierung der Eingabeparameter
        StringBuilder stringInvalid = new StringBuilder();
        //Validation if Task is selected
        if (newTaskNameProperty.getValue().isEmpty()) stringInvalid.append("Keine Aufgabe ausgewählt! \n");
        //Validierung Aufgabenwiederholung
        int repeat = 0;
        if (newTaskRepeatProperty().getValue().isEmpty()) {//Test ob in Wiederholung etwas geschrieben wurde
            stringInvalid.append("Wiederholung ist leer! \n");
        } else {
            //Test, ob Aufgabenwiederholung einem Integer entspricht
            try {
                repeat = Integer.parseInt(newTaskRepeatProperty().getValue());
                //Test, ob Wiederholung in akzeptablen Bereich ist
                if (repeat < 0 | repeat > 200) {
                    stringInvalid.append("Wiederholung ist keine valide Nummer \n");
                }
            } catch (NumberFormatException numEx) {
                stringInvalid.append("Wiederholung ist keine Nummer! \n");
            }
        }
        //Rückgabe von "false", wenn Aufgabenparameter nicht valide sind
        if (!stringInvalid.isEmpty()) {
            this.setNewTaskValidationProperty(stringInvalid.toString());
            return false;
        }
        //Wenn die Aufgabe nicht verändert wurde, muss auch nichts neu in die Dateien geschrieben werden
        Task changedTask = new Task(selectedTaskProperty().getValue().getName(), repeat, newTaskRolloverProperty().get());
        if (selectedTaskProperty().getValue().getDateLastDone() != null)
            changedTask.setDateLastDone(selectedTaskProperty().getValue().getDateLastDone());
        if (selectedTaskProperty().getValue().equals(changedTask)) {
            return true;
        }

        //Bei Änderung der Aufgabenwiederholung, muss getestet werden ob sich etwas in der Todoliste oder der Planliste ändern
        if (selectedTaskProperty().getValue().getRepeat() != changedTask.getRepeat()) {//Wiederholung wurde geändert
            if (selectedTaskProperty().getValue().getRepeat() == 0) {//Wiederholung wurde von 0 auf positive Zahl geändert
                if (changedTask.getDateLastDone() == null) {
                    changedTask.setTodo(true);
                    changedTask.setPlanned(true);
                } else {
                    if (changedTask.getDateLastDone().until(planDate, ChronoUnit.DAYS) >= changedTask.getRepeat()) {
                        changedTask.setTodo(true);
                        changedTask.setPlanned(true);
                    }
                }
            } else {//Wiederholung wurde von positiver Zahl auf 0 oder andere positive Zahl geändert
                if (changedTask.getRepeat() == 0) {//Wiederholung wurde von positiver Zahl zu 0 geändert
                    changedTask.setTodo(false);
                    changedTask.setPlanned(false);
                } else {//Wiederholung wurde von einer positiven Zahl zu einer anderen positiven Zahl geändert
                    if (changedTask.getDateLastDone() == null) {
                        changedTask.setTodo(true);
                        changedTask.setPlanned(true);
                    } else {
                        if (changedTask.getDateLastDone().until(planDate, ChronoUnit.DAYS) >= changedTask.getRepeat()) {
                            changedTask.setTodo(true);
                            changedTask.setPlanned(true);
                        } else {
                            changedTask.setTodo(false);
                            changedTask.setPlanned(false);
                        }
                    }
                }
            }
        }
        if (changedTask.isTodo() != stringListTodoProperty().contains(changedTask.getName()) ||
                changedTask.isPlanned() != stringListPlanProperty().contains(changedTask.getName())) {
            ArrayList<String> listPlanNew = new ArrayList<>(stringListPlanProperty().getValue());
            ArrayList<String> listTodoNew = new ArrayList<>(stringListPlanProperty().getValue());

            if (changedTask.isPlanned()) {
                listPlanNew.add(changedTask.getName());
            } else {
                listPlanNew.remove(changedTask.getName());
            }

            if (changedTask.isTodo()) {
                listTodoNew.add(changedTask.getName());
            } else {
                listTodoNew.remove(changedTask.getName());
            }

            if (!writePlanningJson(filePlanning, planDate, listPlanNew, listTodoNew)) return false;
            stringListPlanProperty().setAll(listPlanNew);
            stringListTodoProperty().setAll(listTodoNew);
        }

        //temporären Aufgabenplan erstellen und aktualisieren
        ArrayList<Task> listTaskNew = new ArrayList<>(taskListAllProperty().getValue());
        listTaskNew.remove(selectedTaskProperty().getValue());
        listTaskNew.add(changedTask);
        //Aufgaben in File schreiben,und falls dies nicht funktioniert false zurückgeben
        if (!writeTasksJson(fileTasks, listTaskNew)) return false;
        //nach erfolgreichem Schreiben taskListAllProperty neu populieren
        taskListAllProperty().setAll(listTaskNew);
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
        ArrayList<String> todoListNew = new ArrayList<>(stringListTodoProperty().getValue());
        ArrayList<String> planListNew = new ArrayList<>(stringListPlanProperty().getValue());
        boolean stringListChanged = false;
        if (stringListTodoProperty().contains(deletedTask.getName())) {
            todoListNew.remove(deletedTask.getName());
            stringListChanged = true;
        }
        if (stringListPlanProperty().contains(deletedTask.getName())) {
            planListNew.remove(deletedTask.getName());
            stringListChanged = true;
        }
        if (stringListChanged) {
            if (!writePlanningJson(filePlanning, planDate, todoListNew, planListNew)) {
                return false;
            }
            stringListPlanProperty().setAll(planListNew);
            stringListTodoProperty().setAll(todoListNew);
        }

        return true;
    }

    public boolean writeSignInTask() {
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
            //in Aufgabenliste schreiben, dass Aufgabe in Aufgabenplan ist
            taskListAllProperty().getValue().get(taskListAllProperty().getValue().indexOf(taskToSignIn)).setPlanned(true);

            newList.add(taskToSignIn.getName());
        }
        //Aufgabe in filePlan schreiben und bei Erfolg in Plannungsliste eintragen
        if (writePlanningJson(filePlanning, planDate, newList, stringListTodoProperty())) {
            stringListPlanProperty().getValue().add(taskToSignIn.getName());
        } else {
            return false;
        }

        return true;
    }

    public boolean writeSignOutTask() {
        //Test, ob eine Aufgabe ausgewählt wurde
        if (selectedStringProperty().getValue() == null) {
            return false;
        }

        //temporäre Variablen erstellen und mit Werten füllen
        String taskToSignOut = selectedStringProperty().getValue();
        ArrayList<String> newList = new ArrayList<>(stringListPlanProperty());
        //ausgewählte Aufgabe aus temporärer Liste entfernen
        newList.remove(taskToSignOut);

        //temporäre Liste in filePlanning schreiben und Propertys updaten
        if (writePlanningJson(filePlanning, planDate, newList, stringListTodoProperty().getValue())) {
            //stringListProperty für mainWindow updaten
            stringListPlanProperty().getValue().setAll(newList);
            //taskListProperty für taskOverview updaten
            int indexTaskToSignOut = findIndexOfTaskByName(taskToSignOut, taskListAllProperty());
            if (indexTaskToSignOut != -1) {
                taskListAllProperty().getValue().get(indexTaskToSignOut).setPlanned(false);
            } else {
                System.out.println("Aufgabe aus Aufgabenplan gelöscht, die nicht in Aufgabenliste exisitiert!");
            }

        } else {
            return false;
        }

        return true;
    }

    public boolean writeDoneTask() {
        //Test, ob eine Aufgabe ausgewählt wurde
        if (selectedStringProperty().getValue() == null) {
            return false;
        }

        //temporäre Variablen erstellen und mit Werten füllen
        String taskDone = selectedStringProperty().getValue();
        ArrayList<String> newTodoList = new ArrayList<>(stringListTodoProperty());
        ArrayList<String> newPlanList = new ArrayList<>(stringListPlanProperty());

        //ausgewählte Aufgabe aus temporären Listen entfernen
        newTodoList.remove(taskDone);
        newPlanList.remove(taskDone);

        //temporäre Liste in filePlanning schreiben und Propertys updaten
        if (writePlanningJson(filePlanning, planDate, newPlanList, newTodoList)) {
            //stringListProperty für mainWindow updaten
            stringListPlanProperty().getValue().setAll(newPlanList);
            //stringListTodoProperty updaten
            stringListTodoProperty().getValue().setAll(newTodoList);
            //taskListProperty für taskOverview updaten
            int indexTaskDone = findIndexOfTaskByName(taskDone, taskListAllProperty());
            if (indexTaskDone != -1) {
                taskListAllProperty().getValue().get(indexTaskDone).setTodo(false);
                taskListAllProperty().getValue().get(indexTaskDone).setPlanned(false);

                taskListAllProperty().getValue().get(indexTaskDone).setDateLastDone(LocalDate.now());
                if (!writeTasksJson(fileTasks, taskListAllProperty())) {
                    System.err.println("Datum konnte nicht geschrieben werden, weil es Fehler beim Schreiben in das Aufgabenfile gab!");
                    Platform.exit();
                }
            } else {
                System.out.println("Aufgabe aus Aufgabenplan gelöscht, die nicht in Aufgabenliste exisitiert!");
            }

        } else {
            return false;
        }


        return true;
    }

    private boolean writePlanningJson(File filePlanning, LocalDate planDate, List<String> listPlan, List<String> listTodo) {
        try (FileWriter fileWriter = new FileWriter(filePlanning);
             JsonWriter gsonWriter = new JsonWriter(fileWriter)) {

            JSONWriter jsonWriter = new JSONWriter(gsonWriter);

            gsonWriter.beginObject();

            jsonWriter.writeDate("planDate", planDate);
            jsonWriter.writeStringArray("plan", listPlan);
            jsonWriter.writeStringArray("todo", listTodo);

            gsonWriter.endObject();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return true;
    }

    /*
    private void writeHistoryArray(JsonWriter jsonWriter, List<String> listPlan) throws IOException {
        jsonWriter.name("history");
        jsonWriter.beginArray();

        for (String stringPlan : listPlan) {
            jsonWriter.value(stringPlan);
        }

        jsonWriter.endArray();
    }
     */




    //alle Listen und Datum einlesen in die zugehörigen Propertys
    private boolean readPlanningJson(File filePlanning) {
        if (filePlanning.length() == 0) return true;
        try (FileReader fileReader = new FileReader(filePlanning);
             JsonReader jsonReader = new JsonReader(fileReader)) {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "planDate":
                        planDate = readPlanDate(jsonReader);
                        break;
                    case "plan":
                        stringListPlanProperty().getValue().addAll(readPlanArray(jsonReader));
                        break;
                    case "todo":
                        stringListTodoProperty().getValue().addAll(readTodoArray(jsonReader));
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

        return true;
    }

    private LocalDate readPlanDate(JsonReader jsonReader) throws IOException {
        return LocalDate.parse(jsonReader.nextString(), dateFormat);
    }

    private List<String> readTodoArray(JsonReader jsonReader) throws IOException {
        ArrayList<String> retArray = new ArrayList<>();

        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            retArray.add(jsonReader.nextString());
        }
        jsonReader.endArray();

        return retArray;
    }

    private List<String> readPlanArray(JsonReader jsonReader) throws IOException {
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
    private boolean writeTasksJson(File fileTasks, List<Task> listTasks) {
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

    private void writeTaskArray(JsonWriter jsonWriter, List<Task> listTasks) throws IOException {
        jsonWriter.beginArray();
        for (Task task : listTasks) {
            writeTask(jsonWriter, task);
        }
        jsonWriter.endArray();
    }

    private void writeTask(JsonWriter jsonWriter, Task task) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("name").value(task.getName());
        jsonWriter.name("repeat").value(Integer.toString(task.getRepeat()));
        jsonWriter.name("rollover").value(Boolean.toString(task.isRollover()));
        if (task.getDateLastDone() == null) {
            jsonWriter.name("dateLastDone").nullValue();
        } else {
            jsonWriter.name("dateLastDone").value(dateFormat.format(task.getDateLastDone()));
        }
        jsonWriter.endObject();
    }

    /**
     *
     * @param fileTasks
     * @return
     * @throws IOException
     */
    private ArrayList<Task> readTasksJson(File fileTasks) throws IOException {
        if (fileTasks.length() == 0) return new ArrayList<Task>();
        try (FileReader fileReader = new FileReader(fileTasks);
             JsonReader jsonReader = new JsonReader(fileReader)) {
            return readTasksArray(jsonReader);
        }

        //Leserechte für Datei sicherstellen sonst Fehlermeldung
    }

    private ArrayList<Task> readTasksArray(JsonReader reader) throws IOException {
        //was passiert bei leeren Array?
        ArrayList<Task> returnArray = new ArrayList<>();

        reader.beginArray();
        while(reader.hasNext()) {
           returnArray.add(readTask(reader));
        }
        reader.endArray();
        return returnArray;
    }

    private Task readTask(JsonReader reader) throws IOException {
        //richtiges File öffnen
        //File einlesen
            //Informationen in Graph speichern(aufpassen das Graph nicht zu groß wird wegen Speicher)

        String name = "";
        int repeat = 0;
        boolean rollover = false;
        LocalDate dateLastDone = null;

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
                    case ("dateLastDone"):
                        if (reader.peek() == JsonToken.NULL) {
                            reader.nextNull();
                        } else {
                            dateLastDone = LocalDate.parse(reader.nextString(), dateFormat);
                        }
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

        if (dateLastDone != null) {
            return new Task(name, repeat, rollover, dateLastDone);
        } else {
            return new Task(name, repeat, rollover);
        }
    }

    public int findIndexOfTaskByName(String name, List<Task> taskList) {
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getName().equals(name)) {
                return i;
            }
        }

        return -1;
    }

    public Stage getStage() {
        return stage;
    }

    public SimpleObjectProperty<String> selectedStringProperty() {
        return selectedStringProperty;
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

    public SimpleListProperty<String> stringListHistoryProperty() {
        return stringListHistoryProperty;
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

    public SimpleBooleanProperty newTaskRolloverProperty() {
        return newTaskRolloverProperty;
    }

    public void setNewTaskValidationProperty(String stringInvalid) {
        newTaskValidationProperty().setValue(stringInvalid);
    }

    public SimpleStringProperty newTaskValidationProperty() {
        return newTaskValidationProperty;
    }

    /*
    public SimpleBooleanProperty createTabClosedProperty() {
        return createTabClosedProperty;
    }
    */

    /*
    public SimpleBooleanProperty newTaskCheckNeedProperty() {
        return newTaskCheckNeedProperty;
    }
    */
}