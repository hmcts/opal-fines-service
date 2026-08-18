package uk.gov.hmcts.opal.mapper;

import java.time.LocalTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.entity.FixedPenaltyOffenceEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.FixedPenaltyTicketDetailsCommon;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountFixedPenaltyResponse;
import uk.gov.hmcts.opal.generated.model.VehicleFixedPenaltyDetailsCommon;

@Mapper(componentModel = "spring")
public interface DefendantAccountFixedPenaltyMapper {

    String NON_VEHICLE_REGISTRATION = "NV";

    @Mapping(target = "vehicleFixedPenaltyFlag", source = "offence", qualifiedByName = "isVehicleFixedPenalty")
    @Mapping(target = "fixedPenaltyTicketDetails", expression = "java(toFixedPenaltyTicketDetails(account, offence))")
    @Mapping(target = "vehicleFixedPenaltyDetails", source = "offence",
        qualifiedByName = "toVehicleFixedPenaltyDetailsOrNull")
    @Mapping(target = "version", source = "account.version")
    GetDefendantAccountFixedPenaltyResponse toResponse(DefendantAccountEntity account,
        FixedPenaltyOffenceEntity offence);

    @Named("isVehicleFixedPenalty")
    default boolean isVehicleFixedPenalty(FixedPenaltyOffenceEntity offence) {
        return offence.getVehicleRegistration() != null
            && !NON_VEHICLE_REGISTRATION.equalsIgnoreCase(offence.getVehicleRegistration());
    }

    @Mapping(target = "issuingAuthority", source = "account.originatorName")
    @Mapping(target = "ticketNumber", source = "offence.ticketNumber")
    @Mapping(target = "timeOfOffence", source = "offence.timeOfOffence", qualifiedByName = "localTimeToString")
    @Mapping(target = "placeOfOffence", source = "offence.offenceLocation")
    FixedPenaltyTicketDetailsCommon toFixedPenaltyTicketDetails(DefendantAccountEntity account,
        FixedPenaltyOffenceEntity offence);

    @Mapping(target = "vehicleRegistrationNumber", source = "vehicleRegistration")
    @Mapping(target = "vehicleDriversLicense", source = "licenceNumber")
    @Mapping(target = "noticeNumber", source = "noticeNumber")
    @Mapping(target = "dateNoticeIssued", source = "issuedDate")
    VehicleFixedPenaltyDetailsCommon toVehicleFixedPenaltyDetails(FixedPenaltyOffenceEntity offence);

    @Named("toVehicleFixedPenaltyDetailsOrNull")
    default JsonNullable<VehicleFixedPenaltyDetailsCommon> toVehicleFixedPenaltyDetailsOrNull(
        FixedPenaltyOffenceEntity offence) {
        return JsonNullable.of(isVehicleFixedPenalty(offence) ? toVehicleFixedPenaltyDetails(offence) : null);
    }

    default <T> JsonNullable<T> toJsonNullable(T value) {
        return JsonNullable.of(value);
    }

    @Named("localTimeToString")
    default String localTimeToString(LocalTime value) {
        return value == null ? null : value.toString();
    }
}
