package uk.gov.hmcts.opal.mapper.history;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.history.AmendmentDetails;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.HistoryItemType;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AmendmentEntityHistoryMapper {

    @Mapping(target = "postedDetails", source = ".")
    @Mapping(target = "type", expression = "java(uk.gov.hmcts.opal.dto.history.HistoryItemType.AMENDMENT)")
    @Mapping(target = "details", source = ".", qualifiedByName = "toAmendmentDetails")
    @Mapping(target = "eventDateTime", source = "amendedDate", qualifiedByName = "toStartOfDay")
    @Mapping(target = "sourceId", source = "amendmentId")
    DefendantAccountHistoryItem toHistoryItem(AmendmentEntity entity);

    @Mapping(target = "postedDate", source = "amendedDate", qualifiedByName = "toStartOfDay")
    @Mapping(target = "postedBy", source = "amendedBy")
    PostedDetails toPostedDetails(AmendmentEntity entity);

    @Named("toAmendmentDetails")
    @Mapping(target = "attributeName", source = "fieldCode")
    AmendmentDetails toAmendmentDetails(AmendmentEntity entity);

    default String map(Short fieldCode) {
        return fieldCode == null ? null : fieldCode.toString();
    }

    @Named("toStartOfDay")
    default LocalDateTime toStartOfDay(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }
}
