package aitpcafe;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

public class MenuController implements Initializable {

    @FXML
    private AnchorPane mainContainer;
    @FXML
    private AnchorPane dashboard_form, menu_form, inventory_form;
    @FXML
    private GridPane menu_gridpane;
    @FXML
    private ScrollPane menu_scrollpane;
    @FXML
    private VBox orderItemsContainer;
    @FXML
    private Label subTotalLabel, taxLabel, totalLabel;
    @FXML
    private Button placeOrderBtn;
    @FXML
    private Button dineInBtn, takeAwayBtn, deliveryBtn;
    @FXML
    private Button cashBtn, cardBtn, digitalBtn;
    
    // ✅ إضافة Search Field و Clear Button
    @FXML
    private TextField searchField;
    @FXML
    private Button clearSearchBtn;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private ObservableList<productData> cardListData = FXCollections.observableArrayList();
    private ObservableList<productData> allProducts = FXCollections.observableArrayList(); // ✅ حفظ كل المنتجات
    private ArrayList<OrderItem> currentOrder = new ArrayList<>();

    private SidebarController sidebarController;
    private AnchorPane sidebar;
    private Button menu_btn, inventory_btn, dashboard_btn;
    private UserSession session;

    private String selectedOrderType = "Dine In";
    private String selectedPaymentMethod = "Cash";

    private Button selectedOrderTypeBtn;
    private Button selectedPaymentBtn;

    private int customerIdCounter = 1000;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        session = UserSession.getInstance();

        loadSidebar();
        setupGridPane();
        menuDisplayCard();
        adjustContentPosition();
        updateOrderSummary();
        loadLastCustomerId();
        
        // ✅ إعداد Search Functionality
        setupSearchFunctionality();

        if (dineInBtn != null) {
            selectedOrderTypeBtn = dineInBtn;
            dineInBtn.getStyleClass().add("order-type-btn-selected");
        }

        if (cashBtn != null) {
            selectedPaymentBtn = cashBtn;
            cashBtn.getStyleClass().add("payment-btn-selected");
        }
    }

    // ✅ دالة إعداد البحث
    private void setupSearchFunctionality() {
        if (searchField != null) {
            // البحث أثناء الكتابة
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterProducts(newValue);
            });
        }

        if (clearSearchBtn != null) {
            clearSearchBtn.setOnAction(e -> clearSearch());
        }
    }

    // ✅ دالة الفلترة
    private void filterProducts(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            // عرض كل المنتجات إذا كان البحث فارغ
            displayProducts(allProducts);
            return;
        }

        String searchLower = searchText.toLowerCase().trim();
        
        // فلترة المنتجات حسب الاسم
        ObservableList<productData> filteredList = allProducts.stream()
            .filter(product -> product.getProductName().toLowerCase().contains(searchLower))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));

        displayProducts(filteredList);
        
        // عرض رسالة إذا لم يتم العثور على نتائج
        if (filteredList.isEmpty() && menu_gridpane != null) {
            showNoResultsMessage();
        }
    }

    // ✅ عرض رسالة عدم وجود نتائج
    private void showNoResultsMessage() {
        menu_gridpane.getChildren().clear();
        
        VBox noResultsBox = new VBox(10);
        noResultsBox.setAlignment(Pos.CENTER);
        noResultsBox.setPadding(new Insets(50));
        
        FontIcon icon = new FontIcon("fas-search");
        icon.setIconSize(48);
        icon.setIconColor(javafx.scene.paint.Color.GRAY);
        
        Label message = new Label("No products found");
        message.setStyle("-fx-font-size: 18px; -fx-text-fill: #757575;");
        
        Label hint = new Label("Try a different search term");
        hint.setStyle("-fx-font-size: 14px; -fx-text-fill: #9e9e9e;");
        
        noResultsBox.getChildren().addAll(icon, message, hint);
        menu_gridpane.add(noResultsBox, 0, 0);
    }

    // ✅ مسح البحث
    @FXML
    private void clearSearch() {
        if (searchField != null) {
            searchField.clear();
        }
        displayProducts(allProducts);
    }

    // ✅ دالة عرض المنتجات (معدلة)
    private void displayProducts(ObservableList<productData> products) {
        if (menu_gridpane != null) {
            menu_gridpane.getChildren().clear();

            int row = 0;
            int column = 0;
            int maxColumns = 3;

            for (productData product : products) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("cardProduct.fxml"));
                    AnchorPane pane = loader.load();

                    CardProductController controller = loader.getController();
                    if (controller != null) {
                        controller.setData(product);
                        controller.setMenuController(this);
                    }

                    pane.setMaxWidth(170);
                    pane.setMaxHeight(220);
                    pane.setPrefWidth(170);
                    pane.setPrefHeight(220);
                    pane.setMinWidth(170);
                    pane.setMinHeight(220);

                    if (column == maxColumns) {
                        column = 0;
                        row++;
                    }

                    menu_gridpane.add(pane, column++, row);
                    GridPane.setMargin(pane, new Insets(8));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadLastCustomerId() {
        try {
            connect = database.connectDB();
            if (connect != null) {
                String sql = "SELECT MAX(customer_id) as last_id FROM customer_receipt";
                prepare = connect.prepareStatement(sql);
                result = prepare.executeQuery();

                if (result.next()) {
                    int lastId = result.getInt("last_id");
                    customerIdCounter = (lastId > 0) ? lastId + 1 : 1000;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }

    private void setupGridPane() {
        if (menu_gridpane != null) {
            menu_gridpane.setHgap(12);
            menu_gridpane.setVgap(12);
            menu_gridpane.setPadding(new Insets(10));
        }
    }

    private void loadSidebar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sidebar.fxml"));
            sidebar = loader.load();
            sidebarController = loader.getController();

            sidebarController.setTitle("Cafe Menu");
            sidebarController.setSubtitle("Order Management");

            sidebarController.setOnToggleCallback(this::adjustContentPosition);

            menu_btn = createNavButton("menu_btn", "  Menu", "fas-utensils");
            inventory_btn = createNavButton("inventory_btn", "  Inventory", "fas-box");
            dashboard_btn = createNavButton("dashboard_btn", "  Dashboard", "fas-chart-line");

            menu_btn.setOnAction(this::switchForm);
            inventory_btn.setOnAction(this::switchForm);
            dashboard_btn.setOnAction(this::switchForm);

            menu_btn.getStyleClass().add("nav-btn-active");

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
        double sidebarWidth = sidebarController.isExpanded() ? 220 : 70;

        if (menu_form != null) {
            AnchorPane.setLeftAnchor(menu_form, sidebarWidth);
            AnchorPane.setRightAnchor(menu_form, 0.0);
            AnchorPane.setTopAnchor(menu_form, 0.0);
            AnchorPane.setBottomAnchor(menu_form, 0.0);
        }

        if (dashboard_form != null) {
            AnchorPane.setLeftAnchor(dashboard_form, sidebarWidth);
            AnchorPane.setRightAnchor(dashboard_form, 0.0);
            AnchorPane.setTopAnchor(dashboard_form, 0.0);
            AnchorPane.setBottomAnchor(dashboard_form, 0.0);
        }

        if (inventory_form != null) {
            AnchorPane.setLeftAnchor(inventory_form, sidebarWidth);
            AnchorPane.setRightAnchor(inventory_form, 0.0);
            AnchorPane.setTopAnchor(inventory_form, 0.0);
            AnchorPane.setBottomAnchor(inventory_form, 0.0);
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
        } else if (event.getSource() == inventory_btn) {
            switchToPage("inventoryScreen.fxml");
        } else if (event.getSource() == menu_btn) {
            menu_btn.getStyleClass().add("nav-btn-active");
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

    public ObservableList<productData> menuGetData() {
        ObservableList<productData> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM product WHERE status = 'Available' ORDER BY id ASC";

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

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load products: " + e.getMessage());
        } finally {
            closeResources();
        }

        return listData;
    }

    // ✅ تعديل menuDisplayCard لحفظ كل المنتجات
    public void menuDisplayCard() {
        allProducts.clear();
        allProducts.addAll(menuGetData());
        
        cardListData.clear();
        cardListData.addAll(allProducts);

        displayProducts(cardListData);
    }

    public void addToOrder(productData product, int quantity) {
        boolean found = false;

        for (OrderItem item : currentOrder) {
            if (item.getProduct().getProductId().equals(product.getProductId())) {
                item.setQuantity(item.getQuantity() + quantity);
                found = true;
                break;
            }
        }

        if (!found) {
            currentOrder.add(new OrderItem(product, quantity));
        }

        updateOrderDisplay();
        updateOrderSummary();
    }

    private void updateOrderDisplay() {
        if (orderItemsContainer == null) {
            return;
        }

        orderItemsContainer.getChildren().clear();

        if (currentOrder.isEmpty()) {
            Label emptyLabel = new Label("No items yet");
            emptyLabel.getStyleClass().add("empty-label");
            emptyLabel.setAlignment(Pos.CENTER);
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            orderItemsContainer.getChildren().add(emptyLabel);
        } else {
            for (OrderItem item : currentOrder) {
                HBox itemBox = createOrderItemBox(item);
                orderItemsContainer.getChildren().add(itemBox);
            }
        }
    }

    private HBox createOrderItemBox(OrderItem item) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("order-item-box");
        box.setPadding(new Insets(10, 12, 10, 12));

        VBox infoBox = new VBox(4);
        Label nameLabel = new Label(item.getProduct().getProductName());
        nameLabel.getStyleClass().add("item-name");

        Label detailLabel = new Label(item.getQuantity() + " × "
                + String.format("%.2f", item.getProduct().getPrice()));
        detailLabel.getStyleClass().add("item-detail");

        infoBox.getChildren().addAll(nameLabel, detailLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label priceLabel = new Label(String.format("%.2f", item.getTotal()));
        priceLabel.getStyleClass().add("item-price");

        Button removeBtn = new Button();
        removeBtn.getStyleClass().add("remove-item-btn");

        FontIcon trashIcon = new FontIcon("fas-trash");
        trashIcon.setIconSize(14);
        trashIcon.setIconColor(javafx.scene.paint.Color.web("#c62828"));
        removeBtn.setGraphic(trashIcon);

        removeBtn.setOnAction(e -> removeFromOrder(item));

        box.getChildren().addAll(infoBox, priceLabel, removeBtn);
        return box;
    }

    private void removeFromOrder(OrderItem item) {
        currentOrder.remove(item);
        updateOrderDisplay();
        updateOrderSummary();
    }

    private void updateOrderSummary() {
        double subTotal = currentOrder.stream()
                .mapToDouble(OrderItem::getTotal)
                .sum();

        double tax = subTotal * 0.05;
        double total = subTotal + tax;

        if (subTotalLabel != null) {
            subTotalLabel.setText(String.format("%.2f EGP", subTotal));
        }
        if (taxLabel != null) {
            taxLabel.setText(String.format("%.2f EGP", tax));
        }
        if (totalLabel != null) {
            totalLabel.setText(String.format("%.2f EGP", total));
        }
    }

    @FXML
    public void setOrderType(ActionEvent event) {
        Button btn = (Button) event.getSource();

        if (selectedOrderTypeBtn != null) {
            selectedOrderTypeBtn.getStyleClass().remove("order-type-btn-selected");
        }

        btn.getStyleClass().add("order-type-btn-selected");
        selectedOrderTypeBtn = btn;
        selectedOrderType = btn.getText();
    }

    @FXML
    public void setPaymentMethod(ActionEvent event) {
        Button btn = (Button) event.getSource();

        if (selectedPaymentBtn != null) {
            selectedPaymentBtn.getStyleClass().remove("payment-btn-selected");
        }

        btn.getStyleClass().add("payment-btn-selected");
        selectedPaymentBtn = btn;

        if (btn == cashBtn) {
            selectedPaymentMethod = "Cash";
        } else if (btn == cardBtn) {
            selectedPaymentMethod = "Credit";
        } else {
            selectedPaymentMethod = "VF-Cash";
        }
    }

    @FXML
    public void placeOrder() {
        if (currentOrder.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Order", "Please add items to your order!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Customer Information");
        dialog.setHeaderText("Enter customer name (optional)");
        dialog.setContentText("Customer Name:");

        Optional<String> result = dialog.showAndWait();
        String customerName = result.orElse("Walk-in Customer");

        boolean success = saveOrderToDatabase(customerName);

        if (success) {
            generateReceipt(customerName);
            clearOrder();
        }
    }

    private boolean saveOrderToDatabase(String customerName) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = database.connectDB();
            if (conn == null) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Cannot connect to database!");
                return false;
            }

            conn.setAutoCommit(false);

            double subTotal = currentOrder.stream().mapToDouble(OrderItem::getTotal).sum();
            double tax = subTotal * 0.05;
            double total = subTotal + tax;

            int currentCustomerId = customerIdCounter++;

            String sql = "INSERT INTO customer_receipt (customer_id, customer_name, order_type, payment_method, total, tax, final_amount, date, em_username) VALUES (?, ?, ?, ?, ?, ?, ?, CURDATE(), ?)";
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, currentCustomerId);
            pstmt.setString(2, customerName);
            pstmt.setString(3, selectedOrderType);
            pstmt.setString(4, selectedPaymentMethod);
            pstmt.setDouble(5, subTotal);
            pstmt.setDouble(6, tax);
            pstmt.setDouble(7, total);
            pstmt.setString(8, session.getUsername());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int receiptId = rs.getInt(1);

                    String itemSql = "INSERT INTO order_items (receipt_id, product_id, product_name, quantity, price, total) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement itemStmt = conn.prepareStatement(itemSql);

                    for (OrderItem item : currentOrder) {
                        itemStmt.setInt(1, receiptId);
                        itemStmt.setString(2, item.getProduct().getProductId());
                        itemStmt.setString(3, item.getProduct().getProductName());
                        itemStmt.setInt(4, item.getQuantity());
                        itemStmt.setDouble(5, item.getProduct().getPrice());
                        itemStmt.setDouble(6, item.getTotal());
                        itemStmt.executeUpdate();

                        String updateStock = "UPDATE product SET stock = stock - ? WHERE product_id = ?";
                        PreparedStatement stockStmt = conn.prepareStatement(updateStock);
                        stockStmt.setInt(1, item.getQuantity());
                        stockStmt.setString(2, item.getProduct().getProductId());
                        stockStmt.executeUpdate();
                        stockStmt.close();
                    }

                    itemStmt.close();
                }
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Order placed successfully!");
            return true;

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save order: " + e.getMessage());
            return false;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void generateReceipt(String customerName) {
        String cafeName = "M A R I A M 'S   C A F E";
        String boldLine = "=====================================";
        StringBuilder receipt = new StringBuilder();

        receipt.append("  " + cafeName + "\n");
        String line = "-------------------------------------";
        receipt.append(boldLine + "\n\n");

        receipt.append(String.format("Customer     : %s\n", customerName));
        receipt.append(String.format("Order Type   : %s\n", selectedOrderType));
        receipt.append(String.format("Payment      : %s\n", selectedPaymentMethod));
        receipt.append(String.format("Cashier      : %s\n", session.getUsername()));
        receipt.append("\n" + line + "\n");
        receipt.append("ITEM               QTY   TOTAL\n");
        receipt.append(line + "\n");

        for (OrderItem item : currentOrder) {
            receipt.append(String.format(
                    "%-15s  %3d   %7.2f\n",
                    item.getProduct().getProductName(),
                    item.getQuantity(),
                    item.getTotal()
            ));
        }

        double subTotal = currentOrder.stream().mapToDouble(OrderItem::getTotal).sum();
        double tax = subTotal * 0.05;
        double total = subTotal + tax;

        receipt.append("\n" + line + "\n");
        receipt.append(String.format("Sub Total          %10.2f\n", subTotal));
        receipt.append(String.format("Tax (5%%)           %10.2f\n", tax));
        receipt.append(boldLine + "\n");
        receipt.append(String.format("TOTAL              %10.2f\n", total));
        receipt.append(boldLine + "\n\n");

        receipt.append("     THANK YOU FOR YOUR ORDER\n");
        receipt.append("        HAVE A GREAT DAY!\n");
        receipt.append(boldLine);

        Alert receiptAlert = new Alert(Alert.AlertType.INFORMATION);
        receiptAlert.setTitle("Order Receipt");
        receiptAlert.setHeaderText("Order Completed Successfully!");

        TextArea textArea = new TextArea(receipt.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setStyle(
                "-fx-font-family: 'Consolas';"
                + "-fx-font-size: 11px;"
                + "-fx-control-inner-background: white;"
                + "-fx-text-fill: black;"
        );

        textArea.setPrefRowCount(18);

        receiptAlert.getDialogPane().setContent(textArea);
        receiptAlert.showAndWait();
    }

    private void clearOrder() {
        currentOrder.clear();
        updateOrderDisplay();
        updateOrderSummary();
        menuDisplayCard();
    }

    @FXML
    public void logout() {
        try {
            session.clearSession();
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));

            Stage loginStage = new Stage();
            loginStage.setTitle("Cafe Management System - Login");
            loginStage.setScene(new Scene(root, 1100, 650));

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
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class OrderItem {
        private productData product;
        private int quantity;

        public OrderItem(productData product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public productData getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getTotal() {
            return product.getPrice() * quantity;
        }
    }
}