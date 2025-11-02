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
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class AdminController implements Initializable {

    @FXML private Button dashboard_btn, menu_btn, inventory_btn, logout_btn;
    @FXML private AnchorPane dashboard_form, menu_form, inventory_form;
    
    @FXML private Label totalCashiers, todayOrders, todayRevenue;
    @FXML private TableView<CashierActivity> cashierActivityTable;
    @FXML private TableColumn<CashierActivity, String> col_cashierName, col_status, col_loginTime;
    @FXML private TableColumn<CashierActivity, Integer> col_orders, col_customers;
    @FXML private TableColumn<CashierActivity, Double> col_sales;
    
    @FXML private TextField emp_username, emp_fullname, emp_phone, emp_email, emp_salary, emp_answer;
    @FXML private PasswordField emp_password;
    @FXML private ComboBox<String> emp_question;
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> col_empUsername, col_empName, col_empPhone, col_empEmail, col_empStatus;
    @FXML private TableColumn<Employee, Double> col_empSalary;
    @FXML private VBox employeeSection;
    
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private String currentAdmin = "admin";

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
        loadDashboardData();
        setupCashierActivityTable();
        loadCashierActivityData();
        setupEmployeeTable();
        loadEmployeeData();
        setupQuestions();
        employeeSection.setVisible(false);
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

    private void loadDashboardData() {
        connect = connectDB();
        try {
            String query = "SELECT COUNT(*) as total FROM employee WHERE role = 'cashier'";
            prepare = connect.prepareStatement(query);
            result = prepare.executeQuery();
            if (result.next()) {
                totalCashiers.setText(String.valueOf(result.getInt("total")));
            }

            query = "SELECT COUNT(*) as total FROM customer_receipt WHERE date = CURDATE()";
            prepare = connect.prepareStatement(query);
            result = prepare.executeQuery();
            if (result.next()) {
                todayOrders.setText(String.valueOf(result.getInt("total")));
            }

            query = "SELECT COALESCE(SUM(final_amount), 0) as total FROM customer_receipt WHERE date = CURDATE()";
            prepare = connect.prepareStatement(query);
            result = prepare.executeQuery();
            if (result.next()) {
                todayRevenue.setText(String.format("%.2f EGP", result.getDouble("total")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (result != null) result.close(); } catch (Exception e) {}
            try { if (prepare != null) prepare.close(); } catch (Exception e) {}
            try { if (connect != null) connect.close(); } catch (Exception e) {}
        }
    }

    private void setupCashierActivityTable() {
        col_cashierName.setCellValueFactory(new PropertyValueFactory<>("cashierName"));
        col_orders.setCellValueFactory(new PropertyValueFactory<>("orders"));
        col_sales.setCellValueFactory(new PropertyValueFactory<>("sales"));
        col_customers.setCellValueFactory(new PropertyValueFactory<>("customers"));
        col_loginTime.setCellValueFactory(new PropertyValueFactory<>("loginTime"));
        col_status.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadCashierActivityData() {
        ObservableList<CashierActivity> activityList = FXCollections.observableArrayList();
        connect = connectDB();
        try {
            String query = "SELECT e.full_name, e.status, e.last_login, " +
                          "COALESCE(COUNT(cr.id), 0) as orders, " +
                          "COALESCE(SUM(cr.final_amount), 0) as sales, " +
                          "COALESCE(COUNT(DISTINCT cr.customer_id), 0) as customers " +
                          "FROM employee e " +
                          "LEFT JOIN customer_receipt cr ON e.username = cr.em_username AND cr.date = CURDATE() " +
                          "WHERE e.role = 'cashier' " +
                          "GROUP BY e.username, e.full_name, e.status, e.last_login";
            
            prepare = connect.prepareStatement(query);
            result = prepare.executeQuery();

            while (result.next()) {
                Timestamp loginTime = result.getTimestamp("last_login");
                String loginStr = loginTime != null ? loginTime.toString() : "Never";
                
                CashierActivity activity = new CashierActivity(
                    result.getString("full_name"),
                    result.getInt("orders"),
                    result.getDouble("sales"),
                    result.getInt("customers"),
                    loginStr,
                    result.getString("status")
                );
                activityList.add(activity);
            }
            cashierActivityTable.setItems(activityList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (result != null) result.close(); } catch (Exception e) {}
            try { if (prepare != null) prepare.close(); } catch (Exception e) {}
            try { if (connect != null) connect.close(); } catch (Exception e) {}
        }
    }

    private void setupEmployeeTable() {
        col_empUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        col_empName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        col_empPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        col_empEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        col_empSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        col_empStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadEmployeeData() {
        ObservableList<Employee> employeeList = FXCollections.observableArrayList();
        connect = connectDB();
        try {
            String query = "SELECT username, full_name, phone, email, salary, status FROM employee WHERE role = 'cashier'";
            prepare = connect.prepareStatement(query);
            result = prepare.executeQuery();

            while (result.next()) {
                Employee emp = new Employee(
                    result.getString("username"),
                    result.getString("full_name"),
                    result.getString("phone"),
                    result.getString("email"),
                    result.getDouble("salary"),
                    result.getString("status")
                );
                employeeList.add(emp);
            }
            employeeTable.setItems(employeeList);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (result != null) result.close(); } catch (Exception e) {}
            try { if (prepare != null) prepare.close(); } catch (Exception e) {}
            try { if (connect != null) connect.close(); } catch (Exception e) {}
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
    public void showEmployeeManagement() {
        employeeSection.setVisible(!employeeSection.isVisible());
    }

    @FXML
    public void showProductManagement() {
        switchForm(new ActionEvent(menu_btn, null));
    }

    @FXML
    public void showCustomerReports() {
        showAlert(Alert.AlertType.INFORMATION, "Customer reports feature coming soon");
    }

    @FXML
    public void editAdminProfile() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Admin Profile");
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
            prepare.setString(1, currentAdmin);
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
                updateAdminProfile(fullNameField.getText(), phoneField.getText(), emailField.getText(), 
                                  oldPassField.getText(), newPassField.getText());
            }
        });
    }

    private void updateAdminProfile(String fullName, String phone, String email, String oldPass, String newPass) {
        connect = connectDB();
        try {
            if (!newPass.isEmpty()) {
                String verifyQuery = "SELECT password FROM employee WHERE username = ?";
                prepare = connect.prepareStatement(verifyQuery);
                prepare.setString(1, currentAdmin);
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
                prepare.setString(5, currentAdmin);
            } else {
                prepare.setString(4, currentAdmin);
            }

            prepare.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Profile updated successfully");
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
    public void addEmployee() {
        if (emp_username.getText().isEmpty() || emp_password.getText().isEmpty() ||
            emp_fullname.getText().isEmpty() || emp_phone.getText().isEmpty() ||
            emp_email.getText().isEmpty() || emp_salary.getText().isEmpty() ||
            emp_question.getValue() == null || emp_answer.getText().isEmpty()) {
            
            showAlert(Alert.AlertType.ERROR, "Please fill all fields");
            return;
        }

        connect = connectDB();
        try {
            String checkQuery = "SELECT username FROM employee WHERE username = ?";
            prepare = connect.prepareStatement(checkQuery);
            prepare.setString(1, emp_username.getText());
            result = prepare.executeQuery();

            if (result.next()) {
                showAlert(Alert.AlertType.ERROR, "Username already exists");
                return;
            }

            String insertQuery = "INSERT INTO employee (username, password, question, answer, role, full_name, phone, email, salary, hire_date, status, date) VALUES (?, ?, ?, ?, 'cashier', ?, ?, ?, ?, ?, 'active', ?)";
            prepare = connect.prepareStatement(insertQuery);
            prepare.setString(1, emp_username.getText());
            prepare.setString(2, hashPassword(emp_password.getText()));
            prepare.setString(3, emp_question.getValue());
            prepare.setString(4, emp_answer.getText());
            prepare.setString(5, emp_fullname.getText());
            prepare.setString(6, emp_phone.getText());
            prepare.setString(7, emp_email.getText());
            prepare.setDouble(8, Double.parseDouble(emp_salary.getText()));
            prepare.setDate(9, Date.valueOf(LocalDate.now()));
            prepare.setDate(10, Date.valueOf(LocalDate.now()));

            prepare.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Cashier added successfully");
            clearFields();
            loadEmployeeData();
            loadDashboardData();
            loadCashierActivityData();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error adding cashier");
        } finally {
            try { if (result != null) result.close(); } catch (Exception e) {}
            try { if (prepare != null) prepare.close(); } catch (Exception e) {}
            try { if (connect != null) connect.close(); } catch (Exception e) {}
        }
    }

    @FXML
    public void updateEmployee() {
        if (emp_username.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please select an employee");
            return;
        }

        connect = connectDB();
        try {
            String updateQuery = emp_password.getText().isEmpty() ?
                "UPDATE employee SET full_name = ?, phone = ?, email = ?, salary = ?, question = ?, answer = ? WHERE username = ?" :
                "UPDATE employee SET full_name = ?, phone = ?, email = ?, salary = ?, question = ?, answer = ?, password = ? WHERE username = ?";
            
            prepare = connect.prepareStatement(updateQuery);
            prepare.setString(1, emp_fullname.getText());
            prepare.setString(2, emp_phone.getText());
            prepare.setString(3, emp_email.getText());
            prepare.setDouble(4, Double.parseDouble(emp_salary.getText()));
            prepare.setString(5, emp_question.getValue());
            prepare.setString(6, emp_answer.getText());
            
            if (!emp_password.getText().isEmpty()) {
                prepare.setString(7, hashPassword(emp_password.getText()));
                prepare.setString(8, emp_username.getText());
            } else {
                prepare.setString(7, emp_username.getText());
            }

            prepare.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Employee updated successfully");
            clearFields();
            loadEmployeeData();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error updating employee");
        } finally {
            try { if (prepare != null) prepare.close(); } catch (Exception e) {}
            try { if (connect != null) connect.close(); } catch (Exception e) {}
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

        connect = connectDB();
        try {
            String deleteQuery = "DELETE FROM employee WHERE username = ? AND role = 'cashier'";
            prepare = connect.prepareStatement(deleteQuery);
            prepare.setString(1, emp_username.getText());

            prepare.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Employee deleted successfully");
            clearFields();
            loadEmployeeData();
            loadDashboardData();
            loadCashierActivityData();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error deleting employee");
        } finally {
            try { if (prepare != null) prepare.close(); } catch (Exception e) {}
            try { if (connect != null) connect.close(); } catch (Exception e) {}
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
        System.exit(0);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class CashierActivity {
        private String cashierName;
        private Integer orders;
        private Double sales;
        private Integer customers;
        private String loginTime;
        private String status;

        public CashierActivity(String cashierName, Integer orders, Double sales, Integer customers, String loginTime, String status) {
            this.cashierName = cashierName;
            this.orders = orders;
            this.sales = sales;
            this.customers = customers;
            this.loginTime = loginTime;
            this.status = status;
        }

        public String getCashierName() { return cashierName; }
        public Integer getOrders() { return orders; }
        public Double getSales() { return sales; }
        public Integer getCustomers() { return customers; }
        public String getLoginTime() { return loginTime; }
        public String getStatus() { return status; }
    }

    public static class Employee {
        private String username;
        private String fullName;
        private String phone;
        private String email;
        private Double salary;
        private String status;

        public Employee(String username, String fullName, String phone, String email, Double salary, String status) {
            this.username = username;
            this.fullName = fullName;
            this.phone = phone;
            this.email = email;
            this.salary = salary;
            this.status = status;
        }

        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public Double getSalary() { return salary; }
        public String getStatus() { return status; }
    }
}