package com.javafx.terminmanagement.Controllers;

import com.javafx.terminmanagement.StartApplication;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * enthält alle wichtigen Funktionen für die Arbeit mit der Mainstage
 * @param stage Hauptstage der Anwendung
 */
public class MasterController {
    private static MasterController instance;
    private static Stage stage;
    public MasterController(Stage stage) {
        MasterController.stage = stage;
    }

    /**
     *
     * @return Singleton MasterController
     *
     */
    public static MasterController getInstance() {

        if (instance == null) {
            instance = new MasterController(stage);
        }
        return instance;
    }

    public Stage getStage() {
        return stage;
    }
}
