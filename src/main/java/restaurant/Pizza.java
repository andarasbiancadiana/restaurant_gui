package restaurant;

import java.util.ArrayList;
import java.util.List;

public final class Pizza extends Food {
    private String dough;
    private String sauce;
    private List<String> toppings;

    private Pizza(Builder builder) {
        super(builder.name, builder.price, true, builder.weight);
        this.dough = builder.dough;
        this.sauce = builder.sauce;
        this.toppings = builder.toppings;
    }

    @Override
    public void displayInfo() {
        System.out.println("> " + name + " - " + price + " RON - Gramaj: " + weight + "g");
        System.out.println("  Blat: " + dough + ", Sos: " + sauce);
        if (!toppings.isEmpty()) {
            System.out.print("  Topping-uri: ");
            for (int i = 0; i < toppings.size(); i++) {
                System.out.print(toppings.get(i));
                if (i < toppings.size() - 1) System.out.print(", ");
            }
            System.out.println();
        }
    }

    public static class Builder {
        private String name;
        private double price;
        private int weight;
        private String dough;
        private String sauce;
        private List<String> toppings = new ArrayList<>();

        public Builder(String name, double price, int weight) {
            this.name = name;
            this.price = price;
            this.weight = weight;
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

        public Pizza build() {
            if (dough == null || sauce == null) {
                throw new IllegalStateException("Trebuie să selectați tipul de blat și sos!");
            }
            return new Pizza(this);
        }
    }
}
