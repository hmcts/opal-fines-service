package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

@Entity
@Table(name = "offences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "offenceId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OffenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offence_id_seq_generator")
    @SequenceGenerator(name = "offence_id_seq_generator", sequenceName = "offence_id_seq", allocationSize = 1)
    @Column(name = "offence_id", nullable = false)
    private Long offenceId;

    @Column(name = "cjs_code", length = 10, nullable = false)
    private String cjsCode;

    @ManyToOne
    @JoinColumn(name = "business_unit_id")
    private BusinessUnitEntity businessUnit;

    @Column(name = "offence_title", length = 120)
    private String offenceTitle;

    @Column(name = "offence_title_cy", length = 120)
    private String offenceTitleCy;

    @Column(name = "date_used_to")
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime dateUsedTo;

    @Column(name = "offence_oas")
    private String offenceOas;

    @Column(name = "offence_oas_cy")
    private String offenceOasCy;
}
