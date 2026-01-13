package restaurant.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Menu;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.*;
import javafx.stage.FileChooser;
import restaurant.*;
import restaurant.model.*;
import restaurant.repository.*;
import restaurant.util.*;
import restaurant.view.LoginView;
import restaurant.controller.LoginController;
import javafx.concurrent.Task;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class RestaurantGUI extends Application {

    // UI Components
    private TableView<Product> productTable;
    private TextField nameField, priceField, categoryField, weightField, volumeField, weightVolumeField, subCatgeoryField;
    private TextField searchField, minPriceField, maxPriceField;
    private CheckBox vegetarianCheckBox, filterVegCheckBox;
    private ComboBox<String> filterTypeCombo;
    private ImageView productImageView;
    private Label descriptionLabel, detailsTitle, totalLabel = new Label("Total: 0.00 RON");

    // Specific product panels
    private VBox pizzaDetailsPanel, foodDetailsPanel, drinkDetailsPanel;
    private TextField doughField, sauceField;
    private ListView<String> toppingsListView;

    // Data & State
    private Stage mainStage;
    private final LoginController loginController = new LoginController();
    private final ProductRepository productRepository = new ProductRepository();
    private final OrderRepository orderRepository = new OrderRepository();
    private final UserRepository userRepository = new UserRepository();
    private final TableRepository tableRepository = new TableRepository();

    private ObservableList<Product> productList;
    private ObservableList<OrderItem> cartItems = FXCollections.observableArrayList();
    private TableView<OrderItem> cartTable;

    private User loggedUser;
    private Order currentOrder;
    private RestaurantTable selectedTable;
    private Product currentEditingProduct;

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final ProgressIndicator loadingSpinner = new ProgressIndicator();

    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;
        ensureTestUsersExist();
        showLoginScreen();
    }

    // LOGIN SCREEN
    private void showLoginScreen() {
        LoginView loginView = new LoginView();
        loginView.getLoginButton().setOnAction(e -> {
            try {
                User user = loginController.login(loginView.getUsername(), loginView.getPassword());
                this.loggedUser = user;
                showMainMenu(user.getRole());
            } catch (Exception ex) {
                showError("Login Failed", ex.getMessage());
            }
        });

        loginView.getGuestButton().setOnAction(e -> {
            this.loggedUser = new User("guest", "", Role.GUEST);
            showMainMenu(Role.GUEST);
        });

        Scene scene = new Scene(loginView, 450, 450);
        mainStage.setScene(scene);
        mainStage.setTitle("Restaurant La Andrei - Login");
        mainStage.centerOnScreen();
        mainStage.show();
    }

    // MAIN MENU
    private void showMainMenu(Role role) {
        // StackPane pentru a suprapune spinner-ul de încărcare
        StackPane rootStack = new StackPane();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Digital Menu
        Tab menuTab = new Tab("Meniu Produse");
        BorderPane menuLayout = new BorderPane();
        menuLayout.setLeft(createLeftPanel());
        menuLayout.setCenter(createCenterPanel());
        menuTab.setContent(menuLayout);
        tabPane.getTabs().add(menuTab);

        // Waiter Module
        if (role == Role.STAFF || role == Role.ADMIN) {
            Tab ordersTab = new Tab("Mese & Comenzi");
            ordersTab.setContent(createWaiterModule());
            tabPane.getTabs().add(ordersTab);
        }

        // Admin Module
        if (role == Role.ADMIN) {
            Tab adminTab = new Tab("Administrare");
            adminTab.setContent(createAdminModule());
            tabPane.getTabs().add(adminTab);
        }

        // Feedback-ul Vizual
        loadingSpinner.setMaxSize(80, 80);
        // loadingSpinner.setVisible(false); // Va fi controlat de Task

        // Adăugăm tab-urile și spinner-ul deasupra lor
        rootStack.getChildren().addAll(tabPane, loadingSpinner);

        BorderPane mainRoot = new BorderPane();
        if (role != Role.GUEST) {
            mainRoot.setTop(createMenuBar(mainStage));
        }
        mainRoot.setCenter(rootStack);

        applyRolePermissions(role);

        Scene scene = new Scene(mainRoot, 1200, 800);
        mainStage.setScene(scene);
        mainStage.setTitle("Restaurant La Andrei - Conectat ca: " + role);
        mainStage.centerOnScreen();

        // Înlocuim loadProducts() cu versiunea asincronă care folosește Task
        loadProductsAsync();
    }

    private void loadProductsAsync() {
        if (productList == null) {
            productList = FXCollections.observableArrayList();
        }

        // creăm un Task (sarcina de fundal)
        Task<List<Product>> loadTask = new Task<>() {
            @Override
            protected List<Product> call() throws Exception {
                // Această parte rulează pe un fir secundar (Background Thread)
                List<Product> data = productRepository.findAll();

                if (data.isEmpty()) {
                    System.out.println("LOG: Baza de date goală. Populez date inițiale...");
                    populateInitialData();
                    data = productRepository.findAll();
                }
                return data;
            }
        };

        loadingSpinner.visibleProperty().unbind();
        // feedback Vizual: Legăm spinner-ul de starea Task-ului
        loadingSpinner.visibleProperty().bind(loadTask.runningProperty());
        // dezactivăm tabelul în timp ce se încarcă
        productTable.disableProperty().bind(loadTask.runningProperty());

        // cand se termina cu succes
        loadTask.setOnSucceeded(e -> {
            productList.setAll(loadTask.getValue());
            if (productTable != null) {
                productTable.setItems(productList);
                productTable.refresh();
            }
            System.out.println("LOG: Date încărcate asincron.");
        });

        // in caz de eroare
        loadTask.setOnFailed(e -> {
            showError("Eroare la încărcare", loadTask.getException().getMessage());
        });

        // trimitem sarcina spre execuție
        executorService.submit(loadTask);
    }

    // FILTERING Streams API
    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        boolean vegOnly = filterVegCheckBox.isSelected();
        String type = filterTypeCombo.getValue();

        double min = minPriceField.getText().isEmpty() ? 0 : Double.parseDouble(minPriceField.getText());
        double max = maxPriceField.getText().isEmpty() ? 9999 : Double.parseDouble(maxPriceField.getText());

        List<Product> filtered = productList.stream()
                .filter(p -> p.getName().toLowerCase().contains(search))
                .filter(p -> !vegOnly || p.isVegetarian())
                .filter(p -> p.getPrice() >= min && p.getPrice() <= max)
                .filter(p -> type == null || type.equals("Toate") || p.getCategory().equalsIgnoreCase(type))
                .collect(Collectors.toList());

        productTable.setItems(FXCollections.observableArrayList(filtered));
    }

    // LEFT PANEL
    private VBox createLeftPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(450);

        // Filter section
        VBox filterBox = new VBox(8);
        filterBox.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 10; -fx-border-color: #ddd;");

        searchField = new TextField();
        searchField.setPromptText("Caută produs...");
        searchField.setOnKeyReleased(e -> applyFilters());

        HBox priceRange = new HBox(5,
                new Label("Preț:"),
                minPriceField = new TextField(),
                new Label("-"),
                maxPriceField = new TextField()
        );
        minPriceField.setPrefWidth(50);
        maxPriceField.setPrefWidth(50);
        minPriceField.setPromptText("Min");
        maxPriceField.setPromptText("Max");
        minPriceField.setOnKeyReleased(e -> applyFilters());
        maxPriceField.setOnKeyReleased(e -> applyFilters());

        filterVegCheckBox = new CheckBox("Vegetarian");
        filterVegCheckBox.setOnAction(e -> applyFilters());

        filterTypeCombo = new ComboBox<>(FXCollections.observableArrayList("Toate", "Mancare", "Bautura"));
        filterTypeCombo.setValue("Toate");
        filterTypeCombo.setOnAction(e -> applyFilters());

        filterBox.getChildren().addAll(
                new Label("Filtrează Meniul"),
                searchField, priceRange, filterVegCheckBox, filterTypeCombo
        );

        // Product table
        productTable = new TableView<>();
        TableColumn<Product, String> nameCol = new TableColumn<>("Produs");
        nameCol.setCellValueFactory(d -> d.getValue().nameProperty());

        TableColumn<Product, Number> priceCol = new TableColumn<>("Preț (RON)");
        priceCol.setCellValueFactory(d -> d.getValue().priceProperty());

        productTable.getColumns().addAll(nameCol, priceCol);
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                bindProduct(newV);
                updateVisualDetails(newV);
            }
        });

        panel.getChildren().addAll(filterBox, productTable);
        VBox.setVgrow(productTable, Priority.ALWAYS);
        return panel;
    }

    // CENTER PANEL
    private VBox createCenterPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white;");

        detailsTitle = new Label("Detalii Produs");
        detailsTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        productImageView = new ImageView();
        productImageView.setFitWidth(250);
        productImageView.setPreserveRatio(true);

        descriptionLabel = new Label("Selectează un produs pentru a vedea descrierea.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(400);

        GridPane commonForm = createCommonForm();
        pizzaDetailsPanel = createPizzaDetailsPanel();
        foodDetailsPanel = createFoodDetailsPanel();
        drinkDetailsPanel = createDrinkDetailsPanel();
        hideAllSpecificPanels();

        HBox adminActionButtons = new HBox(15);
        adminActionButtons.setPadding(new Insets(10, 0, 0, 0));

        Button btnSaveToDB = new Button("Salvează / Update în DB");
        btnSaveToDB.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSaveToDB.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                productRepository.save(selected);
                loadProducts();
                showSuccess("Succes", "Produsul a fost actualizat.");
            }
        });

        Button btnDeleteFromDB = new Button("Șterge din DB");
        btnDeleteFromDB.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnDeleteFromDB.setOnAction(e -> {
            Product selected = productTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Ștergi definitiv acest produs?");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        productRepository.delete(selected);
                        loadProducts();
                        clearForm();
                    }
                });
            }
        });

        adminActionButtons.getChildren().addAll(btnSaveToDB, btnDeleteFromDB);

        adminActionButtons.visibleProperty().bind(nameField.disableProperty().not());
        adminActionButtons.managedProperty().bind(adminActionButtons.visibleProperty());

        panel.getChildren().addAll(
                detailsTitle,
                productImageView,
                descriptionLabel,
                new Separator(),
                commonForm,
                pizzaDetailsPanel,
                foodDetailsPanel,
                drinkDetailsPanel,
                new Separator(),
                adminActionButtons
        );

        return panel;
    }

    // PRODUCT BINDING
    private void bindProduct(Product product) {
        unbindProduct();

        this.currentEditingProduct = product;

        nameField.textProperty().bindBidirectional(product.nameProperty());

        priceField.textProperty().bindBidirectional(product.priceProperty(), new javafx.util.converter.NumberStringConverter());

        vegetarianCheckBox.selectedProperty().bindBidirectional(product.vegetarianProperty());

        categoryField.setText(product.getCategory());
        subCatgeoryField.setText(product.getCategoryEnum().getDisplayName());
        weightVolumeField.setText(product.getWeightVolume());

        if (product instanceof Food f && !(product instanceof Pizza)) {
            weightField.textProperty().bindBidirectional(f.weightProperty(), new javafx.util.converter.NumberStringConverter());
        } else if (product instanceof Drink d) {
            volumeField.textProperty().bindBidirectional(d.volumeProperty(), new javafx.util.converter.NumberStringConverter());
        }
    }

    private void unbindProduct() {
        if (currentEditingProduct != null) {
            nameField.textProperty().unbindBidirectional(currentEditingProduct.nameProperty());
            priceField.textProperty().unbindBidirectional(currentEditingProduct.priceProperty());
            vegetarianCheckBox.selectedProperty().unbindBidirectional(currentEditingProduct.vegetarianProperty());

            if (currentEditingProduct instanceof Food f) weightField.textProperty().unbindBidirectional(f.weightProperty());
            if (currentEditingProduct instanceof Drink d) volumeField.textProperty().unbindBidirectional(d.volumeProperty());

            currentEditingProduct = null;
        }
    }

    private void updateVisualDetails(Product p) {
        detailsTitle.setText("Detalii: " + p.getName());
        descriptionLabel.setText(p.getCategory() + " de calitate superioară. Conține ingrediente proaspete.");
    }

    // COMMON FORM
    private GridPane createCommonForm() {
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(12);
        form.setPadding(new Insets(10));

        nameField = new TextField();
        nameField.setPromptText("Numele produsului");
        nameField.setPrefWidth(300);

        priceField = new TextField();
        priceField.setPromptText("Preț în RON");
        priceField.setPrefWidth(300);

        categoryField = new TextField();
        categoryField.setEditable(true);
        categoryField.setStyle("-fx-background-color: #ecf0f1;");

        subCatgeoryField = new TextField();
        subCatgeoryField.setEditable(true);
        subCatgeoryField.setStyle("-fx-background-color: #ecf0f1;");

        weightVolumeField = new TextField();
        weightVolumeField.setEditable(true);
        weightVolumeField.setStyle("-fx-background-color: #ecf0f1;");

        vegetarianCheckBox = new CheckBox("Vegetarian");

        form.add(createBoldLabel("Nume:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(createBoldLabel("Preț (RON):"), 0, 1);
        form.add(priceField, 1, 1);
        form.add(createBoldLabel("Categorie:"), 0, 2);
        form.add(categoryField, 1, 2);
        form.add(createBoldLabel("Subcategorie:"), 0, 3);
        form.add(subCatgeoryField, 1, 3);
        form.add(createBoldLabel("Gramaj/Volum:"), 0, 4);
        form.add(weightVolumeField, 1, 4);
        form.add(vegetarianCheckBox, 1, 5);

        return form;
    }

    private Label createBoldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold;");
        return label;
    }

    // SPECIFIC PANELS
    private VBox createPizzaDetailsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15, 0, 0, 0));
        Label pizzaLabel = new Label("Detalii Pizza");
        pizzaLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        GridPane pizzaGrid = new GridPane();
        pizzaGrid.setHgap(15);
        pizzaGrid.setVgap(12);

        doughField = new TextField();
        doughField.setPromptText("Ex: Subțire, Groasă");
        sauceField = new TextField();
        sauceField.setPromptText("Ex: Roșii, Smântână");
        toppingsListView = new ListView<>();
        toppingsListView.setPrefHeight(100);

        pizzaGrid.add(createBoldLabel("Blat:"), 0, 0);
        pizzaGrid.add(doughField, 1, 0);
        pizzaGrid.add(createBoldLabel("Sos:"), 0, 1);
        pizzaGrid.add(sauceField, 1, 1);
        pizzaGrid.add(createBoldLabel("Topping-uri:"), 0, 2);
        pizzaGrid.add(toppingsListView, 1, 2);

        panel.getChildren().addAll(new Separator(), pizzaLabel, pizzaGrid);
        return panel;
    }

    private VBox createFoodDetailsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15, 0, 0, 0));
        Label foodLabel = new Label("Detalii Food");
        foodLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f39c12;");

        GridPane foodGrid = new GridPane();
        foodGrid.setHgap(15);
        foodGrid.setVgap(12);

        weightField = new TextField();
        weightField.setPromptText("Gramaj în g");
        foodGrid.add(createBoldLabel("Gramaj (g):"), 0, 0);
        foodGrid.add(weightField, 1, 0);

        panel.getChildren().addAll(new Separator(), foodLabel, foodGrid);
        return panel;
    }

    private VBox createDrinkDetailsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15, 0, 0, 0));
        Label drinkLabel = new Label("Detalii Drink");
        drinkLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3498db;");

        GridPane drinkGrid = new GridPane();
        drinkGrid.setHgap(15);
        drinkGrid.setVgap(12);

        volumeField = new TextField();
        volumeField.setPromptText("Volum în ml");
        drinkGrid.add(createBoldLabel("Volum (ml):"), 0, 0);
        drinkGrid.add(volumeField, 1, 0);

        panel.getChildren().addAll(new Separator(), drinkLabel, drinkGrid);
        return panel;
    }

    private void hideAllSpecificPanels() {
        pizzaDetailsPanel.setVisible(false);
        pizzaDetailsPanel.setManaged(false);
        foodDetailsPanel.setVisible(false);
        foodDetailsPanel.setManaged(false);
        drinkDetailsPanel.setVisible(false);
        drinkDetailsPanel.setManaged(false);
    }

    private void showRelevantPanel(Product product) {
        hideAllSpecificPanels();
        if (product instanceof Pizza) {
            pizzaDetailsPanel.setVisible(true);
            pizzaDetailsPanel.setManaged(true);
        } else if (product instanceof Food) {
            foodDetailsPanel.setVisible(true);
            foodDetailsPanel.setManaged(true);
        } else if (product instanceof Drink) {
            drinkDetailsPanel.setVisible(true);
            drinkDetailsPanel.setManaged(true);
        }
    }

    // WAITER MODULE
    private VBox createWaiterModule() {
        HBox layout = new HBox(20);
        layout.setPadding(new Insets(20));

        FlowPane tablesGrid = new FlowPane(10, 10);
        tablesGrid.setPrefWidth(350);
        List<RestaurantTable> tables = tableRepository.findAll();
        if (tables.isEmpty()) {
            for (int i = 1; i <= 8; i++) tableRepository.save(new RestaurantTable(i));
            tables = tableRepository.findAll();
        }

        for (RestaurantTable t : tables) {
            Button btn = new Button("Masa " + t.getTableNumber());
            btn.setPrefSize(80, 80);
            btn.setStyle(t.isOccupied() ? "-fx-background-color: #ff7675;" : "-fx-background-color: #55efc4;");
            btn.setOnAction(e -> selectTable(t, btn));
            tablesGrid.getChildren().add(btn);
        }

        // Cart & Order section
        VBox cartBox = new VBox(10);
        cartBox.setPrefWidth(500);

        cartTable = new TableView<>(cartItems);
        TableColumn<OrderItem, String> cName = new TableColumn<>("Produs");
        cName.setCellValueFactory(d -> d.getValue().getProduct().nameProperty());
        TableColumn<OrderItem, Number> cQty = new TableColumn<>("Cantitate");
        cQty.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantity"));
        cartTable.getColumns().addAll(cName, cQty);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnAdd = new Button("Adaugă Produsul Selectat");
        btnAdd.setOnAction(e -> addSelectedToCart());

        Button btnFinalize = new Button("Finalizează Comanda");
        btnFinalize.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold;");
        btnFinalize.setOnAction(e -> finalizeOrderAction());

        cartBox.getChildren().addAll(
                new Label("Coș Comandă:"),
                cartTable, btnAdd, totalLabel, btnFinalize
        );

        // Personal history
        TableView<Order> personalHistory = new TableView<>();
        TableColumn<Order, Long> hId = new TableColumn<>("ID Comandă");
        hId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        TableColumn<Order, Double> hTotal = new TableColumn<>("Total Încasat");
        hTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("total"));
        personalHistory.getColumns().addAll(hId, hTotal);
        personalHistory.setPrefHeight(200);

        if (loggedUser != null) {
            List<Order> history = orderRepository.findByWaiter(loggedUser);
            personalHistory.setItems(FXCollections.observableArrayList(history));
        }

        VBox historyBox = new VBox(10, new Label("Istoricul tău:"), personalHistory);

        layout.getChildren().addAll(
                new VBox(10, new Label("Sala:"), tablesGrid),
                new VBox(10, cartBox, new Separator(), historyBox)
        );

        return new VBox(layout);
    }

    private void selectTable(RestaurantTable table, Button btn) {
        if (table.isOccupied()) {
            showError("Masa Ocupată", "Masa " + table.getTableNumber() + " este deja ocupată!");
            return;
        }
        this.selectedTable = table;
        this.currentOrder = new Order();
        this.currentOrder.setRestaurantTable(table);
        this.currentOrder.setWaiter(loggedUser);
        table.setOccupated(true);
        tableRepository.save(table);
        btn.setStyle("-fx-background-color: #ff7675;");
        cartItems.clear();
        showSuccess("Masa Selectată", "Masa " + table.getTableNumber() + " - Comanda pornită");
    }

    private void addSelectedToCart() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null && currentOrder != null) {
            currentOrder.addProduct(selected, 1);
            cartItems.setAll(new ArrayList<>(currentOrder.getItems()));
            totalLabel.setText("Subtotal: " + String.format("%.2f", currentOrder.calculateTotalWithoutOffer()) + " RON");
        } else {
            showError("Atenție", "Selectați o masă și un produs!");
        }
    }

    // DISCOUNT ENGINE
    private class DiscountResult {
        double amount = 0;
        String details = "";
    }

    private DiscountResult getDetailedDiscounts(Order order) {
        DiscountResult result = new DiscountResult();
        StringBuilder sb = new StringBuilder();

        // 1. Happy Hour: -50% la fiecare a doua băutură
        long drinkCount = order.getItems().stream()
                .filter(i -> i.getProduct() instanceof Drink)
                .mapToLong(OrderItem::getQuantity).sum();
        if (drinkCount >= 2) {
            double avgPrice = order.getItems().stream()
                    .filter(i -> i.getProduct() instanceof Drink)
                    .mapToDouble(i -> i.getProduct().getPrice()).average().orElse(0);
            double hhDisc = (drinkCount / 2) * (avgPrice * 0.5);
            result.amount += hhDisc;
            sb.append("- Happy Hour Băuturi: ").append(String.format("%.2f", hhDisc)).append(" RON\n");
        }

        // 2. Party Pack: La 4 Pizza, una (cea mai ieftină) e gratis
        long pizzaCount = order.getItems().stream()
                .filter(i -> i.getProduct() instanceof Pizza)
                .mapToLong(OrderItem::getQuantity).sum();
        if (pizzaCount >= 4) {
            double cheapestPizza = order.getItems().stream()
                    .filter(i -> i.getProduct() instanceof Pizza)
                    .mapToDouble(i -> i.getProduct().getPrice()).min().orElse(0);
            result.amount += cheapestPizza;
            sb.append("- Party Pack (4+ Pizza): ").append(String.format("%.2f", cheapestPizza)).append(" RON\n");
        }

        // 3. Meal Deal: Pizza + Desert (Food care nu e Pizza) = 25% reducere desert
        boolean hasPizza = order.getItems().stream().anyMatch(i -> i.getProduct() instanceof Pizza);
        Optional<OrderItem> desert = order.getItems().stream()
                .filter(i -> i.getProduct() instanceof Food && !(i.getProduct() instanceof Pizza))
                .min(Comparator.comparingDouble(i -> i.getProduct().getPrice()));
        if (hasPizza && desert.isPresent()) {
            double mdDisc = desert.get().getProduct().getPrice() * 0.25;
            result.amount += mdDisc;
            sb.append("- Meal Deal (Pizza+Desert): ").append(String.format("%.2f", mdDisc)).append(" RON\n");
        }

        result.details = sb.toString();
        return result;
    }
    private double calculateDiscounts(Order order) {
        double d = 0;

        // Happy Hour: Every 2nd drink 50% off
        long drinks = order.getItems().stream()
                .filter(i -> i.getProduct() instanceof Drink)
                .mapToLong(OrderItem::getQuantity).sum();
        if (drinks >= 2) {
            double avgDrinkPrice = order.getItems().stream()
                    .filter(i -> i.getProduct() instanceof Drink)
                    .mapToDouble(i -> i.getProduct().getPrice())
                    .average().orElse(0);
            d += (drinks / 2) * (avgDrinkPrice * 0.5);
        }

        // Party Pack: 4 pizzas, 1 free
        long pizzas = order.getItems().stream()
                .filter(i -> i.getProduct() instanceof Pizza)
                .mapToLong(OrderItem::getQuantity).sum();
        if (pizzas >= 4) {
            double cheapestPizza = order.getItems().stream()
                    .filter(i -> i.getProduct() instanceof Pizza)
                    .mapToDouble(i -> i.getProduct().getPrice())
                    .min().orElse(0);
            d += cheapestPizza;
        }

        return d;
    }

    private void finalizeOrderAction() {
        if (currentOrder == null || currentOrder.isEmpty()) {
            showError("Eroare", "Comanda este goală!");
            return;
        }

        // Obținem reducerile detaliate (pe bon)
        DiscountResult disc = getDetailedDiscounts(currentOrder);
        double subtotal = currentOrder.calculateTotalWithoutOffer();
        double finalTotal = subtotal - disc.amount;

        currentOrder.setCurrentDiscount(disc.amount);
        currentOrder.setTotal(finalTotal);

        // Salvăm în DB
        orderRepository.save(currentOrder);

        // Eliberăm masa în DB (Cerință Barem)
        selectedTable.setOccupated(false);
        tableRepository.save(selectedTable);

        // Afișăm Bonul
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bon Fiscal - Masa " + selectedTable.getTableNumber());
        alert.setHeaderText("Comandă Finalizată cu Succes");
        alert.setContentText(
                "Subtotal: " + String.format("%.2f", subtotal) + " RON\n" +
                        "------------------\n" +
                        (disc.amount > 0 ? "REDUCERI APLICATE:\n" + disc.details : "Nicio ofertă aplicată.\n") +
                        "------------------\n" +
                        "TOTAL DE PLATĂ: " + String.format("%.2f", finalTotal) + " RON"
        );
        alert.showAndWait();

        // RESETARE SESIUNE
        currentOrder = null;
        selectedTable = null;
        cartItems.clear();
        totalLabel.setText("Total: 0.00 RON");

        // REFRESH UI
        showMainMenu(loggedUser.getRole());

        showSuccess("Masa eliberata", "Comanda a fost salvata si masa este acum libera (verde).");
    }

    // ADMIN MODULE
    private VBox createAdminModule() {
        TabPane adminTabs = new TabPane();
        adminTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // GESTIUNE PERSONAL
        Tab staffTab = new Tab("Personal (CRUD)");
        VBox staffLayout = new VBox(10);
        staffLayout.setPadding(new Insets(15));

        TableView<User> userTable = new TableView<>();
        TableColumn<User, String> uNameCol = new TableColumn<>("Utilizator");
        uNameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("username"));
        TableColumn<User, Role> uRoleCol = new TableColumn<>("Rol");
        uRoleCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("role"));
        userTable.getColumns().addAll(uNameCol, uRoleCol);

        userTable.setItems(FXCollections.observableArrayList(userRepository.findAll()));

        HBox addUserBox = new HBox(10);
        TextField newUName = new TextField(); newUName.setPromptText("Username");
        TextField newUPass = new TextField(); newUPass.setPromptText("Parolă");
        ComboBox<Role> roleCombo = new ComboBox<>(FXCollections.observableArrayList(Role.STAFF, Role.ADMIN));
        roleCombo.setValue(Role.STAFF);
        Button btnAddUser = new Button("Angajează (Add)");

        btnAddUser.setOnAction(e -> {
            if(!newUName.getText().isEmpty()) {
                userRepository.save(new User(newUName.getText(), newUPass.getText(), roleCombo.getValue()));
                userTable.setItems(FXCollections.observableArrayList(userRepository.findAll()));
                newUName.clear(); newUPass.clear();
                showSuccess("Succes", "Utilizator nou adăugat.");
            }
        });
        addUserBox.getChildren().addAll(newUName, newUPass, roleCombo, btnAddUser);

        Button btnDeleteUser = new Button("Concediază (Delete)");
        btnDeleteUser.setStyle("-fx-background-color: #ff7675; -fx-text-fill: white;");
        btnDeleteUser.setOnAction(e -> {
            User selected = userTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Acțiune Critică");
                confirm.setHeaderText("Ștergi utilizatorul " + selected.getUsername() + "?");
                confirm.setContentText("ATENȚIE: Această acțiune va șterge automat tot istoricul de comenzi al acestui ospătar!");

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        userRepository.delete(selected);
                        userTable.setItems(FXCollections.observableArrayList(userRepository.findAll()));
                    }
                });
            }
        });

        staffLayout.getChildren().addAll(new Label("Gestiune Angajați:"), userTable, addUserBox, btnDeleteUser);
        staffTab.setContent(staffLayout);

        // GESTIUNE MENIU
        Tab menuManageTab = new Tab("Gestiune Meniu");
        VBox menuBox = new VBox(10);
        menuBox.setPadding(new Insets(15));

        Label menuTitle = new Label("Administrare Produse:");
        menuTitle.setStyle("-fx-font-weight: bold;");

        Button btnAddNew = new Button("Adaugă Produs Nou (Mâncare)");
        btnAddNew.setPrefWidth(250);
        btnAddNew.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        btnAddNew.setOnAction(e -> {
            Product newProd = new Food("Produs Nou", 0.0, true, Category.FEL_PRINCIPAL, 0);
            productRepository.save(newProd);
            loadProducts();
            productTable.getSelectionModel().select(newProd); // Îl selectăm pentru editare în centrul ecranului
            showSuccess("Creat", "Produs nou adăugat în DB. Editați-l în panoul central.");
        });


        menuBox.getChildren().addAll(menuTitle, btnAddNew);
        menuManageTab.setContent(menuBox);

        // CONTROL OFERTE
        Tab offersTab = new Tab("Setări Oferte");
        VBox offersLayout = new VBox(15);
        offersLayout.setPadding(new Insets(20));

        CheckBox chkHappyHour = new CheckBox("Activează Happy Hour (A doua băutură -50%)");
        CheckBox chkMealDeal = new CheckBox("Activează Meal Deal (Pizza + Desert -25%)");
        CheckBox chkPartyPack = new CheckBox("Activează Party Pack (4 Pizza -> 1 moca)");

        chkHappyHour.setSelected(true); chkMealDeal.setSelected(true); chkPartyPack.setSelected(true);

        offersLayout.getChildren().addAll(new Label("Activează/Dezactivează dinamic regulile de reducere:"),
                chkHappyHour, chkMealDeal, chkPartyPack);
        offersTab.setContent(offersLayout);

        // ISTORIC GLOBAL
        Tab historyTab = new Tab("Istoric Global");
        VBox historyLayout = new VBox(10);
        historyLayout.setPadding(new Insets(10));

        TableView<Order> globalTable = new TableView<>();
        TableColumn<Order, Long> hId = new TableColumn<>("ID");
        hId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        TableColumn<Order, String> hWaiter = new TableColumn<>("Ospătar");
        hWaiter.setCellValueFactory(d -> {
            String waiterName = (d.getValue().getWaiter() != null) ? d.getValue().getWaiter().getUsername() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(waiterName);
        });
        TableColumn<Order, Double> hTotal = new TableColumn<>("Total (RON)");
        hTotal.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("total"));

        globalTable.getColumns().addAll(hId, hWaiter, hTotal);
        globalTable.setItems(FXCollections.observableArrayList(orderRepository.findAll()));

        historyLayout.getChildren().addAll(new Label("Toate comenzile din restaurant:"), globalTable);
        historyTab.setContent(historyLayout);

        adminTabs.getTabs().addAll(staffTab, menuManageTab, offersTab, historyTab);
        return new VBox(adminTabs);
    }

    private void updateDetailsTitle(Product product) {
        if (product != null) {
            detailsTitle.setText("Detalii: " + product.getName());
        } else {
            detailsTitle.setText("Detalii Produs");
        }
    }

    private void clearForm() {
        unbindProduct();
        detailsTitle.setText("Detalii Produs");
        nameField.clear();
        priceField.clear();
        categoryField.clear();
        subCatgeoryField.clear();
        weightVolumeField.clear();
        vegetarianCheckBox.setSelected(false);
        weightField.clear();
        volumeField.clear();
        hideAllSpecificPanels();
    }

    // UTILITIES
    private void loadProducts() {
        productList = FXCollections.observableArrayList(productRepository.findAll());
        if (productList.isEmpty()) {
            populateInitialData();
            productList.setAll(productRepository.findAll());
        }
        productTable.setItems(productList);
    }

    private void populateInitialData() {
        productRepository.save(new Food("Pizza Margherita", 35.0, true, Category.FEL_PRINCIPAL, 450));
        productRepository.save(new Food("Burger Beef", 45.0, false, Category.FEL_PRINCIPAL, 500));
        productRepository.save(new Drink("Limonadă", 18.0, true, Category.BAUTURI_RACORITOARE, 400));
    }

    private void applyRolePermissions(Role role) {
        boolean canEdit = (role == Role.GUEST || role == Role.STAFF);

        nameField.setEditable(!canEdit);
        nameField.setDisable(canEdit);
        priceField.setEditable(!canEdit);
        priceField.setDisable(canEdit);
        vegetarianCheckBox.setDisable(canEdit);
        weightVolumeField.setEditable(!canEdit);
        weightVolumeField.setDisable(canEdit);
        categoryField.setEditable(!canEdit);
        categoryField.setDisable(canEdit);
        subCatgeoryField.setEditable(!canEdit);
        subCatgeoryField.setDisable(canEdit);

        if (doughField != null) {
            doughField.setEditable(!canEdit);
            doughField.setDisable(canEdit);
        }
        if (sauceField != null) {
            sauceField.setEditable(!canEdit);
            sauceField.setDisable(canEdit);
        }

    }

    private void ensureTestUsersExist() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            userRepository.save(new User("admin", "admin", Role.ADMIN));
            userRepository.save(new User("waiter", "1234", Role.STAFF));
        }
    }

    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");

        MenuItem exportItem = new MenuItem("Export JSON");
        exportItem.setOnAction(e -> handleExport(stage));

        MenuItem importItem = new MenuItem("Import JSON");
        importItem.setOnAction(e -> handleImport(stage));

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> stage.close());

        fileMenu.getItems().addAll(exportItem, importItem, new SeparatorMenuItem(), exitItem);
        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }

    private void handleExport(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvează Meniu (Export)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                String json = JsonManager.toJson(productRepository.findAll());
                Files.writeString(file.toPath(), json);
                showSuccess("Export", "Salvat în: " + file.getAbsolutePath());
            } catch (Exception e) {
                showError("Eroare la export", e.getMessage());
            }
        }
    }

    private void handleImport(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Încarcă Meniu (Import)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                String json = Files.readString(file.toPath());
                Product[] importedProducts = JsonManager.fromJson(json);
                for (Product p : importedProducts) {
                    productRepository.save(p);
                }
                loadProducts();
                showSuccess("Import", "Încărcat din: " + file.getAbsolutePath());
            } catch (Exception e) {
                showError("Eroare la import", e.getMessage());
            }
        }
    }

    private void showError(String title, String content) {
        new Alert(Alert.AlertType.ERROR, content).showAndWait();
    }

    private void showSuccess(String title, String content) {
        new Alert(Alert.AlertType.INFORMATION, content).showAndWait();
    }

    @Override
    public void stop() {
        executorService.shutdown();
        PersistenceManager.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}