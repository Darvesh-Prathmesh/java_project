package eventmanagement.model;

public class Volunteer extends User {
    private String firstName;
    private String lastName;
    private int hoursLogged;

    public Volunteer(int userId, String email, String firstName, String lastName, int hoursLogged) {
        super(userId, email, Role.VOLUNTEER);
        this.firstName = firstName;
        this.lastName = lastName;
        this.hoursLogged = hoursLogged;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public int getHoursLogged() { return hoursLogged; }
}
