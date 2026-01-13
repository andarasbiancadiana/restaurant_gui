package restaurant.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LoginView extends VBox {
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button guestButton;

    public LoginView() {
        this.setSpacing(15);
        this.setPadding(new Insets(50));
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: #f4f4f4;");

        Label title = new Label("La Andrei - Login");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(250);

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(250);

        loginButton = new Button("Login as Staff/Admin");
        loginButton.setPrefWidth(250);
        loginButton.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");

        guestButton = new Button("Continue as Guest");
        guestButton.setPrefWidth(250);

        this.getChildren().addAll(title, usernameField, passwordField, loginButton, guestButton);
    }

    public String getUsername() { return usernameField.getText(); }
    public String getPassword() { return passwordField.getText(); }
    public Button getLoginButton() { return loginButton; }
    public Button getGuestButton() { return guestButton; }
}