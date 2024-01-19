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

import java.util.Collections;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class LegacyAccountDetailsResponseDto implements ToJsonString {

    @JsonProperty("defendant_account")
    private DefendantAccountDto defendantAccount;


    public static AccountDetailsDto toAccountDetailsDto(LegacyAccountDetailsResponseDto legacy) {

        DefendantAccountDto defendantAccountDto = legacy.getDefendantAccount();
        PartyDto partyDto = defendantAccountDto.getParties().get(0);
        PaymentTermsDto paymentTermsDto = defendantAccountDto.getPaymentTerms();

        return AccountDetailsDto.builder()
            .defendantAccountId(defendantAccountDto.getDefendantAccountId())
            .accountNumber(defendantAccountDto.getAccountNumber())
            .fullName(partyDto.getOrganisationName() == null
                          ? partyDto.getFullName()
                          : partyDto.getOrganisationName())
            .accountCT(defendantAccountDto.getBusinessUnitName())
            .address(DefendantAccountService.buildFullAddress(
                partyDto.getAddressLine1(),
                partyDto.getAddressLine2(),
                partyDto.getAddressLine3(),
                partyDto.getAddressLine4(),
                partyDto.getAddressLine5()
            ))
            .postCode(partyDto.getPostcode())
            .dob(partyDto.getBirthDate())
            .detailsChanged(defendantAccountDto.getLastChangedDate())
            .lastCourtAppAndCourtCode(defendantAccountDto.getLastHearingDate()
                                          + " " + defendantAccountDto.getLastHearingCourtCode())
            .lastMovement(defendantAccountDto.getLastMovementDate())
            .commentField(Collections.singletonList(defendantAccountDto.getAccountComments()))
            .pcr(defendantAccountDto.getProsecutorCaseReference())
            .paymentDetails(DefendantAccountService.buildPaymentDetails(
                paymentTermsDto.getTermsTypeCode(),
                paymentTermsDto.getInstalmentAmount(),
                paymentTermsDto.getInstalmentPeriod(),
                paymentTermsDto.getTermsDate()
            ))
            .lumpSum(paymentTermsDto.getInstalmentLumpSum())
            .commencing(paymentTermsDto.getTermsTypeCode().equals("I")
                            ? paymentTermsDto.getTermsDate()
                            : null)
            .daysInDefault(defendantAccountDto.getPaymentTerms().getJailDays())
            .sentencedDate(defendantAccountDto.getImposedHearingDate())
            .lastEnforcement(defendantAccountDto.getLastEnforcement())
            .override(defendantAccountDto.getEnfOverrideResultId())
            .enforcer(defendantAccountDto.getEnfOverrideEnforcerCode())
            .enforcementCourt(defendantAccountDto.getEnforcingCourtCode())
            .imposed(defendantAccountDto.getAmountImposed())
            .amountPaid(defendantAccountDto.getAmountPaid())
            .balance(defendantAccountDto.getAccountBalance())
            .build();
    }

}
