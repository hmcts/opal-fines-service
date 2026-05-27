package uk.gov.hmcts.opal.service.report.operationbyenforcement.mapper;

import org.mapstruct.Named;
import uk.gov.hmcts.opal.entity.PartyEntity;

public interface CommonMappingHelper {

    String YES = "Y";
    String NO = "N";
    String EMPTY_STRING = "";

    default String buildDefendantName(PartyEntity party) {
        if (party.isOrganisation()) {
            return party.getOrganisationName();
        }
        String surname = party.getSurname() == null ? EMPTY_STRING : party.getSurname();
        String forenames = party.getForenames() == null ? EMPTY_STRING : party.getForenames();
        return forenames.isBlank() ? surname : surname + ", " + forenames;
    }

    default String truncate34(String value) {
        return value != null && value.length() > 34
            ? value.substring(0, 34)
            : value;
    }

    @Named("booleanToYesNo")
    default String booleanToYesNo(Boolean value) {
        return value == null ? null : (value ? YES : NO);
    }

}
