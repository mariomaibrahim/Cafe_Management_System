package aitpcafe;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Aitpcafe extends Application {

    @Override
    public void start(Stage stage) {
        try {
            // تحميل واجهة تسجيل الدخول
            Parent root = FXMLLoader.load(getClass().getResource("menuScreen.fxml"));

            Scene scene = new Scene(root);
            stage.setTitle("Cafe Management System");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace(); // علشان لو حصل خطأ تعرفي مكانه
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
