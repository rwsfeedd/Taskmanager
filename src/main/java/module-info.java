module com.javafx.terminmanagement {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.javafx.terminmanagement to javafx.fxml;
    exports com.javafx.terminmanagement;
    exports com.javafx.terminmanagement.Controllers;
    opens com.javafx.terminmanagement.Controllers to javafx.fxml;
    exports com.javafx.terminmanagement.JSON;
    opens com.javafx.terminmanagement.JSON to javafx.fxml;
}