package com.javafx.terminmanagement;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {
    private String name;
    private int dayRepeat; // 0->keine Wiederholung; 1->jeden Tag; 2->aller 2 Tage
    private int nRepeat; // 0->keine Wiederholung; 1->1mal Wiederholen; 2->2mal Wiederholen //dayRepeat und nRepeat zusammenarbeit bei Tageswechsel?
    private boolean rollover; // in nächsten Tag tun, wenn nicht gemacht
    private Date doneLast; // bei erstem Auftreten Fehler im zusammenhang mit repeat
    //rollover = true, repeat = 1, -> Aufgaben dürfen nicht mehrmals in einen Tag geschrieben werden?? ->Zähneputzen2xtgl

    /**
     *
     * @param name  Name der Aufgabe
     * @param active Aktivitaets- und Bearbeitungszustand
     */
    public Task(String name, boolean active) {
        this.name = name;
        this.active = active;
    }

    public boolean isNull() {
        if (name.isEmpty()) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "Aufgabenname: " + name + ", Aktiv: " + active;
    }

    public String getName() {
        return name;
    }

    public boolean getActive() {
        return active;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
