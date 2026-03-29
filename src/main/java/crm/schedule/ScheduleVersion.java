package crm.schedule;

import crm.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_versions")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ScheduleVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    // ВОТ ЭТИХ ДВУХ ПОЛЕЙ НЕ ХВАТАЛО:
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "comment")
    private String comment;
}