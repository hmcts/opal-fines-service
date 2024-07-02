package uk.gov.hmcts.opal.dto.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.service.opal.DefendantAccountService;

import java.util.Comparator;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyAccountDetailsResponseDto implements ToJsonString {

    @JsonProperty("defendant_account")
    @XmlElement(name = "defendant_account")
    private DefendantAccountDto defendantAccount;


    public static AccountDetailsDto toAccountDetailsDto(LegacyAccountDetailsResponseDto legacy) {

        DefendantAccountDto defendantAccountDto = legacy.getDefendantAccount();
        PartyDto partyDto = defendantAccountDto.getParties().getParty().get(0);
        PaymentTermsDto paymentTermsDto = defendantAccountDto.getPaymentTerms();
        AccountActivityDto accountActivityDto = getLatestAccountActivity(defendantAccountDto.getAccountActivities()
                                                                             .getAccountActivity());

        return AccountDetailsDto.builder()
            .defendantAccountId(defendantAccountDto.getDefendantAccountId())
            .accountNumber(defendantAccountDto.getAccountNumber())
            .fullName(partyDto.getOrganisation()
                          ? partyDto.getOrganisationName()
                          : partyDto.getFullName())
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
            .commentField(List.of(defendantAccountDto.getAccountComments()))
            .accountNotes(accountActivityDto.getActivityText())
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

    private static AccountActivityDto getLatestAccountActivity(List<AccountActivityDto> accountActivities) {

        return accountActivities.stream()
            .max(Comparator.comparing(AccountActivityDto::getPostedDate))
            .orElse(new AccountActivityDto());
    }
}
