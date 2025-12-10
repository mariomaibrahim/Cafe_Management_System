package aitpcafe;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CashierdashController implements Initializable {

    @FXML
    private AnchorPane dashboard_form, menu_form, inventory_form, mainContainer;
    @FXML
    private Label todayOrders, todayCustomers, todaySales;
    @FXML
    private Label cashierName, cashierPhone, cashierEmail, cashierSalary;
    @FXML
    private ProgressBar performanceBar;
    @FXML
    private Label performanceLabel, performanceStatus;
    @FXML
    private Button editProfileBtn;

    // Orders Table
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, Integer> col_orderId;
    @FXML
    private TableColumn<Order, String> col_customer, col_items, col_time;
    @FXML
    private TableColumn<Order, Double> col_amount;

    // Statistics Labels
    @FXML
    private Label totalOrders, totalSales, totalCustomers;
    @FXML
    private Label weekOrders, weekSales, avgOrder;

    private SidebarController sidebarController;
    private AnchorPane sidebar;
    private Button menu_btn, inventory_btn, dashboard_btn;
    private UserSession session;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        session = UserSession.getInstance();

        if (!session.isLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please login first!");
            logout();
            return;
        }

        if (!session.isCashier()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied", "This page is for cashiers only!");
            logout();
            return;
        }

        loadSidebar();
        loadCashierInfo();
        loadDashboardData();
        setupOrdersTable();
        loadRecentOrders();
        loadStatistics();
        calculatePerformance();
        adjustContentPosition();
    }

    private void loadSidebar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sidebar.fxml"));
            sidebar = loader.load();
            sidebarController = loader.getController();

            sidebarController.setTitle("Cafe Cashier");
            sidebarController.setSubtitle("Management System");

            // إضافة callback للـ toggle
            sidebarController.setOnToggleCallback(this::adjustContentPosition);

            menu_btn = createNavButton("menu_btn", "  Menu", "fas-utensils");
            inventory_btn = createNavButton("inventory_btn", "  Inventory", "fas-box");
            dashboard_btn = createNavButton("dashboard_btn", "  Dashboard", "fas-chart-line");

            menu_btn.setOnAction(this::switchForm);
            inventory_btn.setOnAction(this::switchForm);
            dashboard_btn.setOnAction(this::switchForm);

            dashboard_btn.getStyleClass().add("nav-btn-active");

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
        dashboard_form.setVisible(false);
        menu_form.setVisible(false);
        inventory_form.setVisible(false);

        menu_btn.getStyleClass().remove("nav-btn-active");
        inventory_btn.getStyleClass().remove("nav-btn-active");
        dashboard_btn.getStyleClass().remove("nav-btn-active");

        if (event.getSource() == dashboard_btn) {
            dashboard_form.setVisible(true);
            dashboard_btn.getStyleClass().add("nav-btn-active");
            refreshDashboard();
        } else if (event.getSource() == menu_btn) {
            switchToPage("menuScreen.fxml");
        } else if (event.getSource() == inventory_btn) {
            switchToPage("inventoryScreen.fxml");
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

    private void loadCashierInfo() {
        cashierName.setText(session.getFullName());
        cashierPhone.setText(session.getPhone());
        cashierEmail.setText(session.getEmail() != null ? session.getEmail() : "N/A");
        cashierSalary.setText(String.format("%.2f EGP", session.getSalary()));
    }

    private void loadDashboardData() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            String username = session.getUsername();

            // Today Orders
            String query = "SELECT COUNT(*) as total FROM customer_receipt WHERE em_username = ? AND DATE(date) = CURDATE()";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                todayOrders.setText(String.valueOf(rs.getInt("total")));
            }
            rs.close();
            pstmt.close();

            // Today Customers
            query = "SELECT COUNT(DISTINCT customer_id) as total FROM customer_receipt WHERE em_username = ? AND DATE(date) = CURDATE()";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                todayCustomers.setText(String.valueOf(rs.getInt("total")));
            }
            rs.close();
            pstmt.close();

            // Today Sales
            query = "SELECT COALESCE(SUM(final_amount), 0) as total FROM customer_receipt WHERE em_username = ? AND DATE(date) = CURDATE()";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                todaySales.setText(String.format("%.2f EGP", rs.getDouble("total")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load dashboard data");
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    private void setupOrdersTable() {
        col_orderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        col_customer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        col_items.setCellValueFactory(new PropertyValueFactory<>("items"));
        col_amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        col_time.setCellValueFactory(new PropertyValueFactory<>("time"));

        // Format amount column
        col_amount.setCellFactory(column -> new TableCell<Order, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f EGP", item));
                }
            }
        });
    }

    private void loadRecentOrders() {
        ObservableList<Order> ordersList = FXCollections.observableArrayList();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            String query = "SELECT id, customer_id, customer_name, final_amount, date "
                    + "FROM customer_receipt "
                    + "WHERE em_username = ? AND DATE(date) = CURDATE() "
                    + "ORDER BY id DESC LIMIT 10";

            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, session.getUsername());
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("id"),
                        rs.getString("customer_name") != null ? rs.getString("customer_name") : "Walk-in Customer",
                        "Mixed Items",
                        rs.getDouble("final_amount"),
                        rs.getTimestamp("date").toString()
                );
                ordersList.add(order);
            }
            ordersTable.setItems(ordersList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load recent orders");
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    private void loadStatistics() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            String username = session.getUsername();

            // Total Orders
            String query = "SELECT COUNT(*) as total FROM customer_receipt WHERE em_username = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                totalOrders.setText(String.valueOf(rs.getInt("total")));
            }
            rs.close();
            pstmt.close();

            // Total Sales
            query = "SELECT COALESCE(SUM(final_amount), 0) as total FROM customer_receipt WHERE em_username = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                totalSales.setText(String.format("%.2f EGP", rs.getDouble("total")));
            }
            rs.close();
            pstmt.close();

            // Total Customers
            query = "SELECT COUNT(DISTINCT customer_id) as total FROM customer_receipt WHERE em_username = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                totalCustomers.setText(String.valueOf(rs.getInt("total")));
            }
            rs.close();
            pstmt.close();

            // Week Orders
            query = "SELECT COUNT(*) as total FROM customer_receipt WHERE em_username = ? AND date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                weekOrders.setText(String.valueOf(rs.getInt("total")));
            }
            rs.close();
            pstmt.close();

            // Week Sales
            query = "SELECT COALESCE(SUM(final_amount), 0) as total FROM customer_receipt WHERE em_username = ? AND date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                weekSales.setText(String.format("%.2f EGP", rs.getDouble("total")));
            }
            rs.close();
            pstmt.close();

            // Average Order
            query = "SELECT COALESCE(AVG(final_amount), 0) as avg FROM customer_receipt WHERE em_username = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                avgOrder.setText(String.format("%.2f EGP", rs.getDouble("avg")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load statistics");
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    private void calculatePerformance() {
        try {
            int orders = Integer.parseInt(todayOrders.getText());
            double performance = Math.min(orders / 20.0, 1.0);
            performanceBar.setProgress(performance);
            performanceLabel.setText(String.format("%.0f%%", performance * 100));

            // Update status message
            if (performance >= 1.0) {
                performanceStatus.setText("Target Achieved!");
            } else if (performance >= 0.75) {
                performanceStatus.setText("Almost there! Keep going!");
            } else if (performance >= 0.5) {
                performanceStatus.setText("Good progress!");
            } else {
                performanceStatus.setText("Keep working hard!");
            }
        } catch (Exception e) {
            performanceBar.setProgress(0);
            performanceLabel.setText("0%");
            performanceStatus.setText("Keep going!");
        }
    }

    private void refreshDashboard() {
        loadDashboardData();
        loadRecentOrders();
        loadStatistics();
        calculatePerformance();
    }

    @FXML
    public void editProfile() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Update your profile information");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField fullNameField = new TextField(session.getFullName());
        TextField phoneField = new TextField(session.getPhone());
        TextField emailField = new TextField(session.getEmail() != null ? session.getEmail() : "");
        PasswordField oldPassField = new PasswordField();
        PasswordField newPassField = new PasswordField();
        PasswordField confirmPassField = new PasswordField();

        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(fullNameField, 1, 0);
        grid.add(new Label("Phone:"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Old Password:"), 0, 3);
        grid.add(oldPassField, 1, 3);
        grid.add(new Label("New Password:"), 0, 4);
        grid.add(newPassField, 1, 4);
        grid.add(new Label("Confirm Password:"), 0, 5);
        grid.add(confirmPassField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (!newPassField.getText().isEmpty() && !newPassField.getText().equals(confirmPassField.getText())) {
                    showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match!");
                    return;
                }
                updateProfile(fullNameField.getText(), phoneField.getText(), emailField.getText(),
                        oldPassField.getText(), newPassField.getText());
            }
        });
    }

    private void updateProfile(String fullName, String phone, String email, String oldPass, String newPass) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();

            // Verify old password if changing password
            if (!newPass.isEmpty()) {
                String verifyQuery = "SELECT password FROM employee WHERE username = ?";
                pstmt = conn.prepareStatement(verifyQuery);
                pstmt.setString(1, session.getUsername());
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    String storedPass = rs.getString("password");
                    if (!hashPassword(oldPass).equals(storedPass)) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Old password is incorrect!");
                        return;
                    }
                }
                rs.close();
                pstmt.close();
            }

            // Update profile
            String updateQuery = newPass.isEmpty()
                    ? "UPDATE employee SET full_name = ?, phone = ?, email = ? WHERE username = ?"
                    : "UPDATE employee SET full_name = ?, phone = ?, email = ?, password = ? WHERE username = ?";

            pstmt = conn.prepareStatement(updateQuery);
            pstmt.setString(1, fullName);
            pstmt.setString(2, phone);
            pstmt.setString(3, email);

            if (!newPass.isEmpty()) {
                pstmt.setString(4, hashPassword(newPass));
                pstmt.setString(5, session.getUsername());
            } else {
                pstmt.setString(4, session.getUsername());
            }

            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                session.setFullName(fullName);
                session.setPhone(phone);
                session.setEmail(email);

                showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
                loadCashierInfo();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }
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

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Order Model Class
    public static class Order {

        private Integer orderId;
        private String customer, items, time;
        private Double amount;

        public Order(Integer orderId, String customer, String items, Double amount, String time) {
            this.orderId = orderId;
            this.customer = customer;
            this.items = items;
            this.amount = amount;
            this.time = time;
        }

        public Integer getOrderId() {
            return orderId;
        }

        public String getCustomer() {
            return customer;
        }

        public String getItems() {
            return items;
        }

        public Double getAmount() {
            return amount;
        }

        public String getTime() {
            return time;
        }
    }
}
