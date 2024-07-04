package uk.gov.hmcts.opal.entity.print;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "print_job")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrintJob {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "print_job_id_seq")
    @SequenceGenerator(name = "print_job_id_seq", sequenceName = "print_job_id_seq", allocationSize = 1)
    @Column(name = "print_job_id")
    private Long printJobId;

    @Column(name = "batch_uuid", nullable = false)
    private UUID batchId;

    // completed jobs may be moved to a different table for archiving
    @Column(name = "job_uuid", nullable = false)
    private UUID jobId;


    @Column(name = "xml_data", nullable = false)
    private String xmlData;

    @Column(name = "doc_type", nullable = false, length = 50)
    private String docType;

    @Column(name = "doc_version", nullable = false, length = 20)
    private String docVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PrintStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
