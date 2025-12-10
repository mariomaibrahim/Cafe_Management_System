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

            // -------- Product Name --------
            String name = prodData.getProductName();
            if (name != null && name.length() > 30) {
                name = name.substring(0, 27) + "...";
            }
            productName.setText(name != null ? name : "Unknown Product");

            // -------- Price --------
            productPrice.setText(String.format("%.2f EGP", prodData.getPrice()));

            // -------- Image Load --------
            loadProductImage(prodData.getImage());

            // -------- Add To Cart Button --------
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
                    image = new Image(file.toURI().toString(), 149, 100, true, true);
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

                // Check stock limit
                if (quantity > prodData.getStock()) {
                    showAlert(Alert.AlertType.WARNING, "Insufficient Stock",
                            "Only " + prodData.getStock() + " items available!");
                    return;
                }

                // Add product to cart
                menuController.addToOrder(prodData, quantity);

                // Success message
                showAlert(Alert.AlertType.INFORMATION, "Added!",
                        quantity + "x " + prodData.getProductName() + " added successfully!");

                // Reset spinner
                productScroll.getValueFactory().setValue(1);

            } else {
                showAlert(Alert.AlertType.WARNING, "Invalid Quantity",
                        "Quantity must be greater than 0!");
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // -------- Quantity Spinner Settings --------
        if (productScroll != null) {

            SpinnerValueFactory<Integer> valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
            productScroll.setValueFactory(valueFactory);

            productScroll.setEditable(true);

            productScroll.valueProperty().addListener((obs, oldValue, newValue) -> {

                if (newValue == null || newValue < 1) {
                    productScroll.getValueFactory().setValue(1);
                }

                else if (prodData != null && newValue > prodData.getStock()) {
                    productScroll.getValueFactory().setValue(prodData.getStock());

                    showAlert(Alert.AlertType.WARNING, "Stock Limit",
                            "Max allowed is " + prodData.getStock());
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