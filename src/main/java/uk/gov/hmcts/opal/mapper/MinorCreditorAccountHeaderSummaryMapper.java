package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.common.CreditorAccountTypeReference;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountType;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.mapper.common.BusinessUnitSummaryMapper;

@Mapper(componentModel = "spring",
    uses = {BusinessUnitSummaryMapper.class},
    builder = @Builder(disableBuilder = true))
public interface MinorCreditorAccountHeaderSummaryMapper {

    @Mapping(target = "creditorAccountId", expression = "java(String.valueOf(entity.getCreditorAccountId()))")
    @Mapping(target = "accountNumber", source = "creditorAccountNumber")
    @Mapping(target = "creditorAccountType", source = "creditorAccountType")
    @Mapping(target = "version", source = "versionNumber")
    @Mapping(target = "businessUnitSummary", source = ".")
    @Mapping(target = "partyDetails", expression = "java(toPartyDetails(entity))")
    @Mapping(target = "awardedAmount", source = "awarded")
    @Mapping(target = "paidOutAmount", source = "paidOut")
    @Mapping(target = "awaitingPayoutAmount", source = "awaitingPayment")
    @Mapping(target = "outstandingAmount", source = "outstanding")
    @Mapping(target = "hasAssociatedDefendant", expression = "java(hasAssociatedDefendant(entity))")
    GetMinorCreditorAccountHeaderSummaryResponse toResponse(MinorCreditorAccountHeaderEntity entity);

    default CreditorAccountTypeReference toCreditorAccountTypeReference(CreditorAccountType type) {
        if (type == null) {
            return null;
        }
        return CreditorAccountTypeReference.builder()
            .type(type.name())
            .displayName(type.getLabel())
            .build();
    }

    default PartyDetails toPartyDetails(MinorCreditorAccountHeaderEntity entity) {
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

    default boolean hasAssociatedDefendant(MinorCreditorAccountHeaderEntity entity) {
        return (entity.getAwarded() != null && entity.getAwarded().signum() > 0)
            || (entity.getOutstanding() != null && entity.getOutstanding().signum() > 0);
    }
}
