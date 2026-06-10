package uk.gov.hmcts.opal.mapper.request;

import java.math.BigInteger;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyCreditorAccountPaymentDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.CreditorAccountPaymentDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualAliasCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationAliasCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface UpdateMinorCreditorAccountRequestMapper {

    @Mappings({
        @Mapping(target = "creditorAccountId", source = "creditorAccountId", qualifiedByName = "numberToString"),
        @Mapping(target = "businessUnitId", source = "businessUnitId", qualifiedByName = "numberToString"),
        @Mapping(target = "businessUnitUserId", source = "businessUnitUserId"),
        @Mapping(target = "accountVersion", source = "accountVersion", qualifiedByName = "bigIntegerToInteger"),
        @Mapping(target = "partyDetails", source = "request.partyDetails"),
        @Mapping(target = "address", source = "request.address"),
        @Mapping(target = "payment", source = "request.payment")
    })
    LegacyUpdateMinorCreditorAccountRequest toLegacyUpdateMinorCreditorAccountRequest(
        Long creditorAccountId,
        Short businessUnitId,
        String businessUnitUserId,
        BigInteger accountVersion,
        PatchMinorCreditorAccountRequest request
    );

    LegacyPartyDetails map(PartyDetailsCommon source);

    OrganisationDetails map(OrganisationDetailsCommon source);

    @Mapping(target = "sequenceNumber", source = "sequenceNumber", qualifiedByName = "integerToShort")
    OrganisationDetails.OrganisationAlias map(OrganisationAliasCommon source);

    @Mapping(target = "forenames", source = "forenames")
    IndividualDetails map(IndividualDetailsCommon source);

    @Mapping(target = "sequenceNumber", source = "sequenceNumber", qualifiedByName = "integerToShort")
    IndividualDetails.IndividualAlias map(IndividualAliasCommon source);

    AddressDetailsLegacy map(AddressDetailsCommon source);

    LegacyCreditorAccountPaymentDetails map(CreditorAccountPaymentDetailsCommon source);

    @Named("numberToString")
    default String numberToString(Number value) {
        return value == null ? null : String.valueOf(value.longValue());
    }

    @Named("bigIntegerToInteger")
    default Integer bigIntegerToInteger(BigInteger value) {
        return value == null ? null : value.intValueExact();
    }

    @Named("integerToShort")
    default Short integerToShort(Integer value) {
        return value == null ? null : value.shortValue();
    }
}
