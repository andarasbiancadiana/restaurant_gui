package restaurant;

import javafx.beans.property.*;

public sealed class Food extends Product permits Pizza {
    protected final IntegerProperty weight;

    public Food(String name, double price, boolean vegetarian, int weight) {
        super(name, price, vegetarian);
        this. weight = new SimpleIntegerProperty(weight);
    }

    // Property getter
    public IntegerProperty weightProperty() { return weight; }

    // Standard getter
    public int getWeight() { return weight.get(); }

    // Standard setter
    public void setWeight(int weight) { this. weight.set(weight); }

    @Override
    public void displayInfo() {
        System.out.println("> " + getName() + " - " + getPrice() + " RON - Gramaj: " + getWeight() + "g");
    }

    @Override
    public String getWeightVolume() {
        return getWeight() + "g";
    }

    @Override
    public String getCategory() {
        return "Mancare";
    }
}