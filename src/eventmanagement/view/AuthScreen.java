package eventmanagement.view;

import eventmanagement.dao.UserDAO;
import eventmanagement.model.Role;
import eventmanagement.model.User;
import eventmanagement.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class AuthScreen {
    private Stage stage;
    private UserDAO userDAO = new UserDAO();

    // Constant Styles
    private final String BG_COLOR = "-fx-background-color: #1A1A1A;";
    private final String FIELD_STYLE = "-fx-background-color: #333333; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 12 15; -fx-prompt-text-fill: #AAAAAA;";
    private final String BTN_PRIMARY = "-fx-background-color: linear-gradient(to right, #8E24AA, #D500F9); -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 10; -fx-font-weight: bold; -fx-cursor: hand;";
    private final String BTN_SECONDARY = "-fx-background-color: transparent; -fx-border-color: white; -fx-border-radius: 20; -fx-text-fill: white; -fx-padding: 10; -fx-cursor: hand;";
    private final String TEXT_White = "-fx-text-fill: white; -fx-font-family: 'Segoe UI', sans-serif;";
    private final String LINK_STYLE = "-fx-text-fill: #CCCCCC; -fx-font-size: 11px; -fx-cursor: hand; -fx-underline: true;";

    public AuthScreen(Stage stage) {
        this.stage = stage;
    }

    public Scene getLoginScene() {
        return createSplitLayout(getLoginForm());
    }

    public Scene getRegisterScene() {
        return createSplitLayout(getRegisterForm());
    }

    private Scene createSplitLayout(VBox formPane) {
        HBox hbox = new HBox();
        hbox.setStyle("-fx-background-color: #1A1A1A;");

        // Left Pane: Image Background
        StackPane leftPane = new StackPane();
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        leftPane.setPrefWidth(405); // 45% of 900

        String imagePath = "file:///C:/Users/Asus/OneDrive/Desktop/project/java_project/src/eventmanagement/public/img1.jpg"; 
        Image image = new Image(imagePath);
        BackgroundImage bgImage = new BackgroundImage(
                image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true)
        );
        leftPane.setBackground(new Background(bgImage));

        // Right Pane: Form Area wrapped for centering
        StackPane rightPane = new StackPane(formPane);
        rightPane.setPrefWidth(495); // 55% of 900
        HBox.setHgrow(rightPane, Priority.ALWAYS);
        rightPane.setStyle(BG_COLOR);

        hbox.getChildren().addAll(leftPane, rightPane);

        Scene scene = new Scene(hbox, 900, 600);
        return scene;
    }

    private VBox getLoginForm() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setMaxWidth(350);
        root.setPadding(new Insets(40));
        root.setStyle(BG_COLOR);

        Label title = new Label("Welcome back...");
        title.setStyle(TEXT_White + " -fx-font-size: 28px; -fx-font-weight: bold;");
        VBox.setMargin(title, new Insets(0, 0, 20, 0));

        TextField emailField = new TextField();
        emailField.setPromptText("username");
        emailField.setStyle(FIELD_STYLE);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("*************");
        passwordField.setStyle(FIELD_STYLE);

        Label forgotPassword = new Label("forgot password");
        forgotPassword.setStyle(LINK_STYLE);
        
        Button loginBtn = new Button("Login");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(BTN_PRIMARY);

        // Logic
        loginBtn.setOnAction(e -> {
            User user = userDAO.loginUser(emailField.getText(), passwordField.getText());
            if (user != null) {
                SessionManager.getInstance().setCurrentUser(user);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Login Successful!");
                alert.setHeaderText(null);
                alert.showAndWait();
                
                if (user.getRole() == Role.ORGANIZATION) {
                    stage.setScene(new OrgDashboardScreen(stage).getScene());
                } else {
                    stage.setScene(new UserDashboardScreen(stage).getScene());
                }
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR, "Invalid Credentials");
                err.setHeaderText(null);
                err.show();
            }
        });

        // "OR" separator
        HBox orBox = new HBox(10);
        orBox.setAlignment(Pos.CENTER);
        Line leftLine = new Line(0, 0, 80, 0); leftLine.setStroke(Color.web("#555555"));
        Label orLabel = new Label("OR"); orLabel.setStyle(TEXT_White + " -fx-font-weight: bold;");
        Line rightLine = new Line(0, 0, 80, 0); rightLine.setStroke(Color.web("#555555"));
        orBox.getChildren().addAll(leftLine, orLabel, rightLine);
        VBox.setMargin(orBox, new Insets(10, 0, 10, 0));

        Button signupBtn = new Button("Sign up for an account");
        signupBtn.setMaxWidth(Double.MAX_VALUE);
        signupBtn.setStyle(BTN_SECONDARY);
        signupBtn.setOnAction(e -> stage.setScene(getRegisterScene()));

        Label footer = new Label("Developed by Prathmesh Darvesh (Roll No: ECSB441)");
        footer.setStyle("-fx-font-size: 10px; -fx-text-fill: #555555;");
        VBox.setMargin(footer, new Insets(40, 0, 0, 0));

        root.getChildren().addAll(title, emailField, passwordField, forgotPassword, loginBtn, orBox, signupBtn, footer);
        return root;
    }

    private VBox getRegisterForm() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setMaxWidth(350);
        root.setPadding(new Insets(40));
        root.setStyle(BG_COLOR);

        Label title = new Label("Create Account");
        title.setStyle(TEXT_White + " -fx-font-size: 28px; -fx-font-weight: bold;");
        VBox.setMargin(title, new Insets(0, 0, 10, 0));

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle(FIELD_STYLE);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle(FIELD_STYLE);
        
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm Password");
        confirmPassField.setStyle(FIELD_STYLE);

        // Role Combobox styled
        ComboBox<Role> roleBox = new ComboBox<>();
        roleBox.getItems().addAll(Role.values());
        roleBox.setValue(Role.PARTICIPANT);
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setStyle("-fx-background-color: #333333; -fx-background-radius: 20; -fx-padding: 5;");
        // Quick visual fix for combo box popup (often looks ugly out of the box in dark mode, but keeping simple)

        TextField field1 = new TextField();
        field1.setStyle(FIELD_STYLE);
        TextField field2 = new TextField();
        field2.setStyle(FIELD_STYLE);
        
        // Dynamic Fields toggle
        roleBox.setOnAction(e -> {
            if (roleBox.getValue() == Role.ORGANIZATION) {
                field1.setPromptText("Organization Name");
                field2.setVisible(false);
                field2.setManaged(false);
            } else {
                field1.setPromptText("First Name");
                field2.setVisible(true);
                field2.setManaged(true);
                field2.setPromptText("Last Name");
            }
        });
        roleBox.getOnAction().handle(null);

        Button registerBtn = new Button("Register");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle(BTN_PRIMARY);

        registerBtn.setOnAction(e -> {
            if (!passwordField.getText().equals(confirmPassField.getText())) {
                new Alert(Alert.AlertType.ERROR, "Passwords do not match").show();
                return;
            }
            if (emailField.getText().isEmpty() || passwordField.getText().isEmpty() || field1.getText().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Please fill all fields").show();
                return;
            }

            Role role = roleBox.getValue();
            boolean success = false;
            
            // Logic Constraint: Handled precisely by injecting specifically requested user role
            if (role == Role.ORGANIZATION) {
                success = userDAO.registerOrganization(emailField.getText(), passwordField.getText(), field1.getText());
            } else if (role == Role.PARTICIPANT) {
                success = userDAO.registerParticipant(emailField.getText(), passwordField.getText(), field1.getText(), field2.getText());
            } else if (role == Role.VOLUNTEER) {
                success = userDAO.registerVolunteer(emailField.getText(), passwordField.getText(), field1.getText(), field2.getText());
            }

            if (success) {
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Registration Successful! You can now login.");
                a.setHeaderText(null);
                a.showAndWait();
                stage.setScene(getLoginScene());
            } else {
                new Alert(Alert.AlertType.ERROR, "Registration Failed. Email might exist.").show();
            }
        });

        // "OR" separator
        HBox orBox = new HBox(10);
        orBox.setAlignment(Pos.CENTER);
        Line leftLine = new Line(0, 0, 80, 0); leftLine.setStroke(Color.web("#555555"));
        Label orLabel = new Label("OR"); orLabel.setStyle(TEXT_White + " -fx-font-weight: bold;");
        Line rightLine = new Line(0, 0, 80, 0); rightLine.setStroke(Color.web("#555555"));
        orBox.getChildren().addAll(leftLine, orLabel, rightLine);
        VBox.setMargin(orBox, new Insets(10, 0, 10, 0));

        Button loginBtn = new Button("Already have an account? Login here");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(BTN_SECONDARY);
        loginBtn.setOnAction(e -> stage.setScene(getLoginScene()));
        
        Label footer = new Label("Developed by Prathmesh Darvesh (Roll No: ECSB441)");
        footer.setStyle("-fx-font-size: 10px; -fx-text-fill: #555555;");
        VBox.setMargin(footer, new Insets(10, 0, 0, 0));

        root.getChildren().addAll(
            title, emailField, passwordField, confirmPassField, roleBox, field1, field2, 
            registerBtn, orBox, loginBtn, footer
        );
        return root;
    }
}
