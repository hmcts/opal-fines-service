package uk.gov.hmcts.opal.mapper.legacy;

import java.math.BigInteger;
import java.time.LocalDate;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.AddressDetailsLegacy;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateMinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyCreditorAccountPaymentDetails;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPartyDetails;
import uk.gov.hmcts.opal.dto.legacy.common.OrganisationDetails;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.IndividualAliasCommon;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommon;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountResponseMinorCreditorPayment;
import uk.gov.hmcts.opal.generated.model.OrganisationAliasCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface LegacyUpdateMinorCreditorAccountResponseMapper {

    @Mappings({
        @Mapping(target = "version", source = "accountVersion", qualifiedByName = "integerToBigInteger"),
        @Mapping(target = "creditorAccountId", source = "creditorAccountId"),
        @Mapping(target = "partyDetails", source = "partyDetails"),
        @Mapping(target = "address", source = "address"),
        @Mapping(target = "payment", source = "payment")
    })
    MinorCreditorAccountResponse toMinorCreditorAccountResponse(
        LegacyUpdateMinorCreditorAccountResponse legacy
    );

    PartyDetailsCommon map(LegacyPartyDetails source);

    OrganisationDetailsCommon map(OrganisationDetails source);

    @Mapping(target = "sequenceNumber", source = "sequenceNumber", qualifiedByName = "shortToInteger")
    OrganisationAliasCommon map(OrganisationDetails.OrganisationAlias source);

    @Mapping(target = "forenames", source = "firstNames")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth", qualifiedByName = "localDateToString")
    IndividualDetailsCommon map(IndividualDetails source);

    @Mapping(target = "sequenceNumber", source = "sequenceNumber", qualifiedByName = "shortToInteger")
    IndividualAliasCommon map(IndividualDetails.IndividualAlias source);

    AddressDetailsCommon map(AddressDetailsLegacy source);

    MinorCreditorAccountResponseMinorCreditorPayment map(LegacyCreditorAccountPaymentDetails source);

    @Named("integerToBigInteger")
    default BigInteger integerToBigInteger(Integer value) {
        return value == null ? null : BigInteger.valueOf(value.longValue());
    }

    @Named("shortToInteger")
    default Integer shortToInteger(Short value) {
        return value == null ? null : Integer.valueOf(value);
    }

    @Named("localDateToString")
    default String localDateToString(LocalDate value) {
        return value == null ? null : value.toString();
    }
}
