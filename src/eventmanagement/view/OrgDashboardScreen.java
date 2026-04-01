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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;

public class OrgDashboardScreen {
    private Stage stage;
    private EventDAO eventDAO = new EventDAO();
    private FlowPane eventsContainer;
    private BorderPane root;
    private String activeView = "published";

    // ── Design Tokens ──────────────────────────────────────────────
    private static final String APP_BG    = "#F0F4F8";
    private static final String WHITE     = "#FFFFFF";
    private static final String BLUE      = "#2563EB";
    private static final String BLUE_DRK  = "#1D4ED8";
    private static final String BLUE_LITE = "#EFF6FF";
    private static final String SIDEBAR_GRAD = "linear-gradient(to bottom, #4facfe 0%, #00f2fe 100%)";
    private static final String TEXT_PRI  = "#1E293B";
    private static final String TEXT_SEC  = "#64748B";
    private static final String BORDER    = "#E2E8F0";
    private static final String CARD_SHADOW = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 12, 0, 0, 4);";
    private static final String GREEN     = "#16A34A";
    private static final String RED_SOFT  = "#DC2626";

    private final String BTN_PRIMARY =
        "-fx-background-color: " + BLUE + "; -fx-text-fill: white; -fx-font-weight: bold; " +
        "-fx-font-size: 13px; -fx-background-radius: 20; -fx-padding: 9 18; -fx-cursor: hand;";
    private final String BTN_DANGER  =
        "-fx-background-color: transparent; -fx-border-color: " + RED_SOFT + "; -fx-border-radius: 20; " +
        "-fx-text-fill: " + RED_SOFT + "; -fx-font-size: 13px; -fx-padding: 8 16; -fx-cursor: hand;";
    private final String BTN_GREEN   =
        "-fx-background-color: " + GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; " +
        "-fx-font-size: 13px; -fx-background-radius: 20; -fx-padding: 9 18; -fx-cursor: hand;";
    private final String FIELD_STYLE =
        "-fx-background-color: " + WHITE + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 10; " +
        "-fx-background-radius: 10; -fx-padding: 11 14; -fx-font-size: 13px; -fx-text-fill: " + TEXT_PRI + "; " +
        "-fx-prompt-text-fill: #94A3B8;";

    public OrgDashboardScreen(Stage stage) { this.stage = stage; }

    // ── Scene ──────────────────────────────────────────────────────
    public Scene getScene() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + APP_BG + ";");
        root.setLeft(buildSidebar());
        showPublishedView();
        return new Scene(root, 1120, 720);
    }

    // ── Sidebar ────────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sb = new VBox(4);
        sb.setPadding(new Insets(36, 18, 36, 18));
        sb.setStyle("-fx-background-color: " + SIDEBAR_GRAD + ";");
        sb.setPrefWidth(240);

        Label brand = new Label("EventHub");
        brand.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        VBox.setMargin(brand, new Insets(0, 0, 28, 4));

        Label orgLabel = new Label("ORGANIZATION");
        orgLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.65); -fx-font-size: 10px; -fx-font-weight: bold;");
        VBox.setMargin(orgLabel, new Insets(0, 0, 6, 8));

        Button myEventsBtn = navBtn("My Events",    "published");
        Button draftsBtn   = navBtn("Drafts",       "drafts");
        Button createBtn   = navBtn("+ Create Event","create");

        myEventsBtn.setOnAction(e -> { activeView = "published"; root.setLeft(buildSidebar()); showPublishedView(); });
        draftsBtn  .setOnAction(e -> { activeView = "drafts";    root.setLeft(buildSidebar()); showDraftsView(); });
        createBtn  .setOnAction(e -> showCreateDialog());

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);

        Label footer = new Label("Developed by Prathmesh Darvesh\nRoll No: ECSB441");
        footer.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 10px; -fx-text-alignment: center;");
        footer.setWrapText(true);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle("-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white; " +
            "-fx-background-radius: 20; -fx-font-weight: bold; -fx-padding: 10; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> { SessionManager.getInstance().logout(); stage.setScene(new AuthScreen(stage).getLoginScene()); });

        sb.getChildren().addAll(brand, orgLabel, myEventsBtn, draftsBtn, createBtn, spacer, footer, new Label(""), logoutBtn);
        return sb;
    }

    private Button navBtn(String text, String viewKey) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        if (activeView.equals(viewKey)) {
            btn.setStyle("-fx-background-color: rgba(255,255,255,0.28); -fx-text-fill: white; " +
                "-fx-background-radius: 12; -fx-font-weight: bold; -fx-padding: 12 16; -fx-cursor: hand;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); " +
                "-fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand;");
            btn.setOnMouseEntered(ev -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; " +
                "-fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand;"));
            btn.setOnMouseExited(ev -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.8); " +
                "-fx-background-radius: 12; -fx-padding: 12 16; -fx-cursor: hand;"));
        }
        return btn;
    }

    // ── Views ──────────────────────────────────────────────────────
    private void showPublishedView() {
        VBox c = contentPane("My Events", "Your live published events. Attendees can browse and register.");
        eventsContainer = flowPane();
        refreshCards("PUBLISHED");
        c.getChildren().add(scrollWrap(eventsContainer));
        root.setCenter(c);
    }

    private void showDraftsView() {
        VBox c = contentPane("Drafts", "Events saved as drafts. Publish when ready.");
        eventsContainer = flowPane();
        refreshCards("DRAFT");
        c.getChildren().add(scrollWrap(eventsContainer));
        root.setCenter(c);
    }

    private void refreshCards(String statusFilter) {
        eventsContainer.getChildren().clear();
        User u = SessionManager.getInstance().getCurrentUser();
        List<Event> events = eventDAO.getEventsByOrgId(u.getUserId());
        boolean any = false;
        for (Event ev : events) {
            if (!ev.getStatus().equalsIgnoreCase(statusFilter)) continue;
            any = true;
            eventsContainer.getChildren().add(buildCard(ev));
        }
        if (!any) {
            Label empty = new Label(statusFilter.equals("PUBLISHED")
                ? "No published events yet. Create one to get started."
                : "No drafts saved.");
            empty.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 15px;");
            eventsContainer.getChildren().add(empty);
        }
    }

    private VBox buildCard(Event ev) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(22));
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 14; " + CARD_SHADOW);
        card.setPrefWidth(270);

        // Type tag
        Label tag = new Label(ev.getEventType().toString());
        tag.setStyle("-fx-background-color: " + BLUE_LITE + "; -fx-text-fill: " + BLUE + "; " +
            "-fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10;");

        Label title = new Label(ev.getTitle());
        title.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 18px; -fx-font-weight: bold; -fx-wrap-text: true;");
        title.setMaxWidth(230);

        Label date = new Label("Date:  " + ev.getEventDate().toLocalDate());
        date.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 12px;");

        Label loc = new Label("Venue:  " + ev.getLocation());
        loc.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 12px;");

        int rsvp = eventDAO.getRSVPCount(ev.getEventId());
        int cap  = ev.getMaxCapacity();
        String capColor = rsvp >= cap ? RED_SOFT : GREEN;
        Label capLabel = new Label("Attendees:  " + rsvp + " / " + cap);
        capLabel.setStyle("-fx-text-fill: " + capColor + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Progress bar
        ProgressBar prog = new ProgressBar((double) rsvp / Math.max(cap, 1));
        prog.setMaxWidth(Double.MAX_VALUE);
        prog.setStyle("-fx-accent: " + (rsvp >= cap ? RED_SOFT : BLUE) + "; -fx-background-radius: 4; -fx-background-color: " + BORDER + ";");
        prog.setPrefHeight(6);

        if (ev.getDescription() != null && !ev.getDescription().isBlank()) {
            Label desc = new Label(ev.getDescription().length() > 80
                ? ev.getDescription().substring(0, 77) + "..." : ev.getDescription());
            desc.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 12px; -fx-wrap-text: true;");
            desc.setMaxWidth(230);
            card.getChildren().addAll(tag, title, date, loc, capLabel, prog, desc);
        } else {
            card.getChildren().addAll(tag, title, date, loc, capLabel, prog);
        }

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        card.getChildren().add(spacer);

        HBox actions = new HBox(10);
        if ("DRAFT".equalsIgnoreCase(ev.getStatus())) {
            Button pub = new Button("Publish");
            pub.setStyle(BTN_GREEN);
            pub.setOnAction(e -> { eventDAO.updateEventStatus(ev.getEventId(), "PUBLISHED"); activeView = "published"; root.setLeft(buildSidebar()); showPublishedView(); });
            actions.getChildren().add(pub);
        } else {
            Button manage = new Button("Attendees");
            manage.setStyle(BTN_PRIMARY);
            manage.setOnAction(e -> showAttendeesDialog(ev));
            actions.getChildren().add(manage);
        }
        Button del = new Button("Delete");
        del.setStyle(BTN_DANGER);
        del.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete '" + ev.getTitle() + "'?");
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(r -> { if (r == ButtonType.OK) { eventDAO.deleteEvent(ev.getEventId()); refreshCards(ev.getStatus()); }});
        });
        actions.getChildren().add(del);
        card.getChildren().add(actions);
        return card;
    }

    // ── Attendees Modal ────────────────────────────────────────────
    private void showAttendeesDialog(Event ev) {
        Stage d = new Stage(); d.setTitle("Attendees — " + ev.getTitle());
        VBox box = new VBox(20); box.setPadding(new Insets(32)); box.setStyle("-fx-background-color: " + APP_BG + ";");
        Label h = new Label("Attendees for " + ev.getTitle());
        h.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 22px; -fx-font-weight: bold;");
        VBox content = new VBox(14); content.setStyle("-fx-background-color: " + APP_BG + ";");
        refreshAttendees(content, ev);
        ScrollPane sp = whiteScrollPane(content);
        VBox.setVgrow(sp, Priority.ALWAYS);
        box.getChildren().addAll(h, sp);
        d.setScene(new Scene(box, 540, 650)); d.show();
    }

    private void refreshAttendees(VBox box, Event ev) {
        box.getChildren().clear();
        List<EventDAO.EventRegistration> regs = eventDAO.getRegistrationsForEvent(ev.getEventId());
        if (regs.isEmpty()) { box.getChildren().add(emptyLabel("No attendees yet.")); return; }
        for (EventDAO.EventRegistration reg : regs) {
            VBox card = new VBox(10); card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 14; " + CARD_SHADOW + " -fx-padding: 20;");
            String sc = switch(reg.status){ case "ACCEPTED" -> GREEN; case "PENDING" -> "#D97706"; default -> RED_SOFT; };
            String statusStr = reg.status;
            if ("ACCEPTED".equals(reg.status) && reg.hasEntered) {
                statusStr += "  ·  ✓ ENTERED";
            }
            Label pill = new Label(reg.role + "  ·  " + statusStr);
            pill.setStyle("-fx-background-color: transparent; -fx-border-color:" + sc + "; -fx-border-radius: 10; -fx-text-fill: " + sc + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10;");
            Label email = new Label(reg.email);
            email.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 15px; -fx-font-weight: bold;");
            card.getChildren().addAll(pill, email);
            if (reg.applicationText != null && !reg.applicationText.isBlank()) {
                Label msg = new Label("\"" + reg.applicationText + "\"");
                msg.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 13px; -fx-wrap-text: true; -fx-font-style: italic;");
                msg.setMaxWidth(460); card.getChildren().add(msg);
            }
            HBox acts = new HBox(10);
            if ("PENDING".equals(reg.status)) {
                Button acc = acBtn("Accept", GREEN);
                Button rej = acBtn("Reject", RED_SOFT);
                acc.setOnAction(e -> { eventDAO.updateRegistrationStatus(reg.registrationId, "ACCEPTED"); refreshAttendees(box, ev); });
                rej.setOnAction(e -> { eventDAO.updateRegistrationStatus(reg.registrationId, "REJECTED"); refreshAttendees(box, ev); });
                acts.getChildren().addAll(acc, rej);
            } else if (!"BLOCKED".equals(reg.status)) {
                Button blk = acBtn("Block", RED_SOFT);
                blk.setOnAction(e -> { eventDAO.updateRegistrationStatus(reg.registrationId, "BLOCKED"); refreshAttendees(box, ev); });
                acts.getChildren().add(blk);
            }
            card.getChildren().add(acts); box.getChildren().add(card);
        }
    }

    // ── Create Event Dialog ────────────────────────────────────────
    private void showCreateDialog() {
        Stage d = new Stage(); d.setTitle("Create New Event");
        VBox box = new VBox(16); box.setPadding(new Insets(36)); box.setStyle("-fx-background-color: " + APP_BG + ";");
        Label h = new Label("New Event"); h.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 24px; -fx-font-weight: bold;");

        TextField titleF  = f("Event Title");
        TextField locF    = f("Location");
        TextField capF    = f("Max Capacity");  capF.setText("100");
        DatePicker dp = new DatePicker(); dp.setMaxWidth(Double.MAX_VALUE);
        dp.setStyle("-fx-control-inner-background: white; -fx-text-inner-color: " + TEXT_PRI + ";");

        ComboBox<EventType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(EventType.values());
        typeBox.setValue(EventType.CONCERT);
        typeBox.setMaxWidth(Double.MAX_VALUE);
        typeBox.setStyle("-fx-background-color: white; -fx-border-color: " + BORDER + "; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5 10; -fx-font-size: 13px;");

        TextArea desc = new TextArea(); desc.setWrapText(true); desc.setPrefRowCount(4);
        desc.setPromptText("Describe your event — highlights, agenda, what to expect...");
        desc.setStyle("-fx-control-inner-background: white; -fx-text-inner-color: " + TEXT_PRI + "; -fx-font-size: 13px; -fx-background-radius: 10;");

        HBox btns = new HBox(14);
        Button saveDraft = new Button("Save as Draft");
        saveDraft.setStyle("-fx-background-color: " + BLUE_LITE + "; -fx-text-fill: " + BLUE + "; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 10 20; -fx-cursor: hand;");
        Button publish   = new Button("Publish Now");
        publish.setStyle(BTN_PRIMARY);
        btns.getChildren().addAll(saveDraft, publish);

        saveDraft.setOnAction(e -> handleCreate(d, titleF, locF, capF, dp, typeBox, desc, "DRAFT"));
        publish  .setOnAction(e -> handleCreate(d, titleF, locF, capF, dp, typeBox, desc, "PUBLISHED"));

        box.getChildren().addAll(h, titleF, locF, capF, dp, typeBox, desc, btns);
        ScrollPane sp = new ScrollPane(box); sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + APP_BG + "; -fx-background-color: " + APP_BG + "; -fx-border-color: " + APP_BG + ";");
        d.setScene(new Scene(sp, 460, 600)); d.show();
    }

    private void handleCreate(Stage d, TextField titleF, TextField locF, TextField capF,
                              DatePicker dp, ComboBox<EventType> typeBox, TextArea desc, String status) {
        if (dp.getValue() == null || titleF.getText().isBlank() || locF.getText().isBlank()) {
            new Alert(Alert.AlertType.ERROR, "Please fill Title, Location, and Date.").show(); return;
        }
        int cap = 100; try { cap = Integer.parseInt(capF.getText().trim()); } catch (NumberFormatException ignored) {}
        User u = SessionManager.getInstance().getCurrentUser();
        boolean ok = eventDAO.createEvent(u.getUserId(), titleF.getText(), dp.getValue().atStartOfDay(),
            locF.getText(), typeBox.getValue(), desc.getText(), cap, status);
        if (ok) {
            d.close();
            if ("DRAFT".equals(status)) { activeView = "drafts"; root.setLeft(buildSidebar()); showDraftsView(); }
            else { activeView = "published"; root.setLeft(buildSidebar()); showPublishedView(); }
        } else new Alert(Alert.AlertType.ERROR, "Failed to save event.").show();
    }

    // ── Helpers ────────────────────────────────────────────────────
    private VBox contentPane(String title, String subtitle) {
        VBox c = new VBox(22); c.setPadding(new Insets(40)); c.setStyle("-fx-background-color: " + APP_BG + ";");
        Label t = new Label(title); t.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 26px; -fx-font-weight: bold;");
        Label s = new Label(subtitle); s.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 14px;");
        c.getChildren().addAll(t, s); return c;
    }
    private FlowPane flowPane() {
        FlowPane fp = new FlowPane(20, 20); fp.setStyle("-fx-background-color: transparent;"); return fp;
    }
    private ScrollPane scrollWrap(FlowPane fp) {
        ScrollPane sp = new ScrollPane(fp); sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: " + APP_BG + "; -fx-border-color: " + APP_BG + ";");
        VBox.setVgrow(sp, Priority.ALWAYS); return sp;
    }
    private ScrollPane whiteScrollPane(VBox content) {
        ScrollPane sp = new ScrollPane(content); sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: " + APP_BG + "; -fx-border-color: " + APP_BG + ";");
        return sp;
    }
    private TextField f(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt); tf.setStyle(FIELD_STYLE); return tf;
    }
    private Button acBtn(String t, String color) {
        Button b = new Button(t); b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 8 16; -fx-cursor: hand; -fx-font-weight: bold;"); return b;
    }
    private Label emptyLabel(String t) {
        Label l = new Label(t); l.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 14px;"); return l;
    }
}
