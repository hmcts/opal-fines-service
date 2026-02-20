package uk.gov.hmcts.opal.mapper.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.time.LocalDate;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.common.CommentsAndNotes;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverride;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcementOverrideResultReference;
import uk.gov.hmcts.opal.dto.legacy.common.EnforcerReference;
import uk.gov.hmcts.opal.dto.legacy.common.LjaReference;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UpdateDefendantAccountRequestMapper {
    /**
     * Build the LegacyUpdateDefendantAccountRequest from:
     * - API body (UpdateDefendantAccountRequest), and
     * - path/header-derived values (defendantAccountId, businessUnitId, businessUnitUserId, version).
     */
    // header/path values
    @Mapping(target = "defendantAccountId",    source = "defendantAccountId")
    @Mapping(target = "businessUnitId",        source = "businessUnitId")
    @Mapping(target = "businessUnitUserId",    source = "businessUnitUserId")
    @Mapping(target = "version",               source = "version")
    // nested groups from body
    @Mapping(target = "commentAndNotes",       source = "request.commentsAndNotes")
    @Mapping(target = "enforcementCourtId",    source = "request.enforcementCourt.enforcingCourtId",
        qualifiedByName = "numberToString")
    @Mapping(target = "collectionOrder",       source = "request.collectionOrder")
    @Mapping(target = "enforcementOverride",   source = "request.enforcementOverride")
    uk.gov.hmcts.opal.dto.legacy.LegacyUpdateDefendantAccountRequest toLegacyUpdateDefendantAccountRequest(
        UpdateDefendantAccountRequest request,
        String defendantAccountId,
        String businessUnitId,
        String businessUnitUserId,
        String version
    );

    /* ----------- Nested type mappings ----------- */

    // API CommentsAndNotes -> Legacy CommentsAndNotes
    @Mapping(target = "accountComment",  source = "accountNotesAccountComments")
    @Mapping(target = "freeTextNote1",   source = "accountNotesFreeTextNote1")
    @Mapping(target = "freeTextNote2",   source = "accountNotesFreeTextNote2")
    @Mapping(target = "freeTextNote3",   source = "accountNotesFreeTextNote3")
    uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes map(CommentsAndNotes src);

    // API CollectionOrderRequest -> Legacy CollectionOrder (extend as fields evolve)
    @Mapping(target = "collectionOrderFlag", source = "collectionOrder")
    @Mapping(target = "collectionOrderDate", source = "collectionOrderDate", qualifiedByName = "stringToLocalDate")
    uk.gov.hmcts.opal.dto.legacy.common.CollectionOrder map(
        UpdateDefendantAccountRequest.CollectionOrderRequest src);

    // API EnforcementOverrideRequest -> Legacy EnforcementOverride (extend as needed)
    default EnforcementOverride map(UpdateDefendantAccountRequest.EnforcementOverrideRequest src) {
        if (src == null) {
            return null;
        }

        EnforcementOverrideResultReference result = src.getEnforcementOverrideResultId() == null
            ? null
            : EnforcementOverrideResultReference.builder()
                .enforcementOverrideResultId(src.getEnforcementOverrideResultId())
                .enforcementOverrideResultName("")
                .build();

        EnforcerReference enforcer = src.getEnforcementOverrideEnforcerId() == null
            ? null
            : EnforcerReference.builder()
                .enforcerId(src.getEnforcementOverrideEnforcerId())
                .enforcerName("")
                .build();

        LjaReference lja = src.getEnforcementOverrideTfoLjaId() == null
            ? null
            : LjaReference.builder()
                .ljaId(src.getEnforcementOverrideTfoLjaId().shortValue())
                .ljaName("")
                .build();

        return EnforcementOverride.builder()
            .enforcementOverrideResult(result)
            .enforcer(enforcer)
            .lja(lja)
            .build();
    }

    /* ----------- Converters ----------- */
    @Named("numberToString")
    default String numberToString(Number n) {
        return n == null ? null : String.valueOf(n.longValue());
    }

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String value) {
        return value == null ? null : LocalDate.parse(value);
    }
}
