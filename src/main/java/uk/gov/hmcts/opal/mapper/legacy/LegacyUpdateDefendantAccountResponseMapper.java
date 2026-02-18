package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverrideResultReference;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcerReference;
import uk.gov.hmcts.opal.dto.legacy.common.LjaReference;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LegacyUpdateDefendantAccountResponseMapper {
    @Mappings({
        @Mapping(target = "id", source = "defendantAccountId", qualifiedByName = "stringToLong"),
        @Mapping(target = "version", source = "version", qualifiedByName = "intToLong"),
        @Mapping(target = "commentsAndNotes", source = "commentAndNotes"),
        @Mapping(target = "enforcementCourt.enforcingCourtId", source = "enforcementCourtId",
            qualifiedByName = "stringToLong"),
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

    @Mapping(target = "collectionOrder", source = "collectionOrderFlag")
    @Mapping(target = "collectionOrderDate", source = "collectionOrderDate")
    DefendantAccountResponse.CollectionOrderResponse map(uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder src);

    default DefendantAccountResponse.EnforcementOverrideResponse map(
        uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverride src) {
        if (src == null) {
            return null;
        }

        EnforcementOverrideResultReference result = src.getEnforcementOverrideResult();
        EnforcerReference enforcer = src.getEnforcer();
        LjaReference lja = src.getLja();

        return DefendantAccountResponse.EnforcementOverrideResponse.builder()
            .enforcementOverrideResultId(result != null ? result.getEnforcementOverrideResultId() : null)
            .enforcementOverrideEnforcerId(enforcer != null && enforcer.getEnforcerId() != null
                ? enforcer.getEnforcerId() : null)
            .enforcementOverrideTfoLjaId(lja != null && lja.getLjaId() != null
                ? lja.getLjaId().intValue() : null)
            .build();
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

    @Named("intToLong")
    default Long intToLong(Integer i) {
        return i == null ? null : i.longValue();
    }

}
