package com.javafx.terminmanagement.Controllers;

import javafx.stage.Stage;

/**
 * enthält alle wichtigen Funktionen für die Arbeit mit der Mainstage
 * @param stage Hauptstage der Anwendung
 * @param instance
 */
public class MasterController {
    private static MasterController instance;
    private static Stage stage;
    public MasterController(Stage stage) {
        MasterController.stage = stage;
    }

    /**
     * Gibt die einzige Instanz des MasterControllers weiter
     *
     * @return Singleton MasterController
     *
     */
    public static MasterController getInstance() {
        //neue Instanz von MasterController wird erstellt wenn noch keine Instanzen davon exisiteren
        if (instance == null) {
            instance = new MasterController(stage);
        }
        return instance;
    }

    public Stage getStage() {
        return stage;
    }
}