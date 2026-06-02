package com.example.portalinternogruposolutia;

import com.example.portalinternogruposolutia.model.Project;
import com.example.portalinternogruposolutia.model.User;

import java.util.Arrays;
import java.util.List;

public class MockData {

    public static List<Project> getProjects() {
        List<Project> projects = Arrays.asList(
            new Project(1, "Gestor de Incidencias",
                "Plataforma centralizada para la gestión y seguimiento de incidencias técnicas.",
                "production", "Producción",
                Arrays.asList("React", "Node.js", "SQL", "AWS"), "Sistemas"),

            new Project(2, "Aplicación Remota",
                "Herramienta de acceso remoto seguro para empleados.",
                "production", "Producción",
                Arrays.asList("Vue", "Python", "Docker", "Azure"), "Infraestructura"),

            new Project(3, "Portal de Comunicación Interna",
                "Red social corporativa para la comunicación entre equipos.",
                "production", "Producción",
                Arrays.asList("Angular", ".NET", "SQL Server"), "Comunicación"),

            new Project(4, "Sistema de Inventario",
                "Control y seguimiento del inventario de equipos y materiales.",
                "development", "En Desarrollo",
                Arrays.asList("React", "Firebase", "Node.js"), "Logística"),

            new Project(5, "Módulo de Renovaciones",
                "Gestión de renovaciones de contratos y licencias de software.",
                "development", "En Desarrollo",
                Arrays.asList("Vue", "Express", "MongoDB"), "Administración"),

            new Project(6, "Informes Semanales",
                "Generador automatizado de informes semanales de productividad.",
                "production", "Producción",
                Arrays.asList("Python", "Django", "PostgreSQL"), "Analítica"),

            new Project(7, "Informes Mensuales",
                "Dashboard interactivo con informes mensuales para directivos.",
                "production", "Producción",
                Arrays.asList("Python", "Flask", "MySQL"), "Dirección"),

            new Project(8, "Actualización de Plataforma",
                "Migración y actualización de la plataforma corporativa.",
                "maintenance", "Mantenimiento",
                Arrays.asList("Java", "Spring Boot", "Oracle"), "Sistemas"),

            new Project(9, "Mantenimiento de Infraestructura",
                "Mantenimiento preventivo y correctivo de infraestructura cloud.",
                "maintenance", "Mantenimiento",
                Arrays.asList("Terraform", "AWS", "Docker"), "Infraestructura"),

            new Project(10, "Módulo IA en Desarrollo",
                "Módulo experimental con IA para automatización de procesos.",
                "development", "En Desarrollo",
                Arrays.asList("React", "TypeScript", "GraphQL", "Node.js"), "I+D")
        );

        projects.get(0).addDocument("manual_incidencias.pdf");
        projects.get(0).addDocument("guia_usuario.pdf");
        projects.get(2).addDocument("manual_comunicacion.pdf");

        return projects;
    }

    public static List<User> getUsers() {
        return Arrays.asList(
            new User(1, "Administrador", "admin", "AD", "admin@solutia.com"),
            new User(2, "Tecnico", "tech", "TC", "tecnico@solutia.com"),
            new User(3, "Visitante", "visitor", "VS", "visitante@solutia.com")
        );
    }

    public static List<String> getTechnologies(List<Project> projects) {
        java.util.Set<String> techs = new java.util.TreeSet<>();
        for (Project p : projects) techs.addAll(p.getTechnologies());
        return new java.util.ArrayList<>(techs);
    }
}
