package restaurant;

import java.util.*;

public class Main {
    static Scanner scanner = new Scanner(System.in);
    static SpecialOffer dailyOffer = null;
    static Config config;

    public static void main(String[] args) {
        config = ConfigLoader.load("config.json");
        Menu menu = new Menu();
        List<Order> orders = new ArrayList<>();
        List<SpecialOffer> offers = new ArrayList<>();

        menu.addCategory(Category.FEL_PRINCIPAL, List.of(
                new Food("Pizza Margherita", 45.0, true, 450),
                new Food("Paste Carbonara", 52.5, false, 400)
        ));

        menu.addCategory(Category.BAUTURI_RACORITOARE, List.of(
                new Drink("Limonada", 15.0, true, 400),
                new Drink("Apa Plata", 8.0, true, 500)
        ));

        offers.add(SpecialOffer.PIZZA_DRINK_FREE);
        offers.add(SpecialOffer.VALENTINES_DAY);
        offers.add(SpecialOffer.HAPPY_HOUR);
        offers.add(SpecialOffer.TEN_PERCENT_OVER_5_PRODUCTS);
        offers.add(SpecialOffer.OFF25_SPECIAL);

        boolean running = true;
        while (running) {
            System.out.println("1. Meniu");
            System.out.println("2. Comanda noua");
            System.out.println("3. Alege oferta zilei");
            System.out.println("4. Interogari meniu");
            System.out.println("5. Cauta produs dupa nume");
            System.out.println("6. Exporta meniu in format JSON");
            System.out.println("7. Iesire");
            System.out.println("Optiune aleasa: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("−−− Meniul Restaurantului \"" + config.getRestaurantName() + "\" −−−");
                    for (Category category : menu.getCategories()) {
                        System.out.println("== " + category.getDisplayName() + " ==");
                        List<Product> products = menu.getProductsByCategory(category);
                        for (Product product : products) {
                            product.displayInfo();
                        }
                        System.out.println();
                    }
                    System.out.println("−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−−");
                    break;
                case 2:
                    createNewOrder(menu, orders);
                    break;
                case 3:
                    System.out.println("Alege oferta zilei:");
                    for (int i = 0; i < offers.size(); i++) {
                        System.out.println((i + 1) + ". " + offers.get(i).getName());
                    }
                    System.out.println("Optiune aleasa: ");
                    int offerIndex = scanner.nextInt() - 1;
                    if (offerIndex < 0 || offerIndex >= offers.size()) {
                        System.out.println("Oferta invalida.");
                    } else {
                        dailyOffer = offers.get(offerIndex);
                        System.out.println("Oferta selectată: " + dailyOffer.getName());
                    }
                    break;
                case 4:
                    System.out.println("Interogari meniu:");
                    List<Product> vegProducts = MenuQueries.getVegetarianProducts(menu);
                    System.out.println("1. Preparatele vegetariene, sortate alfabetic:");
                    for (Product p : vegProducts) {
                        p.displayInfo();
                    }

                    OptionalDouble avgDessertPrice = MenuQueries.getAverageDessertPrice(menu);
                    System.out.println("\n2. Pretul mediu al deserturilor:");
                    if (avgDessertPrice.isPresent()) {
                        System.out.printf("Pretul mediu al deserturilor este: %.2f RON%n", avgDessertPrice.getAsDouble());
                    } else {
                        System.out.println("Nu exista deserturi in meniu.");
                    }

                    boolean existsOver100 = MenuQueries.existsOver100(menu);
                    System.out.println("\n3. Exista produse cu pretul peste 100 RON?");
                    System.out.println(existsOver100 ? "Da" : "Nu");
                    break;
                case 5:
                    System.out.println("Introdu numele produsului cautat:");
                    scanner.nextLine();
                    String productName = scanner.nextLine();
                    Optional<Product> productOpt = findProductByName(menu, productName);
                    if (productOpt.isPresent()) {
                        System.out.println("Produs gasit:");
                        productOpt.get().displayInfo();
                    } else {
                        System.out.println("Produsul nu a fost gasit in meniu.");
                    }
                    break;
                case 6:
                    java.nio.file.Path out = java.nio.file.Paths.get("exported_menu.json");
                    MenuExport.exportMenu(menu, out);
                    break;
                case 7:
                    running = false;
                    System.out.println("La revedere!");
                    break;
                default:
                    System.out.println("Optiune invalida. Te rog incearca din nou.");
            }
        }
    }

    public static Optional<Product> findProductByName(Menu menu, String name) {
        if (menu == null) return Optional.empty();
        return menu.findProductByName(name);
    }

    public static void createNewOrder(Menu menu, List<Order> orders) {
        List<Product> productsMenu = menu.getAllProducts();
        Order order = new Order();
        boolean running = true;
        while (running) {
            System.out.println("1. Adauga produs standard");
            System.out.println("2. Adauga pizza customizata");
            System.out.println("3. Finalizeaza comanda");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("Selecteaza produsul dupa numar:");
                    for (Category category : menu.getCategories()) {
                        System.out.println("== " + category.getDisplayName() + " ==");
                        List<Product> catProducts = menu.getProductsByCategory(category);
                        if (catProducts == null) continue;
                        for (int i = 0; i < productsMenu.size(); i++) {
                            Product p = productsMenu.get(i);
                            if (catProducts.contains(p)) {
                                System.out.print((i + 1) + ". ");
                                p.displayInfo();
                            }
                        }
                    }
                    int productIndex = scanner.nextInt() - 1;
                    if (productIndex < 0 || productIndex >= productsMenu.size()) {
                        System.out.println("Produsul nu exista.");
                        break;
                    }
                    Product selectedProduct = productsMenu.get(productIndex);
                    System.out.println("Introdu cantitatea:");
                    int quantity = scanner.nextInt();
                    order.addProduct(selectedProduct, quantity);
                    System.out.println("Produs adaugat in comanda.");
                    break;
                case 2:
                    String name = "restaurant.Pizza custom";
                    double price = 75.0;
                    int weight = 500;
                    scanner.nextLine();
                    System.out.println("Introdu tipul blatului:");
                    String dough = scanner.nextLine();
                    System.out.println("Introdu tipul sosului:");
                    String sauce = scanner.nextLine();

                    Pizza.Builder pizzaBuilder = new Pizza.Builder(name, price, weight)
                            .dough(dough)
                            .sauce(sauce);

                    System.out.println("Adauga topping-uri (scrie 'gata' pentru a termina):");
                    while (true) {
                        String topping = scanner.nextLine();
                        if (topping.equalsIgnoreCase("gata")) {
                            break;
                        }
                        pizzaBuilder.addTopping(topping);
                    }

                    Pizza customPizza = pizzaBuilder.build();
                    System.out.println("Introdu cantitatea:");
                    int pizzaQuantity = scanner.nextInt();
                    order.addProduct(customPizza, pizzaQuantity);
                    System.out.println("Pizza customizata adaugata in comanda.");
                    break;
                case 3:
                    if (!order.isEmpty()) {
                        orders.add(order);
                        System.out.println("Comanda finalizata:");
                        order.displayOrder(orders.size());
                        if (dailyOffer != null && dailyOffer.isApplicable(order)) {
                            dailyOffer.applyDiscount(order);
                        }
                    } else {
                        System.out.println("Comanda este goala. Nu se poate finaliza.");
                    }
                    running = false;
                    break;
                default:
                    System.out.println("Optiune invalida.");
            }
        }
    }
}
