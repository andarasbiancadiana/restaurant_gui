module restaurant {
    // 1.DEPENDENTE(REQUIRES)
    requires javafx.controls;
    // Dacă folosești FXML (opțional)
    requires javafx.fxml;
    requires com.google.gson;
    requires javafx.graphics;

    // 2. EXPORTURI (EXPORTS)
    exports restaurant;
    exports restaurant.gui;

    // 3. REFLEXIE (OPENS)
    opens restaurant to com.google.gson;
    opens restaurant.gui to javafx.graphics, javafx. fxml;
}