package eventmanagement;

import eventmanagement.dao.EventDAO;
import eventmanagement.model.Event;
import eventmanagement.view.AuthScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.time.LocalDateTime;

import eventmanagement.model.User;
import eventmanagement.util.SessionManager;
import java.util.List;

// Developed by Prathmesh Darvesh (Roll No: ECSB441)
public class Main extends Application {

    private EventDAO eventDAO = new EventDAO();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Event Management System");
        
        // Start background daemon thread
        startEventNotificationDaemon();

        // Load Auth Screen initially
        AuthScreen authScreen = new AuthScreen(primaryStage);
        primaryStage.setScene(authScreen.getLoginScene());
        primaryStage.show();
    }

    private void startEventNotificationDaemon() {
        Thread daemon = new Thread(() -> {
            while (true) {
                try {
                    User currentUser = SessionManager.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        List<Event> myEvents;
                        if (currentUser.getRole() == eventmanagement.model.Role.ORGANIZATION) {
                            myEvents = eventDAO.getEventsByOrgId(currentUser.getUserId());
                        } else {
                            myEvents = eventDAO.getRegisteredEventsForUser(currentUser.getUserId());
                        }

                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime soon = now.plusDays(1); // Notify for events coming within 24 hours

                        for (Event nextEvent : myEvents) {
                            if (nextEvent.getEventDate().isAfter(now) && nextEvent.getEventDate().isBefore(soon)) {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Event Reminder");
                                    alert.setHeaderText("Upcoming Event within 24 Hours!");
                                    alert.setContentText(nextEvent.getTitle() + " on " + nextEvent.getEventDate().toLocalDate());
                                    alert.show();
                                });
                                break; 
                            }
                        }
                    }
                    Thread.sleep(600000); // Check every 10 mins
                } catch (InterruptedException e) {
                    System.out.println("Daemon interrupted. Exiting.");
                    break;
                } catch (Exception e) {
                    // Ignore transient db errors or nulls implicitly
                }
            }
        });
        daemon.setDaemon(true);
        daemon.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
