package uk.gov.hmcts.opal.mapper.history;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.history.AmendmentDetails;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.entity.amendment.AmendmentEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AmendmentEntityHistoryMapper {

    @Mapping(target = "postedDetails", source = ".")
    @Mapping(target = "type", expression = "java(uk.gov.hmcts.opal.dto.history.HistoryItemType.AMENDMENT)")
    @Mapping(target = "details", source = ".", qualifiedByName = "toAmendmentDetails")
    @Mapping(target = "eventDateTime", source = "amendedDate")
    @Mapping(target = "sourceId", source = "amendmentId")
    DefendantAccountHistoryItem toHistoryItem(AmendmentEntity entity);

    @Mapping(target = "postedDate", source = "amendedDate")
    @Mapping(target = "postedBy", source = "amendedBy")
    @Mapping(target = "postedByName", source = "amendedByName")
    PostedDetails toPostedDetails(AmendmentEntity entity);

    @Named("toAmendmentDetails")
    @Mapping(target = "attributeName", source = "fieldCode")
    AmendmentDetails toAmendmentDetails(AmendmentEntity entity);

    default String map(Short fieldCode) {
        return fieldCode == null ? null : fieldCode.toString();
    }
}
