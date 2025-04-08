package linksharing.metrics;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import static jakarta.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name = "metrics")
@AllArgsConstructor
@NoArgsConstructor
public class Metric {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String userEmail;
    private String linkUrl;
    private String clientIp;
    private String userAgent;
    private Timestamp clickedAt;
}
