package crm.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    boolean existsByBranchIdAndMonthAndYear(Long branchId, Integer month, Integer year);

    List<Schedule> findAllByBranchId(Long branchId);

    List<Schedule> findAllByStatus(ScheduleStatus status);

    Optional<Schedule> findByBranchIdAndMonthAndYear(Long branchId, Integer month, Integer year);
}
