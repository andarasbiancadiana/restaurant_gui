package restaurant.gui;

import javafx.application. Application;
import javafx.scene.Scene;
import javafx. scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.*;
import javafx.geometry.Insets;
import restaurant.Drink;
import restaurant.Food;
import restaurant.Pizza;
import restaurant.Product;

public class RestaurantGUI extends Application {

    // Componente UI
    private ListView<Product> productListView;
    private TextField numeField, pretField, categorieField, gramajVolumField;
    private CheckBox vegetarianCheckBox;
    private Label detailsTitle;

    // Model (Observable List - Date Reactive)
    private ObservableList<Product> productList;

    // Panouri specifice pentru fiecare tip de produs
    private VBox pizzaDetailsPanel;
    private VBox foodDetailsPanel;
    private VBox drinkDetailsPanel;

    // Fields specifice
    private TextField doughField, sauceField;
    private ListView<String> toppingsListView;
    private TextField weightField;
    private TextField volumeField;

    @Override
    public void start(Stage primaryStage) {
        // scena
        primaryStage.setTitle("Restaurant Menu Manager - GUI");

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10));

        // stanga
        VBox leftPanel = createLeftPanel();
        // dreapta / centru
        VBox centerPanel = createCenterPanel();

        mainLayout.setLeft(leftPanel);
        mainLayout.setCenter(centerPanel);

        // scena
        Scene scene = new Scene(mainLayout, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        loadProducts();
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setPrefWidth(300);
        leftPanel.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-width: 1;");

        Label listLabel = new Label("Lista Produse");
        listLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // lista observabila (Model Reactiv)
        productList = FXCollections.observableArrayList();
        productListView = new ListView<>(productList);
        productListView.setPrefHeight(500);

        productListView.setCellFactory(param -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getName());
                    setStyle("-fx-font-size: 14px; -fx-padding: 5;");
                }
            }
        });

        leftPanel.getChildren().addAll(listLabel, productListView);
        VBox.setVgrow(productListView, Priority. ALWAYS);

        return leftPanel;
    }

    private VBox createCenterPanel() {
        VBox centerPanel = new VBox(15);
        centerPanel.setPadding(new Insets(20));
        centerPanel.setStyle("-fx-background-color: white;");

        detailsTitle = new Label("Detalii Produs");
        detailsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Separator separator1 = new Separator();

        // Formular pentru toate produsele
        GridPane commonForm = createCommonForm();

        // Panouri specifice pentru fiecare tip de produs
        pizzaDetailsPanel = createPizzaDetailsPanel();
        foodDetailsPanel = createFoodDetailsPanel();
        drinkDetailsPanel = createDrinkDetailsPanel();

        // Ascundem initial
        hideAllSpecificPanels();

        centerPanel.getChildren().addAll(
                detailsTitle,
                separator1,
                commonForm,
                pizzaDetailsPanel,
                foodDetailsPanel,
                drinkDetailsPanel
        );

        setupReactiveBinding();

        return centerPanel;
    }

    private GridPane createCommonForm() {
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(12);
        form.setPadding(new Insets(10));

        numeField = new TextField();
        numeField.setPromptText("Numele produsului");
        numeField.setPrefWidth(300);

        pretField = new TextField();
        pretField.setPromptText("Preț în RON");
        pretField. setPrefWidth(300);

        categorieField = new TextField();
        categorieField.setEditable(false);
        categorieField.setStyle("-fx-background-color: #ecf0f1;");

        gramajVolumField = new TextField();
        gramajVolumField.setEditable(false);
        gramajVolumField.setStyle("-fx-background-color: #ecf0f1;");

        vegetarianCheckBox = new CheckBox("Vegetarian");
        vegetarianCheckBox.setStyle("-fx-font-size: 13px;");

        form.add(createBoldLabel("Nume:"), 0, 0);
        form.add(numeField, 1, 0);

        form.add(createBoldLabel("Preț (RON):"), 0, 1);
        form.add(pretField, 1, 1);

        form.add(createBoldLabel("Categorie:"), 0, 2);
        form.add(categorieField, 1, 2);

        form.add(createBoldLabel("Gramaj/Volum:"), 0, 3);
        form.add(gramajVolumField, 1, 3);

        form.add(vegetarianCheckBox, 1, 4);

        return form;
    }

    private Label createBoldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        return label;
    }

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
        doughField.setPrefWidth(300);

        sauceField = new TextField();
        sauceField.setPromptText("Ex: Roșii, Smântână");
        sauceField. setPrefWidth(300);

        toppingsListView = new ListView<>();
        toppingsListView. setPrefHeight(100);
        toppingsListView.setStyle("-fx-border-color: #ddd;");

        pizzaGrid.add(createBoldLabel("Blat:"), 0, 0);
        pizzaGrid.add(doughField, 1, 0);
        pizzaGrid. add(createBoldLabel("Sos:"), 0, 1);
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
        weightField.setPrefWidth(300);

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
        volumeField. setPrefWidth(300);

        drinkGrid.add(createBoldLabel("Volum (ml):"), 0, 0);
        drinkGrid. add(volumeField, 1, 0);

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

    private void setupReactiveBinding() {
        // (Observer Pattern - Subiectul: selectedItemProperty, Observatorul:funcție lambda)
        productListView.getSelectionModel().selectedItemProperty(). addListener(
                (observable, oldValue, newValue) -> {
                    if (oldValue != null) {
                        unbindProduct(oldValue);
                    }

                    if (newValue != null) {
                        bindProduct(newValue);
                        updateDetailsTitle(newValue);
                        showRelevantPanel(newValue);
                    } else {
                        clearForm();
                    }
                }
        );
    }

    private void bindProduct(Product product) {
        // Bidirectional Binding pentru câmpurile comune
        // Când modifici în TextField, se actualizează automat în Product
        numeField.textProperty().bindBidirectional(product.nameProperty());

        pretField.textProperty().bindBidirectional(
                product.priceProperty(),
                new javafx.util.converter.NumberStringConverter()
        );

        vegetarianCheckBox.selectedProperty().bindBidirectional(product.vegetarianProperty());

        categorieField.setText(product.getCategory());
        gramajVolumField.setText(product.getWeightVolume());

        if (product instanceof Pizza pizza) {
            doughField. textProperty().bindBidirectional(pizza.doughProperty());
            sauceField.textProperty(). bindBidirectional(pizza. sauceProperty());
            toppingsListView.setItems(pizza.toppingsProperty());

        } else if (product instanceof Food food) {
            weightField.textProperty().bindBidirectional(
                    food.weightProperty(),
                    new javafx.util. converter.NumberStringConverter()
            );

        } else if (product instanceof Drink drink) {
            volumeField.textProperty().bindBidirectional(
                    drink.volumeProperty(),
                    new javafx. util.converter.NumberStringConverter()
            );
        }
    }

    private void unbindProduct(Product product) {
        numeField.textProperty().unbindBidirectional(product.nameProperty());
        pretField.textProperty().unbindBidirectional(product.priceProperty());
        vegetarianCheckBox.selectedProperty(). unbindBidirectional(product.vegetarianProperty());

        if (product instanceof Pizza pizza) {
            doughField.textProperty(). unbindBidirectional(pizza.doughProperty());
            sauceField.textProperty().unbindBidirectional(pizza. sauceProperty());

        } else if (product instanceof Food food) {
            weightField. textProperty().unbindBidirectional(food.weightProperty());

        } else if (product instanceof Drink drink) {
            volumeField.textProperty().unbindBidirectional(drink.volumeProperty());
        }
    }

    private void updateDetailsTitle(Product product) {
        detailsTitle.setText("Detalii: " + product.getName());
    }

    private void showRelevantPanel(Product product) {
        hideAllSpecificPanels();

        if (product instanceof Pizza) {
            pizzaDetailsPanel. setVisible(true);
            pizzaDetailsPanel.setManaged(true);

        } else if (product instanceof Food && !(product instanceof Pizza)) {
            foodDetailsPanel. setVisible(true);
            foodDetailsPanel.setManaged(true);

        } else if (product instanceof Drink) {
            drinkDetailsPanel.setVisible(true);
            drinkDetailsPanel.setManaged(true);
        }
    }

    private void clearForm() {
        numeField.clear();
        pretField.clear();
        categorieField.clear();
        gramajVolumField.clear();
        vegetarianCheckBox.setSelected(false);
        doughField.clear();
        sauceField.clear();
        toppingsListView.setItems(null);
        weightField.clear();
        volumeField.clear();
        detailsTitle.setText("Detalii Produs");
        hideAllSpecificPanels();
    }

    /**
     * Încarcă produsele (deocamdată date dummy, data viitoare din DB)
     */
    private void loadProducts() {
        // TODO: Încarcă din baza de date folosind Repository
        // For now, creăm date de test

        Pizza margherita = new Pizza. Builder(1, "Pizza Margherita", 25.50, 500)
                . dough("Subțire")
                .sauce("Roșii")
                .addTopping("Mozzarella")
                .addTopping("Busuioc")
                .addTopping("Roșii cherry")
                .build();

        Pizza quattroStagioni = new Pizza.Builder(2, "Pizza Quattro Stagioni", 32.00, 550)
                .dough("Groasă")
                .sauce("Roșii")
                .addTopping("Ciuperci")
                .addTopping("Șuncă")
                .addTopping("Măsline")
                .addTopping("Anghinare")
                .build();

        Food burger = new Food("Burger Classic", 18.00, false, 300);
        Food salad = new Food("Salată Caesar", 15.50, true, 250);

        Drink cola = new Drink("Coca Cola", 7.50, true, 330);
        Drink water = new Drink("Apă Minerală", 5.00, true, 500);

        productList.addAll(margherita, quattroStagioni, burger, salad, cola, water);
    }

    public static void main(String[] args) {
        launch(args);
    }
}