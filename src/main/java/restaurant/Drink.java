package restaurant;

import javafx.beans.property.*;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("DRINK")
public final class Drink extends Product {

    @Column(name = "volume")
    private int volumeSimple;

    @Transient
    protected final transient IntegerProperty volume = new SimpleIntegerProperty();

    protected Drink() {
        super();
    }

    public Drink(String name, double price, boolean vegetarian, Category category, int volume) {
        super(name, price, vegetarian, category);
        this.volumeSimple = volume;
        syncToProperties();
    }

    @Override
    protected void syncToProperties() {
        super.syncToProperties();
        this.volume.set(volumeSimple);
    }

    @Override
    public void syncFromProperties() {
        super.syncFromProperties();
        this.volumeSimple = this.volume.get();
    }

    public IntegerProperty volumeProperty() { return volume; }
    public int getVolume() { return volume.get(); }

    public void setVolume(int volume) {
        this.volumeSimple = volume;
        this.volume.set(volume);
    }

    @Override public String getWeightVolume() { return getVolume() + "ml"; }
    @Override public String getCategory() { return "Bautura"; }
    @Override public void displayInfo() { }
}