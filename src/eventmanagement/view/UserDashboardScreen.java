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
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

public class UserDashboardScreen {
    private Stage stage;
    private EventDAO eventDAO = new EventDAO();
    private YearMonth currentYearMonth;
    private GridPane calendarGrid;
    private FlowPane browseContainer;
    private BorderPane root;
    private String activeView = "browse";

    // ---- Design System ----
    private static final String BG      = "-fx-background-color: #12121A;";
    private static final String SIDEBAR  = "-fx-background-color: #1C1C28;";
    private static final String CARD     = "-fx-background-color: #1E1E2E; -fx-background-radius: 18; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 14, 0, 0, 6);";
    private static final String BTN_PRI  = "-fx-background-color: linear-gradient(to right, #8E24AA, #D500F9); -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 10 16; -fx-font-weight: bold; -fx-cursor: hand;";
    private static final String TXT_W    = "-fx-text-fill: white; -fx-font-family: 'Segoe UI', sans-serif;";

    public UserDashboardScreen(Stage stage) {
        this.stage = stage;
        this.currentYearMonth = YearMonth.now();
    }

    public Scene getScene() {
        root = new BorderPane();
        root.setStyle(BG);
        root.setLeft(buildSidebar());
        browseContainer = new FlowPane(20, 20);
        browseContainer.setStyle("-fx-background-color: transparent;");
        calendarGrid = new GridPane();
        calendarGrid.setHgap(10); calendarGrid.setVgap(10);
        calendarGrid.setPadding(new Insets(20, 0, 0, 0));
        showBrowseView();
        return new Scene(root, 1100, 700);
    }

    // ─────────────── SIDEBAR ───────────────
    private VBox buildSidebar() {
        User u = SessionManager.getInstance().getCurrentUser();
        VBox sb = new VBox(6);
        sb.setPadding(new Insets(30, 14, 30, 14));
        sb.setStyle(SIDEBAR);
        sb.setPrefWidth(230);

        Label logo = new Label(u.getRole() == Role.VOLUNTEER ? "* Volunteer Hub" : "# EventPass");
        logo.setStyle(TXT_W + " -fx-font-size: 19px; -fx-font-weight: bold;");
        VBox.setMargin(logo, new Insets(0, 0, 20, 6));

        Button browseBtn   = sidebarBtn("[>] Browse Events",  "browse");
        Button calendarBtn = sidebarBtn("[C] My Calendar",    "calendar");
        Button myTickets   = sidebarBtn("[T] My Tickets",     "tickets");

        browseBtn  .setOnAction(e -> { activeView = "browse";   root.setLeft(buildSidebar()); showBrowseView(); });
        calendarBtn.setOnAction(e -> { activeView = "calendar"; root.setLeft(buildSidebar()); showCalendarView(); });
        myTickets  .setOnAction(e -> { activeView = "tickets";  root.setLeft(buildSidebar()); showMyTicketsView(); });

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);

        Label roleTag = new Label(u.getRole().toString().toUpperCase());
        roleTag.setStyle("-fx-background-color: #2D1B4E; -fx-text-fill: #CC88FF; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 10;");

        Button logout = new Button("Logout");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setStyle("-fx-background-color: #2A1A1A; -fx-text-fill: #FF5555; -fx-background-radius: 12; -fx-padding: 10; -fx-cursor: hand; -fx-font-weight: bold;");
        logout.setOnAction(e -> { SessionManager.getInstance().logout(); stage.setScene(new AuthScreen(stage).getLoginScene()); });

        sb.getChildren().addAll(logo, browseBtn, calendarBtn, myTickets, spacer, roleTag, new Label(""), logout);
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

    // ─────────────── BROWSE VIEW ───────────────
    private void showBrowseView() {
        VBox center = contentWrapper("Browse Events", "Discover upcoming events. RSVP or apply to volunteer.");
        browseContainer.getChildren().clear();
        refreshBrowseCards();
        ScrollPane sp = scrollPaneFor(browseContainer);
        center.getChildren().add(sp);
        root.setCenter(center);
    }

    private void refreshBrowseCards() {
        browseContainer.getChildren().clear();
        User u = SessionManager.getInstance().getCurrentUser();
        List<Event> events = eventDAO.getAllEvents().stream()
                .filter(e -> "PUBLISHED".equalsIgnoreCase(e.getStatus()))
                .collect(Collectors.toList());

        if (events.isEmpty()) {
            browseContainer.getChildren().add(noDataLabel("No published events yet."));
            return;
        }

        for (Event ev : events) {
            VBox card = new VBox(10);
            card.setPadding(new Insets(20));
            card.setStyle(CARD);
            card.setPrefWidth(260);

            Label typePill = new Label("  " + ev.getEventType() + "  ");
            typePill.setStyle("-fx-background-color: #2D1B4E; -fx-text-fill: #D500F9; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: bold;");

            Label title = new Label(ev.getTitle());
            title.setStyle(TXT_W + " -fx-font-size: 17px; -fx-font-weight: bold; -fx-wrap-text: true;");
            title.setMaxWidth(220);

            Label dateLoc = new Label("Date: " + ev.getEventDate().toLocalDate() + "  Loc: " + ev.getLocation());
            dateLoc.setStyle("-fx-text-fill: #AAAACC; -fx-font-size: 12px; -fx-wrap-text: true;");
            dateLoc.setMaxWidth(220);

            int rsvp = eventDAO.getRSVPCount(ev.getEventId());
            int cap  = ev.getMaxCapacity();
            Label capL = new Label("Spots: " + rsvp + " / " + cap);
            String capColor = rsvp >= cap ? "#E53935" : "#4CAF50";
            capL.setStyle("-fx-text-fill: " + capColor + "; -fx-font-size: 12px; -fx-font-weight: bold;");

            card.getChildren().addAll(typePill, title, dateLoc, capL);

            if (ev.getDescription() != null && !ev.getDescription().isBlank()) {
                Label desc = new Label(ev.getDescription().length() > 90
                        ? ev.getDescription().substring(0, 87) + "..." : ev.getDescription());
                desc.setStyle("-fx-text-fill: #8888AA; -fx-font-size: 12px; -fx-wrap-text: true;");
                desc.setMaxWidth(220);
                card.getChildren().add(desc);
            }

            Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
            card.getChildren().add(spacer);

            boolean isFull = rsvp >= cap;
            Button actionBtn = new Button();
            actionBtn.setMaxWidth(Double.MAX_VALUE);

            if (isFull) {
                actionBtn.setText("Event Full");
                actionBtn.setStyle("-fx-background-color: #2A2A3D; -fx-text-fill: #666680; -fx-background-radius: 12; -fx-padding: 10 16;");
                actionBtn.setDisable(true);
            } else if (u.getRole() == Role.VOLUNTEER) {
                actionBtn.setText("Apply to Volunteer");
                actionBtn.setStyle(BTN_PRI);
                actionBtn.setOnAction(e -> showVolunteerApplicationDialog(ev, u));
            } else {
                actionBtn.setText("RSVP Event");
                actionBtn.setStyle(BTN_PRI);
                actionBtn.setOnAction(e -> showRsvpConfirmationDialog(ev, u));
            }
            card.getChildren().add(actionBtn);
            browseContainer.getChildren().add(card);
        }
    }

    // ─────────────── RSVP CONFIRMATION ───────────────
    private void showRsvpConfirmationDialog(Event ev, User u) {
        Stage dialog = new Stage();
        dialog.setTitle("Confirm RSVP");
        VBox box = new VBox(18);
        box.setPadding(new Insets(35));
        box.setStyle(BG);

        Label header = new Label("Confirm Registration");
        header.setStyle(TXT_W + " -fx-font-size: 22px; -fx-font-weight: bold;");

        VBox summary = new VBox(10);
        summary.setStyle(CARD + " -fx-padding: 20;");
        summary.getChildren().addAll(
            styledRow("Event :",    ev.getTitle()),
            styledRow("Date :",     ev.getEventDate().toLocalDate().toString()),
            styledRow("Location :", ev.getLocation()),
            styledRow("Email :",    u.getEmail())
        );

        Label note = new Label("By confirming, you'll receive a unique digital ticket and QR code.");
        note.setStyle("-fx-text-fill: #8888AA; -fx-font-size: 12px; -fx-wrap-text: true;");

        Button confirm = new Button("Confirm & Get Ticket");
        confirm.setMaxWidth(Double.MAX_VALUE);
        confirm.setStyle(BTN_PRI);
        confirm.setOnAction(e -> {
            boolean ok = eventDAO.registerForEvent(ev.getEventId(), u.getUserId(), u.getRole(), null);
            if (ok) {
                dialog.close();
                String ticket = eventDAO.getTicketNumberForUserEvent(ev.getEventId(), u.getUserId());
                if (ticket != null) showTicketModal(ev, ticket);
            } else {
                new Alert(Alert.AlertType.ERROR, "Already registered or an error occurred.").show();
            }
        });

        Button cancel = new Button("Cancel");
        cancel.setMaxWidth(Double.MAX_VALUE);
        cancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #AAAACC; -fx-cursor: hand;");
        cancel.setOnAction(e -> dialog.close());

        box.getChildren().addAll(header, summary, note, confirm, cancel);
        dialog.setScene(new Scene(box, 420, 440));
        dialog.show();
    }

    // ─────────────── QR TICKET MODAL ───────────────
    private void showTicketModal(Event ev, String ticketNumber) {
        Stage dialog = new Stage();
        dialog.setTitle("Your Ticket");
        VBox box = new VBox(18);
        box.setPadding(new Insets(35));
        box.setStyle(BG);
        box.setAlignment(Pos.CENTER);

        Label header = new Label("Your Digital Ticket");
        header.setStyle(TXT_W + " -fx-font-size: 22px; -fx-font-weight: bold;");

        VBox ticketCard = new VBox(12);
        ticketCard.setAlignment(Pos.CENTER);
        ticketCard.setStyle("-fx-background-color: #1E1E2E; -fx-background-radius: 20; -fx-padding: 30; -fx-effect: dropshadow(three-pass-box, rgba(142,36,170,0.4), 20, 0, 0, 0);");

        Label evtTitle = new Label(ev.getTitle());
        evtTitle.setStyle(TXT_W + " -fx-font-size: 20px; -fx-font-weight: bold;");

        Label dateLoc = new Label(ev.getEventDate().toLocalDate() + "  |  " + ev.getLocation());
        dateLoc.setStyle("-fx-text-fill: #AAAACC; -fx-font-size: 13px;");

        Region sep = new Region();
        sep.setStyle("-fx-background-color: #2A2A3D;");
        sep.setPrefHeight(1); sep.setMaxWidth(Double.MAX_VALUE);

        Label tktLabel = new Label("TICKET ID");
        tktLabel.setStyle("-fx-text-fill: #666680; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label tktNum = new Label(ticketNumber);
        tktNum.setStyle("-fx-text-fill: #D500F9; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: 'Courier New', monospace;");

        // QR Code via qrserver API
        ImageView qrView = new ImageView();
        qrView.setFitWidth(180); qrView.setFitHeight(180); qrView.setPreserveRatio(true);
        try {
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=" + ticketNumber;
            qrView.setImage(new Image(qrUrl, true));
        } catch (Exception ignored) {}

        Label scanNote = new Label("Present this QR code at the venue entrance for check-in");
        scanNote.setStyle("-fx-text-fill: #666680; -fx-font-size: 12px; -fx-wrap-text: true;");
        scanNote.setMaxWidth(280);

        ticketCard.getChildren().addAll(evtTitle, dateLoc, sep, tktLabel, tktNum, qrView, scanNote);
        box.getChildren().addAll(header, ticketCard);

        dialog.setScene(new Scene(box, 400, 580));
        dialog.show();
    }

    // ─────────────── VOLUNTEER APPLICATION ───────────────
    private void showVolunteerApplicationDialog(Event ev, User u) {
        Stage dialog = new Stage();
        dialog.setTitle("Volunteer Application");
        VBox box = new VBox(16);
        box.setPadding(new Insets(30));
        box.setStyle(BG);

        Label header = new Label("Apply to Volunteer\n" + ev.getTitle());
        header.setStyle(TXT_W + " -fx-font-size: 20px; -fx-font-weight: bold;");

        Label prompt = new Label("Why do you want to volunteer for this event?");
        prompt.setStyle("-fx-text-fill: #AAAACC; -fx-font-size: 14px;");

        TextArea ta = new TextArea();
        ta.setWrapText(true); ta.setPrefRowCount(4);
        ta.setStyle("-fx-control-inner-background: #2A2A3D; -fx-text-inner-color: white;");

        Button submit = new Button("Submit Application");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setStyle(BTN_PRI);
        submit.setOnAction(e -> {
            if (ta.getText().isBlank()) {
                new Alert(Alert.AlertType.ERROR, "Please write a short description.").show();
                return;
            }
            boolean ok = eventDAO.registerForEvent(ev.getEventId(), u.getUserId(), u.getRole(), ta.getText());
            if (ok) {
                new Alert(Alert.AlertType.INFORMATION, "Application submitted! Pending organizer approval.").show();
                dialog.close();
            } else {
                new Alert(Alert.AlertType.ERROR, "Already applied or event is full.").show();
            }
        });

        box.getChildren().addAll(header, prompt, ta, submit);
        dialog.setScene(new Scene(box, 400, 340));
        dialog.show();
    }

    // ─────────────── CALENDAR VIEW ───────────────
    private void showCalendarView() {
        VBox center = contentWrapper("My Calendar", "Your accepted events are highlighted in purple.");

        HBox nav = new HBox(15);
        nav.setAlignment(Pos.CENTER_LEFT);
        Button prev = navBtn("<< Prev"); Button next = navBtn("Next >>");
        Label monthLabel = new Label();
        monthLabel.setStyle(TXT_W + " -fx-font-size: 18px; -fx-font-weight: bold; -fx-min-width: 180;");
        nav.getChildren().addAll(prev, monthLabel, next);

        prev.setOnAction(e -> { currentYearMonth = currentYearMonth.minusMonths(1); populateCalendar(monthLabel); });
        next.setOnAction(e -> { currentYearMonth = currentYearMonth.plusMonths(1);  populateCalendar(monthLabel); });

        populateCalendar(monthLabel);
        center.getChildren().addAll(nav, calendarGrid);
        root.setCenter(center);
    }

    private void populateCalendar(Label monthLabel) {
        calendarGrid.getChildren().clear();
        monthLabel.setText(currentYearMonth.getMonth() + " " + currentYearMonth.getYear());

        String[] days = {"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
        for (int i = 0; i < 7; i++) {
            Label d = new Label(days[i]);
            d.setStyle("-fx-font-weight: bold; -fx-text-fill: #666680; -fx-font-size: 13px; -fx-alignment: center; -fx-pref-width: 80;");
            calendarGrid.add(d, i, 0);
        }

        LocalDate first = currentYearMonth.atDay(1);
        int startCol = first.getDayOfWeek().getValue() - 1;
        int col = startCol, row = 1;

        User u = SessionManager.getInstance().getCurrentUser();
        List<Event> myEvents = eventDAO.getRegisteredEventsForUser(u.getUserId());

        for (int day = 1; day <= currentYearMonth.lengthOfMonth(); day++) {
            LocalDate date = currentYearMonth.atDay(day);
            List<Event> dayEvents = myEvents.stream()
                    .filter(e -> e.getEventDate().toLocalDate().equals(date))
                    .collect(Collectors.toList());

            Button btn = new Button(String.valueOf(day));
            btn.setPrefSize(84, 84);
            if (!dayEvents.isEmpty()) {
                btn.setStyle("-fx-background-color: linear-gradient(to bottom right, #8E24AA, #D500F9); -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 12; -fx-cursor: hand;");
                btn.setOnAction(e -> showDayEventsDialog(date, dayEvents, u));
            } else {
                btn.setStyle("-fx-background-color: #1E1E2E; -fx-text-fill: #AAAACC; -fx-font-size: 14px; -fx-background-radius: 12;");
            }
            calendarGrid.add(btn, col, row);
            col++; if (col == 7) { col = 0; row++; }
        }
    }

    private void showDayEventsDialog(LocalDate date, List<Event> events, User u) {
        Stage dialog = new Stage();
        dialog.setTitle("Events on " + date);
        VBox box = new VBox(16);
        box.setPadding(new Insets(30));
        box.setStyle(BG);

        Label header = new Label("Events on " + date);
        header.setStyle(TXT_W + " -fx-font-size: 20px; -fx-font-weight: bold;");
        box.getChildren().add(header);

        for (Event ev : events) {
            VBox card = new VBox(10);
            card.setStyle(CARD + " -fx-padding: 20;");
            card.getChildren().addAll(
                styledRow("Event :",    ev.getTitle()),
                styledRow("Location :", ev.getLocation()),
                styledRow("Type :",     ev.getEventType().toString())
            );
            Button viewTicket = new Button("View My Ticket & QR");
            viewTicket.setStyle(BTN_PRI);
            viewTicket.setOnAction(e -> {
                String ticket = eventDAO.getTicketNumberForUserEvent(ev.getEventId(), u.getUserId());
                if (ticket != null) showTicketModal(ev, ticket);
                else new Alert(Alert.AlertType.INFORMATION, "No ticket found. Application may still be pending.").show();
            });
            card.getChildren().add(viewTicket);
            box.getChildren().add(card);
        }

        ScrollPane sp = new ScrollPane(box);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #12121A; -fx-background-color: #12121A;");
        dialog.setScene(new Scene(sp, 420, 480));
        dialog.show();
    }

    // ─────────────── MY TICKETS VIEW ───────────────
    private void showMyTicketsView() {
        VBox center = contentWrapper("My Tickets", "Your confirmed event registrations with QR codes.");
        User u = SessionManager.getInstance().getCurrentUser();
        List<Event> allAccepted = eventDAO.getRegisteredEventsForUser(u.getUserId());

        VBox ticketList = new VBox(14);
        ticketList.setStyle(BG);

        if (allAccepted.isEmpty()) {
            ticketList.getChildren().add(noDataLabel("No confirmed registrations yet."));
        } else {
            for (Event ev : allAccepted) {
                String ticket = eventDAO.getTicketNumberForUserEvent(ev.getEventId(), u.getUserId());
                VBox card = new VBox(10);
                card.setStyle(CARD + " -fx-padding: 20;");
                card.getChildren().addAll(
                    styledRow("Event :",    ev.getTitle()),
                    styledRow("Date :",     ev.getEventDate().toLocalDate().toString()),
                    styledRow("Location :", ev.getLocation()),
                    styledRow("Ticket :",   ticket != null ? ticket : "Pending")
                );
                if (ticket != null) {
                    Button viewBtn = new Button("Open Ticket & QR Code");
                    viewBtn.setStyle(BTN_PRI);
                    viewBtn.setOnAction(e -> showTicketModal(ev, ticket));
                    card.getChildren().add(viewBtn);
                }
                ticketList.getChildren().add(card);
            }
        }

        ScrollPane sp = new ScrollPane(ticketList);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: #12121A; -fx-border-color: #12121A;");
        VBox.setVgrow(sp, Priority.ALWAYS);
        center.getChildren().add(sp);
        root.setCenter(center);
    }

    // ─────────────── HELPERS ───────────────
    private VBox contentWrapper(String title, String subtitle) {
        VBox c = new VBox(20);
        c.setPadding(new Insets(40));
        c.setStyle(BG);
        Label t = new Label(title);
        t.setStyle(TXT_W + " -fx-font-size: 26px; -fx-font-weight: bold;");
        Label s = new Label(subtitle);
        s.setStyle("-fx-text-fill: #666680; -fx-font-size: 14px;");
        c.getChildren().addAll(t, s);
        return c;
    }

    private ScrollPane scrollPaneFor(FlowPane fp) {
        ScrollPane sp = new ScrollPane(fp);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: #12121A; -fx-border-color: #12121A;");
        VBox.setVgrow(sp, Priority.ALWAYS);
        return sp;
    }

    private HBox styledRow(String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #666680; -fx-font-size: 13px; -fx-min-width: 90;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-wrap-text: true;");
        v.setMaxWidth(260);
        HBox row = new HBox(8, l, v);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Button navBtn(String text) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: #2A2A3D; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 8 14;");
        return b;
    }

    private Label noDataLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #505070; -fx-font-size: 16px;");
        return l;
    }
}
