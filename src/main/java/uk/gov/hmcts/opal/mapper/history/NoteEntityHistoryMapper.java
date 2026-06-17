package uk.gov.hmcts.opal.mapper.history;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryItem;
import uk.gov.hmcts.opal.dto.history.NoteDetails;
import uk.gov.hmcts.opal.entity.NoteEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NoteEntityHistoryMapper {

    @Mapping(target = "postedDetails.postedDate", source = "postedDate")
    @Mapping(target = "postedDetails.postedBy", source = "businessUnitUserId")
    @Mapping(target = "postedDetails.postedByName", source = "postedByUsername")
    @Mapping(target = "type", expression = "java(uk.gov.hmcts.opal.dto.history.HistoryItemType.NOTE)")
    @Mapping(target = "details", source = ".", qualifiedByName = "toNoteDetails")
    @Mapping(target = "eventDateTime", source = "postedDate")
    @Mapping(target = "sourceId", source = "noteId")
    DefendantAccountHistoryItem toHistoryItem(NoteEntity entity);

    @Named("toNoteDetails")
    NoteDetails toNoteDetails(NoteEntity entity);
}
