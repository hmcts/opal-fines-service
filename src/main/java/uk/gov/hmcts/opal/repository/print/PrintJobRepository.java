package uk.gov.hmcts.opal.repository.print;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.print.PrintJob;
import uk.gov.hmcts.opal.entity.print.PrintStatus;


import java.time.LocalDateTime;
import java.util.List;



public interface PrintJobRepository extends JpaRepository<PrintJob, Long> {

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PrintJob p WHERE p.status = :status AND p.createdAt <= :cutoffDate")
    List<PrintJob> findPendingJobsForUpdate(@Param("status") PrintStatus status, @Param("cutoffDate") LocalDateTime cutoffDate);

   }
