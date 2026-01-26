package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.common.BusinessUnitSummary;
import uk.gov.hmcts.opal.dto.common.CreditorAccountTypeReference;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.util.Versioned;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetMinorCreditorAccountHeaderSummaryResponse implements ToJsonString, Versioned {

    @JsonProperty("creditor_account_id")
    private String creditorAccountId;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("creditor_account_type")
    private CreditorAccountTypeReference creditorAccountType;

    @JsonIgnore
    private BigInteger version;

    @JsonProperty("business_unit_summary")
    private BusinessUnitSummary businessUnitSummary;

    @JsonProperty("party_details")
    private PartyDetails partyDetails;

    @JsonProperty("awarded_amount")
    private BigDecimal awardedAmount;

    @JsonProperty("paid_out_amount")
    private BigDecimal paidOutAmount;

    @JsonProperty("awaiting_payout_amount")
    private BigDecimal awaitingPayoutAmount;

    @JsonProperty("outstanding_amount")
    private BigDecimal outstandingAmount;

    @JsonProperty("has_associated_defendant")
    private Boolean hasAssociatedDefendant;

    public static GetMinorCreditorAccountHeaderSummaryResponse fromEntity(MinorCreditorAccountHeaderEntity entity) {
        return GetMinorCreditorAccountHeaderSummaryResponse.builder()
            .creditorAccountId(String.valueOf(entity.getCreditorAccountId()))
            .accountNumber(entity.getCreditorAccountNumber())
            .creditorAccountType(toCreditorAccountTypeReference(entity.getCreditorAccountType()))
            .version(entity.getVersionNumber() == null ? null : BigInteger.valueOf(entity.getVersionNumber()))
            .businessUnitSummary(toBusinessUnitSummary(entity))
            .partyDetails(toPartyDetails(entity))
            .awardedAmount(entity.getAwarded())
            .paidOutAmount(entity.getPaidOut())
            .awaitingPayoutAmount(entity.getAwaitingPayment())
            .outstandingAmount(entity.getOutstanding())
            .hasAssociatedDefendant(hasAssociatedDefendant(entity))
            .build();
    }

    private static CreditorAccountTypeReference toCreditorAccountTypeReference(String typeCode) {
        return CreditorAccountTypeReference.builder()
            .type(typeCode)
            .displayName(CreditorAccountType.getDisplayName(typeCode))
            .build();
    }

    private static BusinessUnitSummary toBusinessUnitSummary(MinorCreditorAccountHeaderEntity entity) {
        return BusinessUnitSummary.builder()
            .businessUnitId(String.valueOf(entity.getBusinessUnitId()))
            .businessUnitName(entity.getBusinessUnitName())
            .welshSpeaking(entity.isWelshLanguage() ? "Y" : "N")
            .build();
    }

    private static PartyDetails toPartyDetails(MinorCreditorAccountHeaderEntity entity) {
        boolean isOrg = entity.isOrganisation();
        return PartyDetails.builder()
            .partyId(String.valueOf(entity.getPartyId()))
            .organisationFlag(isOrg)
            .organisationDetails(isOrg ? OrganisationDetails.builder()
                .organisationName(entity.getOrganisationName())
                .build() : null)
            .individualDetails(!isOrg ? IndividualDetails.builder()
                .title(entity.getTitle())
                .forenames(entity.getForenames())
                .surname(entity.getSurname())
                .build() : null)
            .build();
    }

    private static boolean hasAssociatedDefendant(MinorCreditorAccountHeaderEntity entity) {
        return (entity.getAwarded() != null && entity.getAwarded().signum() > 0)
            || (entity.getOutstanding() != null && entity.getOutstanding().signum() > 0);
    }
}
