package restaurant;

import javafx. beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util. ArrayList;
import java.util. List;

public final class Pizza extends Food {
    private final StringProperty dough;
    private final StringProperty sauce;
    private final ObservableList<String> toppings;

    private Pizza(Builder builder) {
        super(builder.name, builder.price, true, builder.weight);
        this.dough = new SimpleStringProperty(builder.dough);
        this.sauce = new SimpleStringProperty(builder.sauce);
        this.toppings = FXCollections.observableArrayList(builder.toppings);
    }

    // Property getters
    public StringProperty doughProperty() { return dough; }
    public StringProperty sauceProperty() { return sauce; }
    public ObservableList<String> toppingsProperty() { return toppings; }

    // Standard getters
    public String getDough() { return dough.get(); }
    public String getSauce() { return sauce.get(); }
    public List<String> getToppings() { return new ArrayList<>(toppings); }

    // Standard setters
    public void setDough(String dough) { this.dough.set(dough); }
    public void setSauce(String sauce) { this. sauce.set(sauce); }
    public void addTopping(String topping) { this.toppings.add(topping); }
    public void removeTopping(String topping) { this.toppings.remove(topping); }
    public void clearToppings() { this.toppings.clear(); }

    @Override
    public void displayInfo() {
        System.out. println("> " + getName() + " - " + getPrice() + " RON - Gramaj: " + getWeight() + "g");
        System. out.println("  Blat: " + getDough() + ", Sos: " + getSauce());
        if (!toppings.isEmpty()) {
            System.out.print("  Topping-uri: ");
            for (int i = 0; i < toppings.size(); i++) {
                System.out.print(toppings.get(i));
                if (i < toppings.size() - 1) System.out.print(", ");
            }
            System.out.println();
        }
    }

    @Override
    public String getCategory() {
        return "Pizza";
    }

    public static class Builder {
        private int id;
        private String name;
        private double price;
        private int weight;
        private String dough;
        private String sauce;
        private List<String> toppings = new ArrayList<>();

        public Builder(String name, double price, int weight) {
            this.id = 0; // Default ID, will be set by database
            this.name = name;
            this.price = price;
            this.weight = weight;
        }

        public Builder(int id, String name, double price, int weight) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.weight = weight;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder dough(String dough) {
            this.dough = dough;
            return this;
        }

        public Builder sauce(String sauce) {
            this.sauce = sauce;
            return this;
        }

        public Builder addTopping(String topping) {
            toppings.add(topping);
            return this;
        }

        public Builder toppings(List<String> toppings) {
            this. toppings = new ArrayList<>(toppings);
            return this;
        }

        public Pizza build() {
            if (dough == null || sauce == null) {
                throw new IllegalStateException("Trebuie să selectați tipul de blat și sos!");
            }
            return new Pizza(this);
        }
    }
}