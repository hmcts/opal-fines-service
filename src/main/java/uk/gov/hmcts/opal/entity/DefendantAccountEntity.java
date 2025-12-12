package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.converter.DefendantAccountTypeConverter;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.util.LocalDateAdapter;
import uk.gov.hmcts.opal.util.Versioned;

@Entity
@Data
@Table(name = "defendant_accounts")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "defendantAccountId")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DefendantAccountEntity implements Versioned {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "defendant_account_id_seq")
    @SequenceGenerator(name = "defendant_account_id_seq", sequenceName = "defendant_account_id_seq", allocationSize = 1)
    @Column(name = "defendant_account_id")
    private Long defendantAccountId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", referencedColumnName = "business_unit_id", nullable = false)
    private BusinessUnitFullEntity businessUnit;

    @Column(name = "account_number", length = 20)
    private String accountNumber;

    @Column(name = "imposed_hearing_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate imposedHearingDate;

    @Column(name = "imposing_court_id")
    private Long imposingCourtId;

    @Column(name = "amount_imposed", precision = 18, scale = 2)
    private BigDecimal amountImposed;

    @Column(name = "amount_paid", precision = 18, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "account_balance", precision = 18, scale = 2)
    private BigDecimal accountBalance;

    @Column(name = "account_status", length = 2)
    private String accountStatus;

    @Column(name = "completed_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate completedDate;

    @ManyToOne
    @JoinColumn(name = "enforcing_court_id", referencedColumnName = "court_id")
    private CourtEntity.Lite enforcingCourt;

    @ManyToOne
    @JoinColumn(name = "last_hearing_court_id", referencedColumnName = "court_id")
    private CourtEntity.Lite lastHearingCourt;

    @Column(name = "last_hearing_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate lastHearingDate;

    @Column(name = "last_movement_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate lastMovementDate;

    @Column(name = "last_enforcement", length = 6)
    private String lastEnforcement;

    @Column(name = "last_changed_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate lastChangedDate;

    @Column(name = "originator_name", length = 100)
    private String originatorName;

    @Column(name = "originator_id", length = 40)
    private String originatorId;

    @Column(name = "originator_type", length = 10)
    private String originatorType;

    @Column(name = "allow_writeoffs")
    private Boolean allowWriteoffs;

    @Column(name = "allow_cheques")
    private Boolean allowCheques;

    @Column(name = "cheque_clearance_period")
    private Short chequeClearancePeriod;

    @Column(name = "credit_trans_clearance_period")
    private Short creditTransferClearancePeriod;

    @Column(name = "enf_override_result_id", length = 10)
    private String enforcementOverrideResultId;

    @Column(name = "enf_override_enforcer_id")
    private Long enforcementOverrideEnforcerId;

    @Column(name = "enf_override_tfo_lja_id")
    private Short enforcementOverrideTfoLjaId;

    @Column(name = "unit_fine_detail", length = 100)
    private String unitFineDetail;

    @Column(name = "unit_fine_value", precision = 18, scale = 2)
    private BigDecimal unitFineValue;

    @Column(name = "collection_order")
    private Boolean collectionOrder;

    @Column(name = "collection_order_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate collectionOrderEffectiveDate;

    @Column(name = "further_steps_notice_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate furtherStepsNoticeDate;

    @Column(name = "confiscation_order_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate confiscationOrderDate;

    @Column(name = "fine_registration_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate fineRegistrationDate;

    @Column(name = "consolidated_account_type", length = 1)
    private String consolidatedAccountType;

    @Column(name = "payment_card_requested")
    private Boolean paymentCardRequested;

    @Column(name = "payment_card_requested_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate paymentCardRequestedDate;

    @Column(name = "payment_card_requested_by", length = 20)
    private String paymentCardRequestedBy;

    @Column(name = "payment_card_requested_by_name", length = 100)
    private String paymentCardRequestedByName;

    @Column(name = "prosecutor_case_reference", length = 40)
    private String prosecutorCaseReference;

    @Column(name = "enforcement_case_status", length = 10)
    private String enforcementCaseStatus;

    @OneToMany(mappedBy = "defendantAccount", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<DefendantAccountPartiesEntity> parties;

    @Column(name = "account_type", length = 30, nullable = false)
    @Convert(converter = DefendantAccountTypeConverter.class)
    private DefendantAccountType accountType;

    @Column(name = "suspended_committal_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate suspendedCommittalDate;

    @Column(name = "account_comments")
    private String accountComments;

    @Column(name = "account_note_1")
    private String accountNote1;

    @Column(name = "account_note_2")
    private String accountNote2;

    @Column(name = "account_note_3")
    private String accountNote3;

    @Column(name = "jail_days")
    private Integer jailDays;

    /**
     * This avoids potential infinite recursion issues when
     * printing entities with bidirectional relationships.
     */
    @Override
    public String toString() {
        return "DefendantAccountEntity{"
            + "defendantAccountId=" + defendantAccountId
            + '}';
    }

    @Column(name = "version_number")
    @Version
    private Long versionNumber;

    @Override
    public BigInteger getVersion() {
        return Optional.ofNullable(versionNumber).map(BigInteger::valueOf).orElse(null);
    }
}
