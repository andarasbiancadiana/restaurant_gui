package restaurant;

import java.util.HashMap;
import java.util.Map;

public class Order {
    private static final int TVA;
    static {
        int t = 9;
        try {
            Config config = ConfigLoader.load("config.json");
            if (config != null) {
                t = config.getTva();
            }
        } catch (Throwable e) {
            System.err.println("Nu s-a putut incarca TVA din fisierul de configurare, se va folosi valoarea implicita");
        }
        TVA = t;
    }
    private Map<Product, Integer> products;
    private double currentDiscount = 0;

    public Order() {
        products = new HashMap<>();
    }

    public Map<Product, Integer> getProducts() { return products; }

    public void addProduct(Product product, int quantity) {
        products.put(product, products.getOrDefault(product, 0) + quantity);
    }

    public void setCurrentDiscount(double discount) { this.currentDiscount = discount; }

    public double calculateTotalWithoutOffer() {
        double total = 0.0;
        for (Map.Entry<Product, Integer> entry : products.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            total += product.getPrice() * quantity;
        }
        total += total * TVA / 100;
        return total;
    }

    public void displayOrder(int id) {
        System.out.println("−−− Comanda " + id + " −−−");
        for (Map.Entry<Product, Integer> entry : products.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            System.out.println("> " + quantity + " x " + product.name + " - " + (product.getPrice() * quantity) + " RON");
        }
        System.out.println("Total (inclusiv TVA): " + calculateTotalWithoutOffer() + " RON");

    }

    public boolean isEmpty() {
        return products.isEmpty();
    }
}
