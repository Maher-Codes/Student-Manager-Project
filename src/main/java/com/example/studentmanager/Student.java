package com.example.studentmanager;

import javafx.beans.property.*;

public class Student {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty department;
    private final DoubleProperty gpa;
    private final StringProperty note;

    // ✅ Updated constructor with note
    public Student(int id, String name, String department, double gpa, String note) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.department = new SimpleStringProperty(department);
        this.gpa = new SimpleDoubleProperty(gpa);
        this.note = new SimpleStringProperty(note);
    }

    // Getters and setters for ID
    public int getId() {
        return id.get();
    }
    public IntegerProperty idProperty() {
        return id;
    }

    // Getters and setters for Name
    public String getName() {
        return name.get();
    }
    public StringProperty nameProperty() {
        return name;
    }

    // Getters and setters for Department
    public String getDepartment() {
        return department.get();
    }
    public StringProperty departmentProperty() {
        return department;
    }

    // Getters and setters for GPA
    public double getGpa() {
        return gpa.get();
    }
    public DoubleProperty gpaProperty() {
        return gpa;
    }

    // ✅ Getters and setters for Note
    public String getNote() {
        return note.get();
    }
    public StringProperty noteProperty() {
        return note;
    }
    public void setNote(String note) {
        this.note.set(note);
    }
}