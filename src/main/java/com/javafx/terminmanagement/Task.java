package com.javafx.terminmanagement;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Task implements Serializable {
    //Attribute, die bei Erstellung beeinflusst werden
    private int id;
    public IntegerProperty idProp;
    private String name;
    public StringProperty nameProp;
    private int repeat; // 0->keine Wiederholung; 1->jeden Tag; 2->aller 2 Tage
    //private int nRepeat; // 0->keine Wiederholung; 1->1mal Wiederholen; 2->2mal Wiederholen //dayRepeat und nRepeat zusammenarbeit bei Tageswechsel?
    private boolean rollover; // in nächsten Tag tun, wenn nicht gemacht
    //private boolean checkNeed; // Aufgabe täglich anbieten zur Bearbeitung
    private boolean planned;
    private boolean todo;
    private LocalDate dateLastDone;

    /** Einfacher Konstruktor für Aufgaben, wo todo und planned mit false und dateLastDone mit null initialisiert werden
     *
     * @param name Name der Aufgabe
     * @param repeat Wiederholung der Aufgabe aller repeat Tage
     * @param rollover Aufgabe wird bei Tageswechsel, wenn sie nicht fertiggestellt wurde, in den Plan geschrieben
     */
    public Task(String name, int repeat, boolean rollover) {
        Model.incLastId();
        this.id = Model.getLastId();
        idProp.set(id);
        this.name = name;
        nameProp.set(this.name);
        this.repeat = repeat;
        this.rollover = rollover;

        this.todo = false;
        this.planned = false;

        this.dateLastDone = null;
        //this.checkNeed = checkNeed;
    }

    public Task(int id, String name, int repeat, boolean rollover) {
        this.id = id;
        this.name = name;
        this.repeat = repeat;
        this.rollover = rollover;

        this.todo = false;
        this.planned = false;

        this.dateLastDone = null;
        //this.checkNeed = checkNeed;
    }

    /**
     * Mit dateLastDone erweiterter Konstruktor für Aufgaben, wo todo und planned mit false initialisiert werden
     *
     * @param name         Name der Aufgabe
     * @param repeat       Wiederholung der Aufgabe aller repeat Tage
     * @param rollover     Aufgabe wird bei Tageswechsel, wenn sie nicht fertiggestellt wurde, automatisch in den Plan geschrieben
     * @param dateLastDone letzte Durchführung der Aufgabe
     */
    public Task(String name, int repeat, boolean rollover, LocalDate dateLastDone) {
        Model.incLastId();
        this.id = Model.getLastId();
        this.name = name;
        this.repeat = repeat;
        this.rollover = rollover;

        this.todo = false;
        this.planned = false;

        this.dateLastDone = dateLastDone;
        //this.checkNeed = checkNeed;
    }

    public Task(int id, String name, int repeat, boolean rollover, LocalDate dateLastDone) {
        this.id = id;
        this.name = name;
        this.repeat = repeat;
        this.rollover = rollover;

        this.todo = false;
        this.planned = false;

        this.dateLastDone = dateLastDone;
        //this.checkNeed = checkNeed;
    }

    /**
     * Vergleichsmethode für Tasks ohne die Attribute dateLastDone, planned und todo zu berücksichtigen
     *
     * @param task zu vergleichende Aufgabe
     * @return true, aber nur wenn name, repeat und rollover gleich sind. Sonst Rückgabe von false
     */
    public boolean equals(Task task) {
        if (!this.name.equals(task.getName())) return false;
        if (this.repeat != task.getRepeat()) return false;
        if (this.rollover != task.isRollover()) return false;
        return true;
    }

    /**
     * @return Aufgabe als String zusammengefasst, wobei rollover, todo und planned nur angezeigt werden wenn sie true sind
     */
    public String toString() {
        StringBuilder stringRet = new StringBuilder(name + ": Wiederholung: " + repeat);
        if (id >= 0) stringRet.append(", id:" + id);
        if (isRollover()) stringRet.append(" ROLLOVER");
        if (isTodo()) stringRet.append(" TODO");
        if (isPlanned()) stringRet.append(" PLAN");
        if (dateLastDone != null) {
            stringRet.append(", zuletzt am ");
            stringRet.append(dateLastDone.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        return stringRet.toString();
    }

    public int getId() {
        return id;
    }

    /**
     * Abfragemethode für name
     *
     * @return name dieser Aufgabe
     */
    public String getName() {
        return name;
    }

    /** Abfragemethode für repeat
     *
     * @return repeat dieser Aufgabe
     */
    public int getRepeat() {
        return repeat;
    }

    /**
     * Abfragemethode für dateLastDone
     *
     * @return dateLastDone dieser Aufgabe
     */
    public LocalDate getDateLastDone() {
        return dateLastDone;
    }

    /**
     * Abfragemethode für Todo
     *
     * @return todo dieser Aufgabe
     */
    public boolean isTodo() {
        return todo;
    }

    /**
     * Abfragemethode für Planned
     *
     * @return planned dieser Aufgabe
     */
    public boolean isPlanned() {
        return planned;
    }

    /** Abfragemethode für Planned
     *
     * @return planned dieser Aufgabe
     */
    public boolean isRollover() {
        return rollover;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public void setRollover(boolean rollover) {
        this.rollover = rollover;
    }

    public void setDateLastDone(LocalDate lastTimeDone) {
        this.dateLastDone = lastTimeDone;
    }

    public void setTodo(boolean todo) {
        this.todo = todo;
    }

    public void setPlanned(boolean planned) {
        this.planned = planned;
    }
}
