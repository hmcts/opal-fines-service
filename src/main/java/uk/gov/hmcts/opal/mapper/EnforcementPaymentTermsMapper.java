package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.generated.model.EnforcementInstalmentPeriodCommonStrict;
import uk.gov.hmcts.opal.generated.model.EnforcementPaymentTermsCommonStrict;
import uk.gov.hmcts.opal.generated.model.EnforcementPaymentTermsTypeCommonStrict;
import uk.gov.hmcts.opal.generated.model.InstalmentPeriodCommonStrict;
import uk.gov.hmcts.opal.generated.model.PaymentTermsDefendantAccount;
import uk.gov.hmcts.opal.generated.model.PaymentTermsTypeCommonStrict;

@Mapper(componentModel = "spring")
public interface EnforcementPaymentTermsMapper {

    default PaymentTermsDefendantAccount toPaymentTerms(EnforcementPaymentTermsCommonStrict source) {
        if (source == null) {
            return null;
        }

        EnforcementPaymentTermsTypeCommonStrict paymentTermsType = source.getPaymentTermsType();
        EnforcementInstalmentPeriodCommonStrict instalmentPeriod = source.getInstalmentPeriod().orElse(null);

        return PaymentTermsDefendantAccount.builder()
            .daysInDefault(source.getDaysInDefault().orElse(null))
            .dateDaysInDefaultImposed(source.getDateDaysInDefaultImposed().orElse(null))
            .extension(source.getExtension())
            .reasonForExtension(source.getReasonForExtension().orElse(null))
            .paymentTermsType(paymentTermsType == null ? null : PaymentTermsTypeCommonStrict.builder()
                .paymentTermsTypeCode(PaymentTermsTypeCommonStrict.PaymentTermsTypeCodeEnum.fromValue(
                    paymentTermsType.getPaymentTermsTypeCode().getValue()))
                .paymentTermsTypeDisplayName(toPaymentTermsDisplayName(
                    paymentTermsType.getPaymentTermsTypeCode().getValue()))
                .build())
            .effectiveDate(source.getEffectiveDate().orElse(null))
            .instalmentPeriod(JsonNullable.of(instalmentPeriod == null ? null : InstalmentPeriodCommonStrict.builder()
                .instalmentPeriodCode(InstalmentPeriodCommonStrict.InstalmentPeriodCodeEnum.fromValue(
                    instalmentPeriod.getInstalmentPeriodCode().getValue()))
                .instalmentPeriodDisplayName(toInstalmentPeriodDisplayName(
                    instalmentPeriod.getInstalmentPeriodCode().getValue()))
                .build()))
            .lumpSumAmount(source.getLumpSumAmount().orElse(null))
            .instalmentAmount(source.getInstalmentAmount().orElse(null))
            .postedDetails(JsonNullable.of(source.getPostedDetails().orElse(null)))
            .build();
    }

    private static PaymentTermsTypeCommonStrict.PaymentTermsTypeDisplayNameEnum toPaymentTermsDisplayName(String code) {
        return PaymentTermsTypeCommonStrict.PaymentTermsTypeDisplayNameEnum.fromValue(switch (code) {
            case "B" -> "By date";
            case "P" -> "Paid";
            case "I" -> "Instalments";
            default -> throw new IllegalArgumentException("Unknown payment terms type: " + code);
        });
    }

    private static InstalmentPeriodCommonStrict.InstalmentPeriodDisplayNameEnum toInstalmentPeriodDisplayName(
        String code) {
        return InstalmentPeriodCommonStrict.InstalmentPeriodDisplayNameEnum.fromValue(switch (code) {
            case "W" -> "Weekly";
            case "M" -> "Monthly";
            case "F" -> "Fortnightly";
            default -> throw new IllegalArgumentException("Unknown instalment period: " + code);
        });
    }
}
