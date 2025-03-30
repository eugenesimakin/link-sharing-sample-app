package linksharing.db;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    private String email;

    private String firstName;
    private String lastName;
    private String imageUrl;
    private Timestamp createdOn;

    @OneToMany(mappedBy = "user")
    private List<Link> links;

    @PrePersist
    public void prePersist() {
        createdOn = new Timestamp(System.currentTimeMillis());
    }
}
