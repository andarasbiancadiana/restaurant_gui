package restaurant.model;

import jakarta.persistence.*;
import restaurant.Config;
import restaurant.ConfigLoader;
import restaurant.Product;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private static final int TVA;
    static {
        int t = 9;
        try {
            Config config = ConfigLoader.load("config.json");
            if (config != null) { t = config.getTva(); }
        } catch (Throwable e) {
            System.err.println("Nu s-a putut incarca TVA din config, se foloseste 9%");
        }
        TVA = t;
    }

    // RELAȚII JPA
    @ManyToOne
    @JoinColumn(name = "waiter_id")
    private User waiter;

    @ManyToOne
    @JoinColumn(name = "table_id")
    private RestaurantTable restaurantTable;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();


    @Column(name = "current_discount")
    private double currentDiscount = 0;

    @Column(name = "final_total")
    private double total;

    private boolean isActive = true;

    public Order() {}


    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void setCurrentDiscount(double discount) {
        this.currentDiscount = discount;
    }

    public double calculateTotalWithoutOffer() {
        double subtotal = 0.0;
        for (OrderItem item : items) {
            subtotal += item.getProduct().getPrice() * item.getQuantity();
        }
        return subtotal + (subtotal * TVA / 100);
    }

    public void displayOrder(int id) {
        System.out.println("−−− Comanda " + id + " −−−");
        for (OrderItem item : items) {
            System.out.println("> " + item.getQuantity() + " x " +
                    item.getProduct().getName() + " - " +
                    (item.getProduct().getPrice() * item.getQuantity()) + " RON");
        }
        System.out.println("Total (inclusiv TVA): " + calculateTotalWithoutOffer() + " RON");
        if (currentDiscount > 0) {
            System.out.println("Discount aplicat: " + currentDiscount + " RON");
            System.out.println("Total de plată: " + (calculateTotalWithoutOffer() - currentDiscount) + " RON");
        }
    }

    public void addProduct(Product product, int quantity) {
        for (OrderItem item : items) {
            if (item.getProduct().getId() != null && item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        // Dacă e produs nou în această comandă, adăugăm rând nou
        OrderItem newItem = new OrderItem(product, quantity);
        newItem.setOrder(this);
        this.items.add(newItem);
    }

    // --- GETTERS / SETTERS ---
    public Long getId() { return id; }
    public List<OrderItem> getItems() { return items; }
    public double getCurrentDiscount() { return currentDiscount; }
    public User getWaiter() { return waiter; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public void setWaiter(User waiter) { this.waiter = waiter; }
    public void setRestaurantTable(RestaurantTable table) { this.restaurantTable = table; }
}