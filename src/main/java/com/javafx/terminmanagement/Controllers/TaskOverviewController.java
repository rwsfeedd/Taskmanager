package com.javafx.terminmanagement.Controllers;

import com.javafx.terminmanagement.Model;
import com.javafx.terminmanagement.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class TaskOverviewController {
    @FXML
    private ListView<Task> taskList;

    public void initialize() {
        Model model = Model.getInstance();
        taskList.itemsProperty().bind(model.getCurrentTasks());
    }
}
