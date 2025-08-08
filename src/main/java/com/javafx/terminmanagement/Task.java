package com.javafx.terminmanagement;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {
    private String name;
    //private boolean active; //
    private int repeat; // 0->keine Wiederholung; 1->jeden Tag; 2->aller 2 Tage
    //private int nRepeat; // 0->keine Wiederholung; 1->1mal Wiederholen; 2->2mal Wiederholen //dayRepeat und nRepeat zusammenarbeit bei Tageswechsel?
    private boolean rollover; // in nächsten Tag tun, wenn nicht gemacht
    private boolean checkNeed; // Aufgabe täglich anbieten zur Bearbeitung
    //private Date doneLast; // bei erstem Auftreten Fehler im Zusammenhang mit repeat
    //rollover = true, repeat = 1, -> Aufgaben dürfen nicht mehrmals in einen Tag geschrieben werden?? ->Zähneputzen2xtgl

    /**
     *
     * @param name  Name der Aufgabe
     * @param active Aktivitaets- und Bearbeitungszustand
     */
    public Task(String name, int repeat, boolean rollover, boolean checkNeed) {
        this.name = name;
        this.repeat = repeat;
        this.rollover = rollover;
        this.checkNeed = checkNeed;
    }

    public boolean isNull() {
        if (name.isEmpty()) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "Aufgabenname: " + name + ", Wiederholung: " + repeat + ", Uebertragen: " + rollover;
    }

    public String getName() {
        return name;
    }

    public int getRepeat() {
        return repeat;
    }

    public boolean getRollover() {
        return rollover;
    }

    public boolean getCheckNeed() {
        return checkNeed;
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

    public void setCheckNeed(boolean checkNeed) {
        this.checkNeed = checkNeed;
    }
}
