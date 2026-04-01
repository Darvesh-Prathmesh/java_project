package eventmanagement.view;

import eventmanagement.dao.EventDAO;
import eventmanagement.model.Event;
import eventmanagement.model.Role;
import eventmanagement.model.User;
import eventmanagement.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

public class UserDashboardScreen {
    private Stage stage;
    private EventDAO eventDAO = new EventDAO();
    private YearMonth currentYearMonth = YearMonth.now();
    private BorderPane root;
    private String activeView = "browse";
    private GridPane calendarGrid;
    private FlowPane browsePane;

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
        "-fx-font-size: 13px; -fx-background-radius: 20; -fx-padding: 10 20; -fx-cursor: hand;";
    private final String FIELD_STYLE =
        "-fx-background-color: " + WHITE + "; -fx-border-color: " + BORDER + "; -fx-border-radius: 10; " +
        "-fx-background-radius: 10; -fx-padding: 11 14; -fx-font-size: 13px; -fx-text-fill: " + TEXT_PRI + "; " +
        "-fx-prompt-text-fill: #94A3B8;";

    public UserDashboardScreen(Stage stage) { this.stage = stage; }

    // ── Scene ──────────────────────────────────────────────────────
    public Scene getScene() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + APP_BG + ";");
        browsePane   = new FlowPane(20, 20);
        browsePane.setStyle("-fx-background-color: transparent;");
        calendarGrid = new GridPane();
        calendarGrid.setHgap(10); calendarGrid.setVgap(10);
        root.setLeft(buildSidebar());
        showBrowseView();
        return new Scene(root, 1120, 720);
    }

    // ── Sidebar ────────────────────────────────────────────────────
    private VBox buildSidebar() {
        User u = SessionManager.getInstance().getCurrentUser();
        VBox sb = new VBox(4);
        sb.setPadding(new Insets(36, 18, 36, 18));
        sb.setStyle("-fx-background-color: " + SIDEBAR_GRAD + ";");
        sb.setPrefWidth(240);

        Label brand  = new Label("EventHub");
        brand.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        VBox.setMargin(brand, new Insets(0, 0, 4, 4));

        String roleStr = (u.getRole() == Role.VOLUNTEER) ? "VOLUNTEER" : "PARTICIPANT";
        Label roleTag = new Label(roleStr);
        roleTag.setStyle("-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white; " +
            "-fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 10;");
        VBox.setMargin(roleTag, new Insets(0, 0, 24, 0));

        Button browseBtn   = navBtn("Browse Events", "browse");
        Button calBtn      = navBtn("My Calendar",  "calendar");
        Button ticketsBtn  = navBtn("My Tickets",   "tickets");

        browseBtn .setOnAction(e -> { activeView = "browse";   root.setLeft(buildSidebar()); showBrowseView(); });
        calBtn    .setOnAction(e -> { activeView = "calendar"; root.setLeft(buildSidebar()); showCalendarView(); });
        ticketsBtn.setOnAction(e -> { activeView = "tickets";  root.setLeft(buildSidebar()); showTicketsView(); });
        
        Button scannerBtn = null;
        if (u.getRole() == Role.VOLUNTEER) {
            scannerBtn = navBtn("QR Scanner", "scanner");
            scannerBtn.setOnAction(e -> { activeView = "scanner"; root.setLeft(buildSidebar()); showScannerView(); });
        }

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);

        Label footer = new Label("Developed by Prathmesh Darvesh\nRoll No: ECSB441");
        footer.setStyle("-fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 10px; -fx-text-alignment: center;");
        footer.setWrapText(true);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle("-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white; " +
            "-fx-background-radius: 20; -fx-font-weight: bold; -fx-padding: 10; -fx-cursor: hand;");
        logoutBtn.setOnAction(e -> { SessionManager.getInstance().logout(); stage.setScene(new AuthScreen(stage).getLoginScene()); });

        sb.getChildren().addAll(brand, roleTag, browseBtn, calBtn, ticketsBtn);
        if (scannerBtn != null) sb.getChildren().add(scannerBtn);
        sb.getChildren().addAll(spacer, footer, new Label(""), logoutBtn);
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

    // ── Browse View ────────────────────────────────────────────────
    private void showBrowseView() {
        VBox c = wrap("Browse Events", "Discover and join events happening near you.");
        browsePane.getChildren().clear();
        User u = SessionManager.getInstance().getCurrentUser();
        List<Event> events = eventDAO.getAllEvents().stream()
            .filter(e -> "PUBLISHED".equalsIgnoreCase(e.getStatus())).collect(Collectors.toList());

        if (events.isEmpty()) {
            browsePane.getChildren().add(emptyLabel("No published events available yet."));
        } else {
            for (Event ev : events) browsePane.getChildren().add(buildBrowseCard(ev, u));
        }
        ScrollPane sp = lightScroll(browsePane);
        c.getChildren().add(sp); root.setCenter(c);
    }

    private VBox buildBrowseCard(Event ev, User u) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(22));
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 14; " + CARD_SHADOW);
        card.setPrefWidth(270);

        Label tag = new Label(ev.getEventType().toString());
        tag.setStyle("-fx-background-color: " + BLUE_LITE + "; -fx-text-fill: " + BLUE + "; " +
            "-fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10;");

        Label title = new Label(ev.getTitle());
        title.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 17px; -fx-font-weight: bold; -fx-wrap-text: true;");
        title.setMaxWidth(230);

        Label dateLoc = new Label(ev.getEventDate().toLocalDate() + "  •  " + ev.getLocation());
        dateLoc.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 12px; -fx-wrap-text: true;");
        dateLoc.setMaxWidth(230);

        int rsvp = eventDAO.getRSVPCount(ev.getEventId()), cap = ev.getMaxCapacity();
        String cc = rsvp >= cap ? RED_SOFT : GREEN;
        Label capL = new Label("Spots:  " + rsvp + " / " + cap);
        capL.setStyle("-fx-text-fill: " + cc + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        card.getChildren().addAll(tag, title, dateLoc, capL);

        if (ev.getDescription() != null && !ev.getDescription().isBlank()) {
            Label desc = new Label(ev.getDescription().length() > 90
                ? ev.getDescription().substring(0, 87) + "..." : ev.getDescription());
            desc.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 12px; -fx-wrap-text: true;");
            desc.setMaxWidth(230);
            card.getChildren().add(desc);
        }

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS); card.getChildren().add(spacer);

        boolean isFull = rsvp >= cap;
        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);
        if (isFull) {
            btn.setText("Event Full"); btn.setDisable(true);
            btn.setStyle("-fx-background-color: " + BORDER + "; -fx-text-fill: " + TEXT_SEC + "; -fx-background-radius: 20; -fx-padding: 10 18;");
        } else if (u.getRole() == Role.VOLUNTEER) {
            btn.setText("Apply to Volunteer"); btn.setStyle(BTN_PRIMARY);
            btn.setOnAction(e -> showVolunteerDialog(ev, u));
        } else {
            btn.setText("RSVP Event"); btn.setStyle(BTN_PRIMARY);
            btn.setOnAction(e -> showRsvpDialog(ev, u));
        }
        card.getChildren().add(btn);
        return card;
    }

    // ── RSVP Dialog ────────────────────────────────────────────────
    private void showRsvpDialog(Event ev, User u) {
        Stage d = new Stage(); d.setTitle("Confirm RSVP");
        VBox box = new VBox(20); box.setPadding(new Insets(36)); box.setStyle("-fx-background-color: " + APP_BG + ";");

        Label h = new Label("Confirm Your Spot");
        h.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 22px; -fx-font-weight: bold;");

        VBox summary = new VBox(12); summary.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 14; " + CARD_SHADOW + " -fx-padding: 22;");
        summary.getChildren().addAll(row("Event", ev.getTitle()), row("Date", ev.getEventDate().toLocalDate().toString()), row("Venue", ev.getLocation()), row("Account", u.getEmail()));

        Label note = new Label("Confirming will generate a unique ticket and QR code for entry.");
        note.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 13px; -fx-wrap-text: true;");

        Button confirmBtn = new Button("Confirm & Get Ticket");
        confirmBtn.setMaxWidth(Double.MAX_VALUE); confirmBtn.setStyle(BTN_PRIMARY);
        confirmBtn.setOnAction(e -> {
            boolean ok = eventDAO.registerForEvent(ev.getEventId(), u.getUserId(), u.getRole(), null);
            if (ok) { d.close(); String t = eventDAO.getTicketNumberForUserEvent(ev.getEventId(), u.getUserId()); if (t != null) showTicketModal(ev, t); }
            else new Alert(Alert.AlertType.ERROR, "Already registered or event is full.").show();
        });
        Button cancel = new Button("Cancel"); cancel.setMaxWidth(Double.MAX_VALUE);
        cancel.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_SEC + "; -fx-cursor: hand;");
        cancel.setOnAction(e -> d.close());
        box.getChildren().addAll(h, summary, note, confirmBtn, cancel);
        d.setScene(new Scene(box, 430, 440)); d.show();
    }

    // ── Ticket Modal ───────────────────────────────────────────────
    private void showTicketModal(Event ev, String ticketNumber) {
        Stage d = new Stage(); d.setTitle("Your Ticket");
        VBox box = new VBox(24); box.setPadding(new Insets(36)); box.setStyle("-fx-background-color: " + APP_BG + ";"); box.setAlignment(Pos.CENTER);

        Label h = new Label("Your Digital Ticket");
        h.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 22px; -fx-font-weight: bold;");

        VBox card = new VBox(14); card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 20; -fx-padding: 32; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(37,99,235,0.15), 24, 0, 0, 8);");

        Label evTitle = new Label(ev.getTitle());
        evTitle.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 19px; -fx-font-weight: bold;");

        Label evDate = new Label(ev.getEventDate().toLocalDate() + "  •  " + ev.getLocation());
        evDate.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 13px;");

        // Divider
        Region div = new Region(); div.setStyle("-fx-background-color: " + BORDER + ";");
        div.setPrefHeight(1); div.setMaxWidth(Double.MAX_VALUE);

        Label tktLbl = new Label("TICKET ID");
        tktLbl.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label tktNum = new Label(ticketNumber);
        tktNum.setStyle("-fx-text-fill: " + BLUE + "; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Courier New', monospace;");

        // QR Code via API
        ImageView qr = new ImageView();
        qr.setFitWidth(180); qr.setFitHeight(180); qr.setPreserveRatio(true);
        try { qr.setImage(new Image("https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=" + ticketNumber, true)); }
        catch (Exception ignored) {}

        Label scan = new Label("Present at venue for check-in");
        scan.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 12px;");

        card.getChildren().addAll(evTitle, evDate, div, tktLbl, tktNum, qr, scan);
        box.getChildren().addAll(h, card);
        d.setScene(new Scene(box, 400, 560)); d.show();
    }

    // ── Volunteer Dialog ───────────────────────────────────────────
    private void showVolunteerDialog(Event ev, User u) {
        Stage d = new Stage(); d.setTitle("Apply to Volunteer");
        VBox box = new VBox(18); box.setPadding(new Insets(32)); box.setStyle("-fx-background-color: " + APP_BG + ";");
        Label h = new Label("Volunteer Application\n" + ev.getTitle());
        h.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label prompt = new Label("Why do you want to volunteer for this event?");
        prompt.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 14px;");
        TextArea ta = new TextArea(); ta.setWrapText(true); ta.setPrefRowCount(4);
        ta.setStyle("-fx-control-inner-background: white; -fx-text-inner-color: " + TEXT_PRI + "; -fx-background-radius: 10; -fx-font-size: 13px;");
        Button sub = new Button("Submit Application"); sub.setMaxWidth(Double.MAX_VALUE); sub.setStyle(BTN_PRIMARY);
        sub.setOnAction(e -> {
            if (ta.getText().isBlank()) { new Alert(Alert.AlertType.ERROR, "Please write a short description.").show(); return; }
            boolean ok = eventDAO.registerForEvent(ev.getEventId(), u.getUserId(), u.getRole(), ta.getText());
            if (ok) { new Alert(Alert.AlertType.INFORMATION, "Application submitted! Awaiting organizer approval.").show(); d.close(); }
            else new Alert(Alert.AlertType.ERROR, "Already applied or event is full.").show();
        });
        box.getChildren().addAll(h, prompt, ta, sub);
        d.setScene(new Scene(box, 400, 340)); d.show();
    }

    // ── Calendar View ──────────────────────────────────────────────
    private void showCalendarView() {
        VBox c = wrap("My Calendar", "Accepted events are highlighted. Click a date to see details.");
        HBox nav = new HBox(16); nav.setAlignment(Pos.CENTER_LEFT);
        Button prev = navArrBtn("< Prev"); Button next = navArrBtn("Next >");
        Label monthLbl = new Label(); monthLbl.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 18px; -fx-font-weight: bold; -fx-min-width: 180;");
        nav.getChildren().addAll(prev, monthLbl, next);
        prev.setOnAction(e -> { currentYearMonth = currentYearMonth.minusMonths(1); buildCalendar(monthLbl); });
        next.setOnAction(e -> { currentYearMonth = currentYearMonth.plusMonths(1); buildCalendar(monthLbl); });
        buildCalendar(monthLbl);
        c.getChildren().addAll(nav, calendarGrid);
        root.setCenter(c);
    }

    private void buildCalendar(Label monthLbl) {
        calendarGrid.getChildren().clear();
        monthLbl.setText(currentYearMonth.getMonth() + " " + currentYearMonth.getYear());

        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        for (int i = 0; i < 7; i++) {
            Label d = new Label(days[i]);
            d.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-alignment: center; -fx-pref-width: 86;");
            calendarGrid.add(d, i, 0);
        }

        User u = SessionManager.getInstance().getCurrentUser();
        List<Event> myEvents = eventDAO.getRegisteredEventsForUser(u.getUserId());
        LocalDate today = LocalDate.now();
        int col = currentYearMonth.atDay(1).getDayOfWeek().getValue() - 1, row = 1;

        for (int day = 1; day <= currentYearMonth.lengthOfMonth(); day++) {
            LocalDate date = currentYearMonth.atDay(day);
            List<Event> dayEvents = myEvents.stream()
                .filter(e -> e.getEventDate().toLocalDate().equals(date)).collect(Collectors.toList());

            VBox cell = new VBox(4); cell.setAlignment(Pos.CENTER);
            cell.setPrefSize(86, 70);
            boolean isToday = date.equals(today);

            // Day number — circle for today
            StackPane numPane = new StackPane();
            if (isToday) {
                Circle circle = new Circle(16, Color.web(BLUE));
                Label numLbl = new Label(String.valueOf(day));
                numLbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
                numPane.getChildren().addAll(circle, numLbl);
            } else {
                Label numLbl = new Label(String.valueOf(day));
                numLbl.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 14px;" + (dayEvents.isEmpty() ? "" : " -fx-font-weight: bold;"));
                numPane.getChildren().add(numLbl);
            }
            cell.getChildren().add(numPane);

            // Blue dot if events exist
            if (!dayEvents.isEmpty()) {
                Circle dot = new Circle(4, Color.web(BLUE));
                cell.getChildren().add(dot);
            }

            String cellStyle;
            if (!dayEvents.isEmpty()) {
                cellStyle = "-fx-background-color: " + BLUE_LITE + "; -fx-background-radius: 12; -fx-cursor: hand;";
                cell.setStyle(cellStyle);
                cell.setOnMouseEntered(e -> cell.setStyle("-fx-background-color: #DBEAFE; -fx-background-radius: 12; -fx-cursor: hand;"));
                cell.setOnMouseExited(e -> cell.setStyle(cellStyle));
                cell.setOnMouseClicked(e -> showDayDialog(date, dayEvents, u));
            } else {
                cell.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 6, 0, 0, 2);");
            }

            calendarGrid.add(cell, col, row);
            col++; if (col == 7) { col = 0; row++; }
        }
    }

    private void showDayDialog(LocalDate date, List<Event> events, User u) {
        Stage d = new Stage(); d.setTitle("Events on " + date);
        VBox box = new VBox(16); box.setPadding(new Insets(30)); box.setStyle("-fx-background-color: " + APP_BG + ";");
        Label h = new Label("Events on " + date); h.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 20px; -fx-font-weight: bold;");
        box.getChildren().add(h);
        for (Event ev : events) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 14; " + CARD_SHADOW + " -fx-padding: 20;");
            card.getChildren().addAll(row("Event", ev.getTitle()), row("Location", ev.getLocation()), row("Type", ev.getEventType().toString()));
            Button ticketBtn = new Button("View My Ticket");
            ticketBtn.setStyle(BTN_PRIMARY);
            ticketBtn.setOnAction(e -> {
                String t = eventDAO.getTicketNumberForUserEvent(ev.getEventId(), u.getUserId());
                if (t != null) showTicketModal(ev, t);
                else new Alert(Alert.AlertType.INFORMATION, "No ticket found. Application may still be pending.").show();
            });
            card.getChildren().add(ticketBtn); box.getChildren().add(card);
        }
        ScrollPane sp = new ScrollPane(box); sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + APP_BG + "; -fx-background-color: " + APP_BG + "; -fx-border-color: " + APP_BG + ";");
        d.setScene(new Scene(sp, 420, 480)); d.show();
    }

    // ── My Tickets View ────────────────────────────────────────────
    private void showTicketsView() {
        VBox c = wrap("My Tickets", "All your confirmed registrations and QR codes.");
        User u = SessionManager.getInstance().getCurrentUser();
        List<Event> accepted = eventDAO.getRegisteredEventsForUser(u.getUserId());
        VBox list = new VBox(16); list.setStyle("-fx-background-color: " + APP_BG + ";");
        if (accepted.isEmpty()) {
            list.getChildren().add(emptyLabel("No confirmed registrations yet."));
        } else {
            for (Event ev : accepted) {
                String ticket = eventDAO.getTicketNumberForUserEvent(ev.getEventId(), u.getUserId());
                VBox card = new VBox(12);
                card.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 14; " + CARD_SHADOW + " -fx-padding: 22;");
                card.getChildren().addAll(row("Event", ev.getTitle()), row("Date", ev.getEventDate().toLocalDate().toString()), row("Venue", ev.getLocation()), row("Ticket", ticket != null ? ticket : "Pending"));
                if (ticket != null) {
                    Button viewBtn = new Button("Open Ticket & QR Code");
                    viewBtn.setStyle(BTN_PRIMARY);
                    viewBtn.setOnAction(e -> showTicketModal(ev, ticket));
                    card.getChildren().add(viewBtn);
                }
                list.getChildren().add(card);
            }
        }
        ScrollPane sp = new ScrollPane(list); sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: " + APP_BG + "; -fx-border-color: " + APP_BG + ";");
        VBox.setVgrow(sp, Priority.ALWAYS);
        c.getChildren().add(sp); root.setCenter(c);
    }

    // ── Scanner View (Volunteers Only) ─────────────────────────────
    private void showScannerView() {
        VBox c = wrap("QR Scanner", "Scan participant tickets to check them into the event.");
        
        VBox scannerBox = new VBox(20);
        scannerBox.setPadding(new Insets(40));
        scannerBox.setAlignment(Pos.CENTER);
        scannerBox.setStyle("-fx-background-color: " + WHITE + "; -fx-background-radius: 14; " + CARD_SHADOW + " -fx-max-width: 500;");
        
        Label inst = new Label("ENTER TICKET ID:");
        inst.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextField tktInput = new TextField();
        tktInput.setPromptText("e.g. TKT-ABCDEF12");
        tktInput.setStyle(FIELD_STYLE + " -fx-font-size: 18px; -fx-alignment: center;");
        tktInput.setPrefWidth(300);
        
        Button scanBtn = new Button("Verify & Check-In");
        scanBtn.setStyle(BTN_PRIMARY + " -fx-font-size: 15px; -fx-padding: 12 30;");
        
        Label resultLbl = new Label("");
        resultLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-alignment: center;");
        resultLbl.setWrapText(true);
        
        scanBtn.setOnAction(e -> {
            String val = tktInput.getText().trim();
            if (val.isEmpty()) return;
            
            EventDAO.EventRegistration reg = eventDAO.getRegistrationByTicket(val);
            if (reg == null) {
                resultLbl.setStyle("-fx-text-fill: " + RED_SOFT + "; -fx-font-size: 16px; -fx-font-weight: bold;");
                resultLbl.setText("❌ INVALID TICKET\nNo matching record found.");
            } else if (!"ACCEPTED".equals(reg.status)) {
                resultLbl.setStyle("-fx-text-fill: " + RED_SOFT + "; -fx-font-size: 16px; -fx-font-weight: bold;");
                resultLbl.setText("❌ REGISTRATION NOT ACCEPTED\nCurrent status: " + reg.status);
            } else if (reg.hasEntered) {
                resultLbl.setStyle("-fx-text-fill: " + RED_SOFT + "; -fx-font-size: 16px; -fx-font-weight: bold;");
                resultLbl.setText("⚠️ TICKET ALREADY USED\nParticipant has already entered.");
            } else {
                boolean ok = eventDAO.markTicketAsUsed(reg.registrationId);
                if (ok) {
                    resultLbl.setStyle("-fx-text-fill: " + GREEN + "; -fx-font-size: 16px; -fx-font-weight: bold;");
                    resultLbl.setText("✅ ENTRY APPROVED\nWelcome, " + reg.email + "!");
                    tktInput.clear();
                } else {
                    resultLbl.setStyle("-fx-text-fill: " + RED_SOFT + "; -fx-font-size: 16px; -fx-font-weight: bold;");
                    resultLbl.setText("Error updating database.");
                }
            }
        });
        
        scannerBox.getChildren().addAll(inst, tktInput, scanBtn, resultLbl);
        
        c.getChildren().add(scannerBox);
        c.setAlignment(Pos.TOP_LEFT);
        
        root.setCenter(c);
    }

    // ── Helpers ────────────────────────────────────────────────────
    private VBox wrap(String title, String subtitle) {
        VBox c = new VBox(22); c.setPadding(new Insets(40)); c.setStyle("-fx-background-color: " + APP_BG + ";");
        Label t = new Label(title); t.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 26px; -fx-font-weight: bold;");
        Label s = new Label(subtitle); s.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 14px;");
        c.getChildren().addAll(t, s); return c;
    }
    private ScrollPane lightScroll(FlowPane fp) {
        ScrollPane sp = new ScrollPane(fp); sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: " + APP_BG + "; -fx-border-color: " + APP_BG + ";");
        VBox.setVgrow(sp, Priority.ALWAYS); return sp;
    }
    private HBox row(String label, String value) {
        Label l = new Label(label + ":"); l.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 13px; -fx-min-width: 80;");
        Label v = new Label(value); v.setStyle("-fx-text-fill: " + TEXT_PRI + "; -fx-font-size: 13px; -fx-font-weight: bold; -fx-wrap-text: true;"); v.setMaxWidth(270);
        HBox r = new HBox(10, l, v); r.setAlignment(Pos.CENTER_LEFT); return r;
    }
    private Button navArrBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + WHITE + "; -fx-text-fill: " + BLUE + "; -fx-background-radius: 12; -fx-border-color: " + BORDER + "; -fx-border-radius: 12; -fx-padding: 8 16; -fx-cursor: hand; -fx-font-weight: bold;");
        return b;
    }
    private Label emptyLabel(String text) {
        Label l = new Label(text); l.setStyle("-fx-text-fill: " + TEXT_SEC + "; -fx-font-size: 14px;"); return l;
    }
}
