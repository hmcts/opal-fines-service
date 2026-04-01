package uk.gov.hmcts.opal.mapper.legacy;

import java.time.LocalDate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder;
import uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverride;
import uk.gov.hmcts.opal.generated.model.CollectionOrderCommon;
import uk.gov.hmcts.opal.generated.model.CommentsAndNotesCommon;
import uk.gov.hmcts.opal.generated.model.EnforcementOverrideDefendantAccount;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LegacyUpdateDefendantAccountResponseMapper {
    @Mappings({
        @Mapping(target = "payload.id", source = "defendantAccountId"),
        @Mapping(target = "payload.commentAndNotes", source = "commentAndNotes"),
        @Mapping(target = "payload.enforcementCourt.courtId", source = "enforcementCourtId"
            ),
        @Mapping(target = "payload.collectionOrder", source = "collectionOrder"),
        @Mapping(target = "payload.enforcementOverride", source = "enforcementOverride")
    })
    UpdateDefendantAccountResponse toUpdateDefendantAccountResponse(LegacyUpdateDefendantAccountResponse legacy);

    /* ---------- Nested mappings ---------- */

    @Mappings({
        @Mapping(target = "accountComment", source = "accountComment"),
        @Mapping(target = "freeTextNote1", source = "freeTextNote1"),
        @Mapping(target = "freeTextNote2", source = "freeTextNote2"),
        @Mapping(target = "freeTextNote3", source = "freeTextNote3")
    })
    CommentsAndNotesCommon map(CommentsAndNotes src);

    @Mapping(target = "collectionOrderFlag", source = "collectionOrderFlag")
    @Mapping(target = "collectionOrderDate", source = "collectionOrderDate")
    CollectionOrderCommon map(CollectionOrder src);

    EnforcementOverrideDefendantAccount map(EnforcementOverride src);

    default JsonNullable<LocalDate> map(LocalDate date) {
        return JsonNullable.of(date);
    }

    @Named("stringToLong")
    default Long stringToLong(String v) {
        if (v == null) {
            return null;
        }
        try {
            return Long.valueOf(v.trim());
        } catch (NumberFormatException e) {
            return null; // or rethrow for strict behavior
        }
    }

}
