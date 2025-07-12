module com.example.studentmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;      // ✅ Required for SwingFXUtils
    requires java.desktop;      // ✅ Required for javax.imageio.ImageIO
    requires java.sql;

    opens com.example.studentmanager to javafx.fxml;
    exports com.example.studentmanager;
}
