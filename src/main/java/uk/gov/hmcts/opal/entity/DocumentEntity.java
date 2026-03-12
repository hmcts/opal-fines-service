package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.converter.PriorityConverter;
import uk.gov.hmcts.opal.entity.converter.SignatureSourceConverter;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "documentId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentEntity {

    @Id
    @Column(name = "document_id", length = 10, nullable = false)
    private String documentId;

    @Column(name = "recipient", length = 4, nullable = false)
    @Enumerated(EnumType.STRING)
    private Recipient recipient;

    @Column(name = "document_language", length = 2, nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentLanguage documentLanguage;

    @Column(name = "signature_source", length = 4, nullable = false)
    @Convert(converter = SignatureSourceConverter.class)
    private SignatureSource signatureSource;

    @Column(name = "priority", nullable = false)
    @Convert(converter = PriorityConverter.class)
    private Priority priority;

    @Column(name = "header_type", length = 2, nullable = false)
    @Enumerated(EnumType.STRING)
    private HeaderType headerType;

    @Column(name = "document_elements", columnDefinition = "json", nullable = false)
    private String documentElements;

}
