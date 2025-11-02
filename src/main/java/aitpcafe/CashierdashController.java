package aitpcafe;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class CashierdashController implements Initializable {

    @FXML private Button dashboard_btn, menu_btn, inventory_btn, logout_btn;
    @FXML private AnchorPane dashboard_form, menu_form, inventory_form;
    
    @FXML private Label todayOrders, todayCustomers, todaySales;
    @FXML private Label cashierName, cashierPhone, cashierSalary;
    @FXML private ProgressBar performanceBar;
    @FXML private Label performanceLabel;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> col_orderId;
    @FXML private TableColumn<Order, String> col_customer, col_items, col_time;
    @FXML private TableColumn<Order, Double> col_amount;
    
    @FXML private VBox statisticsSection;
    @FXML private Label totalOrders, totalSales, totalCustomers;
    @FXML private Label weekOrders, weekSales, avgOrder;
    
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private String currentUsername = "cashier1";

    private Connection connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost/cafe?useSSL=false&allowPublicKeyRetrieval=true", "root", "");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCashierInfo();
        loadDashboardData();
        setupOrdersTable();
        loadRecentOrders();
        loadStatistics();
        calculatePerformance();
    }

    @FXML
    public void switchForm(ActionEvent event) {
        if (event.getSource() == dashboard_btn) {
            dashboard_form.setVisible(true);
            menu_form.setVisible(false);
            inventory_form.setVisible(false);
            loadDashboardData();
        } else if (event.getSource() == menu_btn) {
            dashboard_form.setVisible(false);
            menu_form.setVisible(true);
            inventory_form.setVisible(false);
        } else if (event.getSource() == inventory_btn) {
            dashboard_form.setVisible(false);
            menu_form.setVisible(false);
            inventory_form.setVisible(true);
        }
    }

    private void loadCashierInfo() {
        connect = connectDB();
        try {
            String query = "SELECT full_name, phone, salary FROM employee WHERE username = ?";
            prepare = connect.prepareStatement(query);
            prepare.setString(1, currentUsername);
            result = prepare.executeQuery();

            if (result.next()) {
                cashierName.setText(result.getString("full_name"));
                cashierPhone.setText(result.getString("phone"));
                cashierSalary.setText(String.format("%.2f EGP", result.getDouble("salary")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (result != null) result.close(); } catch (Exception e) {}
            try { if (prepare != null) prepare.close(); } catch (Exception e) {}
            try { if (connect != null) connect.close(); } catch (Exception e) {}
        }
    }

    private void loadDashboardData() {
        connect = connectDB();
        try {
            String query = "SELECT COUNT(*) as total FROM customer_receipt WHERE em_username = ? AND date = CURDATE()";
            prepare = connect.prepareStatement(query);
            prepare.setString(1, currentUsername);
            result = prepare.executeQuery();
            if (result.next()) {
                todayOrders.setText(String.valueOf(result.getInt("total")));
            }

            query = "SELECT COUNT(DISTINCT customer_id) as total FROM customer_receipt WHERE em_username = ? AND date = CURDATE()";
            prepare = connect.prepareStatement(query);
            prepare.setString(1, currentUsername);
            result = prepare.executeQuery();
            if (result.next()) {
                todayCustomers.setText(String.valueOf(result.getInt("total")));
            }

            query = "SELECT COALESCE(SUM(final_amount), 0) as total FROM customer_receipt WHERE em_username = ? AND date = CURDATE()";
            prepare = connect.prepareStatement(query);
            prepare.setString(1, currentUsername);
            result = prepare.executeQuery();
            if (result.next()) {
                todaySales.setText(String.format("%.2f EGP", result.getDouble("total")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (result != null) result.close(); } catch (Exception e) {}
            try { if (prepare != null) prepare.close(); } catch (Exception e) {}
            try { if (connect != null) connect.close(); } catch (Exception e) {}
        }
    }

    private void setupOrdersTable() {
        col_orderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        col_customer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        col_items.setCellValueFactory(new PropertyValueFactory<>("items"));
        col_amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        col_time.setCellValueFactory(new PropertyValueFactory<>("time"));
    }

    private void loadRecentOrders() {
        ObservableList<Order> ordersList = FXCollections.observableArrayList();
        connect = connectDB();
        try {
            String query = "SELECT customer_id, customer_name, final_amount, date " +
                          "FROM customer_receipt " +
                          "WHERE em_username = ? AND date = CURDATE() " +
                          "ORDER BY id DESC LIMIT 5";
            
            prepare = connect.prepareStatement(query);
            prepare.setString(1, currentUsername);
            result = prepare.executeQuery();

            while (result.next()) {
                Order order = new Order(
                    result.getInt("customer_id"),
                    result.getString("customer_name") != null ? result.getString("customer_name") : "Walk-in",
                    "Mixed Items",
                    result.getDouble("final_amount"),
                    result.getString("date")
                );
                ordersList.add(order);
            }
            ordersTable.setItems(ordersList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (result != null) result.close(); } catch (Exception e) {}
            try { if (prepare != null) prepare.close(); } catch (Exception e) {}
            try { if (connect != null) connect.close(); } catch (Exception e) {}
        }
    }

    private void loadStatistics() {
        connect = connectDB();
        try {
            String query = "SELECT COUNT(*) as total FROM customer_receipt WHERE em_username = ?";
            prepare = connect.prepareStatement(query);
            prepare.setString(1, currentUsername);
            result = prepare.executeQuery();
            if (result.next()) {
                totalOrders.setText(String.valueOf(result.getInt("total")));
            }

            query = "SELECT COALESCE(SUM(final_amount), 0) as total FROM customer_receipt WHERE em_username = ?";
            prepare = connect.prepareStatement(query);
            prepare.setString(1, currentUsername);
            result = prepare.executeQuery();
            if (result.next()) {
                totalSales.setText(String.format("%.2f EGP", result.getDouble("total")));
            }

            query = "SELECT COUNT(DISTINCT customer_id) as total FROM customer_receipt WHERE em_username = ?";
            prepare = connect.prepareStatement(query);
            prepare.setString(1, currentUsername);
            result = prepare.executeQuery();
            if (result.next()) {
                totalCustomers.setText(String.valueOf(result.getInt("total")));
            }

            query = "SELECT COUNT(*) as total FROM customer_receipt WHERE em_username = ? AND date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            prepare = connect.prepareStatement(query);
            prepare.setString(1, currentUsername);
            result = prepare.executeQuery();
            if (result.next()) {
                weekOrders.setText(String.valueOf(result.getInt("total")));
            }

            query = "SELECT COALESCE(SUM(final_amount), 0) as total FROM customer_receipt WHERE em_username = ? AND date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            prepare = connect.prepareStatement(query);
            prepare.setString(1, currentUsername);
            result = prepare.executeQuery();
            if (result.next()) {
                weekSales.setText(String.format("%.2f EGP", result.getDouble("total")));
            }

            query = "SELECT COALESCE(AVG(final_amount), 0) as avg FROM customer_receipt WHERE em_username = ?";
            prepare = connect.prepareStatement(query);
            prepare.setString(1, currentUsername);
            result = prepare.executeQuery();
            if (result.next()) {
                avgOrder.setText(String.format("%.2f EGP", result.getDouble("avg")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (result != null) result.close(); } catch (Exception e) {}
            try { if (prepare != null) prepare.close(); } catch (Exception e) {}
            try { if (connect != null) connect.close(); } catch (Exception e) {}
        }
    }

    private void calculatePerformance() {
        try {
            int orders = Integer.parseInt(todayOrders.getText());
            double performance = Math.min(orders / 20.0, 1.0);
            performanceBar.setProgress(performance);
            performanceLabel.setText(String.format("%.0f%%", performance * 100));
        } catch (Exception e) {
            performanceBar.setProgress(0);
            performanceLabel.setText("0%");
        }
    }

    @FXML
    public void showStatistics() {
        statisticsSection.setVisible(!statisticsSection.isVisible());
        if (statisticsSection.isVisible()) {
            loadStatistics();
        }
    }

    @FXML
    public void showCustomers() {
        showAlert(Alert.AlertType.INFORMATION, "Customer list feature coming soon");
    }

    @FXML
    public void editProfile() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Update your profile information");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField fullNameField = new TextField();
        TextField phoneField = new TextField();
        TextField emailField = new TextField();
        PasswordField oldPassField = new PasswordField();
        PasswordField newPassField = new PasswordField();

        connect = connectDB();
        try {
            String query = "SELECT full_name, phone, email FROM employee WHERE username = ?";
            prepare = connect.prepareStatement(query);
            prepare.setString(1, currentUsername);
            result = prepare.executeQuery();
            if (result.next()) {
                fullNameField.setText(result.getString("full_name"));
                phoneField.setText(result.getString("phone"));
                emailField.setText(result.getString("email"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (result != null) result.close(); } catch (Exception e) {}
            try { if (prepare != null) prepare.close(); } catch (Exception e) {}
            try { if (connect != null) connect.close(); } catch (Exception e) {}
        }

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

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                updateProfile(fullNameField.getText(), phoneField.getText(), emailField.getText(), 
                             oldPassField.getText(), newPassField.getText());
            }
        });
    }

    private void updateProfile(String fullName, String phone, String email, String oldPass, String newPass) {
        connect = connectDB();
        try {
            if (!newPass.isEmpty()) {
                String verifyQuery = "SELECT password FROM employee WHERE username = ?";
                prepare = connect.prepareStatement(verifyQuery);
                prepare.setString(1, currentUsername);
                result = prepare.executeQuery();
                if (result.next()) {
                    String storedPass = result.getString("password");
                    if (!hashPassword(oldPass).equals(storedPass)) {
                        showAlert(Alert.AlertType.ERROR, "Old password is incorrect");
                        return;
                    }
                }
            }

            String updateQuery = newPass.isEmpty() ? 
                "UPDATE employee SET full_name = ?, phone = ?, email = ? WHERE username = ?" :
                "UPDATE employee SET full_name = ?, phone = ?, email = ?, password = ? WHERE username = ?";
            
            prepare = connect.prepareStatement(updateQuery);
            prepare.setString(1, fullName);
            prepare.setString(2, phone);
            prepare.setString(3, email);
            
            if (!newPass.isEmpty()) {
                prepare.setString(4, hashPassword(newPass));
                prepare.setString(5, currentUsername);
            } else {
                prepare.setString(4, currentUsername);
            }

            prepare.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Profile updated successfully");
            loadCashierInfo();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error updating profile");
        } finally {
            try { if (result != null) result.close(); } catch (Exception e) {}
            try { if (prepare != null) prepare.close(); } catch (Exception e) {}
            try { if (connect != null) connect.close(); } catch (Exception e) {}
        }
    }

    @FXML
    public void logout() {
        System.exit(0);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Order {
        private Integer orderId;
        private String customer;
        private String items;
        private Double amount;
        private String time;

        public Order(Integer orderId, String customer, String items, Double amount, String time) {
            this.orderId = orderId;
            this.customer = customer;
            this.items = items;
            this.amount = amount;
            this.time = time;
        }

        public Integer getOrderId() { return orderId; }
        public String getCustomer() { return customer; }
        public String getItems() { return items; }
        public Double getAmount() { return amount; }
        public String getTime() { return time; }
    }
}