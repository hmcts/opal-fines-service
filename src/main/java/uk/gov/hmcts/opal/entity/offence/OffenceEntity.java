package uk.gov.hmcts.opal.entity.offence;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "businessUnit")
@ToString(exclude = "businessUnit")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "offenceId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Offence")
@Entity
@Table(name = "offences")
@NamedEntityGraph(name = OffenceEntity.ENTITY_GRAPH_LITE)
@NamedEntityGraph(
    name = OffenceEntity.ENTITY_GRAPH_FULL,
    attributeNodes = {
        @NamedAttributeNode("businessUnit")
    }
)
public class OffenceEntity {

    public static final String ENTITY_GRAPH_LITE = "OffenceEntity.lite";
    public static final String ENTITY_GRAPH_FULL = "OffenceEntity.full";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offence_id_seq_generator")
    @SequenceGenerator(name = "offence_id_seq_generator", sequenceName = "offence_id_seq", allocationSize = 1)
    @Column(name = "offence_id", nullable = false)
    private Long offenceId;

    @Column(name = "business_unit_id", insertable = false, updatable = false)
    private Short businessUnitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_unit_id", insertable = false, updatable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "cjs_code", length = 10, nullable = false)
    private String cjsCode;

    @Column(name = "offence_title", length = 120)
    private String offenceTitle;

    @Column(name = "offence_title_cy", length = 120)
    private String offenceTitleCy;

    @Column(name = "date_used_from")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime dateUsedFrom;

    @Column(name = "date_used_to")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime dateUsedTo;

    @Column(name = "offence_oas")
    private String offenceOas;

    @Column(name = "offence_oas_cy")
    private String offenceOasCy;
}
