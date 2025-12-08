package restaurant;

import java.util.*;

public class Menu {
    private final Map<Category, List<Product>> categories = new LinkedHashMap<>();

    public void addCategory(Category category, List<Product> products) {
        categories.put(category, new ArrayList<>(products));
    }

    public void addProduct(Category category, Product product) {
        categories.computeIfAbsent(category, k -> new ArrayList<>()).add(product);
    }

    public List<Product> getProductsByCategory(Category category) {
        return Collections.unmodifiableList(categories.getOrDefault(category, Collections.emptyList()));
    }

    public List<Product> getAllProducts() {
        List<Product> all = new ArrayList<>();
        for (List<Product> list : categories.values()) {
            all.addAll(list);
        }
        return all;
    }

    public Set<Category> getCategories() {
        return Collections.unmodifiableSet(categories.keySet());
    }

    public Map<Category, List<Product>> asMap() {
        Map<Category, List<Product>> copy = new LinkedHashMap<>();
        for (Map.Entry<Category, List<Product>> e : categories.entrySet()) {
            copy.put(e.getKey(), Collections.unmodifiableList(e.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    public Optional<Product> findProductByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        String query = name.trim();
        return getAllProducts().stream()
                .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(query))
                .findFirst();
    }
}
