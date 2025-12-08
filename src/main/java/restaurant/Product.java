package restaurant;

import javafx.beans.property.*;

public sealed abstract class Product permits Food, Drink {
    protected final StringProperty name;
    protected final DoubleProperty price;
    protected final BooleanProperty vegetarian;

    public Product(String name, double price, boolean vegetarian) {
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.vegetarian = new SimpleBooleanProperty(vegetarian);
    }

    // Property getters
    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
    public BooleanProperty vegetarianProperty() { return vegetarian; }

    // Standard getters
    public String getName() { return name.get(); }
    public double getPrice() { return price.get(); }
    public boolean isVegetarian() { return vegetarian.get(); }

    // Standard setters
    public void setName(String name) { this.name.set(name); }
    public void setPrice(double price) { this.price.set(price); }
    public void setVegetarian(boolean vegetarian) { this.vegetarian.set(vegetarian); }

    public abstract void displayInfo();
    public abstract String getWeightVolume();

    public abstract String getCategory();
}