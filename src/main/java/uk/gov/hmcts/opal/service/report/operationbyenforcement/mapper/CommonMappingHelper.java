package uk.gov.hmcts.opal.service.report.operationbyenforcement.mapper;

import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.COMMA_DELIMINATOR;
import static uk.gov.hmcts.opal.service.report.CommonReportStringConstants.EMPTY_STRING;

import org.mapstruct.Named;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.service.report.CommonReportStringConstants;

public interface CommonMappingHelper {

    default String buildDefendantName(PartyEntity party) {
        if (party.isOrganisation()) {
            return party.getOrganisationName();
        }
        String surname = party.getSurname() == null ? EMPTY_STRING : party.getSurname();
        String forenames = party.getForenames() == null ? EMPTY_STRING : party.getForenames();
        return forenames.isBlank() ? surname : surname + COMMA_DELIMINATOR + forenames;
    }

    default String truncate34(String value) {
        return value != null && value.length() > 34
            ? value.substring(0, 34)
            : value;
    }

    default String truncate(String value, int length) {
        return value != null && value.length() > length
            ? value.substring(0, length)
            : value;
    }

    @Named("booleanToYesNo")
    default String booleanToYesNo(Boolean value) {
        return value == null
            ? null
            : (value
               ? CommonReportStringConstants.YES
                : CommonReportStringConstants.NO);
    }

    @Named("organisationToYesNo")
    default String organisationToYesNo(PartyEntity party) {
        return booleanToYesNo(party.isOrganisation());
    }
}
