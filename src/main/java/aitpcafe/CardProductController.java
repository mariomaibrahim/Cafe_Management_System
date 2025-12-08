package aitpcafe;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;

import java.net.URL;
import java.io.File;
import java.util.ResourceBundle;

public class CardProductController implements Initializable {

    @FXML private Button productBtn;
    @FXML private Label productName;
    @FXML private Label productPrice;
    @FXML private Spinner<Integer> productScroll;
    @FXML private ImageView productimage;

    private productData prodData;
    private Image image;
    private MenuController menuController;

    public void setMenuController(MenuController menuController) {
        this.menuController = menuController;
    }

    public void setData(productData prodData){
        this.prodData = prodData;

        if (prodData != null) {
            // Set product name with proper truncation
            String name = prodData.getProductName();
            if (name != null && name.length() > 30) {
                name = name.substring(0, 27) + "...";
            }
            productName.setText(name != null ? name : "Unknown Product");
            
            // Set product price with EGP currency
            productPrice.setText(String.format("%.2f EGP", prodData.getPrice()));

            // Load product image
            loadProductImage(prodData.getImage());
            
            // Set button action
            if (productBtn != null) {
                productBtn.setOnAction(event -> addToCart());
            }
        }
    }

    private void loadProductImage(String imagePath) {
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);

                if (file.exists()) {
                    image = new Image(file.toURI().toString(), 190, 140, true, true);
                    productimage.setImage(image);
                } else {
                    loadDefaultImage();
                    System.out.println("IMAGE NOT FOUND => " + imagePath);
                }
            } else {
                loadDefaultImage();
            }
        } catch (Exception e) {
            loadDefaultImage();
            e.printStackTrace();
        }
    }

    private void loadDefaultImage() {
        try {
            productimage.setImage(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addToCart() {
        if (prodData != null && productScroll != null && menuController != null) {
            int quantity = productScroll.getValue();
            
            if (quantity > 0) {
                // Check if quantity is available in stock
                if (quantity > prodData.getStock()) {
                    showAlert(Alert.AlertType.WARNING, "Insufficient Stock", 
                             "Only " + prodData.getStock() + " items available in stock!");
                    return;
                }
                
                // Add to cart through menu controller
                menuController.addToOrder(prodData, quantity);
                
                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Added to Cart", 
                         quantity + "x " + prodData.getProductName() + " added successfully!");
                
                // Reset spinner to 1
                productScroll.getValueFactory().setValue(1);
            } else {
                showAlert(Alert.AlertType.WARNING, "Invalid Quantity", 
                         "Please select a quantity greater than 0!");
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (productScroll != null) {
            SpinnerValueFactory<Integer> valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
            productScroll.setValueFactory(valueFactory);
            
            productScroll.setEditable(true);
            
            productScroll.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue == null || newValue < 1) {
                    productScroll.getValueFactory().setValue(1);
                } else if (prodData != null && newValue > prodData.getStock()) {
                    productScroll.getValueFactory().setValue(prodData.getStock());
                    showAlert(Alert.AlertType.WARNING, "Stock Limit", 
                             "Maximum available quantity is " + prodData.getStock());
                }
            });
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}