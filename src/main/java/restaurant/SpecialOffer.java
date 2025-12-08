package restaurant;

import java.util.Map;

public abstract class SpecialOffer {
    public abstract String getName();
    public abstract boolean isApplicable(Order order);
    public abstract void applyDiscount(Order order);

    protected void printOfferApplied(Order order, double discount) {
        System.out.println("Oferta aplicată: " + getName());
        System.out.println("Total (după ofertă): " + String.format("%.2f", (order.calculateTotalWithoutOffer() - discount)) + " RON");
        System.out.println(" ----------- ");
    }

    public static final SpecialOffer PIZZA_DRINK_FREE = new SpecialOffer() {
        @Override
        public String getName() {
            return "restaurant.Pizza + băutură gratis";
        }

        @Override
        public boolean isApplicable(Order order) {
            boolean hasPizza = order.getProducts().keySet().stream()
                    .anyMatch(p -> p instanceof Food && p.getName().toLowerCase().contains("pizza"));
            boolean hasDrink = order.getProducts().keySet().stream()
                    .anyMatch(p -> p instanceof Drink);
            return hasPizza && hasDrink;
        }

        @Override
        public void applyDiscount(Order order) {
            if (!isApplicable(order)) return;

            Product cheapestDrink = null;
            for (Map.Entry<Product, Integer> entry : order.getProducts().entrySet()) {
                Product p = entry.getKey();
                if (p instanceof Drink) {
                    if (cheapestDrink == null || p.getPrice() < cheapestDrink.getPrice()) {
                        cheapestDrink = p;
                    }
                }
            }

            if (cheapestDrink != null) {
                double discount = cheapestDrink.getPrice();
                order.setCurrentDiscount(discount);
                printOfferApplied(order, discount);
            }
        }
    };

    public static final SpecialOffer VALENTINES_DAY = new SpecialOffer() {
        @Override
        public String getName() { return "Valentine’s Day - 10% reducere"; }

        @Override
        public boolean isApplicable(Order order) { return true; }

        @Override
        public void applyDiscount(Order order) {
            double discount = 0;
            for (Map.Entry<Product, Integer> entry : order.getProducts().entrySet()) {
                discount += entry.getKey().getPrice() * entry.getValue() * 0.10;
            }
            order.setCurrentDiscount(discount);
            printOfferApplied(order, discount);
        }
    };

    public static final SpecialOffer HAPPY_HOUR = new SpecialOffer() {
        @Override
        public String getName() { return "Happy Hour - 20% reducere la băuturi"; }

        @Override
        public boolean isApplicable(Order order) { return true; }

        @Override
        public void applyDiscount(Order order) {
            double discount = 0;
            for (Map.Entry<Product, Integer> entry : order.getProducts().entrySet()) {
                Product p = entry.getKey();
                if (p instanceof Drink) {
                    discount += p.getPrice() * entry.getValue() * 0.20;
                }
            }
            order.setCurrentDiscount(discount);
            printOfferApplied(order, discount);
        }
    };

    public static final SpecialOffer TEN_PERCENT_OVER_5_PRODUCTS = new SpecialOffer() {
        @Override
        public String getName() {
            return "10% reducere pentru 5 sau mai multe produse";
        }

        @Override
        public boolean isApplicable(Order order) {
            int total = 0;
            for (int qty : order.getProducts().values()) total += qty;
            return total >= 5;
        }

        @Override
        public void applyDiscount(Order order) {
            if (!isApplicable(order)) return;

            double discount = 0;
            for (Map.Entry<Product, Integer> entry : order.getProducts().entrySet()) {
                discount += entry.getKey().getPrice() * entry.getValue() * 0.10;
            }
            order.setCurrentDiscount(discount);
            printOfferApplied(order, discount);
        }
    };

    public static final SpecialOffer OFF25_SPECIAL = new SpecialOffer() {
        @Override
        public String getName() { return "Special - 25% reducere"; }

        @Override
        public boolean isApplicable(Order order) { return true; }

        @Override
        public void applyDiscount(Order order) {
            double discount = 0;
            for (Map.Entry<Product, Integer> entry : order.getProducts().entrySet()) {
                discount += entry.getKey().getPrice() * entry.getValue() * 0.25;
            }
            order.setCurrentDiscount(discount);
            printOfferApplied(order, discount);
        }
    };
}
