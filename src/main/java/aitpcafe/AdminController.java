package aitpcafe;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDate;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

public class AdminController implements Initializable {

    @FXML
    private AnchorPane dashboard_form, menu_form, inventory_form, mainContainer;
    @FXML
    private Label totalCashiers, todayOrders, todayRevenue, totalOrders, totalRevenue, totalCustomers;
    @FXML
    private Label weekOrders, weekRevenue, monthOrders;

    // Cashier Activity Table
    @FXML
    private TableView<CashierActivity> cashierActivityTable;
    @FXML
    private TableColumn<CashierActivity, String> col_cashierName, col_status, col_loginTime;
    @FXML
    private TableColumn<CashierActivity, Integer> col_orders, col_customers;
    @FXML
    private TableColumn<CashierActivity, Double> col_sales;

    // Orders Table with Filter
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, Integer> col_orderId;
    @FXML
    private TableColumn<Order, String> col_cashier, col_customer, col_orderTime;
    @FXML
    private TableColumn<Order, Double> col_amount;
    @FXML
    private ComboBox<String> filterPeriod;

    // Employee Management
    @FXML
    private TextField emp_username, emp_fullname, emp_phone, emp_email, emp_salary, emp_answer;
    @FXML
    private PasswordField emp_password;
    @FXML
    private ComboBox<String> emp_question;
    @FXML
    private TableView<Employee> employeeTable;
    @FXML
    private TableColumn<Employee, String> col_empUsername, col_empName, col_empPhone, col_empEmail, col_empStatus;
    @FXML
    private TableColumn<Employee, Double> col_empSalary;
    @FXML
    private VBox employeeSection;
    @FXML
    private Button toggleEmployeeBtn;

    private SidebarController sidebarController;
    private AnchorPane sidebar;
    private Button menu_btn, inventory_btn, dashboard_btn;
    private UserSession session;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        session = UserSession.getInstance();

        if (!session.isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Access Denied! You don't have admin privileges.");
            logout();
            return;
        }

        loadSidebar();
        loadDashboardData();
        setupCashierActivityTable();
        loadCashierActivityData();
        setupEmployeeTable();
        loadEmployeeData();
        setupQuestions();
        setupOrdersTable();
        setupFilterPeriod();
        loadOrders();

        if (employeeSection != null) {
            employeeSection.setVisible(false);
            employeeSection.setManaged(false);
        }
        adjustContentPosition();
    }

    private void loadSidebar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sidebar.fxml"));
            sidebar = loader.load();
            sidebarController = loader.getController();

            sidebarController.setTitle("Cafe Admin");
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
            loadDashboardData();
            loadCashierActivityData();
            loadOrders();
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
            showAlert(Alert.AlertType.ERROR, "Failed to load page: " + e.getMessage());
        }
    }

    private void loadDashboardData() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();

            // Total Active Cashiers
            String query = "SELECT COUNT(*) as total FROM employee WHERE role = 'cashier' AND status = 'active'";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                totalCashiers.setText(String.valueOf(rs.getInt("total")));
            }
            rs.close();
            pstmt.close();

            // Today Orders
            query = "SELECT COUNT(*) as total FROM customer_receipt WHERE DATE(date) = CURDATE()";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                todayOrders.setText(String.valueOf(rs.getInt("total")));
            }
            rs.close();
            pstmt.close();

            // Today Revenue
            query = "SELECT COALESCE(SUM(final_amount), 0) as total FROM customer_receipt WHERE DATE(date) = CURDATE()";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                todayRevenue.setText(String.format("%.2f EGP", rs.getDouble("total")));
            }
            rs.close();
            pstmt.close();

            // Total Orders
            query = "SELECT COUNT(*) as total FROM customer_receipt";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                totalOrders.setText(String.valueOf(rs.getInt("total")));
            }
            rs.close();
            pstmt.close();

            // Total Revenue
            query = "SELECT COALESCE(SUM(final_amount), 0) as total FROM customer_receipt";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                totalRevenue.setText(String.format("%.2f EGP", rs.getDouble("total")));
            }
            rs.close();
            pstmt.close();

            // Total Customers
            query = "SELECT COUNT(DISTINCT customer_id) as total FROM customer_receipt";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                totalCustomers.setText(String.valueOf(rs.getInt("total")));
            }
            rs.close();
            pstmt.close();

            // Week Orders
            query = "SELECT COUNT(*) as total FROM customer_receipt WHERE date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                weekOrders.setText(String.valueOf(rs.getInt("total")));
            }
            rs.close();
            pstmt.close();

            // Week Revenue
            query = "SELECT COALESCE(SUM(final_amount), 0) as total FROM customer_receipt WHERE date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                weekRevenue.setText(String.format("%.2f EGP", rs.getDouble("total")));
            }
            rs.close();
            pstmt.close();

            // Month Orders
            query = "SELECT COUNT(*) as total FROM customer_receipt WHERE date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                monthOrders.setText(String.valueOf(rs.getInt("total")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to load dashboard data");
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    private void setupCashierActivityTable() {
        col_cashierName.setCellValueFactory(new PropertyValueFactory<>("cashierName"));
        col_orders.setCellValueFactory(new PropertyValueFactory<>("orders"));
        col_sales.setCellValueFactory(new PropertyValueFactory<>("sales"));
        col_customers.setCellValueFactory(new PropertyValueFactory<>("customers"));
        col_loginTime.setCellValueFactory(new PropertyValueFactory<>("loginTime"));
        col_status.setCellValueFactory(new PropertyValueFactory<>("status"));

        col_sales.setCellFactory(column -> new TableCell<CashierActivity, Double>() {
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

    private void loadCashierActivityData() {
        ObservableList<CashierActivity> activityList = FXCollections.observableArrayList();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            String query = "SELECT e.full_name, e.status, e.last_login, "
                    + "COALESCE(COUNT(cr.id), 0) as orders, "
                    + "COALESCE(SUM(cr.final_amount), 0) as sales, "
                    + "COALESCE(COUNT(DISTINCT cr.customer_id), 0) as customers "
                    + "FROM employee e "
                    + "LEFT JOIN customer_receipt cr ON e.username = cr.em_username AND DATE(cr.date) = CURDATE() "
                    + "WHERE e.role = 'cashier' "
                    + "GROUP BY e.username, e.full_name, e.status, e.last_login "
                    + "ORDER BY sales DESC";

            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Timestamp loginTime = rs.getTimestamp("last_login");
                String loginStr = loginTime != null ? loginTime.toString() : "Never";

                CashierActivity activity = new CashierActivity(
                        rs.getString("full_name"),
                        rs.getInt("orders"),
                        rs.getDouble("sales"),
                        rs.getInt("customers"),
                        loginStr,
                        rs.getString("status")
                );
                activityList.add(activity);
            }
            cashierActivityTable.setItems(activityList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to load cashier activity");
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    private void setupOrdersTable() {
        col_orderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        col_cashier.setCellValueFactory(new PropertyValueFactory<>("cashier"));
        col_customer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        col_amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        col_orderTime.setCellValueFactory(new PropertyValueFactory<>("orderTime"));

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

    private void setupFilterPeriod() {
        ObservableList<String> periods = FXCollections.observableArrayList(
                "Today", "This Week", "This Month", "Last 3 Months", "This Year", "All Time"
        );
        filterPeriod.setItems(periods);
        filterPeriod.setValue("Today");
        filterPeriod.setOnAction(e -> loadOrders());
    }

    @FXML
    public void loadOrders() {
        ObservableList<Order> ordersList = FXCollections.observableArrayList();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            String dateFilter = getDateFilter();

            String query = "SELECT cr.id, cr.em_username, e.full_name as cashier_name, "
                    + "cr.customer_name, cr.final_amount, cr.date "
                    + "FROM customer_receipt cr "
                    + "LEFT JOIN employee e ON cr.em_username = e.username "
                    + dateFilter
                    + " ORDER BY cr.id DESC LIMIT 100";

            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("id"),
                        rs.getString("cashier_name") != null ? rs.getString("cashier_name") : rs.getString("em_username"),
                        rs.getString("customer_name") != null ? rs.getString("customer_name") : "Walk-in Customer",
                        rs.getDouble("final_amount"),
                        rs.getTimestamp("date").toString()
                );
                ordersList.add(order);
            }
            ordersTable.setItems(ordersList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to load orders");
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    private String getDateFilter() {
        String period = filterPeriod.getValue();
        switch (period) {
            case "Today":
                return "WHERE DATE(cr.date) = CURDATE()";
            case "This Week":
                return "WHERE cr.date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
            case "This Month":
                return "WHERE cr.date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
            case "Last 3 Months":
                return "WHERE cr.date >= DATE_SUB(CURDATE(), INTERVAL 90 DAY)";
            case "This Year":
                return "WHERE YEAR(cr.date) = YEAR(CURDATE())";
            case "All Time":
            default:
                return "";
        }
    }

    private void setupEmployeeTable() {
        col_empUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        col_empName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        col_empPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        col_empEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        col_empSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        col_empStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        col_empSalary.setCellFactory(column -> new TableCell<Employee, Double>() {
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

    private void loadEmployeeData() {
        ObservableList<Employee> employeeList = FXCollections.observableArrayList();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            String query = "SELECT username, full_name, phone, email, salary, status "
                    + "FROM employee WHERE role = 'cashier' ORDER BY full_name";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Employee emp = new Employee(
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getDouble("salary"),
                        rs.getString("status")
                );
                employeeList.add(emp);
            }
            employeeTable.setItems(employeeList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to load employees");
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    private void setupQuestions() {
        ObservableList<String> questions = FXCollections.observableArrayList(
                "What is your favorite color?",
                "What is your favorite food?",
                "What is your pet's name?",
                "Where were you born?",
                "What is your mother's maiden name?"
        );
        emp_question.setItems(questions);
    }

    @FXML
    public void toggleEmployeeForm() {
        boolean isVisible = employeeSection.isVisible();
        employeeSection.setVisible(!isVisible);
        employeeSection.setManaged(!isVisible);

        if (toggleEmployeeBtn != null) {
            if (isVisible) {
                toggleEmployeeBtn.setText("Manage Employees");
                toggleEmployeeBtn.setGraphic(new FontIcon("fas-user-cog"));
            } else {
                toggleEmployeeBtn.setText("Hide Employee Form");
                toggleEmployeeBtn.setGraphic(new FontIcon("fas-times"));
            }
        }
    }

    @FXML
    public void editAdminProfile() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Admin Profile");
        dialog.setHeaderText("Update your profile information");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField fullNameField = new TextField(session.getFullName());
        TextField phoneField = new TextField(session.getPhone());
        TextField emailField = new TextField(session.getEmail());
        PasswordField oldPassField = new PasswordField();
        PasswordField newPassField = new PasswordField();

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
                updateAdminProfile(fullNameField.getText(), phoneField.getText(),
                        emailField.getText(), oldPassField.getText(), newPassField.getText());
            }
        });
    }

    private void updateAdminProfile(String fullName, String phone, String email, String oldPass, String newPass) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();

            if (!newPass.isEmpty()) {
                String verifyQuery = "SELECT password FROM employee WHERE username = ?";
                pstmt = conn.prepareStatement(verifyQuery);
                pstmt.setString(1, session.getUsername());
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    String storedPass = rs.getString("password");
                    if (!hashPassword(oldPass).equals(storedPass)) {
                        showAlert(Alert.AlertType.ERROR, "Old password is incorrect");
                        return;
                    }
                }
                rs.close();
                pstmt.close();
            }

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

                showAlert(Alert.AlertType.INFORMATION, "Profile updated successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to update profile");
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    @FXML
    public void addEmployee() {
        if (emp_username.getText().isEmpty() || emp_password.getText().isEmpty()
                || emp_fullname.getText().isEmpty() || emp_phone.getText().isEmpty()
                || emp_email.getText().isEmpty() || emp_salary.getText().isEmpty()
                || emp_question.getValue() == null || emp_answer.getText().isEmpty()) {

            showAlert(Alert.AlertType.ERROR, "Please fill all fields");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();

            String checkQuery = "SELECT username FROM employee WHERE username = ?";
            pstmt = conn.prepareStatement(checkQuery);
            pstmt.setString(1, emp_username.getText().trim());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Username already exists!");
                return;
            }
            rs.close();
            pstmt.close();

            String insertQuery = "INSERT INTO employee (username, password, question, answer, role, "
                    + "full_name, phone, email, salary, hire_date, status, date) "
                    + "VALUES (?, ?, ?, ?, 'cashier', ?, ?, ?, ?, ?, 'active', ?)";
            pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, emp_username.getText().trim());
            pstmt.setString(2, hashPassword(emp_password.getText()));
            pstmt.setString(3, emp_question.getValue());
            pstmt.setString(4, emp_answer.getText().trim());
            pstmt.setString(5, emp_fullname.getText().trim());
            pstmt.setString(6, emp_phone.getText().trim());
            pstmt.setString(7, emp_email.getText().trim());
            pstmt.setDouble(8, Double.parseDouble(emp_salary.getText().trim()));
            pstmt.setDate(9, Date.valueOf(LocalDate.now()));
            pstmt.setDate(10, Date.valueOf(LocalDate.now()));

            int inserted = pstmt.executeUpdate();
            if (inserted > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Cashier added successfully!");
                clearFields();
                loadEmployeeData();
                loadDashboardData();
                loadCashierActivityData();
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid salary format");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to add employee");
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    @FXML
    public void updateEmployee() {
        if (emp_username.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please select an employee");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConfig.getConnection();

            String updateQuery = emp_password.getText().isEmpty()
                    ? "UPDATE employee SET full_name = ?, phone = ?, email = ?, salary = ?, question = ?, answer = ? WHERE username = ?"
                    : "UPDATE employee SET full_name = ?, phone = ?, email = ?, salary = ?, question = ?, answer = ?, password = ? WHERE username = ?";

            pstmt = conn.prepareStatement(updateQuery);
            pstmt.setString(1, emp_fullname.getText().trim());
            pstmt.setString(2, emp_phone.getText().trim());
            pstmt.setString(3, emp_email.getText().trim());
            pstmt.setDouble(4, Double.parseDouble(emp_salary.getText().trim()));
            pstmt.setString(5, emp_question.getValue());
            pstmt.setString(6, emp_answer.getText().trim());

            if (!emp_password.getText().isEmpty()) {
                pstmt.setString(7, hashPassword(emp_password.getText()));
                pstmt.setString(8, emp_username.getText().trim());
            } else {
                pstmt.setString(7, emp_username.getText().trim());
            }

            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Employee updated successfully!");
                clearFields();
                loadEmployeeData();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to update employee");
        } finally {
            closeResources(null, pstmt, conn);
        }
    }

    @FXML
    public void deleteEmployee() {
        if (emp_username.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please select an employee");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setContentText("Are you sure you want to delete this employee?");

        if (confirmAlert.showAndWait().get() != ButtonType.OK) {
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            String deleteQuery = "DELETE FROM employee WHERE username = ? AND role = 'cashier'";
            pstmt = conn.prepareStatement(deleteQuery);
            pstmt.setString(1, emp_username.getText().trim());

            int deleted = pstmt.executeUpdate();
            if (deleted > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Employee deleted successfully!");
                clearFields();
                loadEmployeeData();
                loadDashboardData();
                loadCashierActivityData();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed to delete employee");
        } finally {
            closeResources(null, pstmt, conn);
        }
    }

    @FXML
    public void selectEmployee() {
        Employee emp = employeeTable.getSelectionModel().getSelectedItem();
        if (emp != null) {
            emp_username.setText(emp.getUsername());
            emp_fullname.setText(emp.getFullName());
            emp_phone.setText(emp.getPhone());
            emp_email.setText(emp.getEmail());
            emp_salary.setText(String.valueOf(emp.getSalary()));
            emp_password.clear();
        }
    }

    @FXML
    public void clearFields() {
        emp_username.clear();
        emp_password.clear();
        emp_fullname.clear();
        emp_phone.clear();
        emp_email.clear();
        emp_salary.clear();
        emp_answer.clear();
        emp_question.setValue(null);
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

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
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

    // Model Classes
    public static class CashierActivity {

        private String cashierName, loginTime, status;
        private Integer orders, customers;
        private Double sales;

        public CashierActivity(String cashierName, Integer orders, Double sales,
                Integer customers, String loginTime, String status) {
            this.cashierName = cashierName;
            this.orders = orders;
            this.sales = sales;
            this.customers = customers;
            this.loginTime = loginTime;
            this.status = status;
        }

        public String getCashierName() {
            return cashierName;
        }

        public Integer getOrders() {
            return orders;
        }

        public Double getSales() {
            return sales;
        }

        public Integer getCustomers() {
            return customers;
        }

        public String getLoginTime() {
            return loginTime;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class Employee {

        private String username, fullName, phone, email, status;
        private Double salary;

        public Employee(String username, String fullName, String phone,
                String email, Double salary, String status) {
            this.username = username;
            this.fullName = fullName;
            this.phone = phone;
            this.email = email;
            this.salary = salary;
            this.status = status;
        }

        public String getUsername() {
            return username;
        }

        public String getFullName() {
            return fullName;
        }

        public String getPhone() {
            return phone;
        }

        public String getEmail() {
            return email;
        }

        public Double getSalary() {
            return salary;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class Order {

        private Integer orderId;
        private String cashier, customer, orderTime;
        private Double amount;

        public Order(Integer orderId, String cashier, String customer, Double amount, String orderTime) {
            this.orderId = orderId;
            this.cashier = cashier;
            this.customer = customer;
            this.amount = amount;
            this.orderTime = orderTime;
        }

        public Integer getOrderId() {
            return orderId;
        }

        public String getCashier() {
            return cashier;
        }

        public String getCustomer() {
            return customer;
        }

        public Double getAmount() {
            return amount;
        }

        public String getOrderTime() {
            return orderTime;
        }
    }
}
