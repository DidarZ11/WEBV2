package crm.telephony;

import crm.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "operator_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperatorStatus {

    @Id
    private Long id; // Это будет ID юзера

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Говорит Hibernate использовать ID юзера как ID этой сущности
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_online")
    private boolean isOnline;

    @Column(name = "last_active")
    private LocalDateTime lastActive;
}