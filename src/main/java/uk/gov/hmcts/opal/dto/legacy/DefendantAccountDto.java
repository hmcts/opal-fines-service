package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.util.LocalDateAdapter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "defendantAccountEntity")
@XmlAccessorType(XmlAccessType.FIELD)
public class DefendantAccountDto {

    @JsonProperty("defendant_account_id")
    @XmlElement(name = "defendant_account_id")
    private Long defendantAccountId;

    @JsonProperty("account_number")
    @XmlElement(name = "account_number")
    private String accountNumber;

    @JsonProperty("amount_imposed")
    @XmlElement(name = "amount_imposed")
    private BigDecimal amountImposed;

    @JsonProperty("amount_paid")
    @XmlElement(name = "amount_paid")
    private BigDecimal amountPaid;

    @JsonProperty("account_balance")
    @XmlElement(name = "account_balance")
    private BigDecimal accountBalance;

    @JsonProperty("business_unit_id")
    @XmlElement(name = "business_unit_id")
    private int businessUnitId;

    @JsonProperty("business_unit_name")
    @XmlElement(name = "business_unit_name")
    private String businessUnitName;

    @JsonProperty("account_status")
    @XmlElement(name = "account_status")
    private String accountStatus;

    @JsonProperty("originator_name")
    @XmlElement(name = "originator_name")
    private String originatorName;

    @JsonProperty("imposed_hearing_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlElement(name = "imposed_hearing_date")
    private LocalDate imposedHearingDate;

    @JsonProperty("imposing_court_code")
    @XmlElement(name = "imposing_court_code")
    private int imposingCourtCode;

    @JsonProperty("last_hearing_date")
    @XmlElement(name = "last_hearing_date")
    private String lastHearingDate;

    @XmlElement(name = "court")
    @JsonProperty("court")
    private String court;

    @JsonProperty("last_hearing_court_code")
    @XmlElement(name = "last_hearing_court_code")
    private int lastHearingCourtCode;

    @JsonProperty("last_changed_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlElement(name = "last_changed_date")
    private LocalDate lastChangedDate;

    @JsonProperty("last_movement_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    @XmlElement(name = "last_movement_date")
    private LocalDate lastMovementDate;

    @JsonProperty("collection_order")
    @XmlElement(name = "collection_order")
    private boolean collectionOrder;

    @JsonProperty("enforcing_court_code")
    @XmlElement(name = "enforcing_court_code")
    private int enforcingCourtCode;

    @JsonProperty("last_enforcement")
    @XmlElement(name = "last_enforcement")
    private String lastEnforcement;

    @JsonProperty("enf_override_result_id")
    @XmlElement(name = "enf_override_result_id")
    private String enfOverrideResultId;

    @JsonProperty("enf_override_enforcer_code")
    @XmlElement(name = "enf_override_enforcer_code")
    private Short enfOverrideEnforcerCode;

    @JsonProperty("enf_override_tfo_lja_code")
    @XmlElement(name = "enf_override_tfo_lja_code")
    private int enfOverrideTfoLjaCode;

    @JsonProperty("prosecutor_case_reference")
    @XmlElement(name = "prosecutor_case_reference")
    private String prosecutorCaseReference;

    @JsonProperty("account_comments")
    @XmlElement(name = "account_comments")
    private String accountComments;

    @JsonProperty("payment_terms")
    @XmlElement(name = "payment_terms")
    private PaymentTermsDto paymentTerms;

    @JsonProperty("parties")
    @XmlElement(name = "parties")
    private PartiesDto parties;

    @JsonProperty("impositions")
    @XmlElement(name = "impositions")
    private ImpositionsDto impositions;

    @XmlElement(name = "organisation_name")
    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("account_activities")
    @XmlElement(name = "account_activities")
    private AccountActivitiesDto accountActivities;

    @XmlElement(name = "organisation")
    private boolean organisation;

    @XmlElement(name = "address_line_1")
    private String addressLine1;

    @XmlElement(name = "postcode")
    private String postcode;

    @XmlElement(name = "last_enforcement_action")
    private String lastEnforcementAction;

    @XmlElement(name = "defendant_title")
    private String defendantTitle;

    @XmlElement(name = "defendant_firstnames")
    private String defendantFirstNames;

    @XmlElement(name = "defendant_surname")
    private String defendantSurname;

    @XmlElement(name = "birth_date")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate birthDate;

    @XmlElement(name = "national_insurance_number")
    private String nationalInsuranceNumber;

    @XmlElement(name = "parent_guardian_surname")
    private String parentGuardianSurname;

    @XmlElement(name = "parent_guardian_firstnames")
    private String parentGuardianFirstNames;

    @XmlElementWrapper(name = "aliases")
    @XmlElement(name = "alias")
    private List<AliasDto> aliases;

    public DefendantAccountEntity toEntity() {
        return DefendantAccountEntity.builder()
            .defendantAccountId(this.defendantAccountId)
            .accountNumber(this.accountNumber)
            .accountStatus(this.accountStatus)
            .accountBalance(this.accountBalance)
            .amountPaid(this.amountPaid)
            .amountImposed(this.amountImposed)
            .businessUnit(
                BusinessUnitEntity.builder()
                    .businessUnitId((short) this.businessUnitId)
                    .businessUnitName(this.businessUnitName)
                    .build()
            )
            .build();
    }

    public static DefendantAccountDto fromEntity(DefendantAccountEntity entity) {
        return DefendantAccountDto.builder()
            .defendantAccountId(entity.getDefendantAccountId())
            .accountNumber(entity.getAccountNumber())
            .accountStatus(entity.getAccountStatus())
            .accountBalance(entity.getAccountBalance())
            .amountPaid(entity.getAmountPaid())
            .amountImposed(entity.getAmountImposed())
            .businessUnitId(entity.getBusinessUnit() != null ? entity.getBusinessUnit().getBusinessUnitId() : 0)
            .businessUnitName(entity.getBusinessUnit() != null ? entity.getBusinessUnit().getBusinessUnitName() : null)
            .build();
    }
}
