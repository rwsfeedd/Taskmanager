package com.javafx.terminmanagement;

import java.util.LinkedList;

public class TaskList {
    private String name;
    private LinkedList<Task> allTasksList = new LinkedList<>();

    public TaskList(String name) {
        this.name = name;
    }


    /**
     * @param task Aufgabe die zu Liste hinzugefügt werden soll
     * @return true, wenn die Aufgabe erfolgreich in die Liste eingefügt wurde, sonst false
     */
    public boolean addTask(Task task) throws Exception {
        if (task.isNull()) {// Elemente die null entsprechen werden nicht akzeptiert
            return false;
        }
        if (allTasksList.add(task)) {// Element hinzufügen, falls in List noch nicht vorhanden
            return true;//true zurückgeben, falls erfolgreich
        }
        return false;//false zurückgeben, falls element schon vorhanden
    }

}
