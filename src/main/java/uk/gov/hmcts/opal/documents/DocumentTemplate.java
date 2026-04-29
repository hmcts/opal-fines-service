package uk.gov.hmcts.opal.documents;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;
import org.hibernate.annotations.ColumnTransformer;

@Entity
@Table(name = "document_template")
@Data
public class DocumentTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnTransformer(write = "?::document_template_type_enum", read = "document_type::text")
    private DocumentType documentType;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Column(name = "data_mapping_class", nullable = false)
    private String dataMappingClass;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "active_from")
    private LocalDateTime activeFrom;

    @Column(name = "active_to")
    private LocalDateTime activeTo;


    public enum DocumentType {
        HMRC_REPORT
    }
}
