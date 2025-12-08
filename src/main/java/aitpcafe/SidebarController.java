package aitpcafe;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

public class SidebarController implements Initializable {

    @FXML private AnchorPane sidebar;
    @FXML private Button toggleSidebar;
    @FXML private VBox logoSection;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private VBox navButtonsContainer;
    @FXML private Button logoutBtn;
    @FXML private FontIcon coffeeIcon;
    
    private boolean sidebarExpanded = true;
    private static final int SIDEBAR_EXPANDED_WIDTH = 220;
    private static final int SIDEBAR_COLLAPSED_WIDTH = 70;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupToggleButton();
    }

    private void setupToggleButton() {
        updateToggleIcon();
    }

    private void updateToggleIcon() {
        FontIcon toggleIcon = new FontIcon(sidebarExpanded ? "fas-chevron-left" : "fas-chevron-right");
        toggleIcon.setIconColor(javafx.scene.paint.Color.WHITE);
        toggleIcon.setIconSize(16);
        toggleSidebar.setGraphic(toggleIcon);
    }

    @FXML
    public void toggleSidebar() {
        TranslateTransition sidebarTransition = new TranslateTransition(Duration.millis(300), sidebar);
        sidebarTransition.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        
        if (sidebarExpanded) {
            // Collapse sidebar
            sidebar.setPrefWidth(SIDEBAR_COLLAPSED_WIDTH);
            sidebar.setMaxWidth(SIDEBAR_COLLAPSED_WIDTH);
            titleLabel.setVisible(false);
            subtitleLabel.setVisible(false);
            coffeeIcon.setIconSize(30);
            hideButtonText();
        } else {
            // Expand sidebar
            sidebar.setPrefWidth(SIDEBAR_EXPANDED_WIDTH);
            sidebar.setMaxWidth(SIDEBAR_EXPANDED_WIDTH);
            titleLabel.setVisible(true);
            subtitleLabel.setVisible(true);
            coffeeIcon.setIconSize(60);
            showButtonText();
        }
        
        updateToggleIcon();
        sidebarExpanded = !sidebarExpanded;
    }

    private void hideButtonText() {
        navButtonsContainer.getChildren().forEach(node -> {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.setText("");
                btn.setPrefWidth(50);
                btn.setMaxWidth(50);
                btn.setMinWidth(50);
                btn.setAlignment(javafx.geometry.Pos.CENTER);
                if (btn.getGraphic() != null && btn.getGraphic() instanceof FontIcon) {
                    ((FontIcon) btn.getGraphic()).setIconSize(20);
                }
            }
        });
        logoutBtn.setText("");
        logoutBtn.setPrefWidth(50);
        logoutBtn.setMaxWidth(50);
        logoutBtn.setMinWidth(50);
        logoutBtn.setAlignment(javafx.geometry.Pos.CENTER);
        if (logoutBtn.getGraphic() != null && logoutBtn.getGraphic() instanceof FontIcon) {
            ((FontIcon) logoutBtn.getGraphic()).setIconSize(20);
        }
    }

    private void showButtonText() {
        navButtonsContainer.getChildren().forEach(node -> {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String id = btn.getId();
                if (id != null) {
                    switch (id) {
                        case "dashboard_btn": btn.setText("  Dashboard"); break;
                        case "menu_btn": btn.setText("  Menu"); break;
                        case "inventory_btn": btn.setText("  Inventory"); break;
                    }
                    btn.setPrefWidth(190);
                    btn.setMaxWidth(190);
                    btn.setMinWidth(190);
                    btn.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    if (btn.getGraphic() != null && btn.getGraphic() instanceof FontIcon) {
                        ((FontIcon) btn.getGraphic()).setIconSize(16);
                    }
                }
            }
        });
        logoutBtn.setText("  Logout");
        logoutBtn.setPrefWidth(190);
        logoutBtn.setMaxWidth(190);
        logoutBtn.setMinWidth(190);
        logoutBtn.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        if (logoutBtn.getGraphic() != null && logoutBtn.getGraphic() instanceof FontIcon) {
            ((FontIcon) logoutBtn.getGraphic()).setIconSize(16);
        }
    }

    public boolean isExpanded() {
        return sidebarExpanded;
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }

    public VBox getNavButtonsContainer() {
        return navButtonsContainer;
    }

    public Button getLogoutButton() {
        return logoutBtn;
    }
    
    public AnchorPane getSidebar() {
        return sidebar;
    }
}