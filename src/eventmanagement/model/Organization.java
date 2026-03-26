package eventmanagement.model;

public class Organization extends User {
    private String organizationName;

    public Organization(int userId, String email, String organizationName) {
        super(userId, email, Role.ORGANIZATION);
        this.organizationName = organizationName;
    }

    public String getOrganizationName() {
        return organizationName;
    }
}
