package aitpcafe;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
    @FXML private Button inventory_addBtn, inventory_updateBtn, inventory_clearBtn,
            inventory_deleteBtn, inventory_importBtn;
    @FXML private ImageView inventory_image;
    @FXML private Label username;

    // Menu display components
    @FXML private GridPane menu_gridpane;
    @FXML private ScrollPane menu_scrollpane;

    // ============================ DATABASE VARIABLES ============================
    private Connection connect;
    private PreparedStatement prepare;
    private Statement statement;
    private ResultSet result;
    private Image image;
    private ObservableList<productData> inventoryList;
    private ObservableList<productData> cardListData = FXCollections.observableArrayList();

    // ============================ PAGE SWITCHER ============================
    @FXML
    private void switchPage(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String fxmlFile = "";

        // ملاحظة: هنا الـ IDs لازم تطابق fx:id في الـ FXML
        switch (clickedButton.getId()) {
            case "menu_btn":
                fxmlFile = "menuScreen.fxml";
                break;
            case "Inventory_btn":
                fxmlFile = "inventoryScreen.fxml";
                break;
            case "Customers_btn":
                fxmlFile = "customers.fxml";
                break;
            default:
                // لو الـ id مش معروف، نطلع بدون تغيير
                return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) clickedButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ============================ INVENTORY DATA LOADING ============================
    private ObservableList<productData> inventoryListData() {
        ObservableList<productData> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM product ORDER BY date DESC";

        try {
            connect = database.connectDB();
            if (connect == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Cannot connect to database!");
                return listData;
            }

            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            while (result.next()) {
                productData prod = new productData(
                        result.getInt("id"),
                        result.getString("product_id"),
                        result.getString("product_name"),
                        result.getString("type"),
                        result.getInt("stock"),
                        result.getDouble("price"),
                        result.getString("status"),
                        result.getString("image"),
                        result.getDate("date")
                );
                listData.add(prod);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading products: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources();
        }

        return listData;
    }

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
        inventory_type.getSelectionModel().select(prod.getType());
        inventory_stock.setText(String.valueOf(prod.getStock()));
        inventory_price.setText(String.valueOf(prod.getPrice()));
        inventory_status.getSelectionModel().select(prod.getStatus());

        if (prod.getImage() != null && !prod.getImage().isEmpty()) {
            File file = new File(prod.getImage());
            if (file.exists()) {
                Image img = new Image(file.toURI().toString(), 129, 148, false, true);
                inventory_image.setImage(img);
                data.path = prod.getImage();
            } else {
                inventory_image.setImage(null);
                data.path = "";
            }
        } else {
            inventory_image.setImage(null);
            data.path = "";
        }
    }

    // ============================ ADD PRODUCT ============================
    @FXML
    private void addProduct(ActionEvent event) {
        if (fieldsEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill all blank fields");
            return;
        }

        String sql = "INSERT INTO product (product_id, product_name, type, stock, price, status, image, date) VALUES (?,?,?,?,?,?,?,?)";

        try {
            connect = database.connectDB();

            if (connect == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Cannot connect to database!");
                return;
            }

            // Check if product id exists
            String checkSQL = "SELECT product_id FROM product WHERE product_id = ?";
            prepare = connect.prepareStatement(checkSQL);
            prepare.setString(1, inventory_productID.getText().trim());
            result = prepare.executeQuery();

            if (result.next()) {
                showAlert(Alert.AlertType.ERROR, "Error Message",
                        "Product ID '" + inventory_productID.getText() + "' already exists!");
                return;
            }

            prepare = connect.prepareStatement(sql);
            prepare.setString(1, inventory_productID.getText().trim());
            prepare.setString(2, inventory_productName.getText().trim());
            prepare.setString(3, inventory_type.getValue());
            prepare.setInt(4, Integer.parseInt(inventory_stock.getText().trim()));
            prepare.setDouble(5, Double.parseDouble(inventory_price.getText().trim()));
            prepare.setString(6, inventory_status.getValue());
            prepare.setString(7, data.path != null ? data.path : "");
            prepare.setDate(8, new java.sql.Date(new Date().getTime()));

            int rowsAffected = prepare.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product Added Successfully!");
                inventoryShowData();
                clearFields();
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add product: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }

    // ============================ UPDATE PRODUCT ============================
    @FXML
    private void updateProduct(ActionEvent event) {
        if (inventory_productID.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a product to update");
            return;
        }

        if (fieldsEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill all blank fields");
            return;
        }

        String sql = "UPDATE product SET product_name=?, type=?, stock=?, price=?, status=?, image=? WHERE product_id=?";

        try {
            connect = database.connectDB();

            prepare = connect.prepareStatement(sql);
            prepare.setString(1, inventory_productName.getText().trim());
            prepare.setString(2, inventory_type.getValue());
            prepare.setInt(3, Integer.parseInt(inventory_stock.getText().trim()));
            prepare.setDouble(4, Double.parseDouble(inventory_price.getText().trim()));
            prepare.setString(5, inventory_status.getValue());
            prepare.setString(6, data.path != null ? data.path : "");
            prepare.setString(7, inventory_productID.getText().trim());

            int rowsAffected = prepare.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product Updated Successfully!");
                inventoryShowData();
                clearFields();
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    // ============================ DELETE PRODUCT ============================
    @FXML
    private void deleteProduct(ActionEvent event) {
        if (inventory_productID.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a product to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete product '" +
                inventory_productID.getText() + "'?");

        if (confirm.showAndWait().get() != ButtonType.OK) {
            return;
        }

        String sql = "DELETE FROM product WHERE product_id=?";

        try {
            connect = database.connectDB();

            prepare = connect.prepareStatement(sql);
            prepare.setString(1, inventory_productID.getText().trim());
            int rowsAffected = prepare.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Product Deleted Successfully!");
                inventoryShowData();
                clearFields();
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }

    // ============================ IMPORT IMAGE ============================
    @FXML
    private void inventoryImportBtn(ActionEvent event) {
        FileChooser openFile = new FileChooser();
        openFile.setTitle("Select Product Image");
        openFile.getExtensionFilters().add(
                new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File file = openFile.showOpenDialog(inventory_form.getScene().getWindow());

        if (file != null) {
            data.path = file.getAbsolutePath();
            image = new Image(file.toURI().toString(), 129, 148, false, true);
            inventory_image.setImage(image);
        }
    }

    // ============================ CLEAR FIELDS ============================
    @FXML
    private void clearFields(ActionEvent event) {
        clearFields();
    }

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
        return inventory_productID.getText().trim().isEmpty()
                || inventory_productName.getText().trim().isEmpty()
                || inventory_type.getValue() == null
                || inventory_stock.getText().trim().isEmpty()
                || inventory_price.getText().trim().isEmpty()
                || inventory_status.getValue() == null;
    }

    // ============================ MENU DATA LOADING ============================
    public ObservableList<productData> menuGetData() {
        ObservableList<productData> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM product WHERE status = 'Available' ORDER BY date DESC";

        try {
            connect = database.connectDB();

            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            while (result.next()) {
                productData prod = new productData(
                        result.getInt("id"),
                        result.getString("product_id"),
                        result.getString("product_name"),
                        result.getString("type"),
                        result.getInt("stock"),
                        result.getDouble("price"),
                        result.getString("status"),
                        result.getString("image"),
                        result.getDate("date")
                );
                listData.add(prod);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }

        return listData;
    }

    // ============================ MENU DISPLAY ============================
    public void menuDisplayCard() {

        cardListData.clear();
        cardListData.addAll(menuGetData());

        if (menu_gridpane != null) {
            menu_gridpane.getChildren().clear();

            int row = 0;
            int column = 0;

            for (productData product : cardListData) {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/aitpcafe/cardProduct.fxml"));
                    AnchorPane pane = loader.load();

                    CardProductController cardController = loader.getController();
                    if (cardController != null) {
                        cardController.setData(product);
                    }

                    if (column == 3) {
                        column = 0;
                        row++;
                    }

                    menu_gridpane.add(pane, column++, row);
                    GridPane.setMargin(pane, new Insets(10));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ============================ NAVIGATION ============================
    @FXML
    public void switchToMenu(ActionEvent event) {
        switchForm("dashboard.fxml", "Cafe Management System - Menu");
    }

    @FXML
    public void switchToCustomers(ActionEvent event) {
        switchForm("customers.fxml", "Cafe Management System - Customers");
    }

    @FXML
    public void logout(ActionEvent event) {
        logout();
    }

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
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to logout: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void switchForm(String fxmlFile, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) inventory_form.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to switch form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================ USERNAME DISPLAY ============================
    public void displayUsername() {
        if (username != null && data.username != null && !data.username.isEmpty()) {
            username.setText(data.username);
        }
    }

    // ============================ SEARCH & FILTER ============================
    public void searchProduct(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            inventoryShowData();
            return;
        }

        ObservableList<productData> filteredList = FXCollections.observableArrayList();
        String lowerSearch = searchText.toLowerCase();

        for (productData prod : inventoryList) {
            if (prod.getProductId().toLowerCase().contains(lowerSearch)
                    || prod.getProductName().toLowerCase().contains(lowerSearch)
                    || prod.getType().toLowerCase().contains(lowerSearch)) {
                filteredList.add(prod);
            }
        }

        inventory_table.setItems(filteredList);
    }

    // ============================ INITIALIZATION ============================
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        displayUsername();

        // Setup ComboBoxes
        inventory_type.setItems(FXCollections.observableArrayList(
                "Meals", "Drinks", "Desserts", "Snacks", "Other"
        ));

        inventory_status.setItems(FXCollections.observableArrayList(
                "Available", "Not Available"
        ));

        // Load inventory data
        inventoryShowData();

        // Load menu cards
        menuDisplayCard();

        // Listener for selecting row
        inventory_table.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        inventorySelectData();
                    }
                }
        );
    }

    // ============================ UTILITY METHODS ============================
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alertBox = new Alert(type);
        alertBox.setTitle(title);
        alertBox.setHeaderText(null);
        alertBox.setContentText(message);
        alertBox.showAndWait();
    }

    private void closeResources() {
        try {
            if (result != null) result.close();
            if (prepare != null) prepare.close();
            if (statement != null) statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<productData> getCardListData() {
        return cardListData;
    }
}
