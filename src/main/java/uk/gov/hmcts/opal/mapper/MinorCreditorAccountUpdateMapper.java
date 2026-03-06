package uk.gov.hmcts.opal.mapper;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.generated.model.AddressDetailsCommon;
import uk.gov.hmcts.opal.generated.model.PartyDetailsCommon;

@Mapper(componentModel = "spring")
public interface MinorCreditorAccountUpdateMapper {

    @Mapping(target = "accountType", ignore = true)
    @Mapping(target = "homeTelephoneNumber", ignore = true)
    @Mapping(target = "workTelephoneNumber", ignore = true)
    @Mapping(target = "mobileTelephoneNumber", ignore = true)
    @Mapping(target = "primaryEmailAddress", ignore = true)
    @Mapping(target = "secondaryEmailAddress", ignore = true)
    @Mapping(target = "lastChangedDate", ignore = true)
    @Mapping(target = "defendantAccountParties", ignore = true)
    @Mapping(target = "organisation", source = "partyDetails.organisationFlag")
    @Mapping(target = "organisationName", source = "partyDetails.organisationDetails.organisationName")
    @Mapping(target = "title", source = "partyDetails.individualDetails.title")
    @Mapping(target = "forenames", source = "partyDetails.individualDetails.forenames")
    @Mapping(target = "surname", source = "partyDetails.individualDetails.surname")
    @Mapping(target = "birthDate", source = "partyDetails.individualDetails.dateOfBirth",
        qualifiedByName = "parseBirthDate")
    @Mapping(target = "age", source = "partyDetails.individualDetails.age", qualifiedByName = "parseAge")
    @Mapping(target = "niNumber", source = "partyDetails.individualDetails.nationalInsuranceNumber")
    @Mapping(target = "addressLine1", source = "address.addressLine1")
    @Mapping(target = "addressLine2", source = "address.addressLine2")
    @Mapping(target = "addressLine3", source = "address.addressLine3")
    @Mapping(target = "addressLine4", source = "address.addressLine4")
    @Mapping(target = "addressLine5", source = "address.addressLine5")
    @Mapping(target = "postcode", source = "address.postcode")
    void updateParty(PartyDetailsCommon partyDetails, AddressDetailsCommon address, @MappingTarget PartyEntity party);

    @AfterMapping
    default void clearUnusedDetails(PartyDetailsCommon partyDetails, @MappingTarget PartyEntity party) {
        if (Boolean.TRUE.equals(partyDetails.getOrganisationFlag())) {
            party.setTitle(null);
            party.setForenames(null);
            party.setSurname(null);
            party.setBirthDate(null);
            party.setAge(null);
            party.setNiNumber(null);
        } else {
            party.setOrganisationName(null);
        }
    }

    @Named("parseBirthDate")
    default LocalDate parseBirthDate(String dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(dateOfBirth);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid individual_details.date_of_birth format", ex);
        }
    }

    @Named("parseAge")
    default Short parseAge(String age) {
        if (age == null || age.isBlank()) {
            return null;
        }

        try {
            return Short.valueOf(age);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid individual_details.age format", ex);
        }
    }
}
