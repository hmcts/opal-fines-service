package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnit;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

@Entity
@Table(name = "amendments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "amendmentId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AmendmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "amendment_id_seq_generator")
    @SequenceGenerator(name = "amendment_id_seq_generator", sequenceName = "amendment_id_seq", allocationSize = 1)
    @Column(name = "amendment_id", nullable = false)
    private Long amendmentId;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "business_unit_id", nullable = false)
    private BusinessUnit.Lite businessUnit;

    @Column(name = "associated_record_type", length = 30, nullable = false)
    private String associatedRecordType;

    @Column(name = "associated_record_id", length = 30, nullable = false)
    private String associatedRecordId;

    @Column(name = "amended_date")
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime amendedDate;

    @Column(name = "amended_by", length = 20, nullable = false)
    private String amendedBy;

    @Column(name = "field_code", nullable = false)
    private Short fieldCode;

    @Column(name = "old_value", length = 200)
    private String oldValue;

    @Column(name = "new_value", length = 200)
    private String newValue;

    @Column(name = "case_reference", length = 40)
    private String caseReference;

    @Column(name = "function_code", length = 30)
    private String functionCode;
}
