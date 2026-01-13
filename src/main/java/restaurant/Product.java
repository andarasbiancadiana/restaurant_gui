package restaurant;

import javafx.beans.property.*;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "product_type", discriminatorType = DiscriminatorType.STRING)
public abstract /*sealed*/ class Product /*permits Food, Drink*/ {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String nameSimple;

    @Column(name = "price")
    private double priceSimple;

    @Column(name = "is_vegetarian")
    private boolean vegetarianSimple;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category categorySimple;

    private String descriptionSimple;
    private String imageUrlSimple;

    @Transient
    protected final transient StringProperty description = new SimpleStringProperty("");
    @Transient
    protected final transient StringProperty imageUrl = new SimpleStringProperty("");
    // Proprietati JavaFX pentru GUI (marcate cu @Transient sa fie ignorate de Hibernate)
    @Transient
    protected final transient StringProperty name = new SimpleStringProperty("");
    @Transient
    protected final transient DoubleProperty price = new SimpleDoubleProperty(0.0);
    @Transient
    protected final transient BooleanProperty vegetarian = new SimpleBooleanProperty(false);

    protected Product() {
    }

    public Product(String name, double price, boolean vegetarian, Category category) {
        this.nameSimple = name;
        this.priceSimple = price;
        this.vegetarianSimple = vegetarian;
        this.name.set(name);
        this.categorySimple = category;
        this.price.set(price);
        this.vegetarian.set(vegetarian);
    }

    @PostLoad
    protected void syncToProperties() {
        this.name.set(nameSimple);
        this.price.set(priceSimple);
        this.vegetarian.set(vegetarianSimple);
        this.description.set(descriptionSimple);
        this.imageUrl.set(imageUrlSimple);
    }

    public void syncFromProperties() {
        this.nameSimple = this.name.get();
        this.priceSimple = this.price.get();
        this.vegetarianSimple = this.vegetarian.get();
    }

    // Property getters pentru Binding
    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
    public BooleanProperty vegetarianProperty() { return vegetarian; }

    // Standard getters
    public String getName() { return name.get(); }
    public double getPrice() { return price.get(); }
    public boolean isVegetarian() { return vegetarian.get(); }
    public Long getId() { return id; }

    // Standard setters
    public void setName(String name) {
        this.nameSimple = name;
        this.name.set(name);
    }
    public void setPrice(double price) {
        this.priceSimple = price;
        this.price.set(price);
    }
    public void setVegetarian(boolean vegetarian) {
        this.vegetarianSimple = vegetarian;
        this.vegetarian.set(vegetarian);
    }

    public void setCategory(Category category) {
        this.categorySimple = category;
    }
    public Category getCategoryEnum() {
        return categorySimple;
    }

    public abstract void displayInfo();
    public abstract String getWeightVolume();
    public abstract String getCategory();
}