package uk.gov.hmcts.opal.mapper.response;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountAtAGlanceEntity;
import uk.gov.hmcts.opal.mapper.common.AddressMapper;
import uk.gov.hmcts.opal.mapper.common.AtAGlanceDefendantMapper;
import uk.gov.hmcts.opal.mapper.common.PartyMapper;
import uk.gov.hmcts.opal.mapper.common.PaymentMapper;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        PartyMapper.class,
        AddressMapper.class,
        AtAGlanceDefendantMapper.class,
        PaymentMapper.class
    }
)
public interface GetMinorCreditorAccountAtAGlanceResponseMapper {
    GetMinorCreditorAccountAtAGlanceResponse toDto(LegacyGetMinorCreditorAccountAtAGlanceResponse legacy);

    @Mappings({
        @Mapping(target = "address.addressLine1", source = "entity.addressLine1"),
        @Mapping(target = "address.addressLine2", source = "entity.addressLine2"),
        @Mapping(target = "address.addressLine3", source = "entity.addressLine3"),
        @Mapping(target = "address.addressLine4", source = "entity.addressLine4"),
        @Mapping(target = "address.addressLine5", source = "entity.addressLine5"),
        @Mapping(target = "address.postcode", source = "entity.postcode"),

        @Mapping(target = "creditorAccountId", source = "entity.creditorId"),

        @Mapping(target = "defendant.accountId", source = "entity.defendantAccountId"),
        @Mapping(target = "defendant.accountNumber", source = "entity.defendantAccountNumber"),
        @Mapping(target = "defendant.title", source = "entity.defendantTitle"),
        @Mapping(target = "defendant.forenames", source = "entity.defendantForenames"),
        @Mapping(target = "defendant.surname", source = "entity.defendantSurname"),

        @Mapping(target = "payment.bacs", source = "entity.payByBacs"),
        @Mapping(target = "payment.holdPayment", source = "entity.holdPayout")
    })
    GetMinorCreditorAccountAtAGlanceResponse toDto(MinorCreditorAccountAtAGlanceEntity entity, PartyEntity party);

}
