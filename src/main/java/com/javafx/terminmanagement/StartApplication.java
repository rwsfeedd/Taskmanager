package com.javafx.terminmanagement;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

import com.javafx.terminmanagement.Controllers.MasterController;

public class StartApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        //Ordner für Datenspeicherung erstellen, falls noch nicht vorhanden
        File directory = new File("data");
        if (directory.exists() == false) {
            if (directory.mkdir()) {
                System.out.println("Data-Verzeichnis konnte erfolgreich erstellt!");
            } else {
                System.out.println("Data-Verzeichnis konnte nicht erstellt werden!");
            }
        }

        //Weitergabe der Hauptstage an den Hauptcontroller
        MasterController masterController = new MasterController(stage);

        //Hauptfenster laden und anzeigen
        FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("mainWindow-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        stage.setTitle("Terminmanagement");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}