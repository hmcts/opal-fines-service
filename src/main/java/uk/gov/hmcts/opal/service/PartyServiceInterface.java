package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.dto.PartyDto;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PartySummary;

import java.util.List;

public interface PartyServiceInterface {

    PartyDto getParty(long partyId);

    PartyDto saveParty(PartyDto party);

    List<PartySummary> searchForParty(AccountSearchDto accountSearchDto);

    default PartyEntity toEntity(PartyDto dto) {
        return PartyEntity.builder()
            .partyId(dto.getPartyId())
            .organisation(dto.isOrganisation())
            .organisationName(dto.getOrganisationName())
            .surname(dto.getSurname())
            .forenames(dto.getForenames())
            .initials(dto.getInitials())
            .title(dto.getTitle())
            .addressLine1(dto.getAddressLine1())
            .addressLine2(dto.getAddressLine2())
            .addressLine3(dto.getAddressLine3())
            .addressLine4(dto.getAddressLine4())
            .addressLine5(dto.getAddressLine5())
            .postcode(dto.getPostcode())
            .accountType(dto.getAccountType())
            .dateOfBirth(dto.getDateOfBirth())
            .age(dto.getAge())
            .niNumber(dto.getNiNumber())
            .lastChangedDate(dto.getLastChangedDate())
            .build();
    }

    default PartyDto toDto(PartyEntity entity) {
        return PartyDto.builder()
            .partyId(entity.getPartyId())
            .organisation(entity.isOrganisation())
            .organisationName(entity.getOrganisationName())
            .surname(entity.getSurname())
            .forenames(entity.getForenames())
            .initials(entity.getInitials())
            .title(entity.getTitle())
            .addressLine1(entity.getAddressLine1())
            .addressLine2(entity.getAddressLine2())
            .addressLine3(entity.getAddressLine3())
            .addressLine4(entity.getAddressLine4())
            .addressLine5(entity.getAddressLine5())
            .postcode(entity.getPostcode())
            .accountType(entity.getAccountType())
            .dateOfBirth(entity.getDateOfBirth())
            .age(entity.getAge())
            .niNumber(entity.getNiNumber())
            .lastChangedDate(entity.getLastChangedDate())
            .build();
    }
}
