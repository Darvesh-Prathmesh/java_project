package eventmanagement.model;

public abstract class User {
    private int userId;
    private String email;
    private Role role;

    public User(int userId, String email, Role role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}
