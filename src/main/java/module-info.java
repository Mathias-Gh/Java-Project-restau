module com.example.javaprojectrestau {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.javaprojectrestau to javafx.fxml;
    exports com.example.javaprojectrestau;
}