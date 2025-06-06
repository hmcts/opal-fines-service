package uk.gov.hmcts.opal.entity.offence;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "offenceId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractOffenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offence_id_seq_generator")
    @SequenceGenerator(name = "offence_id_seq_generator", sequenceName = "offence_id_seq", allocationSize = 1)
    @Column(name = "offence_id", nullable = false)
    private Long offenceId;

    @Column(name = "cjs_code", length = 10, nullable = false)
    private String cjsCode;

    @Column(name = "offence_title", length = 120)
    private String offenceTitle;

    @Column(name = "offence_title_cy", length = 120)
    private String offenceTitleCy;

    @Column(name = "date_used_from")
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime dateUsedFrom;

    @Column(name = "date_used_to")
    @Temporal(TemporalType.TIMESTAMP)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime dateUsedTo;

    @Column(name = "offence_oas")
    private String offenceOas;

    @Column(name = "offence_oas_cy")
    private String offenceOasCy;
}
