package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.DefendantAccountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class LegacyAccountDetailsResponseDto implements ToJsonString {

        @JsonProperty("defendant_account_id")
        private Long defendantAccountId;

        @JsonProperty("account_number")
        private String accountNumber;

        @JsonProperty("amount_imposed")
        private BigDecimal amountImposed;

        @JsonProperty("amount_paid")
        private BigDecimal amountPaid;

        @JsonProperty("account_balance")
        private BigDecimal accountBalance;

        @JsonProperty("business_unit_id")
        private Integer businessUnitId;

        @JsonProperty("business_unit_name")
        private String businessUnitName;

        @JsonProperty("account_status")
        private String accountStatus;

        @JsonProperty("originator_name")
        private String originatorName;

        @JsonProperty("imposed_hearing_date")
        private LocalDate imposedHearingDate;

        @JsonProperty("imposing_court_code")
        private Integer imposingCourtCode;

        @JsonProperty("last_hearing_date")
        private String lastHearingDate;

        @JsonProperty("last_hearing_court_code")
        private Integer lastHearingCourtCode;

        @JsonProperty("last_changed_date")
        private LocalDate lastChangedDate;

        @JsonProperty("last_movement_date")
        private LocalDate lastMovementDate;

        @JsonProperty("collection_order")
        private Boolean collectionOrder;

        @JsonProperty("enforcing_court_code")
        private Integer enforcingCourtCode;

        @JsonProperty("last_enforcement")
        private String lastEnforcement;

        @JsonProperty("enf_override_result_id")
        private String enfOverrideResultId;

        @JsonProperty("enf_override_enforcer_code")
        private Short enfOverrideEnforcerCode;

        @JsonProperty("enf_override_tfo_lja_code")
        private Integer enfOverrideTfoLjaCode;

        @JsonProperty("prosecutor_case_reference")
        private String prosecutorCaseReference;

        @JsonProperty("account_comments")
        private String accountComments;

        @JsonProperty("payment_terms")
        private PaymentTermsDto paymentTerms;

        @JsonProperty("parties")
        private List<PartyDto> parties;

        @JsonProperty("impositions")
        private List<ImpositionDto> impositions;

        @JsonProperty("account_activities")
        private List<AccountActivityDto> accountActivities;


        public static AccountDetailsDto toAccountDetailsDto (LegacyAccountDetailsResponseDto legacy) {

            PartyDto partyDto = legacy.getParties().get(0);
            PaymentTermsDto paymentTermsDto = legacy.getPaymentTerms();

            return AccountDetailsDto.builder()
                .defendantAccountId(legacy.getDefendantAccountId())
                .accountNumber(legacy.getAccountNumber())
                .fullName(partyDto.getOrganisationName() == null
                              ? partyDto.getFullName()
                              : partyDto.getOrganisationName())
                .accountCT(legacy.getBusinessUnitName())
                .address(DefendantAccountService.buildFullAddress(partyDto.getAddressLine1(),
                                                                  partyDto.getAddressLine2(),
                                                                  partyDto.getAddressLine3(),
                                                                  partyDto.getAddressLine4(),
                                                                  partyDto.getAddressLine5()))
                .postCode(partyDto.getPostcode())
                .dob(partyDto.getBirthDate())
                .detailsChanged(legacy.getLastChangedDate())
                .lastCourtAppAndCourtCode(legacy.getLastHearingDate()
                                            + " " + legacy.getLastHearingCourtCode())
                .lastMovement(legacy.getLastMovementDate())
                .commentField(Collections.singletonList(legacy.getAccountComments()))
                .pcr(legacy.getProsecutorCaseReference())
                .paymentDetails(DefendantAccountService.buildPaymentDetails(paymentTermsDto.getTermsTypeCode(),
                                                                            paymentTermsDto.getInstalmentAmount(),
                                                                            paymentTermsDto.getInstalmentPeriod(),
                                                                            paymentTermsDto.getTermsDate()))
                .lumpSum(paymentTermsDto.getInstalmentLumpSum())
                .commencing(paymentTermsDto.getTermsTypeCode().equals("I")
                             ? paymentTermsDto.getTermsDate()
                             : null)
                .daysInDefault(legacy.getPaymentTerms().getJailDays())
                .sentencedDate(legacy.getImposedHearingDate())
                .lastEnforcement(legacy.getLastEnforcement())
                .override(legacy.getEnfOverrideResultId())
                .enforcer(legacy.getEnfOverrideEnforcerCode())
                .enforcementCourt(legacy.getEnforcingCourtCode())
                .imposed(legacy.getAmountImposed())
                .amountPaid(legacy.getAmountPaid())
                .balance(legacy.getAccountBalance())
                .build();
    }

}
