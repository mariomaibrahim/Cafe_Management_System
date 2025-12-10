package aitpcafe;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import org.kordamp.ikonli.javafx.FontIcon;

public class LoginController implements Initializable {

    @FXML
    private Hyperlink si_forgotpass;
    @FXML
    private Button si_loginbtn;
    @FXML
    private AnchorPane si_loginform;
    @FXML
    private PasswordField si_password;
    @FXML
    private TextField si_username;
    @FXML
    private HBox si_passwordContainer;

    @FXML
    private AnchorPane fp_forgotpassform;
    @FXML
    private TextField fp_username;
    @FXML
    private ComboBox<String> fp_question;
    @FXML
    private TextField fp_answer;
    @FXML
    private PasswordField fp_newpassword;
    @FXML
    private HBox fp_passwordContainer;
    @FXML
    private Button fp_proceedbtn;
    @FXML
    private Button fp_backbtn;

    private int loginAttempts = 0;
    private static final int MAX_LOGIN_ATTEMPTS = 5;

    private TextField si_passwordVisible;
    private Button si_togglePasswordBtn;
    private boolean si_passwordShown = false;

    private TextField fp_passwordVisible;
    private Button fp_togglePasswordBtn;
    private boolean fp_passwordShown = false;

    public void loginBtn() {
        String username = si_username.getText().trim();
        String password = si_passwordShown ? si_passwordVisible.getText() : si_password.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields");
            return;
        }

        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Too many login attempts. Please restart the application.");
            return;
        }

        if (!isValidUsername(username)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid username format");
            loginAttempts++;
            return;
        }

        String selectData = "SELECT id, username, password, role, full_name, email, phone, salary "
                + "FROM employee WHERE username = ? AND status = 'active' LIMIT 1";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            pstmt = conn.prepareStatement(selectData);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");

                if (verifyPassword(password, storedPassword)) {
                    UserSession session = UserSession.getInstance();
                    session.setUserData(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("role"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getDouble("salary")
                    );

                    updateLastLogin(conn, username);

                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Welcome " + session.getFullName() + "!");

                    navigateToDashboard(session.getRole());

                    loginAttempts = 0;

                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Incorrect username or password");
                    loginAttempts++;
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Incorrect username or password");
                loginAttempts++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Database connection error. Please try again.");
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    private void setupPasswordToggle() {
        // Setup for login password
        si_passwordVisible = new TextField();
        si_passwordVisible.setPromptText("Enter your password");
        si_passwordVisible.getStyleClass().add("input-field");
        si_passwordVisible.setVisible(false);
        si_passwordVisible.setManaged(false);

        si_togglePasswordBtn = new Button();
        FontIcon eyeIcon = new FontIcon("fas-eye");
        eyeIcon.setIconSize(18);
        eyeIcon.setIconColor(javafx.scene.paint.Color.web("#2a777c"));
        si_togglePasswordBtn.setGraphic(eyeIcon);
        si_togglePasswordBtn.getStyleClass().add("toggle-password-btn");

        si_togglePasswordBtn.setOnAction(e -> {
            si_passwordShown = !si_passwordShown;
            if (si_passwordShown) {
                si_passwordVisible.setText(si_password.getText());
                si_password.setVisible(false);
                si_password.setManaged(false);
                si_passwordVisible.setVisible(true);
                si_passwordVisible.setManaged(true);
                FontIcon hideIcon = new FontIcon("fas-eye-slash");
                hideIcon.setIconSize(18);
                hideIcon.setIconColor(javafx.scene.paint.Color.web("#2a777c"));
                si_togglePasswordBtn.setGraphic(hideIcon);
            } else {
                si_password.setText(si_passwordVisible.getText());
                si_passwordVisible.setVisible(false);
                si_passwordVisible.setManaged(false);
                si_password.setVisible(true);
                si_password.setManaged(true);
                FontIcon showIcon = new FontIcon("fas-eye");
                showIcon.setIconSize(18);
                showIcon.setIconColor(javafx.scene.paint.Color.web("#2a777c"));
                si_togglePasswordBtn.setGraphic(showIcon);
            }
        });

        int passwordIndex = si_passwordContainer.getChildren().indexOf(si_password);
        si_passwordContainer.getChildren().add(passwordIndex + 1, si_passwordVisible);
        si_passwordContainer.getChildren().add(si_togglePasswordBtn);

        // Setup for forgot password
        fp_passwordVisible = new TextField();
        fp_passwordVisible.setPromptText("Enter new password");
        fp_passwordVisible.getStyleClass().add("input-field-fp");
        fp_passwordVisible.setVisible(false);
        fp_passwordVisible.setManaged(false);

        fp_togglePasswordBtn = new Button();
        FontIcon fpEyeIcon = new FontIcon("fas-eye");
        fpEyeIcon.setIconSize(16);
        fpEyeIcon.setIconColor(javafx.scene.paint.Color.web("#2a777c"));
        fp_togglePasswordBtn.setGraphic(fpEyeIcon);
        fp_togglePasswordBtn.getStyleClass().add("toggle-password-btn");

        fp_togglePasswordBtn.setOnAction(e -> {
            fp_passwordShown = !fp_passwordShown;
            if (fp_passwordShown) {
                fp_passwordVisible.setText(fp_newpassword.getText());
                fp_newpassword.setVisible(false);
                fp_newpassword.setManaged(false);
                fp_passwordVisible.setVisible(true);
                fp_passwordVisible.setManaged(true);
                FontIcon hideIcon = new FontIcon("fas-eye-slash");
                hideIcon.setIconSize(16);
                hideIcon.setIconColor(javafx.scene.paint.Color.web("#2a777c"));
                fp_togglePasswordBtn.setGraphic(hideIcon);
            } else {
                fp_newpassword.setText(fp_passwordVisible.getText());
                fp_passwordVisible.setVisible(false);
                fp_passwordVisible.setManaged(false);
                fp_newpassword.setVisible(true);
                fp_newpassword.setManaged(true);
                FontIcon showIcon = new FontIcon("fas-eye");
                showIcon.setIconSize(16);
                showIcon.setIconColor(javafx.scene.paint.Color.web("#2a777c"));
                fp_togglePasswordBtn.setGraphic(showIcon);
            }
        });

        int fpPasswordIndex = fp_passwordContainer.getChildren().indexOf(fp_newpassword);
        fp_passwordContainer.getChildren().add(fpPasswordIndex + 1, fp_passwordVisible);
        fp_passwordContainer.getChildren().add(fp_togglePasswordBtn);
    }

    private void navigateToDashboard(String role) {
        try {
            String fxmlFile;
            String windowTitle;

            if ("admin".equalsIgnoreCase(role)) {
                fxmlFile = "/aitpcafe/menuScreen.fxml";
                windowTitle = "Cafe Management System - Admin Dashboard";
            } else {
                fxmlFile = "/aitpcafe/menuScreen.fxml";
                windowTitle = "Cafe Management System - Cashier Dashboard";
            }

            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = new Stage();
            stage.setTitle(windowTitle);

            // ✅ حجم موحد 1100x650
            stage.setScene(new Scene(root, 1100, 650));
            stage.setMinWidth(1100);
            stage.setMinHeight(650);
            stage.setResizable(true);

            stage.setOnCloseRequest(e -> {
                UserSession.getInstance().clearSession();
                DatabaseConfig.shutdown();
            });
            stage.show();

            si_loginbtn.getScene().getWindow().hide();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load dashboard: " + e.getMessage());
        }
    }

    public void forgotPassBtn() {
        FadeTransition fadeOutLogin = new FadeTransition(Duration.millis(300), si_loginform);
        fadeOutLogin.setFromValue(1.0);
        fadeOutLogin.setToValue(0.0);

        fadeOutLogin.setOnFinished(e -> {
            si_loginform.setVisible(false);
            fp_forgotpassform.setVisible(true);
            fp_forgotpassform.setOpacity(0.0);

            FadeTransition fadeInForgot = new FadeTransition(Duration.millis(300), fp_forgotpassform);
            fadeInForgot.setFromValue(0.0);
            fadeInForgot.setToValue(1.0);
            fadeInForgot.play();
        });

        fadeOutLogin.play();
        forgotPassQuestionList();
    }

    public void backToLoginBtn() {
        FadeTransition fadeOutForgot = new FadeTransition(Duration.millis(300), fp_forgotpassform);
        fadeOutForgot.setFromValue(1.0);
        fadeOutForgot.setToValue(0.0);

        fadeOutForgot.setOnFinished(e -> {
            fp_forgotpassform.setVisible(false);
            clearForgotPasswordFields();

            si_loginform.setVisible(true);
            si_loginform.setOpacity(0.0);

            FadeTransition fadeInLogin = new FadeTransition(Duration.millis(300), si_loginform);
            fadeInLogin.setFromValue(0.0);
            fadeInLogin.setToValue(1.0);
            fadeInLogin.play();
        });

        fadeOutForgot.play();
    }

    public void proceedBtn() {
        String username = fp_username.getText().trim();
        String answer = fp_answer.getText().trim();
        String newPassword = fp_passwordShown ? fp_passwordVisible.getText() : fp_newpassword.getText();
        String selectedQuestion = fp_question.getSelectionModel().getSelectedItem();

        if (username.isEmpty() || selectedQuestion == null || answer.isEmpty() || newPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields");
            return;
        }

        if (!isValidUsername(username)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid username format");
            return;
        }

        if (!isStrongPassword(newPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Password must be at least 8 characters with letters and numbers");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();

            String selectData = "SELECT username FROM employee WHERE username = ? AND question = ? AND answer = ? LIMIT 1";
            pstmt = conn.prepareStatement(selectData);
            pstmt.setString(1, username);
            pstmt.setString(2, selectedQuestion);
            pstmt.setString(3, answer);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = hashPassword(newPassword);
                String updatePass = "UPDATE employee SET password = ? WHERE username = ?";
                pstmt = conn.prepareStatement(updatePass);
                pstmt.setString(1, hashedPassword);
                pstmt.setString(2, username);
                int updated = pstmt.executeUpdate();

                if (updated > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Password changed successfully! Please login with your new password.");
                    backToLoginBtn();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Incorrect username, security question, or answer");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred. Please try again.");
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    private boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]{3,50}$");
    }

    private boolean isStrongPassword(String password) {
        return password != null && password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");
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

    private boolean verifyPassword(String inputPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }

        if (storedPassword.length() == 64) {
            String hashedInput = hashPassword(inputPassword);
            return hashedInput != null && hashedInput.equals(storedPassword);
        } else {
            return inputPassword.equals(storedPassword);
        }
    }

    private void updateLastLogin(Connection conn, String username) {
        PreparedStatement pstmt = null;
        try {
            String updateLogin = "UPDATE employee SET last_login = NOW() WHERE username = ?";
            pstmt = conn.prepareStatement(updateLogin);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    private void clearForgotPasswordFields() {
        fp_username.clear();
        fp_question.getSelectionModel().clearSelection();
        fp_answer.clear();
        fp_newpassword.clear();
        if (fp_passwordVisible != null) {
            fp_passwordVisible.clear();
        }
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

    private void forgotPassQuestionList() {
        ObservableList<String> listData = FXCollections.observableArrayList(
                "What is your favorite color?",
                "What is your favorite food?",
                "What is your pet's name?",
                "Where were you born?",
                "What is your mother's maiden name?"
        );
        fp_question.setItems(listData);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        si_loginform.setVisible(true);
        fp_forgotpassform.setVisible(false);

        setupPasswordToggle();

        if (!DatabaseConfig.testConnection()) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Cannot connect to database. Please check your database configuration.");
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
