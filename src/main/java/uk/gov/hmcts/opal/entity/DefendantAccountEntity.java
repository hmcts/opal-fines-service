package uk.gov.hmcts.opal.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "defendant_accounts")
public class DefendantAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "defendant_account_id_seq")
    @SequenceGenerator(name = "defendant_account_id_seq", sequenceName = "defendant_account_id_seq", allocationSize = 1)
    @Column(name = "defendant_account_id")
    private Long defendantAccountId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", referencedColumnName = "business_unit_id", nullable = false)
    private BusinessUnitsEntity businessUnitId;

    @Column(name = "account_number", length = 20)
    private String accountNumber;

    @Column(name = "imposed_hearing_date")
    @Temporal(TemporalType.DATE)
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
    private LocalDate completedDate;

    @ManyToOne
    @JoinColumn(name = "enforcing_court_id", referencedColumnName = "court_id", nullable = false)
    private CourtsEntity enforcingCourtId;

    @ManyToOne
    @JoinColumn(name = "last_hearing_court_id", referencedColumnName = "court_id", nullable = false)
    private CourtsEntity lastHearingCourtId;

    @Column(name = "last_hearing_date")
    @Temporal(TemporalType.DATE)
    private LocalDate lastHearingDate;

    @Column(name = "last_movement_date")
    @Temporal(TemporalType.DATE)
    private LocalDate lastMovementDate;

    @Column(name = "last_enforcement", length = 6)
    private String lastEnforcement;

    @Column(name = "last_changed_date")
    @Temporal(TemporalType.DATE)
    private LocalDate lastChangedDate;

    @Column(name = "originator_name", length = 100)
    private String originatorName;

    @Column(name = "originator_reference", length = 40)
    private String originatorReference;

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
    private LocalDate collectionOrderEffectiveDate;

    @Column(name = "further_steps_notice_date")
    @Temporal(TemporalType.DATE)
    private LocalDate furtherStepsNoticeDate;

    @Column(name = "confiscation_order_date")
    @Temporal(TemporalType.DATE)
    private LocalDate confiscationOrderDate;

    @Column(name = "fine_registration_date")
    @Temporal(TemporalType.DATE)
    private LocalDate fineRegistrationDate;

    @Column(name = "consolidated_account_type", length = 1)
    private String consolidatedAccountType;

    @Column(name = "payment_card_requested")
    private boolean paymentCardRequested;

    @Column(name = "payment_card_requested_date")
    @Temporal(TemporalType.DATE)
    private LocalDate paymentCardRequestedDate;

    @Column(name = "payment_card_requested_by", length = 20)
    private String paymentCardRequestedBy;

    @Column(name = "prosecutor_case_reference", length = 40)
    private String prosecutorCaseReference;

    @Column(name = "enforcement_case_status", length = 10)
    private String enforcementCaseStatus;

    @OneToMany(mappedBy = "defendantAccount", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<DefendantAccountPartiesEntity> parties;

}
