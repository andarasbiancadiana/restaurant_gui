package restaurant;

public sealed abstract class Product permits Food, Drink {
    protected String name;
    protected double price;
    protected boolean vegetarian;

    public Product(String name, double price, boolean vegetarian) {
        this.name = name;
        this.price = price;
        this.vegetarian = vegetarian;
    }

    public double getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public abstract void displayInfo();
}
