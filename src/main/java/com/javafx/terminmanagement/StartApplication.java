package com.javafx.terminmanagement;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class StartApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {


        //Weitergabe der Hauptstage an den Hauptcontroller
        Model model = new Model(stage);

        //Hauptfenster laden und anzeigen
        FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource("mainWindow-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        stage.setTitle("Terminmanagement");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {

        //run gradle wrapper with 'run --args="args[0] args[1] ..."
        if(args.length > 1) {
            System.out.println(args[1]);
        }

        launch();
    }
}