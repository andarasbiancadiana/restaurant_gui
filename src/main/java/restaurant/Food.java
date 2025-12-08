package restaurant;

public sealed class Food extends Product permits Pizza {
    protected int weight;

    public Food(String name, double price, boolean vegetarian, int weight) {
        super(name, price, vegetarian);
        this.weight = weight;
    }

    @Override
    public void displayInfo() {
        System.out.println("> " + name + " - " + price + " RON - Gramaj: " + weight + "g");
    }
}
