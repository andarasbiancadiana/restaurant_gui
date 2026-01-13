package restaurant;

import restaurant.model.Order;
import restaurant.model.OrderItem;

import java.util.Comparator;

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
            return "Pizza + băutură gratis";
        }

        @Override
        public boolean isApplicable(Order order) {
            // Verificăm dacă există cel puțin o Pizza
            boolean hasPizza = order.getItems().stream()
                    .map(OrderItem::getProduct)
                    .anyMatch(p -> p instanceof Food && p.getName().toLowerCase().contains("pizza"));

            // Verificăm dacă există cel puțin o băutură
            boolean hasDrink = order.getItems().stream()
                    .map(OrderItem::getProduct)
                    .anyMatch(p -> p instanceof Drink);

            return hasPizza && hasDrink;
        }

        @Override
        public void applyDiscount(Order order) {
            if (!isApplicable(order)) return;

            // Găsim cea mai ieftină băutură folosind Streams
            double discount = order.getItems().stream()
                    .map(OrderItem::getProduct)
                    .filter(p -> p instanceof Drink)
                    .mapToDouble(Product::getPrice)
                    .min()
                    .orElse(0.0);

            if (discount > 0) {
                order.setCurrentDiscount(discount);
                printOfferApplied(order, discount);
            }
        }
    };

    public static final SpecialOffer VALENTINES_DAY = new SpecialOffer() {
        @Override
        public String getName() { return "Valentine’s Day - 10% reducere"; }

        @Override
        public boolean isApplicable(Order order) { return !order.isEmpty(); }

        @Override
        public void applyDiscount(Order order) {
            // Calculăm 10% din subtotalul tuturor produselor (fără TVA inclus aici, sau conform logicii tale de calcul)
            double subtotal = order.getItems().stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();

            double discount = subtotal * 0.10;
            order.setCurrentDiscount(discount);
            printOfferApplied(order, discount);
        }
    };

    public static final SpecialOffer HAPPY_HOUR = new SpecialOffer() {
        @Override
        public String getName() { return "Happy Hour - 20% reducere la băuturi"; }

        @Override
        public boolean isApplicable(Order order) {
            return order.getItems().stream().anyMatch(item -> item.getProduct() instanceof Drink);
        }

        @Override
        public void applyDiscount(Order order) {
            // Aplicăm 20% doar pe produsele de tip Drink
            double discount = order.getItems().stream()
                    .filter(item -> item.getProduct() instanceof Drink)
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity() * 0.20)
                    .sum();

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
            // Numărăm cantitatea totală de produse din comandă
            long totalQuantity = order.getItems().stream()
                    .mapToLong(OrderItem::getQuantity)
                    .sum();
            return totalQuantity >= 5;
        }

        @Override
        public void applyDiscount(Order order) {
            if (!isApplicable(order)) return;

            double subtotal = order.getItems().stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();

            double discount = subtotal * 0.10;
            order.setCurrentDiscount(discount);
            printOfferApplied(order, discount);
        }
    };

    public static final SpecialOffer OFF25_SPECIAL = new SpecialOffer() {
        @Override
        public String getName() { return "Special - 25% reducere"; }

        @Override
        public boolean isApplicable(Order order) { return !order.isEmpty(); }

        @Override
        public void applyDiscount(Order order) {
            double subtotal = order.getItems().stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();

            double discount = subtotal * 0.25;
            order.setCurrentDiscount(discount);
            printOfferApplied(order, discount);
        }
    };
}