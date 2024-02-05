package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "document_id_seq_generator")
    @SequenceGenerator(name = "document_id_seq_generator", sequenceName = "document_id_seq", allocationSize = 1)
    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "recipient", length = 4, nullable = false)
    private String recipient;

    @Column(name = "document_language", length = 2, nullable = false)
    private String documentLanguage;

    @Column(name = "signature_source", length = 4, nullable = false)
    private String signatureSource;

    @Column(name = "priority", nullable = false)
    private Short priority;

    @Column(name = "header_type", length = 2, nullable = false)
    private String headerType;

    @Column(name = "document_elements", columnDefinition = "json", nullable = false)
    private String documentElements;

}
