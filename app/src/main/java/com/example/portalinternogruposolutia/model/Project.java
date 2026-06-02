package com.example.portalinternogruposolutia.model;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private int id;
    private String name;
    private String description;
    private String status;
    private String statusLabel;
    private List<String> technologies;
    private String department;
    private List<String> documents;

    public Project(int id, String name, String description, String status,
                   String statusLabel, List<String> technologies, String department) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.statusLabel = statusLabel;
        this.technologies = technologies;
        this.department = department;
        this.documents = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getStatusLabel() { return statusLabel; }
    public List<String> getTechnologies() { return technologies; }
    public String getDepartment() { return department; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
    public void setTechnologies(List<String> technologies) { this.technologies = technologies; }
    public void setDepartment(String department) { this.department = department; }

    public List<String> getDocuments() { return documents; }
    public void setDocuments(List<String> documents) { this.documents = documents; }
    public void addDocument(String doc) { this.documents.add(doc); }
    public void removeDocument(String doc) { this.documents.remove(doc); }
    public int getDocumentCount() { return documents.size(); }

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
