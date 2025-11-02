package aitpcafe;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class InventoryController implements Initializable {

    // ============================ FXML NODES ============================
    @FXML private Button Customers_btn, Inventory_btn, logout_btn, menu_btn;
    @FXML private AnchorPane inventory_form;
    @FXML private TableView<productData> inventory_table;
    @FXML private TableColumn<productData, String> inventory_col_ID, inventory_col_productName, 
            inventory_col_type, inventory_col_stock, inventory_col_price, 
            inventory_col_status, inventory_col_date;
    @FXML private TextField inventory_productID, inventory_productName, inventory_stock, inventory_price;
    @FXML private ComboBox<String> inventory_type, inventory_status;
    @FXML private Button inventory_addBtn, inventory_updateBtn, inventory_clearBtn, inventory_deleteBtn;
    @FXML private ImageView inventory_image;
    @FXML private Label username;

    // ============================ DATABASE VARIABLES ============================
    private Connection connect;
    private PreparedStatement prepare;
    private Statement statement;
    private ResultSet result;
    private Image image; 

    // ============================ INVENTORY CRUD ============================
    private ObservableList<productData> inventoryListData() {
        ObservableList<productData> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM product";
        connect = database.connectDB();

        try (PreparedStatement ps = connect.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productData prod = new productData(
                        rs.getInt("id"),
                        rs.getString("product_id"),
                        rs.getString("product_name"),
                        rs.getInt("stock"),
                        rs.getDouble("price"),
                        rs.getString("status"),
                        rs.getString("image"),
                        rs.getDate("date")
                );
                listData.add(prod);
            }
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
        }
        return listData;
    }

    private ObservableList<productData> inventoryList;

    public void inventoryShowData() {
        inventoryList = inventoryListData();
        inventory_col_ID.setCellValueFactory(new PropertyValueFactory<>("productId"));
        inventory_col_productName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        inventory_col_type.setCellValueFactory(new PropertyValueFactory<>("type"));
        inventory_col_stock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        inventory_col_price.setCellValueFactory(new PropertyValueFactory<>("price"));
        inventory_col_status.setCellValueFactory(new PropertyValueFactory<>("status"));
        inventory_col_date.setCellValueFactory(new PropertyValueFactory<>("date"));
        inventory_table.setItems(inventoryList);
    }

    public void inventorySelectData() {
        productData prod = inventory_table.getSelectionModel().getSelectedItem();
        if (prod == null) {
            return;
        }

        inventory_productID.setText(prod.getProductId());
        inventory_productName.setText(prod.getProductName());
        inventory_stock.setText(String.valueOf(prod.getStock()));
        inventory_price.setText(String.valueOf(prod.getPrice()));
        inventory_type.getSelectionModel().select(prod.getStatus());
        inventory_status.getSelectionModel().select(prod.getStatus());

        if (prod.getImage() != null) {
            File file = new File(prod.getImage());
            if (file.exists()) {
                Image img = new Image(file.toURI().toString());
                inventory_image.setImage(img);
            }
        }
    }

    // ---------------------- ADD PRODUCT ----------------------
    @FXML
    private void addProduct(ActionEvent event) {
        String sql = "INSERT INTO product (product_id, product_name, stock, price, status, image, date) VALUES (?,?,?,?,?,?,?)";
        connect = database.connectDB();

        if (fieldsEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill all blank fields");
            return;
        }

        try {
            String checkSQL = "SELECT product_id FROM product WHERE product_id = ?";
            prepare = connect.prepareStatement(checkSQL);
            prepare.setString(1, inventory_productID.getText());
            result = prepare.executeQuery();

            if (result.next()) {
                showAlert(Alert.AlertType.ERROR, "Error Message",
                        inventory_productID.getText() + " is already taken");
                return;
            }

            prepare = connect.prepareStatement(sql);
            prepare.setString(1, inventory_productID.getText());
            prepare.setString(2, inventory_productName.getText());
            prepare.setInt(3, Integer.parseInt(inventory_stock.getText()));
            prepare.setDouble(4, Double.parseDouble(inventory_price.getText()));
            prepare.setString(5, inventory_status.getValue());
            prepare.setString(6, data.path);
            prepare.setDate(7, new java.sql.Date(new Date().getTime()));
            prepare.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Information Message", "Successfully Added!");
            inventoryShowData();
            clearFields();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid input for Stock or Price.");
        }
    }

    // ---------------------- IMPORT PRODUCT IMAGE ----------------------
    @FXML
    private void inventoryImportBtn(ActionEvent event) {
        FileChooser openFile = new FileChooser();
        openFile.getExtensionFilters().add(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = openFile.showOpenDialog(inventory_form.getScene().getWindow());
        if (file != null) {
            data.path = file.getAbsolutePath();
            image = new Image(file.toURI().toString(), 129, 148, false, true);
            inventory_image.setImage(image);
        }
    }

    // ---------------------- UPDATE PRODUCT ----------------------
    @FXML
    private void updateProduct(ActionEvent event) {
        if (inventory_productID.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a product to update");
            return;
        }

        String sql = "UPDATE product SET product_name=?, stock=?, price=?, status=?, image=? WHERE product_id=?";
        connect = database.connectDB();

        try {
            prepare = connect.prepareStatement(sql);
            prepare.setString(1, inventory_productName.getText());
            prepare.setInt(2, Integer.parseInt(inventory_stock.getText()));
            prepare.setDouble(3, Double.parseDouble(inventory_price.getText()));
            prepare.setString(4, inventory_status.getValue());
            prepare.setString(5, data.path);
            prepare.setString(6, inventory_productID.getText());
            prepare.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Updated", "Successfully Updated!");
            inventoryShowData();
            clearFields();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid input or empty fields.");
        }
    }

    // ---------------------- DELETE PRODUCT ----------------------
    @FXML
    private void deleteProduct(ActionEvent event) {
        if (inventory_productID.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a product to delete");
            return;
        }

        String sql = "DELETE FROM product WHERE product_id=?";
        connect = database.connectDB();

        try {
            prepare = connect.prepareStatement(sql);
            prepare.setString(1, inventory_productID.getText());
            prepare.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Successfully Deleted!");
            inventoryShowData();
            clearFields();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to delete product.");
        }
    }

    // ---------------------- CLEAR FIELDS ----------------------
    @FXML
    private void clearFields(ActionEvent event) { clearFields(); }

    private void clearFields() {
        inventory_productID.clear();
        inventory_productName.clear();
        inventory_stock.clear();
        inventory_price.clear();
        inventory_type.getSelectionModel().clearSelection();
        inventory_status.getSelectionModel().clearSelection();
        inventory_image.setImage(null);
        data.path = "";
    }

    private boolean fieldsEmpty() {
        return inventory_productID.getText().isEmpty()
                || inventory_productName.getText().isEmpty()
                || inventory_stock.getText().isEmpty()
                || inventory_price.getText().isEmpty()
                || inventory_status.getValue() == null;
    }

    // ============================ NAVIGATION ============================
    public void logout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to logout?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                logout_btn.getScene().getWindow().hide();
                Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Cafe Management System - Login");
                stage.show();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout.");
            }
        }
    }

    public void displayUsername() {
        if (username != null) {
            username.setText(data.username);
        }
    }

    // ============================ INITIALIZATION ============================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        displayUsername();
        inventory_type.setItems(FXCollections.observableArrayList("Meals", "Drinks", "Desserts"));
        inventory_status.setItems(FXCollections.observableArrayList("Available", "Not Available"));
        inventoryShowData();

        inventory_table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                inventorySelectData();
            }
        });
    }

    // ============================ ALERT ============================
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alertBox = new Alert(type);
        alertBox.setTitle(title);
        alertBox.setHeaderText(null);
        alertBox.setContentText(message);
        alertBox.showAndWait();
    }
}