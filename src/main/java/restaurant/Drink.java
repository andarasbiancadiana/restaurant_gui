package restaurant;

public final class Drink extends Product {
    private int volume;

    public Drink(String name, double price, boolean vegetarian, int volume) {
        super(name, price, vegetarian);
        this.volume = volume;
    }

    @Override
    public void displayInfo() {
        System.out.println("> " + name + " - " + price + " RON - Volum: " + volume + "ml");
    }
}
