package uk.gov.hmcts.opal.entity.imposition;

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
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountFullEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;

import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "impositions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "impositionId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@EqualsAndHashCode(exclude = {"defendantAccount", "imposingCourt", "creditorAccount"})
@ToString(exclude = {"defendantAccount", "imposingCourt", "creditorAccount"})
@NamedEntityGraph(name = ImpositionEntity.ENTITY_GRAPH_LITE)
@NamedEntityGraph(
    name = ImpositionEntity.ENTITY_GRAPH_FULL,
    attributeNodes = {
        @NamedAttributeNode("defendantAccount"),
        @NamedAttributeNode("imposingCourt"),
        @NamedAttributeNode("creditorAccount")
    }
)
public class ImpositionEntity {

    public static final String ENTITY_GRAPH_LITE = "ImpositionEntity.lite";
    public static final String ENTITY_GRAPH_FULL = "ImpositionEntity.full";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imposition_id_seq_generator")
    @SequenceGenerator(name = "imposition_id_seq_generator", sequenceName = "imposition_id_seq", allocationSize = 1)
    @Column(name = "imposition_id", nullable = false)
    private Long impositionId;

    @Column(name = "defendant_account_id", insertable = false, updatable = false, nullable = false)
    private Long defendantAccountId;

    @Column(name = "posted_date", nullable = false)
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime postedDate;

    @Column(name = "posted_by", length = 20)
    private String postedBy;

    @Column(name = "posted_by_name", length = 100)
    private String postedByUsername;

    @Column(name = "original_posted_date")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime originalPostedDate;

    @Column(name = "result_id", length = 6, nullable = false)
    private String resultId;

    @Column(name = "imposing_court_id")
    private Long imposingCourtId;

    @Column(name = "imposed_date")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime imposedDate;

    @Column(name = "imposed_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal imposedAmount;

    @Column(name = "paid_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal paidAmount;

    @Column(name = "offence_id")
    private Long offenceId;

    @Column(name = "offence_title", length = 120)
    private String offenceTitle;

    @Column(name = "offence_code", length = 10)
    private String offenceCode;

    @Column(name = "original_imposition_id")
    private Long originalImpositionId;

    @Column(name = "creditor_account_id", insertable = false, updatable = false, nullable = false)
    private Long creditorAccountId;

    @Column(name = "unit_fine_adjusted")
    private Boolean unitFineAdjusted;

    @Column(name = "unit_fine_units")
    private Short unitFineUnits;

    @Column(name = "completed")
    private Boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defendant_account_id", insertable = false, updatable = false)
    private DefendantAccountEntity defendantAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imposing_court_id", insertable = false, updatable = false, nullable = false)
    private CourtEntity imposingCourt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creditor_account_id", insertable = false, updatable = false)
    private CreditorAccountFullEntity creditorAccount;
}
