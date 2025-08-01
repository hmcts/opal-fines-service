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
import uk.gov.hmcts.opal.disco.opal.DiscoDefendantAccountService;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
    private LegacyDefendantAccountDto defendantAccount;


    public static AccountDetailsDto toAccountDetailsDto(LegacyAccountDetailsResponseDto legacy) {

        LegacyDefendantAccountDto legacyDefendantAccountDto = legacy.getDefendantAccount();
        LegacyPartyDto legacyPartyDto = legacyDefendantAccountDto.getParties().getParty().get(0);
        LegacyPaymentTermsDto legacyPaymentTermsDto = legacyDefendantAccountDto.getPaymentTerms();
        LegacyAccountActivityDto legacyAccountActivityDto =
            getLatestAccountActivity(legacyDefendantAccountDto.getAccountActivities()
                                                                             .getAccountActivity());

        return AccountDetailsDto.builder()
            .defendantAccountId(legacyDefendantAccountDto.getDefendantAccountId())
            .accountNumber(legacyDefendantAccountDto.getAccountNumber())
            .fullName(legacyPartyDto.getOrganisation()
                          ? legacyPartyDto.getOrganisationName()
                          : legacyPartyDto.getFullName())
            .accountCT(legacyDefendantAccountDto.getBusinessUnitName())
            .businessUnitId((short) legacyDefendantAccountDto.getBusinessUnitId())
            .address(DiscoDefendantAccountService.buildFullAddress(
                legacyPartyDto.getAddressLine1(),
                legacyPartyDto.getAddressLine2(),
                legacyPartyDto.getAddressLine3(),
                legacyPartyDto.getAddressLine4(),
                legacyPartyDto.getAddressLine5()
            ))
            .postCode(legacyPartyDto.getPostcode())
            .dob(legacyPartyDto.getBirthDate())
            .detailsChanged(legacyDefendantAccountDto.getLastChangedDate())
            .lastCourtAppAndCourtCode(legacyDefendantAccountDto.getLastHearingDate()
                                          + " " + legacyDefendantAccountDto.getLastHearingCourtCode())
            .lastMovement(legacyDefendantAccountDto.getLastMovementDate())
            .commentField(
                Optional.ofNullable(legacyDefendantAccountDto.getAccountComments())
                    .map(List::of)
                    .orElse(Collections.emptyList())
            )
            .accountNotes(legacyAccountActivityDto.getActivityText())
            .pcr(legacyDefendantAccountDto.getProsecutorCaseReference())
            .paymentDetails(
                legacyPaymentTermsDto != null
                    ? DiscoDefendantAccountService.buildPaymentDetails(
                    legacyPaymentTermsDto.getTermsTypeCode(),
                    legacyPaymentTermsDto.getInstalmentAmount(),
                    legacyPaymentTermsDto.getInstalmentPeriod(),
                    legacyPaymentTermsDto.getTermsDate()
                )
                    : null
            )
            .lumpSum(legacyPaymentTermsDto != null ? legacyPaymentTermsDto.getInstalmentLumpSum() : null)
            .commencing(legacyPaymentTermsDto.getTermsTypeCode().equals("I")
                            ? legacyPaymentTermsDto.getTermsDate()
                            : null)
            .daysInDefault(legacyDefendantAccountDto.getPaymentTerms().getJailDays())
            .sentencedDate(legacyDefendantAccountDto.getImposedHearingDate())
            .lastEnforcement(legacyDefendantAccountDto.getLastEnforcement())
            .override(legacyDefendantAccountDto.getEnfOverrideResultId())
            .enforcer(legacyDefendantAccountDto.getEnfOverrideEnforcerCode())
            .enforcementCourt(legacyDefendantAccountDto.getEnforcingCourtCode())
            .imposed(legacyDefendantAccountDto.getAmountImposed())
            .amountPaid(legacyDefendantAccountDto.getAmountPaid())
            .balance(legacyDefendantAccountDto.getAccountBalance())
            .build();
    }

    private static LegacyAccountActivityDto getLatestAccountActivity(List<LegacyAccountActivityDto> accountActivities) {

        return accountActivities.stream()
            .max(Comparator.comparing(LegacyAccountActivityDto::getPostedDate))
            .orElse(new LegacyAccountActivityDto());
    }
}
