package uk.gov.hmcts.opal.mapper.history;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.CourtReferenceDto;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.EnforcementDetails;
import uk.gov.hmcts.opal.entity.court.CourtEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EnforcementEntityHistoryMapper {

    @Mapping(target = "postedDetails.postedDate", source = "postedDate")
    @Mapping(target = "postedDetails.postedBy", source = "postedBy")
    @Mapping(target = "postedDetails.postedByName", source = "postedByUsername")
    @Mapping(target = "type", expression = "java(uk.gov.hmcts.opal.dto.history.HistoryItemType.ENFORCEMENT)")
    @Mapping(target = "details", source = ".", qualifiedByName = "toEnforcementDetails")
    @Mapping(target = "eventDateTime", source = "postedDate")
    @Mapping(target = "sourceId", source = "enforcementId")
    DefendantAccountHistoryItem toHistoryItem(EnforcementEntity entity);

    @Named("toEnforcementDetails")
    @Mapping(target = "enforcementAction", source = "resultId")
    @Mapping(target = "daysInDefault", source = "jailDays")
    @Mapping(target = "warrantNumber", source = "warrantReference")
    @Mapping(target = "hearingDate", source = "hearingDate", qualifiedByName = "toLocalDate")
    @Mapping(target = "hearingCourt", source = "hearingCourt")
    @Mapping(target = "caseNumber", source = "caseReference")
    @Mapping(target = "earliestDateOfRelease", source = "earliestReleaseDate", qualifiedByName = "toLocalDate")
    EnforcementDetails toEnforcementDetails(EnforcementEntity entity);

    @Mapping(target = "courtId", source = "courtId")
    CourtReferenceDto toCourtReference(CourtEntity entity);

    @Named("toLocalDate")
    default LocalDate toLocalDate(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toLocalDate();
    }
}
