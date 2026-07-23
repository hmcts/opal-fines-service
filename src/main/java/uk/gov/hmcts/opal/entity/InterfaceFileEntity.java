package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnTransformer;

@Entity
@Table(name = "interface_files")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InterfaceFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "interface_file_id_seq_generator")
    @SequenceGenerator(name = "interface_file_id_seq_generator",
        sequenceName = "interface_file_id_seq", allocationSize = 1)
    @Column(name = "interface_file_id", nullable = false)
    private Long interfaceFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_job_id", nullable = false)
    private InterfaceJobEntity interfaceJob;

    @Column(name = "file_name", length = 200, nullable = false)
    private String fileName;

    @Column(name = "created_datetime")
    private LocalDateTime createdDateTime;

    @ColumnTransformer(write = "?::t_interface_file_source_enum")
    @Column(name = "source", columnDefinition = "t_interface_file_source_enum")
    private String source;

    @Column(name = "record_count")
    private Short recordCount;
}
