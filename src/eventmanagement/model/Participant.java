package eventmanagement.model;

public class Participant extends User {
    private String firstName;
    private String lastName;

    public Participant(int userId, String email, String firstName, String lastName) {
        super(userId, email, Role.PARTICIPANT);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
}
