module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    exports com.example;
    exports com.example.clientqci;

    opens com.example to javafx.fxml;
    opens com.example.clientqci to javafx.fxml;
}
