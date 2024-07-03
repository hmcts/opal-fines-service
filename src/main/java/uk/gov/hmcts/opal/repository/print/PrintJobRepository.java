package uk.gov.hmcts.opal.repository.print;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.print.PrintJob;


import java.time.LocalDateTime;
import java.util.List;



public interface PrintJobRepository extends JpaRepository<PrintJob, Long> {

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PrintJob p WHERE p.status = :status AND p.createdAt <= :cutoffDate")
    List<PrintJob> findPendingJobsForUpdate(@Param("status") String status, @Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Transactional
    @Query("UPDATE PrintJob p SET p.status = :newStatus WHERE p.status = :currentStatus AND p.id IN :ids")
    int updateStatus(@Param("currentStatus") String currentStatus, @Param("newStatus") String newStatus, @Param("ids") List<Long> ids);
}
