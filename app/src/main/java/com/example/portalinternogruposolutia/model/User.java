package com.example.portalinternogruposolutia.model;

public class User {
    private int id;
    private String name;
    private String role;
    private String initials;
    private String email;

    public User(int id, String name, String role, String initials, String email) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.initials = initials;
        this.email = email;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getInitials() { return initials; }
    public String getEmail() { return email; }

    public String getRoleLabel() {
        switch (role) {
            case "admin":   return "Administrador";
            case "tech":    return "Tecnico";
            case "visitor": return "Visitante";
            default:        return role;
        }
    }

    public String getRoleDescription() {
        switch (role) {
            case "admin":   return "Control total";
            case "tech":    return "Control acotado";
            case "visitor": return "Solo visualización";
            default:        return role;
        }
    }
}
