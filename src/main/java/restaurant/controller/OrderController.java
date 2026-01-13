package restaurant.controller;

import restaurant.Category;
import restaurant.model.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class OrderController {

    // Happy Hour - La fiecare a doua băutură, prețul e redus 50%
    public double applyHappyHour(Order order) {
        List<OrderItem> drinks = order.getItems().stream()
                .filter(item -> item.getProduct() instanceof restaurant.Drink)
                .toList();

        int totalDrinkQuantity = drinks.stream().mapToInt(OrderItem::getQuantity).sum();
        // Perechile primesc reducere
        int discountedUnits = totalDrinkQuantity / 2;

        if (drinks.isEmpty()) return 0;
        double drinkPrice = drinks.get(0).getProduct().getPrice();
        return discountedUnits * (drinkPrice * 0.5);
    }

    // Meal Deal - Pizza + cel mai ieftin desert = 25% reducere desert
    public double applyMealDeal(Order order) {
        boolean hasPizza = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getName().toLowerCase().contains("pizza"));

        Optional<OrderItem> cheapestDessert = order.getItems().stream()
                .filter(item -> item.getProduct().getCategoryEnum() == Category.DESERT)
                .min(Comparator.comparingDouble(item -> item.getProduct().getPrice()));

        if (hasPizza && cheapestDessert.isPresent()) {
            // Reducere 25% la cel mai ieftin desert
            return cheapestDessert.get().getProduct().getPrice() * 0.25;
        }
        return 0;
    }

    // Party Pack - La 4 Pizza comandate, una (cea mai ieftina) e gratis
    public double applyPartyPack(Order order) {
        long pizzaCount = order.getItems().stream()
                .filter(item -> item.getProduct().getName().toLowerCase().contains("pizza"))
                .mapToLong(OrderItem::getQuantity)
                .sum();

        if (pizzaCount >= 4) {
            return order.getItems().stream()
                    .filter(item -> item.getProduct().getName().toLowerCase().contains("pizza"))
                    .mapToDouble(item -> item.getProduct().getPrice())
                    .min()
                    .orElse(0);
        }
        return 0;
    }
}