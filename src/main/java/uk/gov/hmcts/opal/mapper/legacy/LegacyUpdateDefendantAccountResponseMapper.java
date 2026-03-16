package uk.gov.hmcts.opal.mapper.legacy;

import java.math.BigInteger;
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
        @Mapping(target = "payload.id", source = "defendantAccountId", qualifiedByName = "stringToLong"),
        @Mapping(target = "version", source = "version", qualifiedByName = "intToBigInteger"),
        @Mapping(target = "payload.commentsAndNotes", source = "commentAndNotes"),
        @Mapping(target = "payload.enforcementCourt.courtId", source = "enforcementCourtId",
            qualifiedByName = "stringToLong"),
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
    CommentsAndNotesCommon map(uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes src);

    @Mapping(target = "collectionOrderFlag", source = "collectionOrderFlag")
    @Mapping(target = "collectionOrderDate", source = "collectionOrderDate")
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

    @Named("intToLong")
    default Long intToLong(Integer i) {
        return i == null ? null : i.longValue();
    }

    @Named("intToBigInteger")
    default BigInteger intToBigInteger(Integer i) {
        return i == null ? null : BigInteger.valueOf(i);
    }
}
