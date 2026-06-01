package com.example.portalinternogruposolutia.model;

import java.util.List;

public class Project {
    private int id;
    private String name;
    private String description;
    private String status;
    private String statusLabel;
    private List<String> technologies;
    private String department;

    public Project(int id, String name, String description, String status,
                   String statusLabel, List<String> technologies, String department) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.statusLabel = statusLabel;
        this.technologies = technologies;
        this.department = department;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getStatusLabel() { return statusLabel; }
    public List<String> getTechnologies() { return technologies; }
    public String getDepartment() { return department; }

    public int getStatusColor() {
        switch (status) {
            case "production":   return 0xFF38A169;
            case "development":  return 0xFFD69E2E;
            case "maintenance":  return 0xFF718096;
            default:             return 0xFF718096;
        }
    }

    public int getStatusBgRes() {
        switch (status) {
            case "production":   return 0xFFC6F6D5;
            case "development":  return 0xFFFEFCBF;
            case "maintenance":  return 0xFFE2E8F0;
            default:             return 0xFFE2E8F0;
        }
    }
}
