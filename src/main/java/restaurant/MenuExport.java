package restaurant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MenuExport {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static boolean exportMenu(Menu menu, Path targetFile) {
        return exportMenu(menu.asMap(), targetFile);
    }

    public static boolean exportMenu(Map<Category, List<Product>> menu, Path targetFile) {
        Map<String, List<Product>> serializable = new LinkedHashMap<>();
        for (Category c : Category.values()) {
            serializable.put(c.name(), menu.getOrDefault(c, List.of()));
        }

        try (FileWriter writer = new FileWriter(targetFile.toFile())) {
            gson.toJson(serializable, writer);
            System.out.println("Export reușit: meniul a fost salvat în '" + targetFile.toAbsolutePath() + "'.");
            return true;
        } catch (IOException e) {
            System.err.println("Eroare la exportul meniului: nu s-a putut scrie în fișierul '" + targetFile + "'.");
            System.err.println("Verifică permisiunile și spațiul pe disc. Dacă problema persistă, contactează suportul tehnic.");
            return false;
        }
    }
}