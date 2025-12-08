package restaurant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigLoader {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Config load(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.err.println("Eroare: Fișierul de configurare lipsește sau este inaccesibil. " +
                    "Se va folosi configurația implicită. Vă rugăm contactați suportul tehnic dacă problema persistă.");
            Config defaultConfig = defaultConfig();
            try {
                saveDefaultConfig(file, defaultConfig);
                System.out.println("S-a creat un fișier de configurare implicit: " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Nu am putut crea fișierul de configurare implicit: " + e.getMessage());
            }
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(file)) {
            Config cfg = gson.fromJson(reader, Config.class);
            if (cfg == null) {
                throw new JsonSyntaxException("Fișier gol sau conținut nevalid");
            }
            return cfg;
        } catch (FileNotFoundException e) {
            System.err.println("Eroare: Fișierul de configurare nu a fost găsit. Se folosește configurația implicită.");
            return defaultConfig();
        } catch (JsonSyntaxException e) {
            System.err.println("Eroare: Fișierul de configurare este corupt sau are format invalid.");
            System.err.println("Vă rugăm verificați fișierul '" + path + "'. S-a creat o copie a fișierului corupt pentru analiză.");
            try {
                File backup = new File(path + ".corrupt-" + System.currentTimeMillis() + ".bak");
                if (file.renameTo(backup)) {
                    System.err.println("Fișier corupt redenumit în: " + backup.getName());
                }
                saveDefaultConfig(file, defaultConfig());
            } catch (Exception ex) {
                System.err.println("Nu s-a reușit prelucrarea fișierului corupt: " + ex.getMessage());
            }
            return defaultConfig();
        } catch (IOException e) {
            System.err.println("Eroare la citirea fișierului de configurare: " + e.getMessage());
            return defaultConfig();
        }
    }

    private static Config defaultConfig() {
        return new Config("La Andrei", 9);
    }

    private static void saveDefaultConfig(File file, Config cfg) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(cfg, writer);
        }
    }
}