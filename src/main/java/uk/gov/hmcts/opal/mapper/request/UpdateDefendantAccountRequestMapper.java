package uk.gov.hmcts.opal.mapper.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.generated.model.CollectionOrderCommon;
import uk.gov.hmcts.opal.generated.model.CommentsAndNotesCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideDefendantAccount;

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
        @Mapping(target = "commentAndNotes",       source = "payload.commentAndNotes"),
        @Mapping(target = "enforcementCourtId",    source = "payload.enforcementCourt.courtId",
            qualifiedByName = "numberToString"),
        @Mapping(target = "collectionOrder",       source = "payload.collectionOrder"),
        @Mapping(target = "enforcementOverride",   source = "payload.enforcementOverride")
    })
    uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountRequest toLegacyUpdateDefendantAccountRequest(
        UpdateDefendantAccountRequest request
    );

    /* ----------- Nested type mappings ----------- */

    // API CommentsAndNotes -> Legacy CommentsAndNotes
    @Mappings({
        @Mapping(target = "accountComment",  source = "accountComment"),
        @Mapping(target = "freeTextNote1",   source = "freeTextNote1"),
        @Mapping(target = "freeTextNote2",   source = "freeTextNote2"),
        @Mapping(target = "freeTextNote3",   source = "freeTextNote3")
    })
    uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes map(CommentsAndNotesCommon src);

    // API CollectionOrderDto -> Legacy CollectionOrder (extend as fields evolve)
    uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder map(CollectionOrderCommon src);

    // API EnforcementOverride -> Legacy EnforcementOverride (extend as needed)
    uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverride map(EnforcementOverrideDefendantAccount src);

    /* ----------- Converters ----------- */
    @Named("numberToString")
    default String numberToString(Number n) {
        return n == null ? null : String.valueOf(n.longValue());
    }
}
