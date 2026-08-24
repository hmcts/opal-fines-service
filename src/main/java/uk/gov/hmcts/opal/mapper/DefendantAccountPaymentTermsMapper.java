package uk.gov.hmcts.opal.mapper;

import java.math.BigInteger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsResponse;

@Mapper(componentModel = "spring", imports = BigInteger.class)
public interface DefendantAccountPaymentTermsMapper {

    //@Mapping(target = "paymentTerms", source = ".")
    @Mapping(target = "paymentCardLastRequested", source = "defendantAccount.paymentCardRequestedDate")
    @Mapping(target = "lastEnforcement", source = "defendantAccount.lastEnforcement")
    @Mapping(target = "version", source = "defendantAccount.version", defaultExpression = "java((BigInteger.ONE).longValue())")
    DefendantAccountPaymentTermsResponse toResponse(PaymentTermsEntity entity);

    //@Mapping(target = "paymentTerms", source = ".")
    @Mapping(target = "version", defaultExpression = "java((BigInteger.ONE).longValue())")
    DefendantAccountPaymentTermsResponse toResponse(LegacyGetDefendantAccountPaymentTermsResponse response);

    /*default <T> JsonNullable<T> toJsonNullable(T value) {
        return JsonNullable.of(value);
    }*/

    /*default EnforcementPaymentTermsCommonStrict toPaymentTerms(PaymentTermsEntity entity) {
        DefendantAccountEntity account = entity.getDefendantAccount();

        return EnforcementPaymentTermsCommonStrict.builder()
            .daysInDefault(entity.getJailDays())
            .dateDaysInDefaultImposed(account.getSuspendedCommittalDate())
            .extension(entity.getExtension())
            .reasonForExtension(entity.getReasonForExtension())
            //.paymentTermsType()
            .effectiveDate(entity.getEffectiveDate())
            //.instalmentPeriod()
            .lumpSumAmount(entity.getInstalmentLumpSum())
            .instalmentAmount(entity.getInstalmentAmount())
            .postedDetails(EnforcementPostedDetailsCommonStrict.builder()
                .postedDate(entity.getPostedDate())
                .postedBy(entity.getPostedBy())
                .postedByName(entity.getPostedByUsername())
                .build())
            .build();
    }*/

    /*default EnforcementPaymentTermsCommonStrict toPaymentTerms(LegacyGetDefendantAccountPaymentTermsResponse legacy) {
        return EnforcementPaymentTermsCommonStrict.builder().build();
    }*/
}
