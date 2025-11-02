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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    private Button fp_proceedbtn;
    @FXML
    private Button fp_backbtn;
    
    @FXML
    private AnchorPane side_form;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private Alert alert;
    private int loginAttempts = 0;
    private static final int MAX_LOGIN_ATTEMPTS = 5;

    public void loginBtn() {
        String username = si_username.getText().trim();
        String password = si_password.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Error", "Please fill all fields");
            return;
        }

        if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            showAlert(AlertType.ERROR, "Error", "Too many login attempts. Please restart the application.");
            return;
        }

        if (!isValidUsername(username)) {
            showAlert(AlertType.ERROR, "Error", "Invalid username format");
            loginAttempts++;
            return;
        }

        String selectData = "SELECT id, username, password, role FROM employee WHERE username = ? AND status = 'active' LIMIT 1";
        connect = database.connectDB();

        try {
            prepare = connect.prepareStatement(selectData);
            prepare.setString(1, username);
            result = prepare.executeQuery();

            if (result.next()) {
                String storedPassword = result.getString("password");
                
                if (verifyPassword(password, storedPassword)) {
                    data.username = username;
                    data.role = result.getString("role");
                    data.employeeId = result.getInt("id");

                    updateLastLogin(username);

                    showAlert(AlertType.INFORMATION, "Success", "Login successful!");

                    String fxmlFile;
                    String windowTitle;
                    
                    if ("admin".equals(data.role)) {
                        fxmlFile = "/aitpcafe/admin.fxml";
                        windowTitle = "Cafe Management System - Admin Dashboard";
                    } else {
                        fxmlFile = "/aitpcafe/cashierdash.fxml";
                        windowTitle = "Cafe Management System - Cashier";
                    }
                    
                    Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
                    Stage stage = new Stage();
                    stage.setTitle(windowTitle);
                    stage.setMinWidth(1100);
                    stage.setMinHeight(600);
                    stage.setScene(new Scene(root));
                    stage.show();
                    
                    si_loginbtn.getScene().getWindow().hide();
                    loginAttempts = 0;

                } else {
                    showAlert(AlertType.ERROR, "Error", "Incorrect username or password");
                    loginAttempts++;
                }
            } else {
                showAlert(AlertType.ERROR, "Error", "Incorrect username or password");
                loginAttempts++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred. Please try again.");
        } finally {
            closeResources();
        }
    }

    public void forgotPassBtn() {
        TranslateTransition slideLoginOut = new TranslateTransition();
        slideLoginOut.setNode(si_loginform);
        slideLoginOut.setDuration(Duration.millis(400));
        slideLoginOut.setToX(450);
        slideLoginOut.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        
        TranslateTransition slideSideForm = new TranslateTransition();
        slideSideForm.setNode(side_form);
        slideSideForm.setDuration(Duration.millis(400));
        slideSideForm.setToX(450);
        slideSideForm.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        
        slideLoginOut.setOnFinished(event -> {
            si_loginform.setVisible(false);
            fp_forgotpassform.setVisible(true);
        });
        
        slideLoginOut.play();
        slideSideForm.play();
        forgotPassQuestionList();
    }

    public void proceedBtn() {
        String username = fp_username.getText().trim();
        String answer = fp_answer.getText().trim();
        String newPassword = fp_newpassword.getText();
        String selectedQuestion = fp_question.getSelectionModel().getSelectedItem();

        if (username.isEmpty() || selectedQuestion == null || answer.isEmpty() || newPassword.isEmpty()) {
            showAlert(AlertType.ERROR, "Error", "Please fill all fields");
            return;
        }

        if (!isValidUsername(username)) {
            showAlert(AlertType.ERROR, "Error", "Invalid username format");
            return;
        }

        if (newPassword.length() < 8) {
            showAlert(AlertType.ERROR, "Error", "Password must be at least 8 characters");
            return;
        }

        if (!isStrongPassword(newPassword)) {
            showAlert(AlertType.ERROR, "Error", "Password must contain letters and numbers");
            return;
        }

        String selectData = "SELECT username FROM employee WHERE username = ? AND question = ? AND answer = ? LIMIT 1";
        connect = database.connectDB();

        try {
            prepare = connect.prepareStatement(selectData);
            prepare.setString(1, username);
            prepare.setString(2, selectedQuestion);
            prepare.setString(3, answer);
            result = prepare.executeQuery();

            if (result.next()) {
                String hashedPassword = hashPassword(newPassword);
                
                String updatePass = "UPDATE employee SET password = ? WHERE username = ?";
                prepare = connect.prepareStatement(updatePass);
                prepare.setString(1, hashedPassword);
                prepare.setString(2, username);
                prepare.executeUpdate();

                showAlert(AlertType.INFORMATION, "Success", "Password changed successfully!");
                backToLoginBtn();
            } else {
                showAlert(AlertType.ERROR, "Error", "Incorrect information");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred. Please try again.");
        } finally {
            closeResources();
        }
    }

    public void backToLoginBtn() {
        TranslateTransition slideSideForm = new TranslateTransition();
        slideSideForm.setNode(side_form);
        slideSideForm.setDuration(Duration.millis(600));
        slideSideForm.setToX(0);
        
        TranslateTransition slideLoginIn = new TranslateTransition();
        slideLoginIn.setNode(si_loginform);
        slideLoginIn.setDuration(Duration.millis(600));
        slideLoginIn.setToX(0);
        
        slideSideForm.setOnFinished(event -> {
            fp_forgotpassform.setVisible(false);
            si_loginform.setVisible(true);
            
            fp_username.setText("");
            fp_question.getSelectionModel().clearSelection();
            fp_answer.setText("");
            fp_newpassword.setText("");
        });
        
        slideSideForm.play();
        slideLoginIn.play();
    }

    private boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9_]{3,50}$");
    }

    private boolean isStrongPassword(String password) {
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    private boolean verifyPassword(String inputPassword, String storedPassword) {
        if (storedPassword.length() == 64) {
            return hashPassword(inputPassword).equals(storedPassword);
        } else {
            return inputPassword.equals(storedPassword);
        }
    }

    private void updateLastLogin(String username) {
        try {
            String updateLogin = "UPDATE employee SET last_login = NOW() WHERE username = ?";
            prepare = connect.prepareStatement(updateLogin);
            prepare.setString(1, username);
            prepare.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeResources() {
        try {
            if (result != null) result.close();
            if (prepare != null) prepare.close();
            if (connect != null) connect.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] questionList = {
        "What is your favorite color?",
        "What is your favorite food?",
        "What is your birth date?"
    };

    public void forgotPassQuestionList() {
        ObservableList<String> listData = FXCollections.observableArrayList(questionList);
        fp_question.setItems(listData);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        si_loginform.setVisible(true);
        fp_forgotpassform.setVisible(false);
    }
    
    private void showAlert(AlertType type, String title, String message) {
        alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}