package uk.gov.hmcts.opal.mapper.response;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.dto.common.AddressDetails;
import uk.gov.hmcts.opal.dto.common.IndividualDetails;
import uk.gov.hmcts.opal.dto.common.IndividualAlias;
import uk.gov.hmcts.opal.dto.common.OrganisationDetails;
import uk.gov.hmcts.opal.dto.common.OrganisationAlias;
import uk.gov.hmcts.opal.dto.common.PartyDetails;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountAtAGlanceEntity;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.IndividualAliasCommonStrict;
import uk.gov.hmcts.opal.generated.model.IndividualDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.generated.model.OrganisationAliasCommon;
import uk.gov.hmcts.opal.generated.model.OrganisationDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommonStrict;
import uk.gov.hmcts.opal.mapper.common.AddressMapper;
import uk.gov.hmcts.opal.mapper.common.PartyMapper;
import uk.gov.hmcts.opal.mapper.common.PaymentMapper;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        PartyMapper.class,
        AddressMapper.class,
        PaymentMapper.class
    }
)
public interface GetMinorCreditorAccountAtAGlanceResponseMapper {

    PartyDetailsCommonStrict toPartyDetails(PartyDetails source);

    AddressDetailsCommonStrict toAddressDetails(AddressDetails source);

    OrganisationDetailsCommonStrict toOrganisationDetails(OrganisationDetails source);

    IndividualDetailsCommonStrict toIndividualDetails(IndividualDetails source);

    List<OrganisationAliasCommon> toOrganisationAliases(List<OrganisationAlias> source);

    List<IndividualAliasCommonStrict> toIndividualAliases(List<IndividualAlias> source);

    default JsonNullable<OrganisationDetailsCommonStrict> toJsonNullable(OrganisationDetails source) {
        return JsonNullable.of(toOrganisationDetails(source));
    }

    default JsonNullable<IndividualDetailsCommonStrict> toJsonNullable(IndividualDetails source) {
        return JsonNullable.of(toIndividualDetails(source));
    }

    default <T> JsonNullable<T> toJsonNullable(T value) {
        return JsonNullable.of(value);
    }

    default JsonNullable<List<OrganisationAliasCommon>> toNullableOrganisationAliases(
        List<OrganisationAlias> source) {
        return JsonNullable.of(toOrganisationAliases(source));
    }

    default JsonNullable<List<IndividualAliasCommonStrict>> toNullableIndividualAliases(
        List<IndividualAlias> source) {
        return JsonNullable.of(toIndividualAliases(source));
    }

    @Mapping(target = "version", source = "creditorAccountVersion")
    @Mapping(target = "payment.isBacs", source = "payment.bacs")
    MinorCreditorAccountAtAGlanceResponse toDto(LegacyGetMinorCreditorAccountAtAGlanceResponse legacy);

    @Mappings({
        @Mapping(target = "address.addressLine1", source = "entity.addressLine1"),
        @Mapping(target = "address.addressLine2", source = "entity.addressLine2"),
        @Mapping(target = "address.addressLine3", source = "entity.addressLine3"),
        @Mapping(target = "address.addressLine4", source = "entity.addressLine4"),
        @Mapping(target = "address.addressLine5", source = "entity.addressLine5"),
        @Mapping(target = "address.postcode", source = "entity.postcode"),

        @Mapping(target = "creditorAccountId", source = "entity.creditorId"),
        @Mapping(target = "version", source = "entity.versionNumber"),

        @Mapping(target = "defendant.accountId", source = "entity.defendantAccountId"),
        @Mapping(target = "defendant.accountNumber", source = "entity.defendantAccountNumber"),
        @Mapping(target = "defendant.title", source = "entity.defendantTitle"),
        @Mapping(target = "defendant.forenames", source = "entity.defendantForenames"),
        @Mapping(target = "defendant.surname", source = "entity.defendantSurname"),

        @Mapping(target = "payment.isBacs", source = "entity.payByBacs"),
        @Mapping(target = "payment.holdPayment", source = "entity.holdPayout")
    })
    MinorCreditorAccountAtAGlanceResponse toDto(MinorCreditorAccountAtAGlanceEntity entity, PartyEntity party);

}
