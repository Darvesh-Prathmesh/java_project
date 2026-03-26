package eventmanagement.view;

import eventmanagement.dao.UserDAO;
import eventmanagement.model.Role;
import eventmanagement.model.User;
import eventmanagement.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class AuthScreen {
    private Stage stage;
    private UserDAO userDAO = new UserDAO();

    // ── Design Tokens ──────────────────────────────────────────────
    // Backgrounds
    private static final String APP_BG   = "#F0F4F8";   // ice-blue page bg
    private static final String WHITE    = "#FFFFFF";

    // Blues
    private static final String BLUE     = "#2563EB";
    private static final String BLUE_DRK = "#1D4ED8";
    private static final String GRAD_LFT = "linear-gradient(to bottom right, #4facfe 0%, #00f2fe 100%)";

    // Text
    private static final String TEXT_PRI = "#1E293B";
    private static final String TEXT_SEC = "#64748B";
    private static final String TEXT_HIN = "#94A3B8";

    // Field / Border
    private static final String FIELD_BG  = "#FFFFFF";
    private static final String BORDER    = "#E2E8F0";

    // ── Inline style constants ─────────────────────────────────────
    private final String FIELD_STYLE =
        "-fx-background-color: " + FIELD_BG + ";" +
        "-fx-border-color: " + BORDER + ";" +
        "-fx-border-radius: 10;" +
        "-fx-background-radius: 10;" +
        "-fx-padding: 12 16;" +
        "-fx-font-size: 14px;" +
        "-fx-text-fill: " + TEXT_PRI + ";" +
        "-fx-prompt-text-fill: " + TEXT_HIN + ";";

    private final String BTN_PRIMARY =
        "-fx-background-color: " + BLUE + ";" +
        "-fx-text-fill: white;" +
        "-fx-font-weight: bold;" +
        "-fx-font-size: 14px;" +
        "-fx-background-radius: 20;" +
        "-fx-padding: 12 0;" +
        "-fx-cursor: hand;";

    private final String BTN_OUTLINE =
        "-fx-background-color: transparent;" +
        "-fx-border-color: " + BLUE + ";" +
        "-fx-border-radius: 20;" +
        "-fx-text-fill: " + BLUE + ";" +
        "-fx-font-weight: bold;" +
        "-fx-font-size: 14px;" +
        "-fx-padding: 12 0;" +
        "-fx-cursor: hand;";

    public AuthScreen(Stage stage) { this.stage = stage; }

    public Scene getLoginScene()   { return buildLayout(buildLoginForm()); }
    public Scene getRegisterScene(){ return buildLayout(buildRegisterForm()); }

    // ── Shell ──────────────────────────────────────────────────────
    private Scene buildLayout(VBox formPane) {
        HBox root = new HBox();
        root.setStyle("-fx-background-color: " + APP_BG + ";");

        // ── Left decorative pane ──────────────────────────────
        VBox left = new VBox(20);
        left.setAlignment(Pos.CENTER);
        left.setPadding(new Insets(60));
        left.setPrefWidth(420);
        left.setMinWidth(320);
        left.setStyle("-fx-background-color: " + GRAD_LFT + ";");

        // Big circle decoration
        Circle bigCircle = new Circle(90);
        bigCircle.setFill(Color.web("#FFFFFF", 0.12));

        Circle medCircle = new Circle(55);
        medCircle.setFill(Color.web("#FFFFFF", 0.10));

        StackPane circles = new StackPane(bigCircle, medCircle);

        Label brand = new Label("EventHub");
        brand.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");

        Label tagline = new Label("Manage, Attend & Volunteer\nfor events that matter.");
        tagline.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 15px; -fx-text-alignment: center; -fx-alignment: center;");
        tagline.setWrapText(true);

        Label footer = new Label("Developed by Prathmesh Darvesh (Roll No: ECSB441)");
        footer.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 11px; -fx-text-alignment: center; -fx-alignment: center;");
        footer.setWrapText(true);
        VBox.setVgrow(footer, Priority.ALWAYS);
        footer.setAlignment(Pos.BOTTOM_CENTER);
        VBox.setMargin(footer, new Insets(40, 0, 0, 0));

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        left.getChildren().addAll(circles, brand, tagline, spacer, footer);

        // ── Right form pane ───────────────────────────────────
        StackPane right = new StackPane(formPane);
        right.setStyle("-fx-background-color: " + WHITE + ";");
        HBox.setHgrow(right, Priority.ALWAYS);

        root.getChildren().addAll(left, right);
        return new Scene(root, 960, 620);
    }

    // ── LOGIN ──────────────────────────────────────────────────────
    private VBox buildLoginForm() {
        VBox form = new VBox(18);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setMaxWidth(360);
        form.setPadding(new Insets(50, 40, 40, 40));

        Label welcome = new Label("Welcome back");
        welcome.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 30px; -fx-font-weight: bold;");

        Label sub = new Label("Sign in to manage your events");
        sub.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 14px;");
        VBox.setMargin(sub, new Insets(0, 0, 10, 0));

        TextField emailField = styledField("Email address");
        PasswordField passField  = styledPass("Password");

        Button loginBtn = new Button("Sign In");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(BTN_PRIMARY);
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(BTN_PRIMARY.replace(BLUE, BLUE_DRK)));
        loginBtn.setOnMouseExited (e -> loginBtn.setStyle(BTN_PRIMARY));

        loginBtn.setOnAction(e -> {
            User user = userDAO.loginUser(emailField.getText(), passField.getText());
            if (user != null) {
                SessionManager.getInstance().setCurrentUser(user);
                if (user.getRole() == Role.ORGANIZATION)
                    stage.setScene(new OrgDashboardScreen(stage).getScene());
                else
                    stage.setScene(new UserDashboardScreen(stage).getScene());
            } else {
                showError("Invalid email or password.");
            }
        });

        HBox divider = orDivider();

        Button signupBtn = new Button("Create an account");
        signupBtn.setMaxWidth(Double.MAX_VALUE);
        signupBtn.setStyle(BTN_OUTLINE);
        signupBtn.setOnMouseEntered(e -> signupBtn.setStyle(BTN_OUTLINE + "-fx-background-color: #EFF6FF;"));
        signupBtn.setOnMouseExited (e -> signupBtn.setStyle(BTN_OUTLINE));
        signupBtn.setOnAction(e -> stage.setScene(getRegisterScene()));

        form.getChildren().addAll(welcome, sub, emailField, passField, loginBtn, divider, signupBtn);
        return form;
    }

    // ── REGISTER ───────────────────────────────────────────────────
    private VBox buildRegisterForm() {
        VBox form = new VBox(14);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setMaxWidth(380);
        form.setPadding(new Insets(40, 40, 40, 40));

        Label title = new Label("Create account");
        title.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label sub = new Label("Join the community today");
        sub.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 14px;");
        VBox.setMargin(sub, new Insets(0, 0, 6, 0));

        TextField emailF   = styledField("Email address");
        PasswordField passF  = styledPass("Password");
        PasswordField confF  = styledPass("Confirm password");

        ComboBox<Role> roleBox = new ComboBox<>();
        roleBox.getItems().addAll(Role.values());
        roleBox.setValue(Role.PARTICIPANT);
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setStyle(
            "-fx-background-color: " + FIELD_BG + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 6 10;" +
            "-fx-font-size: 14px;");

        TextField field1 = styledField("First Name");
        TextField field2 = styledField("Last Name");

        roleBox.setOnAction(e -> {
            if (roleBox.getValue() == Role.ORGANIZATION) {
                field1.setPromptText("Organization Name");
                field2.setVisible(false); field2.setManaged(false);
            } else {
                field1.setPromptText("First Name");
                field2.setVisible(true);  field2.setManaged(true);
                field2.setPromptText("Last Name");
            }
        });
        roleBox.getOnAction().handle(null);

        Button registerBtn = new Button("Create Account");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle(BTN_PRIMARY);
        registerBtn.setOnMouseEntered(e -> registerBtn.setStyle(BTN_PRIMARY.replace(BLUE, BLUE_DRK)));
        registerBtn.setOnMouseExited (e -> registerBtn.setStyle(BTN_PRIMARY));

        registerBtn.setOnAction(e -> {
            if (!passF.getText().equals(confF.getText())) { showError("Passwords do not match."); return; }
            if (emailF.getText().isBlank() || passF.getText().isBlank() || field1.getText().isBlank()) { showError("Please fill all required fields."); return; }

            Role role = roleBox.getValue();
            boolean ok = switch (role) {
                case ORGANIZATION -> userDAO.registerOrganization(emailF.getText(), passF.getText(), field1.getText());
                case PARTICIPANT  -> userDAO.registerParticipant(emailF.getText(),  passF.getText(), field1.getText(), field2.getText());
                case VOLUNTEER    -> userDAO.registerVolunteer(emailF.getText(),    passF.getText(), field1.getText(), field2.getText());
            };
            if (ok) {
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Registration successful! Please sign in.");
                a.setHeaderText(null); a.showAndWait();
                stage.setScene(getLoginScene());
            } else showError("Registration failed. Email may already exist.");
        });

        Button loginLink = new Button("Already have an account? Sign in");
        loginLink.setMaxWidth(Double.MAX_VALUE);
        loginLink.setStyle(BTN_OUTLINE);
        loginLink.setOnMouseEntered(e -> loginLink.setStyle(BTN_OUTLINE + "-fx-background-color: #EFF6FF;"));
        loginLink.setOnMouseExited (e -> loginLink.setStyle(BTN_OUTLINE));
        loginLink.setOnAction(e -> stage.setScene(getLoginScene()));

        form.getChildren().addAll(title, sub, emailF, passF, confF, roleBox, field1, field2, registerBtn, loginLink);
        return form;
    }

    // ── Helpers ────────────────────────────────────────────────────
    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(FIELD_STYLE);
        return tf;
    }

    private PasswordField styledPass(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setStyle(FIELD_STYLE);
        return pf;
    }

    private HBox orDivider() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);
        VBox.setMargin(row, new Insets(4, 0, 4, 0));
        Line l1 = new Line(0,0,100,0); l1.setStroke(Color.web(BORDER));
        Line l2 = new Line(0,0,100,0); l2.setStroke(Color.web(BORDER));
        Label or = new Label("or"); or.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 13px;");
        row.getChildren().addAll(l1, or, l2);
        return row;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg);
        a.setHeaderText(null); a.show();
    }
}
