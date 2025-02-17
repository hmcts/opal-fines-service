package uk.gov.hmcts.opal.entity.defendant;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class DefendantAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "defendant_account_id_seq")
    @SequenceGenerator(name = "defendant_account_id_seq", sequenceName = "defendant_account_id_seq", allocationSize = 1)
    @Column(name = "defendant_account_id")
    private Long defendantAccountId;

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
    private boolean allowWriteoffs;

    @Column(name = "allow_cheques")
    private boolean allowCheques;

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
    private boolean collectionOrder;

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
    private boolean paymentCardRequested;

    @Column(name = "payment_card_requested_date")
    @Temporal(TemporalType.DATE)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate paymentCardRequestedDate;

    @Column(name = "payment_card_requested_by", length = 20)
    private String paymentCardRequestedBy;

    @Column(name = "prosecutor_case_reference", length = 40)
    private String prosecutorCaseReference;

    @Column(name = "enforcement_case_status", length = 10)
    private String enforcementCaseStatus;

    @Column(name = "account_type", length = 30, nullable = false)
    @Convert(converter = DefendantAccountTypeConverter.class)
    private DefendantAccountType accountType;

    @Column(name = "business_unit_id", nullable = false, insertable = false, updatable = false)
    private Short businessUnitId;

    @Column(name = "enforcing_court_id", nullable = false)
    private Long enforcingCourtId;

    @Column(name = "last_hearing_court_id", nullable = false)
    private Long lastHearingCourtId;

    @Entity
    @Data
    @SuperBuilder
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @Table(name = "defendant_accounts")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "defendantAccountId")
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Lite extends DefendantAccount {
    }
}
