package com.example.studentmanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.*;
import java.sql.*;

public class HelloController {

    @FXML private TextField searchField;
    @FXML private TextField nameField;
    @FXML private TextField departmentField;
    @FXML private TextField gpaField;
    @FXML private TextField noteField;
    @FXML private TableView<Student> tableView;
    @FXML private TableColumn<Student, Integer> idColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> departmentColumn;
    @FXML private TableColumn<Student, Double> gpaColumn;
    @FXML private TableColumn<Student, String> noteColumn;

    private final ObservableList<Student> studentList = FXCollections.observableArrayList();
    private final String DB_URL = "jdbc:mysql://localhost:3306/student_manager";
    private final String DB_USER = "root";
    private final String DB_PASS = "";

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cell -> cell.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        departmentColumn.setCellValueFactory(cell -> cell.getValue().departmentProperty());
        gpaColumn.setCellValueFactory(cell -> cell.getValue().gpaProperty().asObject());
        noteColumn.setCellValueFactory(cell -> cell.getValue().noteProperty());

        loadStudents();

        FilteredList<Student> filteredData = new FilteredList<>(studentList, b -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(student -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return student.getName().toLowerCase().contains(lower)
                        || student.getDepartment().toLowerCase().contains(lower)
                        || student.getNote().toLowerCase().contains(lower);
            });
        });
        SortedList<Student> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);

        tableView.setOnMouseClicked(event -> {
            Student selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                nameField.setText(selected.getName());
                departmentField.setText(selected.getDepartment());
                gpaField.setText(String.valueOf(selected.getGpa()));
                noteField.setText(selected.getNote());
            }
        });
    }

    private void loadStudents() {
        studentList.clear();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM students")) {

            while (rs.next()) {
                studentList.add(new Student(
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getDouble("gpa"),
                        rs.getString("note")
                ));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd() {
        String name = nameField.getText();
        String dept = departmentField.getText();
        String gpaText = gpaField.getText();
        String note = noteField.getText();

        if (name.isEmpty() || dept.isEmpty() || gpaText.isEmpty()) {
            showAlert("Validation Error", "Please fill in all fields.");
            return;
        }

        try {
            double gpa = Double.parseDouble(gpaText);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "INSERT INTO students (name, department, gpa, note) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, dept);
                ps.setDouble(3, gpa);
                ps.setString(4, note);
                ps.executeUpdate();
            }
            clearFields();
            loadStudents();
        } catch (NumberFormatException e) {
            showAlert("Input Error", "GPA must be a number.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEdit() {
        Student selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a student to edit.");
            return;
        }

        String name = nameField.getText();
        String dept = departmentField.getText();
        String gpaText = gpaField.getText();
        String note = noteField.getText();

        if (name.isEmpty() || dept.isEmpty() || gpaText.isEmpty()) {
            showAlert("Validation Error", "Please fill in all fields.");
            return;
        }

        try {
            double gpa = Double.parseDouble(gpaText);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "UPDATE students SET name = ?, department = ?, gpa = ?, note = ? WHERE student_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, dept);
                ps.setDouble(3, gpa);
                ps.setString(4, note);
                ps.setInt(5, selected.getId());
                ps.executeUpdate();
            }
            clearFields();
            loadStudents();
        } catch (NumberFormatException e) {
            showAlert("Input Error", "GPA must be a number.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        Student selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a student to delete.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "DELETE FROM students WHERE student_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, selected.getId());
            ps.executeUpdate();
            clearFields();
            loadStudents();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(tableView.getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("ID,Name,Department,GPA,Note\n");
                for (Student s : studentList) {
                    writer.write(String.format("%d,%s,%s,%.2f,%s\n",
                            s.getId(), s.getName(), s.getDepartment(), s.getGpa(), s.getNote()));
                }
                showAlert("Export Successful", "Data exported to CSV successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Export Failed", "Could not write to file.");
            }
        }
    }

    @FXML
    private void handleExportPNG() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Table Snapshot");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        File file = fileChooser.showSaveDialog(tableView.getScene().getWindow());

        if (file != null) {
            WritableImage image = tableView.snapshot(new SnapshotParameters(), null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                showAlert("Export Successful", "Table exported as PNG successfully.");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Export Failed", "Could not save PNG.");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        nameField.clear();
        departmentField.clear();
        gpaField.clear();
        noteField.clear();
    }
}
