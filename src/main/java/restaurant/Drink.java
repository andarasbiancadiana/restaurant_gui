package restaurant;

import javafx. beans.property.*;

public final class Drink extends Product {
    private final IntegerProperty volume;

    public Drink(String name, double price, boolean vegetarian, int volume) {
        super(name, price, vegetarian);
        this.volume = new SimpleIntegerProperty(volume);
    }

    // Property getter
    public IntegerProperty volumeProperty() { return volume; }

    // Standard getter
    public int getVolume() { return volume. get(); }

    // Standard setter
    public void setVolume(int volume) { this.volume. set(volume); }

    @Override
    public void displayInfo() {
        System.out.println("> " + getName() + " - " + getPrice() + " RON - Volum: " + getVolume() + "ml");
    }

    @Override
    public String getWeightVolume() {
        return getVolume() + "ml";
    }

    @Override
    public String getCategory() {
        return "Bautura";
    }
}