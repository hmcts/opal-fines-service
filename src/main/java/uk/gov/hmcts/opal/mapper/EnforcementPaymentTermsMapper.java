package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.dto.PaymentTerms;
import uk.gov.hmcts.opal.dto.PostedDetails;
import uk.gov.hmcts.opal.dto.common.InstalmentPeriod;
import uk.gov.hmcts.opal.dto.common.PaymentTermsType;
import uk.gov.hmcts.opal.generated.model.EnforcementInstalmentPeriodCommonStrict;
import uk.gov.hmcts.opal.generated.model.EnforcementPaymentTermsCommonStrict;
import uk.gov.hmcts.opal.generated.model.EnforcementPaymentTermsTypeCommonStrict;
import uk.gov.hmcts.opal.generated.model.EnforcementPostedDetailsCommonStrict;

@Mapper(componentModel = "spring")
public interface EnforcementPaymentTermsMapper {

    PaymentTerms toPaymentTerms(EnforcementPaymentTermsCommonStrict source);

    PostedDetails toPostedDetails(EnforcementPostedDetailsCommonStrict source);

    @Mapping(target = "paymentTermsTypeCode", source = "paymentTermsTypeCode")
    PaymentTermsType toPaymentTermsType(EnforcementPaymentTermsTypeCommonStrict source);

    @Mapping(target = "instalmentPeriodCode", source = "instalmentPeriodCode")
    InstalmentPeriod toInstalmentPeriod(EnforcementInstalmentPeriodCommonStrict source);

    default <T> T toValue(JsonNullable<T> source) {
        return source == null ? null : source.orElse(null);
    }
}
