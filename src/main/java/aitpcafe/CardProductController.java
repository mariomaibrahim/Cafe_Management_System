package aitpcafe;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;

import java.net.URL;
import java.io.File;
import java.util.ResourceBundle;

public class CardProductController implements Initializable {

    @FXML private Button productBtn;
    @FXML private Label productName;
    @FXML private Label productPrice;
    @FXML private Spinner<Integer> productScroll;
    @FXML private ImageView productimage;

    private productData prodData;
    private Image image;

    public void setData(productData prodData){
        this.prodData = prodData;

        productName.setText(prodData.getProductName());
        productPrice.setText(String.valueOf(prodData.getPrice()));

        // تحميل الصورة
        try {
            if (prodData.getImage() != null && !prodData.getImage().isEmpty()) {

                File file = new File(prodData.getImage());

                if (file.exists()) {
                    image = new Image(file.toURI().toString(), 230, 250, false, true);
                    productimage.setImage(image);
                } else {
                    System.out.println("IMAGE NOT FOUND => " + prodData.getImage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // علشان تتفادي NullPointer لو الفيلد مش متعرف في FXML
        if (productScroll != null) {
            SpinnerValueFactory<Integer> valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
            productScroll.setValueFactory(valueFactory);
        }
    }
}
