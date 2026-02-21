package com.javafx.terminmanagement;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

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

    private static int lastId;

    //private final SimpleMapProperty<Integer, Task> taskMapProperty = new SimpleMapProperty<>(FXCollections.observableHashMap());
    private final SimpleListProperty<Task> taskListProperty = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final SimpleListProperty<Task> dailyListProperty = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final SimpleListProperty<Integer> plannedIdListProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
    /*
    //Property für MainWindowView
    private final SimpleListProperty<String> stringListTodoProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final SimpleListProperty<Task> taskListAllProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final SimpleListProperty<String> stringListHistoryProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
     */

    //Propertys for TaskCreationTab and TaskChangeTab
    private final SimpleStringProperty newTaskNameProperty = new SimpleStringProperty("");
    private final SimpleStringProperty newTaskRepeatProperty = new SimpleStringProperty("0");
    private final SimpleBooleanProperty newTaskRolloverProperty = new SimpleBooleanProperty(false);
    private final SimpleStringProperty newTaskValidationProperty = new SimpleStringProperty();

    public Model(Stage stage) {
        Model.stage = stage;
        instance = this;

        //Check for existence of dataDir and handle nonexistence
        if (!dataDir.exists()) {
            if (dataDir.mkdir()) {
                System.out.println("(INFO) Model() DataDir was newly created!");
            } else {
                System.err.println("(ERR) Model() DataDir nonexistent, can't be created and program is being shut down!");
                Platform.exit();
            }
        }

        //Check for existence of fileTasks and filePlanning and handle nonexistence
        if (!fileTasks.exists()) {
            try {
                if (fileTasks.createNewFile()) {
                    System.out.println("(INFO) Model() " + fileTasks.toString() + " was created");
                } else {
                    System.err.println("(ERR) Model()" + fileTasks.toString() + " couldn't be created and program is being shut down!");
                    Platform.exit();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (!filePlanning.exists()) {
            try {
                if (filePlanning.createNewFile()) {
                    System.out.println("(INFO) Model() " + filePlanning.toString() + " was created");
                } else {
                    System.err.println("(ERR) Model()" + filePlanning.toString() + " couldn't be created and program is being shut down!");
                    Platform.exit();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        //Check Permission to read and write fileTasks and filePlanning
        if (!fileTasks.canRead()) {
            System.err.println("(ERR) Model() " + fileTasks.toString() + " can't be read, causing a shutdown of the program");
            Platform.exit();
        }
        if (!fileTasks.canWrite()) {
            System.err.println("(ERR) Model() " + fileTasks.toString() + " can't be written to, causing a shutdown of the program");
            Platform.exit();
        }
        if (!filePlanning.canRead()) {
            System.err.println("(ERR) Model() " + filePlanning.toString() + " can't be read, causing a shutdown of the program");
            Platform.exit();
        }
        if (!filePlanning.canWrite()) {
            System.err.println("(ERR) Model() " + filePlanning.toString() + " can't be written to, causing a shutdown of the program");
            Platform.exit();
        }


        try {
            readTasksJson(fileTasks);
            ArrayList<Task> readList = new ArrayList<>(taskListProperty().getValue());
            ArrayList<Task> newList = new ArrayList<>();

            //validate read Task IDs
            for (int i = 0; i < readList.size(); i++) {
                //check existence of id
                int id = -1;
                id = readList.get(i).getId();
                if (id == -1) {
                    System.err.println("(ERR) Model() Task: " + readList.get(i).getName() + " has no ID!");
                    continue;
                }

                //check if id of task is unique and remove this task if id is not unique
                boolean unique = true;
                for (int j = (i + 1); j < readList.size(); j++) {
                    if (readList.get(i).getId() == readList.get(j).getId()) {
                        unique = false;
                        System.err.println("(ERR) Model() Task: " + readList.get(i).getName()
                                + " wasn't written into taskListProperty because of nonunique ID!");
                    }
                }
                if (unique) {
                    newList.add(readList.get(i));
                    System.out.println("(INFO) Model() added Task: " + readList.get(i).toString());
                }
            }
            setTaskListProperty(FXCollections.observableList(newList));

            //initialize planDate if not already done
            if (planDate == null) planDate = LocalDate.now();

            //enrich taskList with information of planned tasks
            int index;
            HashSet<Integer> removeList = new HashSet<>();

            //go through all Ids in planned, set planned attribute of associated Tasks in taskList
            // and capture the ones that aren't associated with any task
            for (Integer id : plannedIdListProperty().getValue()) {
                index = -1;
                for (int i = 0; i < taskListProperty().getValue().size(); i++) {
                    if (id == taskListProperty().get(i).getId()) {
                        taskListProperty().get(i).setPlanned(true);
                        dailyListProperty().add(taskListProperty().get(i));
                        index = i;
                        break;
                    }
                }

                if (index < 0) {
                    removeList.add(taskListProperty().get(index).getId());
                }
            }

            //remove ids not associated with any tasks
            if (!removeList.isEmpty()) {
                ArrayList<Integer> newIdList = new ArrayList<>(plannedIdListProperty().getValue());
                for (Integer id : removeList) {
                    newIdList.remove(id);
                }

                if (writeTasksJson(filePlanning, taskListProperty().getValue(), newIdList)) {
                    setPlannedIdListProperty(newIdList);
                } else {
                    System.out.println("(ERR) Model(): new list of planned ids, couln't be writen to " + fileTasks);
                }
            }

            //-------------------------------------
            //
            //CURRENT PROGRESS
            //
            //-------------------------------------

            /* implementieren
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
            */

            /*
            for (int i = 0; i < taskMapProperty().size(); i++) {
                int id = -1;
                id = getNextIndex(i, taskMapProperty());
                taskListProperty.add(taskMapProperty().get(id));
            }
             */

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
        System.out.println("Model:writeNewTask() Propertys: "
                + "-> Task: " + selectedTaskProperty().toString()
                + "-> newTaskNameProperty:" + newTaskNameProperty().toString()
                + "-> newTaskRepeatProperty:" + newTaskRepeatProperty().toString());

        //String used to print invalid Taskpropertys to User
        StringBuilder stringInvalid = new StringBuilder();
        //Validation of the name
        String name = newTaskNameProperty().getValue();
        //Test, ob Aufgabenname leer ist
        if (name.isEmpty()) {
            stringInvalid.append("Aufgabenname ist leer! \n");
        } else {
            //Test, ob Aufgabenname einzigartig ist
            for (Task task : taskListProperty()) {
                if (name.equals(task.getName())) {
                    stringInvalid.append("Aufgabenname ist schon vorhanden! \n");
                    break;
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
        ArrayList<Task> listNew = new ArrayList<>(taskListProperty().getValue());
        System.out.println(", listNew Entrys before add() " + listNew.toString());

        //neue Aufgabe in neue Liste schreiben
        Task newTask = new Task(name, repeat, newTaskRolloverProperty().getValue());
        System.out.println(", New Task:" + newTask.toString());
        listNew.add(newTask);
        System.out.println(", listNew Entrys " + listNew.toString());

        //Aufgaben in File schreiben,und falls dies nicht funktioniert false zurückgeben
        if (!writeTasksJson(fileTasks, listNew, plannedIdListProperty().getValue())) {
            System.err.println("Model:writeNewTask() -> writeTasksJson returned false!");
            return false;
        }

        setTaskListProperty(listNew);

        /*
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
         */

        return true;
    }

    public void loadSelectedTask() {
        Task changeTask = selectedTaskProperty().getValue();

        System.out.println("(INFO) Model:loadChangeTask() loading values: " + changeTask.toString());

        newTaskNameProperty().setValue(changeTask.getName());
        newTaskRepeatProperty().setValue(Integer.toString(changeTask.getRepeat()));
        newTaskRolloverProperty().set(changeTask.isRollover());

        System.out.println("(INFO) Model:loadChangeTask() Values in NewTaskPropertys after loading: "
                + newTaskNameProperty().toString() + newTaskRepeatProperty().toString() + newTaskRolloverProperty().toString());
    }

    /**
     * @return true, wenn Aufgabe nicht verändert wurde oder die Aufgabe geändert wurde und erfolgreich geschrieben wurde
     * <br> false, wenn die Aufgabe nicht geschrieben werden konnte
     */
    public boolean writeChangedTask() {
        //Validierung der Eingabeparameter
        StringBuilder stringInvalid = new StringBuilder();
        //Validation if Task is selected
        if (newTaskNameProperty().getValue().isEmpty()) stringInvalid.append("Keine Aufgabe ausgewählt! \n");
        //Validation of the name
        String name = newTaskNameProperty().getValue();
        //Test, ob Aufgabenname leer ist
        if (name.isEmpty()) {
            stringInvalid.append("Aufgabenname ist leer! \n");
        } else {
            //Test, ob Aufgabenname einzigartig ist
            for (Task task : taskListProperty()) {
                if (name.equals(task.getName())) {
                    stringInvalid.append("Aufgabenname ist schon vorhanden! \n");
                    break;
                }
            }
        }
        //Validation of Taskname TODO
        //if(newTaskNameProperty().getValue())
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
        Task changedTask = new Task(selectedTaskProperty().get().getId(), name, repeat, newTaskRolloverProperty().getValue());
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
        /*
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
        */

        //temporären Aufgabenplan erstellen und aktualisieren
        ArrayList<Task> listTaskNew = new ArrayList<>(taskListProperty().getValue());
        listTaskNew.remove(selectedTaskProperty().getValue());
        listTaskNew.add(changedTask);
        //Aufgaben in File schreiben,und falls dies nicht funktioniert false zurückgeben
        if (!writeTasksJson(fileTasks, listTaskNew, plannedIdListProperty().getValue())) return false;
        //nach erfolgreichem Schreiben taskListAllProperty neu populieren
        taskListProperty().setAll(listTaskNew);

        System.out.println("Model:writeChangedTask() was succesfull!");


        return true;
    }

    //Aufgabe aus selectedTaskProperty lesen, aus Aufgabenfile löschen und taskListAllProperty aktualisieren
    public boolean writeDeletedTask() {
        if (selectedTaskProperty().getValue() == null) return false;
        Task deletedTask = selectedTaskProperty().getValue();
        //System.out.println("Model:writeDeletedTask() -> Task to delete: " + deletedTask.getName());

        //Aufgabe aus Aufgabenliste löschen
        ArrayList<Task> listNew = new ArrayList<>(taskListProperty().get());

        int index = -1;
        for (int i = 0; i < listNew.size(); i++) {
            if (listNew.get(i).equals(deletedTask)) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            listNew.remove(index);
        } else {
            System.out.println("Model:writeDeleteTask() -> attempt to delete nonexistent task!");
        }


        //neue Aufgabenliste schreiben
        if (!writeTasksJson(fileTasks, listNew, plannedIdListProperty().getValue())) {
            return false;
        }
        setTaskListProperty(FXCollections.observableList(listNew));

        /* ergänzen
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
         */

        return true;
    }

    /*
    public boolean writeDailyList(List<Tasks> newList) {
        //write dailyList to fileTasks

        return true;
    }
     */

    public boolean writeSignInTask() {
        //check if any Task was selected
        if (selectedTaskProperty().getValue() == null) {
            return false;
        }

        Task taskToSignIn = selectedTaskProperty().getValue();
        ArrayList<Task> newDailyList = new ArrayList<>(dailyListProperty().getValue());

        //check, if Task is already in dailyList otherwise add selectedTask to dailyList
        if (newDailyList.contains(taskToSignIn)) {
            System.out.println("(WARN) Model:writeSignInTask() User tried to signIn Task that is already in dailyList!");
            return true;
        } else {
            newDailyList.add(taskToSignIn);
        }

        ArrayList<Integer> newDailyIds = new ArrayList<>();
        for (Task task : newDailyList) {
            newDailyIds.add(task.getId());
        }

        ArrayList<Task> newList = new ArrayList<>(taskListProperty().getValue());
        //write newList into fileTasks
        if (!writeTasksJson(fileTasks, newList, newDailyIds)) {
            System.out.println("(ERR) Model:writeSignInTask() Error while trying to writeTasksJson!");
            return false;
        }

        setDailyListProperty(newDailyList);

        return true;
    }
    /*
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
            //taskMapProperty für taskOverview updaten
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
    */
    /*
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
            //taskMapProperty für taskOverview updaten
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
     */

    /*
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
     */

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
                        setPlanDate(readPlanDate(jsonReader));
                        break;
                        /*
                    case "plan":
                        stringListPlanProperty().getValue().addAll(readPlanArray(jsonReader));
                        break;
                         */
                    case "todo":
                        //stringListTodoProperty().getValue().addAll(readTodoArray(jsonReader));
                        readTodoArray(jsonReader);
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
    private boolean writeTasksJson(File fileTasks, List<Task> listTasks, List<Integer> planToday) {
        System.out.println("(INFO) Model:writeTaskJson(File: " + fileTasks.toString());
        System.out.println(", listTasksEntrys:" + listTasks.toString());

        try{
            if (!fileTasks.exists()) {
                if (!fileTasks.createNewFile()) {
                    System.err.println("(ERR) Model:writeTaskJson() Datenfile konnte nicht erstellt werden!");
                    //TODO Errorhandling
                }
            }

            try (FileWriter fileWriter = new FileWriter(fileTasks);
                 JsonWriter gsonWriter = new JsonWriter(fileWriter)) {
                gsonWriter.setIndent("    ");

                JSONWriter jsonWriter = new JSONWriter(gsonWriter);

                gsonWriter.beginObject();

                //TODO change to always print the right date
                jsonWriter.writeDate(planDate);

                jsonWriter.writeIntegerArray("planToday", new ArrayList<>(planToday));

                writeTaskArray(gsonWriter, listTasks);

                gsonWriter.endObject();

                //Alle Streams fertig schreiben
                //jsonWriter.flush();
                fileWriter.flush();

                setTaskListProperty(FXCollections.observableList(listTasks));
            }
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            return false;
        }
        return true;
    }

    private void writeTaskArray(JsonWriter jsonWriter, List<Task> listTasks) throws IOException {

        jsonWriter.name("tasks");
        jsonWriter.beginArray();
        for (Task task : listTasks) {
            writeTask(jsonWriter, task);
        }
        jsonWriter.endArray();

    }

    private void writeTask(JsonWriter jsonWriter, Task task) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("id").value(task.getId());
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
    private boolean readTasksJson(File fileTasks) throws IOException {
        if (fileTasks.length() == 0) return false;
        try (FileReader fileReader = new FileReader(fileTasks);
             JsonReader jsonReader = new JsonReader(fileReader)) {

            jsonReader.beginObject();

            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "planDate":
                        setPlanDate(readPlanDate(jsonReader));
                        break;
                    case "planToday":
                        jsonReader.beginArray();

                        if (jsonReader.peek() == (JsonToken.END_ARRAY)) {
                            jsonReader.endArray();
                            break;
                        }
                        ArrayList<Integer> newList = new ArrayList<>();
                        while (jsonReader.hasNext()) {
                            newList.add(Integer.parseInt(jsonReader.nextString()));
                        }
                        setPlannedIdListProperty(newList);
                        jsonReader.endArray();
                        break;
                    case "tasks":
                        setTaskListProperty(readTasksArray(jsonReader));
                        break;
                    default:
                        //TODO improve Errorhandling
                        System.err.println("(ERR) Model:readTasksJson() Unknown Name in: " + filePlanning);
                        break;
                }
            }
            jsonReader.endObject();

            return true;
        }

        //Leserechte für Datei sicherstellen sonst Fehlermeldung
    }

    private ArrayList<Task> readTasksArray(JsonReader reader) throws IOException {
        //was passiert bei leeren Array?
        ArrayList<Task> returnArray = new ArrayList<>();

        reader.beginArray();
        if (reader.peek() == (JsonToken.END_ARRAY)) {
            reader.endArray();
            return returnArray;
        }
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

        int id = -1;
        String name = "";
        int repeat = 0;
        boolean rollover = false;
        LocalDate dateLastDone = null;

        try{
            reader.beginObject();

            while(reader.hasNext()) {
                switch(reader.nextName()){
                    case ("id"):
                        id = reader.nextInt();
                        if (id > lastId) setLastId(id);
                        break;
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
            return new Task(id, name, repeat, rollover, dateLastDone);
        } else {
            return new Task(id, name, repeat, rollover);
        }
    }

    public Stage getStage() {
        return stage;
    }

    public static int getLastId() {
        return lastId;
    }

    public static void setLastId(int newLastId) {
        if (newLastId > lastId) {
            lastId = newLastId;
        } else {
            System.out.println("(WAR) Model:incLastId() was called with an id that is smaller or even compared to previousId");
        }
    }

    public static void incLastId() {
        lastId++;
    }

    public boolean resetNewTaskPropertys() {
        /*
        if(selectedTaskProperty().getValue() == null) {
            ArrayList<Task> test = new ArrayList<>();
            test.add(new Task());
            setSelectedTaskProperty(test);
        }
         */

        newTaskNameProperty().setValue("");
        newTaskRepeatProperty().setValue("");
        newTaskRolloverProperty().setValue(false);

        return true;
    }

    /*
    public SimpleMapProperty<Integer, Task> taskMapProperty() {
        return taskMapProperty;
    }

    public void setTaskMapProperty(ObservableMap<Integer, Task> newMap) {
        taskMapProperty.setValue(newMap);
    }
     */

    public SimpleListProperty<Task> taskListProperty() {
        return taskListProperty;
    }

    public SimpleListProperty<Task> dailyListProperty() {
        return dailyListProperty;
    }

    public SimpleListProperty<Integer> plannedIdListProperty() {
        return plannedIdListProperty;
    }

    public void setTaskListProperty(Collection<Task> newList) {
        taskListProperty.setAll(newList);
        System.out.println("(INFO) Model:setTaskListProperty(Collection<Task> newList" + taskListProperty().get().toString() + ")");
    }

    public void setDailyListProperty(Collection<Task> newList) {
        dailyListProperty.setAll(newList);
        System.out.println("(INFO) Model:setDailyListProperty(Collection<Task> newList" + dailyListProperty().get().toString() + ")");
    }

    public void setPlannedIdListProperty(Collection<Integer> newList) {
        plannedIdListProperty().setAll(newList);
        System.out.println("(INFO) Model:setPlannedIdListProperty(Collection<Task> newList" + plannedIdListProperty().get().toString() + ")");
    }

    public void setPlanDate(LocalDate newDate) {
        this.planDate = newDate;
    }

    public void setSelectedTaskProperty(List<Task> newValue) {
        this.taskListProperty().setValue(FXCollections.observableList(newValue));
    }

    public SimpleObjectProperty<String> selectedStringProperty() {
        return selectedStringProperty;
    }

    public SimpleObjectProperty<Task> selectedTaskProperty() {
        return selectedTaskProperty;
    }

    /*
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
     */

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