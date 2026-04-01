package uk.gov.hmcts.opal.mapper.request;

import java.time.LocalDate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder;
import uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverride;
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
        @Mapping(target = "collectionOrder",       source = "payload.collectionOrder",
            qualifiedByName = "normaliseCollectionOrder"),
        @Mapping(target = "enforcementOverride",   source = "payload.enforcementOverride")
    })
    LegacyUpdateDefendantAccountRequest toLegacyUpdateDefendantAccountRequest(
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
    CommentsAndNotes map(CommentsAndNotesCommon src);

    // API EnforcementOverride -> Legacy EnforcementOverride (extend as needed)
    EnforcementOverride map(EnforcementOverrideDefendantAccount src);

    // API CollectionOrderDto -> Legacy CollectionOrder (extend as fields evolve)
    @Named("normaliseCollectionOrder")
    default CollectionOrder normaliseCollectionOrder(CollectionOrderCommon src) {
        if (src == null) {
            return null;
        }

        Boolean flag = src.getCollectionOrderFlag();
        JsonNullable<LocalDate> maybeDate = src.getCollectionOrderDate();
        LocalDate date = maybeDate != null && maybeDate.isPresent() ? maybeDate.get() : null;

        if (Boolean.FALSE.equals(flag)) {
            date = null;
        } else if (Boolean.TRUE.equals(flag) && date == null) {
            date = LocalDate.now();
        }

        return CollectionOrder.builder()
            .collectionOrderFlag(flag)
            .collectionOrderDate(date)
            .build();
    }

    /* ----------- Converters ----------- */
    @Named("numberToString")
    default String numberToString(Number n) {
        return n == null ? null : String.valueOf(n.longValue());
    }
}
