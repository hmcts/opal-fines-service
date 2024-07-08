package uk.gov.hmcts.opal.repository.print;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.print.PrintJob;
import uk.gov.hmcts.opal.entity.print.PrintStatus;


import java.time.LocalDateTime;




public interface PrintJobRepository extends JpaRepository<PrintJob, Long> {

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PrintJob p WHERE p.status = :status AND p.createdAt <= :cutoffDate")
    Page<PrintJob> findPendingJobsForUpdate(@Param("status") PrintStatus status,
                                            @Param("cutoffDate") LocalDateTime cutoffDate,
                                            Pageable pageable);
}
