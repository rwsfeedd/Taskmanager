module com.javafx.terminmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens com.javafx.terminmanagement to javafx.fxml;
    exports com.javafx.terminmanagement;
    exports com.javafx.terminmanagement.Controllers;
    opens com.javafx.terminmanagement.Controllers to javafx.fxml;
    exports com.javafx.terminmanagement.JSON;
    opens com.javafx.terminmanagement.JSON to javafx.fxml;
}