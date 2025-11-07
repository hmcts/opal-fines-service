package uk.gov.hmcts.opal.mapper.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.CollectionOrderDto;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.common.EnforcementOverride;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UpdateDefendantAccountRequestMapper {
    /**
     * Build the LegacyUpdateDefendantAccountRequest from:
     * - API body (UpdateDefendantAccountRequest), and
     * - path/header-derived values (defendantAccountId, businessUnitId, businessUnitUserId, version).
     */
    @Mappings({
        // header/path values
        @Mapping(target = "defendantAccountId",    source = "defendantAccountId"),
        @Mapping(target = "businessUnitId",        source = "businessUnitId"),
        @Mapping(target = "businessUnitUserId",    source = "businessUnitUserId"),
        @Mapping(target = "version",               source = "version"),

        // nested groups from body
        @Mapping(target = "commentAndNotes",       source = "request.commentsAndNotes"),
        @Mapping(target = "enforcementCourtId",    source = "request.enforcementCourt.courtId",
            qualifiedByName = "numberToString"),
        @Mapping(target = "collectionOrder",       source = "request.collectionOrder"),
        @Mapping(target = "enforcementOverride",   source = "request.enforcementOverride")
    })
    uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountRequest toLegacyUpdateDefendantAccountRequest(
        UpdateDefendantAccountRequest request,
        String defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        Integer version
    );

    /* ----------- Nested type mappings ----------- */

    // API CommentsAndNotes -> Legacy CommentsAndNotes
    @Mappings({
        @Mapping(target = "accountComment",  source = "accountNotesAccountComments"),
        @Mapping(target = "freeTextNote1",   source = "accountNotesFreeTextNote1"),
        @Mapping(target = "freeTextNote2",   source = "accountNotesFreeTextNote2"),
        @Mapping(target = "freeTextNote3",   source = "accountNotesFreeTextNote3")
    })
    uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes map(CommentsAndNotes src);

    // API CollectionOrderDto -> Legacy CollectionOrder (extend as fields evolve)
    uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder map(CollectionOrderDto src);

    // API EnforcementOverride -> Legacy EnforcementOverride (extend as needed)
    uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverride map(EnforcementOverride src);

    /* ----------- Converters ----------- */
    @Named("numberToString")
    default String numberToString(Number n) {
        return n == null ? null : String.valueOf(n.longValue());
    }
}
