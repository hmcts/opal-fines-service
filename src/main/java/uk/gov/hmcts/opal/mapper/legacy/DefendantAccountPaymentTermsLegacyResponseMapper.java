package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.generated.model.DefendantAccountInstalmentPeriodCommonStrict;
import uk.gov.hmcts.opal.generated.model.DefendantAccountInstalmentPeriodCommonStrict.InstalmentPeriodDisplayNameEnum;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsCommonStrict;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPostedDetailsCommonStrict;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DefendantAccountPaymentTermsLegacyResponseMapper {

    @Mapping(target = "version", defaultValue = "1L")
    DefendantAccountPaymentTermsResponse toResponse(LegacyGetDefendantAccountPaymentTermsResponse response);

    DefendantAccountPaymentTermsCommonStrict toPaymentTerms(LegacyPaymentTerms paymentTerms);

    @Mapping(
        target = "instalmentPeriodDisplayName",
        source = "instalmentPeriodCode",
        qualifiedByName = "legacyPeriodToDisplayName")
    DefendantAccountInstalmentPeriodCommonStrict toInstalmentPeriod(LegacyInstalmentPeriod period);

    DefendantAccountPostedDetailsCommonStrict toPostedDetails(LegacyPostedDetails postedDetails);

    @Named("legacyPeriodToDisplayName")
    default InstalmentPeriodDisplayNameEnum toDisplayName(LegacyInstalmentPeriod.InstalmentPeriodCode code) {
        if (code == null) {
            return null;
        }

        return switch (code) {
            case W -> InstalmentPeriodDisplayNameEnum.WEEKLY;
            case M -> InstalmentPeriodDisplayNameEnum.MONTHLY;
            case F -> InstalmentPeriodDisplayNameEnum.FORTNIGHTLY;
        };
    }

    default <T> JsonNullable<T> toJsonNullable(T value) {
        return JsonNullable.of(value);
    }
}
