package com.javafx.terminmanagement;

import javafx.stage.Stage;

/**
 * enthält alle wichtigen Funktionen für die Arbeit mit der Mainstage
 */
public class Model {
    private static Model instance;
    private static Stage stage;
    public Model(Stage stage) {
        Model.stage = stage;
    }

    public Task readTask(String name) {
        //richtiges File öffnen
        //File einlesen
            //Informationen in Graph speichern(aufpassen das Graph nicht zu groß wird wegen Speicher)

        return new Task("yeye", false);
    }

    /**
     * Gibt die einzige Instanz des Modells weiter
     *
     * @return Singleton Model
     *
     */
    public static Model getInstance() {
        //neue Instanz von Model wird erstellt wenn noch keine Instanzen davon exisiteren
        if (instance == null) {
            instance = new Model(stage);
        }
        return instance;
    }

    public Stage getStage() {
        return stage;
    }
}