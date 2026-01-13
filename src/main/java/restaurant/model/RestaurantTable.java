package restaurant.model;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurant_tables")
public class RestaurantTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int tableNumber;
    private boolean isOccupied = false;

    public RestaurantTable() {}
    public RestaurantTable(int tableNumber) { this.tableNumber = tableNumber; }

    public Long getId() { return id; }
    public int getTableNumber() { return tableNumber; }
    public boolean isOccupied() { return isOccupied; }
    public void setOccupated(boolean occupied) { isOccupied = occupied; }
}