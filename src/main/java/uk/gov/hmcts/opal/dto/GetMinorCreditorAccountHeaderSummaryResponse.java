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

    @JsonIgnore
    private BigInteger version;

    @JsonProperty("party")
    private PartyDetails party;

    @JsonProperty("business_unit")
    private BusinessUnitSummary businessUnit;

    @JsonProperty("creditor")
    private CreditorHeader creditor;

    @JsonProperty("financials")
    private Financials financials;

    public static GetMinorCreditorAccountHeaderSummaryResponse fromEntity(MinorCreditorAccountHeaderEntity entity) {
        return GetMinorCreditorAccountHeaderSummaryResponse.builder()
            .version(entity.getVersionNumber() == null ? null : BigInteger.valueOf(entity.getVersionNumber()))
            .party(toPartyDetails(entity))
            .businessUnit(toBusinessUnitSummary(entity))
            .creditor(CreditorHeader.builder()
                .accountId(String.valueOf(entity.getCreditorAccountId()))
                .accountNumber(entity.getCreditorAccountNumber())
                .accountType(toCreditorAccountTypeReference(entity.getCreditorAccountType()))
                .build())
            .financials(Financials.builder()
                .awarded(entity.getAwarded())
                .paidOut(entity.getPaidOut())
                .awaitingPayout(entity.getAwaitingPayment())
                .outstanding(entity.getOutstanding())
                .build())
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreditorHeader {

        @JsonProperty("account_id")
        private String accountId;

        @JsonProperty("account_number")
        private String accountNumber;

        @JsonProperty("account_type")
        private CreditorAccountTypeReference accountType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Financials {

        @JsonProperty("awarded")
        private BigDecimal awarded;

        @JsonProperty("paid_out")
        private BigDecimal paidOut;

        @JsonProperty("awaiting_payout")
        private BigDecimal awaitingPayout;

        @JsonProperty("outstanding")
        private BigDecimal outstanding;
    }
}
