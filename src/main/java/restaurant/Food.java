package restaurant;

import javafx.beans.property.*;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("FOOD")
public /*sealed*/ class Food extends Product /*permits Pizza*/ {

    @Column(name = "weight")
    private int weightSimple;

    @Transient
    protected final transient IntegerProperty weight = new SimpleIntegerProperty();

    protected Food() {
        super();
    }

    public Food(String name, double price, boolean vegetarian,
                Category category, int weight) {
        super(name, price, vegetarian, category);
        this.weightSimple = weight;
        syncToProperties();
    }

    public Food(String name, double price, boolean b, int weight) {
        super(name, price, b, Category.FEL_PRINCIPAL);
        this.weightSimple = weight;
        syncToProperties();
    }

    @Override
    protected void syncToProperties() {
        super.syncToProperties();
        this.weight.set(weightSimple);
    }

    @Override
    public void syncFromProperties() {
        super.syncFromProperties();
        this.weightSimple = this.weight.get();
    }

    public IntegerProperty weightProperty() { return weight; }
    public int getWeight() { return weight.get(); }

    public void setWeight(int weight) {
        this.weightSimple = weight;
        this.weight.set(weight);
    }

    @Override public String getWeightVolume() { return getWeight() + "g"; }
    @Override public String getCategory() { return "Mancare"; }
    @Override public void displayInfo() { }
}