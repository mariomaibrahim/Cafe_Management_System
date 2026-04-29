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
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

public class InventoryController implements Initializable {

    @FXML
    private AnchorPane mainContainer;
    @FXML
    private AnchorPane dashboard_form, menu_form, inventory_form;
    @FXML
    private TableView<productData> inventory_table;
    @FXML
    private TableColumn<productData, String> inventory_col_ID, inventory_col_productName,
            inventory_col_type, inventory_col_stock, inventory_col_price,
            inventory_col_status, inventory_col_date;

    @FXML
    private TextField inventory_productID, inventory_productName, inventory_stock, inventory_price;
    @FXML
    private ComboBox<String> inventory_type, inventory_status;
    @FXML
    private Button inventory_addBtn, inventory_updateBtn, inventory_clearBtn,
            inventory_deleteBtn, inventory_importBtn;
    @FXML
    private ImageView inventory_image;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private Image image;
    private ObservableList<productData> inventoryList;

    private SidebarController sidebarController;
    private AnchorPane sidebar;
    private Button menu_btn, inventory_btn, dashboard_btn;
    private UserSession session;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        session = UserSession.getInstance();

        loadSidebar();
        setupComboBoxes();
        inventoryShowData();
        setupTableListener();
        adjustContentPosition();
    }

    private void loadSidebar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sidebar.fxml"));
            sidebar = loader.load();
            sidebarController = loader.getController();

            sidebarController.setTitle("Cafe Inventory");
            sidebarController.setSubtitle("Stock Management");

            // إضافة callback للـ toggle
            sidebarController.setOnToggleCallback(this::adjustContentPosition);

            menu_btn = createNavButton("menu_btn", "  Menu", "fas-utensils");
            inventory_btn = createNavButton("inventory_btn", "  Inventory", "fas-box");
            dashboard_btn = createNavButton("dashboard_btn", "  Dashboard", "fas-chart-line");

            menu_btn.setOnAction(this::switchForm);
            inventory_btn.setOnAction(this::switchForm);
            dashboard_btn.setOnAction(this::switchForm);

            inventory_btn.getStyleClass().add("nav-btn-active");

            sidebarController.getNavButtonsContainer().getChildren().addAll(menu_btn, inventory_btn, dashboard_btn);
            sidebarController.getLogoutButton().setOnAction(e -> logout());

            Button toggleBtn = (Button) sidebar.lookup("#toggleSidebar");
            if (toggleBtn != null) {
                toggleBtn.setOnAction(e -> sidebarController.toggleSidebar());
            }

            mainContainer.getChildren().add(0, sidebar);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void adjustContentPosition() {
        if (sidebarController == null) {
            return;
        }

        double sidebarWidth = sidebarController.isExpanded() ? 220 : 70;

        if (inventory_form != null) {
            AnchorPane.setLeftAnchor(inventory_form, sidebarWidth);
            AnchorPane.setRightAnchor(inventory_form, 0.0);
            AnchorPane.setTopAnchor(inventory_form, 0.0);
            AnchorPane.setBottomAnchor(inventory_form, 0.0);
        }

        if (dashboard_form != null) {
            AnchorPane.setLeftAnchor(dashboard_form, sidebarWidth);
            AnchorPane.setRightAnchor(dashboard_form, 0.0);
            AnchorPane.setTopAnchor(dashboard_form, 0.0);
            AnchorPane.setBottomAnchor(dashboard_form, 0.0);
        }

        if (menu_form != null) {
            AnchorPane.setLeftAnchor(menu_form, sidebarWidth);
            AnchorPane.setRightAnchor(menu_form, 0.0);
            AnchorPane.setTopAnchor(menu_form, 0.0);
            AnchorPane.setBottomAnchor(menu_form, 0.0);
        }
    }

    private Button createNavButton(String id, String text, String iconLiteral) {
        Button btn = new Button(text);
        btn.setId(id);
        btn.getStyleClass().add("nav-btn");
        btn.setPrefWidth(190);

        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconColor(javafx.scene.paint.Color.WHITE);
        icon.setIconSize(16);
        btn.setGraphic(icon);

        return btn;
    }

    @FXML
    public void switchForm(ActionEvent event) {
        menu_btn.getStyleClass().remove("nav-btn-active");
        inventory_btn.getStyleClass().remove("nav-btn-active");
        dashboard_btn.getStyleClass().remove("nav-btn-active");

        if (event.getSource() == dashboard_btn) {
            if (session.isAdmin()) {
                switchToPage("admin.fxml");
            } else {
                switchToPage("cashierdash.fxml");
            }
        } else if (event.getSource() == menu_btn) {
            switchToPage("menuScreen.fxml");
        } else if (event.getSource() == inventory_btn) {
            inventory_btn.getStyleClass().add("nav-btn-active");
        }
    }

    private void switchToPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load page: " + e.getMessage());
        }
    }

    private void setupComboBoxes() {
        inventory_type.setItems(FXCollections.observableArrayList(
                "Meals", "Drinks", "Desserts", "Snacks", "Other"
        ));

        inventory_status.setItems(FXCollections.observableArrayList(
                "Available", "Not Available"
        ));
    }

    private void setupTableListener() {
        inventory_table.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        inventorySelectData();
                    }
                }
        );
    }

    private ObservableList<productData> inventoryListData() {
        ObservableList<productData> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM product ORDER BY id DESC";

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
                Image img = new Image(file.toURI().toString(), 140, 140, true, true);
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

    @FXML
    public void addProduct(ActionEvent event) {
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

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter valid numbers for stock and price!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add product: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }

    @FXML
    public void updateProduct(ActionEvent event) {
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

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter valid numbers for stock and price!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update: " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    @FXML
    public void deleteProduct(ActionEvent event) {
        if (inventory_productID.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a product to delete");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete product '"
                + inventory_productID.getText() + "'?");

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

    @FXML
    public void inventoryImportBtn(ActionEvent event) {
        FileChooser openFile = new FileChooser();
        openFile.setTitle("Select Product Image");
        openFile.getExtensionFilters().add(
                new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File file = openFile.showOpenDialog(inventory_form.getScene().getWindow());

        if (file != null) {
            data.path = file.getAbsolutePath();
            image = new Image(file.toURI().toString(), 140, 140, true, true);
            inventory_image.setImage(image);
        }
    }

    @FXML
    public void clearFields(ActionEvent event) {
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
        inventory_table.getSelectionModel().clearSelection();
    }

    private boolean fieldsEmpty() {
        return inventory_productID.getText().trim().isEmpty()
                || inventory_productName.getText().trim().isEmpty()
                || inventory_type.getValue() == null
                || inventory_stock.getText().trim().isEmpty()
                || inventory_price.getText().trim().isEmpty()
                || inventory_status.getValue() == null;
    }

    @FXML
    public void logout() {
        try {
            session.clearSession();
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));

            Stage loginStage = new Stage();
            loginStage.setTitle("Cafe Management System - Login");
            loginStage.setScene(new Scene(root, 1100, 650)); // ✅ الحجم الموحد

            loginStage.setResizable(false);
            loginStage.setMinWidth(1100);
            loginStage.setMinHeight(650);
            loginStage.setMaxWidth(1100);
            loginStage.setMaxHeight(650);

            loginStage.show();

            Stage currentStage = (Stage) mainContainer.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alertBox = new Alert(type);
        alertBox.setTitle(title);
        alertBox.setHeaderText(null);
        alertBox.setContentText(message);
        alertBox.showAndWait();
    }

    private void closeResources() {
        try {
            if (result != null) {
                result.close();
            }
            if (prepare != null) {
                prepare.close();
            }
            if (connect != null) {
                connect.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
