package eventmanagement.view;

import eventmanagement.dao.EventDAO;
import eventmanagement.model.Event;
import eventmanagement.model.EventType;
import eventmanagement.model.User;
import eventmanagement.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;

public class OrgDashboardScreen {
    private Stage stage;
    private EventDAO eventDAO = new EventDAO();
    private FlowPane eventsContainer;
    private BorderPane root;
    private String activeView = "my_events";

    // ---- Design System ----
    private static final String BG      = "-fx-background-color: #12121A;";
    private static final String SIDEBAR  = "-fx-background-color: #1C1C28;";
    private static final String CARD     = "-fx-background-color: #1E1E2E; -fx-background-radius: 18; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 14, 0, 0, 6);";
    private static final String FIELD    = "-fx-background-color: #2A2A3D; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 12 15; -fx-prompt-text-fill: #666680;";
    private static final String BTN_PRI  = "-fx-background-color: linear-gradient(to right, #8E24AA, #D500F9); -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 10 16; -fx-font-weight: bold; -fx-cursor: hand;";
    private static final String BTN_GRN  = "-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 10 16; -fx-font-weight: bold; -fx-cursor: hand;";
    private static final String BTN_RED  = "-fx-background-color: transparent; -fx-border-color: #E53935; -fx-border-radius: 12; -fx-text-fill: #E53935; -fx-padding: 8 14; -fx-cursor: hand;";
    private static final String TXT_W    = "-fx-text-fill: white; -fx-font-family: 'Segoe UI', sans-serif;";

    public OrgDashboardScreen(Stage stage) { this.stage = stage; }

    public Scene getScene() {
        root = new BorderPane();
        root.setStyle(BG);
        root.setLeft(buildSidebar());
        showMyEventsView();
        return new Scene(root, 1100, 700);
    }

    // ─────────────── SIDEBAR ───────────────
    private VBox buildSidebar() {
        VBox sb = new VBox(6);
        sb.setPadding(new Insets(30, 14, 30, 14));
        sb.setStyle(SIDEBAR);
        sb.setPrefWidth(230);

        Label logo = new Label("🎪 EventHub");
        logo.setStyle(TXT_W + " -fx-font-size: 20px; -fx-font-weight: bold;");
        VBox.setMargin(logo, new Insets(0, 0, 20, 6));

        Button myEventsBtn  = sidebarBtn("📋  My Events",  "my_events");
        Button createBtn    = sidebarBtn("➕  Create Event","create");
        Button draftsBtn    = sidebarBtn("📝  Drafts",     "drafts");

        myEventsBtn .setOnAction(e -> { activeView="my_events"; root.setLeft(buildSidebar()); showMyEventsView(); });
        createBtn   .setOnAction(e -> { showCreateEventDialog(); });
        draftsBtn   .setOnAction(e -> { activeView="drafts";    root.setLeft(buildSidebar()); showDraftsView(); });

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logout = new Button("⏻  Logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setStyle("-fx-background-color: #2A1A1A; -fx-text-fill: #FF5555; -fx-background-radius: 12; -fx-padding: 10; -fx-cursor: hand; -fx-font-weight: bold;");
        logout.setOnAction(e -> { SessionManager.getInstance().logout(); stage.setScene(new AuthScreen(stage).getLoginScene()); });

        sb.getChildren().addAll(logo, myEventsBtn, createBtn, draftsBtn, spacer, logout);
        return sb;
    }

    private Button sidebarBtn(String text, String viewKey) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        if (activeView.equals(viewKey)) {
            btn.setStyle("-fx-background-color: linear-gradient(to right, #8E24AA, #D500F9); -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand; -fx-font-weight: bold;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAACC; -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #2A2A3D; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand;"));
            btn.setOnMouseExited (e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAACC; -fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand;"));
        }
        return btn;
    }

    // ─────────────── VIEWS ───────────────
    private VBox contentWrapper(String title, String subtitle) {
        VBox center = new VBox(20);
        center.setPadding(new Insets(40));
        center.setStyle(BG);

        Label t = new Label(title);
        t.setStyle(TXT_W + " -fx-font-size: 26px; -fx-font-weight: bold;");
        Label s = new Label(subtitle);
        s.setStyle("-fx-text-fill: #666680; -fx-font-size: 14px;");
        center.getChildren().addAll(t, s);
        return center;
    }

    private void showMyEventsView() {
        VBox center = contentWrapper("My Published Events", "Manage your live events — attendees can browse these.");
        eventsContainer = new FlowPane(20, 20);
        eventsContainer.setStyle("-fx-background-color: transparent;");
        ScrollPane sp = scrollPane(eventsContainer);
        refreshCards("PUBLISHED");
        center.getChildren().add(sp);
        root.setCenter(center);
    }

    private void showDraftsView() {
        VBox center = contentWrapper("Draft Events", "Events saved as drafts — only you can see these.");
        eventsContainer = new FlowPane(20, 20);
        eventsContainer.setStyle("-fx-background-color: transparent;");
        ScrollPane sp = scrollPane(eventsContainer);
        refreshCards("DRAFT");
        center.getChildren().add(sp);
        root.setCenter(center);
    }

    private ScrollPane scrollPane(FlowPane fp) {
        ScrollPane sp = new ScrollPane(fp);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: #12121A; -fx-border-color: #12121A;");
        VBox.setVgrow(sp, Priority.ALWAYS);
        return sp;
    }

    // ─────────────── EVENT CARDS ───────────────
    private void refreshCards(String statusFilter) {
        eventsContainer.getChildren().clear();
        User u = SessionManager.getInstance().getCurrentUser();
        List<Event> events = eventDAO.getEventsByOrgId(u.getUserId());

        boolean any = false;
        for (Event event : events) {
            if (!event.getStatus().equalsIgnoreCase(statusFilter)) continue;
            any = true;
            eventsContainer.getChildren().add(buildCard(event));
        }
        if (!any) {
            Label empty = new Label(statusFilter.equals("PUBLISHED") ? "No published events. Create one!" : "No drafts saved.");
            empty.setStyle("-fx-text-fill: #505070; -fx-font-size: 16px;");
            eventsContainer.getChildren().add(empty);
        }
    }

    private VBox buildCard(Event event) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(CARD);
        card.setPrefWidth(260);

        // Type pill
        Label typePill = new Label(" " + event.getEventType().toString() + " ");
        typePill.setStyle("-fx-background-color: #2D1B4E; -fx-text-fill: #D500F9; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label titleL = new Label(event.getTitle());
        titleL.setStyle(TXT_W + " -fx-font-size: 18px; -fx-font-weight: bold; -fx-wrap-text: true;");
        titleL.setMaxWidth(220);

        Label dateL = new Label("📅  " + event.getEventDate().toLocalDate());
        dateL.setStyle("-fx-text-fill: #AAAACC; -fx-font-size: 12px;");

        Label locL = new Label("📍  " + event.getLocation());
        locL.setStyle("-fx-text-fill: #AAAACC; -fx-font-size: 12px;");

        int rsvp = eventDAO.getRSVPCount(event.getEventId());
        int cap = event.getMaxCapacity();
        Label capL = new Label("👥  " + rsvp + " / " + cap + " spots");
        String capColor = rsvp >= cap ? "#E53935" : "#4CAF50";
        capL.setStyle("-fx-text-fill: " + capColor + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            Label desc = new Label(event.getDescription().length() > 80 ? event.getDescription().substring(0, 77) + "…" : event.getDescription());
            desc.setStyle("-fx-text-fill: #8888AA; -fx-font-size: 12px; -fx-wrap-text: true;");
            desc.setMaxWidth(220);
            card.getChildren().addAll(typePill, titleL, dateL, locL, capL, desc);
        } else {
            card.getChildren().addAll(typePill, titleL, dateL, locL, capL);
        }

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        card.getChildren().add(spacer);

        HBox actions = new HBox(8);
        if ("DRAFT".equalsIgnoreCase(event.getStatus())) {
            Button publishBtn = new Button("🚀 Publish");
            publishBtn.setStyle(BTN_GRN);
            publishBtn.setOnAction(e -> { eventDAO.updateEventStatus(event.getEventId(), "PUBLISHED"); showMyEventsView(); activeView = "my_events"; root.setLeft(buildSidebar()); });
            actions.getChildren().add(publishBtn);
        } else {
            Button manageBtn = new Button("👥 Attendees");
            manageBtn.setStyle(BTN_PRI);
            manageBtn.setOnAction(e -> showManageAttendeesDialog(event));
            actions.getChildren().add(manageBtn);
        }

        Button delBtn = new Button("🗑 Delete");
        delBtn.setStyle(BTN_RED);
        delBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete '" + event.getTitle() + "'?");
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) { eventDAO.deleteEvent(event.getEventId()); refreshCards(event.getStatus()); }
            });
        });
        actions.getChildren().add(delBtn);
        card.getChildren().add(actions);
        return card;
    }

    // ─────────────── MANAGE ATTENDEES ───────────────
    private void showManageAttendeesDialog(Event event) {
        Stage dialog = new Stage();
        dialog.setTitle("Attendees — " + event.getTitle());
        VBox box = new VBox(20);
        box.setPadding(new Insets(30));
        box.setStyle(BG);

        Label header = new Label("Attendees for " + event.getTitle());
        header.setStyle(TXT_W + " -fx-font-size: 22px; -fx-font-weight: bold;");

        VBox contentBox = new VBox(14);
        contentBox.setStyle(BG);
        populateAttendees(contentBox, event);

        ScrollPane sp = new ScrollPane(contentBox);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: #12121A; -fx-border-color: #12121A;");
        VBox.setVgrow(sp, Priority.ALWAYS);

        box.getChildren().addAll(header, sp);
        dialog.setScene(new Scene(box, 520, 640));
        dialog.show();
    }

    private void populateAttendees(VBox box, Event event) {
        box.getChildren().clear();
        List<EventDAO.EventRegistration> regs = eventDAO.getRegistrationsForEvent(event.getEventId());
        if (regs.isEmpty()) {
            box.getChildren().add(noDataLabel("No attendees yet."));
            return;
        }
        for (EventDAO.EventRegistration reg : regs) {
            VBox card = new VBox(10);
            card.setStyle(CARD + " -fx-padding: 18;");

            String status = reg.status;
            String statusColor = switch (status) {
                case "ACCEPTED" -> "#4CAF50";
                case "PENDING"  -> "#FFC107";
                case "BLOCKED"  -> "#E53935";
                default         -> "#AAAACC";
            };
            Label pill = new Label("  " + reg.role.toUpperCase() + "  ·  " + status + "  ");
            pill.setStyle("-fx-background-color: transparent; -fx-border-color: " + statusColor + "; -fx-border-radius: 8; -fx-text-fill: " + statusColor + "; -fx-font-size: 11px; -fx-font-weight: bold;");

            Label email = new Label(reg.email);
            email.setStyle(TXT_W + " -fx-font-size: 15px; -fx-font-weight: bold;");

            card.getChildren().addAll(pill, email);

            if (reg.applicationText != null && !reg.applicationText.isBlank()) {
                Label msg = new Label("\"" + reg.applicationText + "\"");
                msg.setStyle("-fx-text-fill: #999999; -fx-font-size: 13px; -fx-wrap-text: true; -fx-font-style: italic;");
                msg.setMaxWidth(440);
                card.getChildren().add(msg);
            }

            HBox actions = new HBox(10);
            if ("PENDING".equals(status)) {
                Button acc = actionBtn("✔ Accept", "#4CAF50");
                Button rej = actionBtn("✘ Reject", "#E53935");
                acc.setOnAction(e -> { eventDAO.updateRegistrationStatus(reg.registrationId, "ACCEPTED"); populateAttendees(box, event); });
                rej.setOnAction(e -> { eventDAO.updateRegistrationStatus(reg.registrationId, "REJECTED"); populateAttendees(box, event); });
                actions.getChildren().addAll(acc, rej);
            } else if (!"BLOCKED".equals(status)) {
                Button blk = actionBtn("⛔ Block", "#E53935");
                blk.setOnAction(e -> { eventDAO.updateRegistrationStatus(reg.registrationId, "BLOCKED"); populateAttendees(box, event); });
                actions.getChildren().add(blk);
            }
            card.getChildren().add(actions);
            box.getChildren().add(card);
        }
    }

    // ─────────────── CREATE EVENT ───────────────
    private void showCreateEventDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Create New Event");
        VBox box = new VBox(14);
        box.setPadding(new Insets(35));
        box.setStyle(BG);

        Label header = new Label("New Event");
        header.setStyle(TXT_W + " -fx-font-size: 24px; -fx-font-weight: bold;");

        TextField titleF = field("Event Title");
        TextField locF   = field("Location");
        TextField capF   = field("Max Capacity (e.g. 100)");
        capF.setText("100");

        DatePicker dp = new DatePicker();
        dp.setPromptText("Event Date");
        dp.setMaxWidth(Double.MAX_VALUE);
        dp.setStyle("-fx-control-inner-background: #2A2A3D; -fx-text-inner-color: white;");

        ComboBox<EventType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(EventType.values());
        typeBox.setValue(EventType.CONCERT);
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.setStyle("-fx-background-color: #2A2A3D; -fx-background-radius: 12; -fx-padding: 4;");

        TextArea desc = new TextArea();
        desc.setPromptText("Event description (what to expect, highlights, details…)");
        desc.setWrapText(true);
        desc.setPrefRowCount(4);
        desc.setStyle("-fx-control-inner-background: #2A2A3D; -fx-text-inner-color: white; -fx-background-radius: 12;");

        HBox btnRow = new HBox(12);
        Button saveBtn    = new Button("💾  Save as Draft");
        Button publishBtn = new Button("🚀  Publish Now");
        saveBtn.setStyle("-fx-background-color: #2A2A3D; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 10 16; -fx-cursor: hand; -fx-font-weight: bold;");
        publishBtn.setStyle(BTN_PRI);
        btnRow.getChildren().addAll(saveBtn, publishBtn);

        saveBtn.setOnAction(e    -> handleCreate(dialog, titleF, locF, capF, dp, typeBox, desc, "DRAFT"));
        publishBtn.setOnAction(e -> handleCreate(dialog, titleF, locF, capF, dp, typeBox, desc, "PUBLISHED"));

        box.getChildren().addAll(header, titleF, locF, capF, dp, typeBox, desc, btnRow);
        ScrollPane sp = new ScrollPane(box);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: #12121A; -fx-background: #12121A; -fx-border-color: #12121A;");
        dialog.setScene(new Scene(sp, 440, 580));
        dialog.show();
    }

    private void handleCreate(Stage dialog, TextField titleF, TextField locF, TextField capF,
                              DatePicker dp, ComboBox<EventType> typeBox, TextArea desc, String status) {
        if (dp.getValue() == null || titleF.getText().isBlank() || locF.getText().isBlank()) {
            new Alert(Alert.AlertType.ERROR, "Please fill Title, Location, and Date.").show();
            return;
        }
        int cap = 100;
        try { cap = Integer.parseInt(capF.getText().trim()); } catch (NumberFormatException ignored) {}

        User u = SessionManager.getInstance().getCurrentUser();
        LocalDateTime dt = dp.getValue().atStartOfDay();
        boolean ok = eventDAO.createEvent(u.getUserId(), titleF.getText(), dt, locF.getText(), typeBox.getValue(), desc.getText(), cap, status);
        if (ok) {
            dialog.close();
            if ("DRAFT".equals(status)) { activeView = "drafts"; root.setLeft(buildSidebar()); showDraftsView(); }
            else                         { activeView = "my_events"; root.setLeft(buildSidebar()); showMyEventsView(); }
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to save event.").show();
        }
    }

    // ─────────────── HELPERS ───────────────
    private TextField field(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt); tf.setStyle(FIELD); return tf;
    }
    private Button actionBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 8 14;");
        return b;
    }
    private Label noDataLabel(String text) {
        Label l = new Label(text); l.setStyle("-fx-text-fill: #505070; -fx-font-size: 16px;"); return l;
    }
}
