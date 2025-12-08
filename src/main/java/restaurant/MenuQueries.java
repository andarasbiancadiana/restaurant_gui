package restaurant;

import java.util.*;

public class MenuQueries {

    public static List<Product> getVegetarianProducts(Menu menu) {
        if (menu == null) return List.of();
        return menu.getAllProducts().stream()
                .filter(p -> p instanceof Food food && food.isVegetarian())
                .sorted(Comparator.comparing(Product::getName))
                .toList();
    }

    public static OptionalDouble getAverageDessertPrice(Menu menu) {
        if (menu == null) return OptionalDouble.empty();
        List<Product> desserts = menu.getProductsByCategory(Category.DESERT);
        return desserts.stream()
                .mapToDouble(Product::getPrice)
                .average();
    }

    public static boolean existsOver100(Menu menu) {
        if (menu == null) return false;
        return menu.getAllProducts().stream()
                .anyMatch(p -> p.getPrice() > 100);
    }
}
