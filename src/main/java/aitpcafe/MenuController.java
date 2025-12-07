package aitpcafe;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class MenuController implements Initializable {

    @FXML
    private GridPane menu_gridpane;

    @FXML
    private ScrollPane menu_scrollpane;
    @FXML
private void switchPage(ActionEvent event) {
    Button clickedButton = (Button) event.getSource();
    String fxmlFile = "";

    switch (clickedButton.getId()) {
        case "menu_btn":
            fxmlFile = "menuScreen.fxml";
            break;
        case "inventory_btn":
            fxmlFile = "inventoryScreen.fxml";
            break;
        case "customers_btn":
            fxmlFile = "customers.fxml";
            break;
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

    // DB variables
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    // List for products
    private ObservableList<productData> cardListData = FXCollections.observableArrayList();

    // ------------------------------ LOAD DATA ------------------------------
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
        }

        return listData;
    }

    // ------------------------------ DISPLAY CARDS ------------------------------
    public void menuDisplayCard() {

        cardListData.clear();
        cardListData.addAll(menuGetData());

        menu_gridpane.getChildren().clear();

        int row = 0;
        int column = 0;

        for (productData product : cardListData) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("cardProduct.fxml"));
                AnchorPane pane = loader.load();

                CardProductController controller = loader.getController();
                controller.setData(product);

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

    // ------------------------------ INITIALIZE ------------------------------
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        menuDisplayCard();
    }
}
