module restaurant {
    // 1. DEPENDENTE (REQUIRES)
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires javafx.graphics;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;
    requires java.desktop;

    // 2. EXPORTURI (EXPORTS)
    exports restaurant;
    exports restaurant.gui;
    exports restaurant.repository;
    exports restaurant.util;
    exports restaurant.model;

    // 3. REFLEXIE (OPENS)
    opens restaurant to org.hibernate.orm.core, jakarta.persistence, com.google.gson;
    opens restaurant.gui to javafx.graphics, javafx.fxml;
    opens restaurant.util to jakarta.persistence, org.hibernate.orm.core;
    opens restaurant.repository to org.hibernate.orm.core, jakarta.persistence;
    opens restaurant.model to org.hibernate.orm.core, jakarta.persistence, com.google.gson;
}