package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.*;
import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.CollectionOrderDto;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LegacyUpdateDefendantAccountResponseMapper {
    @Mappings({
        @Mapping(target = "id", source = "defendantAccountId", qualifiedByName = "stringToLong"),
        @Mapping(target = "version", source = "version", qualifiedByName = "intToLong"),
        @Mapping(target = "commentsAndNotes", source = "commentAndNotes"),
        @Mapping(target = "enforcementCourt.courtId", source = "enforcementCourtId", qualifiedByName = "stringToLong"),
        @Mapping(target = "collectionOrder", source = "collectionOrder"),
        @Mapping(target = "enforcementOverride", source = "enforcementOverride")
    })
    DefendantAccountResponse toDefendantAccountResponse(LegacyUpdateDefendantAccountResponse legacy);

    /* ---------- Nested mappings ---------- */

    @Mappings({
        @Mapping(target = "accountNotesAccountComments", source = "accountComment"),
        @Mapping(target = "accountNotesFreeTextNote1", source = "freeTextNote1"),
        @Mapping(target = "accountNotesFreeTextNote2", source = "freeTextNote2"),
        @Mapping(target = "accountNotesFreeTextNote3", source = "freeTextNote3")
    })
    CommentsAndNotes map(uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes src);

    @Mapping(target = "collectionOrderFlag", source = "collectionOrderFlag")
    @Mapping(target = "collectionOrderDate", source = "collectionOrderDate")
    CollectionOrderDto map(uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder src);
    EnforcementOverride map(uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverride src);

    @Named("stringToLong")
    default Long stringToLong(String s) { return (s == null || s.isBlank()) ? null : Long.valueOf(s); }

    @Named("intToLong")
    default Long intToLong(Integer i) { return i == null ? null : i.longValue(); }
}
