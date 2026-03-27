package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountResponse;
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

    CommentsAndNotesCommon map(uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes src);

    CollectionOrderCommon map(uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder src);

    EnforcementOverrideDefendantAccount map(uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverride src);

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
