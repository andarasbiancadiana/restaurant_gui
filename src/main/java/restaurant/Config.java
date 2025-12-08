package restaurant;

public class Config {
    private String restaurantName;
    private int tva;

    public Config() {}

    public Config(String restaurantName, int tva) {
        this.restaurantName = restaurantName;
        this.tva = tva;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public int getTva() {
        return tva;
    }

    @Override
    public String toString() {
        return "Config{restaurantName='" + restaurantName + "', tva=" + tva + "}";
    }
}