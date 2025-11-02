package aitpcafe;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Product {
    private final IntegerProperty id;
    private final StringProperty productId; // New field for product ID (String)
    private final StringProperty productName;
    private final StringProperty type;
    private final IntegerProperty stock;
    private final DoubleProperty price;
    private final StringProperty status;
    private final StringProperty date;

    public Product(int id, String productId, String name, String type, int stock, double price, String status, String date) {
        this.id = new SimpleIntegerProperty(id);
        this.productId = new SimpleStringProperty(productId);
        this.productName = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.stock = new SimpleIntegerProperty(stock);
        this.price = new SimpleDoubleProperty(price);
        this.status = new SimpleStringProperty(status);
        this.date = new SimpleStringProperty(date);
    }

    // Properties
    public IntegerProperty idProperty() { return id; }
    public StringProperty productIdProperty() { return productId; }
    public StringProperty productNameProperty() { return productName; }
    public StringProperty typeProperty() { return type; }
    public IntegerProperty stockProperty() { return stock; }
    public DoubleProperty priceProperty() { return price; }
    public StringProperty statusProperty() { return status; }
    public StringProperty dateProperty() { return date; }

    // Getters
    public int getId() { return id.get(); }
    public String getProductId() { return productId.get(); }
    public String getProductName() { return productName.get(); }
    public String getType() { return type.get(); }
    public int getStock() { return stock.get(); }
    public double getPrice() { return price.get(); }
    public String getStatus() { return status.get(); }
    public String getDate() { return date.get(); }

    // Setters
    public void setProductName(String name) { this.productName.set(name); }
    public void setType(String type) { this.type.set(type); }
    public void setStock(int stock) { this.stock.set(stock); }
    public void setPrice(double price) { this.price.set(price); }
    public void setStatus(String status) { this.status.set(status); }
}
