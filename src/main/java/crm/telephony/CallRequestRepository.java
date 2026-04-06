package crm.telephony;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallRequestRepository extends JpaRepository<CallRequest, Long> {
    // Нам понадобится искать звонки, которые еще никто не взял
    List<CallRequest> findByStatus(CallStatus status);
}