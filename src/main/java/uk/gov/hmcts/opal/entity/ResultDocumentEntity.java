package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
@Table(name = "result_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "resultDocumentId")
public class ResultDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "result_document_id_seq_generator")
    @SequenceGenerator(name = "result_document_id_seq_generator", sequenceName = "result_document_id_seq",
        allocationSize = 1)
    @Column(name = "result_document_id", nullable = false)
    private Long resultDocumentId;

    @Column(name = "result_id", length = 6, nullable = false)
    private String resultId;

    @Column(name = "document_id", length = 10, nullable = false)
    private String documentId;

    @Column(name = "cy_document_id", length = 10, nullable = false)
    private String cyDocumentId;

}
