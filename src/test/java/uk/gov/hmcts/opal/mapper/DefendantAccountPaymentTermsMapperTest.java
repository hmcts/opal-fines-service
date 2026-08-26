package uk.gov.hmcts.opal.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod.InstalmentPeriodCode;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType.PaymentTermsTypeCode;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.paymentterms.InstalmentPeriod;
import uk.gov.hmcts.opal.entity.paymentterms.PaymentTermsEntity;
import uk.gov.hmcts.opal.entity.paymentterms.TermsTypeCode;
import uk.gov.hmcts.opal.generated.model.DefendantAccountInstalmentPeriodCommonStrict.InstalmentPeriodCodeEnum;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsTypeCommonStrict.PaymentTermsTypeCodeEnum;

class DefendantAccountPaymentTermsMapperTest {

    private final DefendantAccountPaymentTermsMapper mapper =
        Mappers.getMapper(DefendantAccountPaymentTermsMapper.class);

    @Test
    void shouldMapPaymentTermsEntityToResponse() {
        DefendantAccountEntity defendantAccount = DefendantAccountEntity.builder()
            .paymentCardRequestedDate(LocalDate.of(2026, 8, 1))
            .lastEnforcement("123456")
            .suspendedCommittalDate(LocalDate.of(2026, 2, 23))
            .versionNumber(5L)
            .build();

        PaymentTermsEntity paymentTerms = PaymentTermsEntity.builder()
            .defendantAccount(defendantAccount)
            .jailDays(10)
            .termsTypeCode(TermsTypeCode.INSTALMENTS)
            .instalmentPeriod(InstalmentPeriod.FORTNIGHT)
            .instalmentLumpSum(new BigDecimal("1000.99"))
            .postedDate(LocalDateTime.of(2026, 6, 10, 10, 0, 0))
            .postedBy("John Doe").postedByUsername("j.doe")
            .build();

        DefendantAccountPaymentTermsResponse defendantAccountPaymentTermsResponse = mapper.toResponse(paymentTerms);

        assertThat(defendantAccountPaymentTermsResponse).isNotNull().extracting(
                DefendantAccountPaymentTermsResponse::getPaymentCardLastRequested,
                DefendantAccountPaymentTermsResponse::getLastEnforcement,
                DefendantAccountPaymentTermsResponse::getVersion
        ).containsExactly(defendantAccount.getPaymentCardRequestedDate(),
            defendantAccount.getLastEnforcement(), 5L);

        assertThat(defendantAccountPaymentTermsResponse.getPaymentTerms())
            .isNotNull().satisfies(paymentTermsCommonStrict -> {
                assertThat(paymentTermsCommonStrict.getDaysInDefault().get()).isEqualTo(10);
                assertThat(paymentTermsCommonStrict.getDateDaysInDefaultImposed().get())
                    .isEqualTo(LocalDate.of(2026, 2, 23));
                assertThat(paymentTermsCommonStrict.getLumpSumAmount().get())
                    .isEqualTo(new BigDecimal("1000.99"));
                assertThat(paymentTermsCommonStrict.getPaymentTermsType().getPaymentTermsTypeCode())
                    .isEqualTo(PaymentTermsTypeCodeEnum.I);
                assertThat(paymentTermsCommonStrict.getInstalmentPeriod().get().getInstalmentPeriodCode())
                    .isEqualTo(InstalmentPeriodCodeEnum.F);
                assertThat(paymentTermsCommonStrict.getPostedDetails().get().getPostedDate())
                    .isEqualTo(LocalDateTime.of(2026, 6, 10, 10, 0, 0));
                assertThat(paymentTermsCommonStrict.getPostedDetails().get().getPostedBy().get())
                    .isEqualTo("John Doe");
                assertThat(paymentTermsCommonStrict.getPostedDetails().get().getPostedByName().get())
                    .isEqualTo("j.doe");
            });
    }

    @Test
    void shouldDefaultVersionToOne() {
        DefendantAccountEntity defendantAccount = DefendantAccountEntity.builder()
            .versionNumber(null)
            .build();

        PaymentTermsEntity entity = PaymentTermsEntity.builder()
            .defendantAccount(defendantAccount)
            .build();

        DefendantAccountPaymentTermsResponse defendantAccountPaymentTermsResponse =
            mapper.toResponse(entity);

        assertThat(defendantAccountPaymentTermsResponse.getVersion())
            .isEqualTo(1L);
    }

    @Test
    void shouldMapLegacyResponse() {
        LegacyPaymentTerms paymentTerms = getLegacyPaymentTerms();

        LegacyGetDefendantAccountPaymentTermsResponse legacyGetDefendantAccountPaymentTermsResponse =
            new LegacyGetDefendantAccountPaymentTermsResponse(new BigInteger("5"), paymentTerms,
                LocalDate.of(2026, 8, 15), "123456");

        DefendantAccountPaymentTermsResponse defendantAccountPaymentTermsResponse =
            mapper.legacyToResponse(legacyGetDefendantAccountPaymentTermsResponse);

        assertThat(defendantAccountPaymentTermsResponse)
            .isNotNull()
            .satisfies(response -> {
                assertThat(response.getVersion()).isEqualTo(5L);
                assertThat(response.getPaymentCardLastRequested())
                    .isEqualTo(LocalDate.of(2026,8,15));
                assertThat(response.getLastEnforcement()).isEqualTo("123456");
            });

        assertThat(defendantAccountPaymentTermsResponse.getPaymentTerms())
            .isNotNull()
            .satisfies(defendantAccountPaymentTerms -> {
                assertThat(defendantAccountPaymentTerms.getDaysInDefault().get()).isEqualTo(10);
                assertThat(defendantAccountPaymentTerms.getDateDaysInDefaultImposed().get())
                    .isEqualTo(LocalDate.of(2026, 8, 15));
                assertThat(defendantAccountPaymentTerms.getLumpSumAmount().get())
                    .isEqualTo(BigDecimal.valueOf(1000.99));
                assertThat(defendantAccountPaymentTerms.getPaymentTermsType().getPaymentTermsTypeCode())
                    .isEqualTo(PaymentTermsTypeCodeEnum.I);
                assertThat(defendantAccountPaymentTerms.getInstalmentPeriod().get().getInstalmentPeriodCode())
                    .isEqualTo(InstalmentPeriodCodeEnum.M);
                assertThat(defendantAccountPaymentTerms.getPostedDetails().get().getPostedDate())
                    .isEqualTo(LocalDateTime.of(2026,8,20,9,0));
                assertThat(defendantAccountPaymentTerms.getPostedDetails().get().getPostedBy().get())
                    .isEqualTo("John Doe");
                assertThat(defendantAccountPaymentTerms.getPostedDetails().get().getPostedByName().get())
                    .isEqualTo("j.doe");
            });
    }

    @Test
    void shouldDefaultLegacyVersionToOne() {
        LegacyGetDefendantAccountPaymentTermsResponse source = LegacyGetDefendantAccountPaymentTermsResponse.builder()
            .version(null).build();

        DefendantAccountPaymentTermsResponse result = mapper.legacyToResponse(source);

        assertThat(result.getVersion()).isEqualTo(1L);
    }

    private static @NonNull LegacyPaymentTerms getLegacyPaymentTerms() {
        LocalDateTime postedDate = LocalDateTime.of(2026, 8, 20, 9, 0, 0);

        LegacyPostedDetails postedDetails =
            new LegacyPostedDetails(postedDate, "John Doe", "j.doe");

        LegacyPaymentTerms paymentTerms = new LegacyPaymentTerms();
        paymentTerms.setDaysInDefault(10);
        paymentTerms.setInstalmentPeriod(new LegacyInstalmentPeriod(InstalmentPeriodCode.M));
        paymentTerms.setPostedDetails(postedDetails);
        paymentTerms.setDateDaysInDefaultImposed(LocalDate.of(2026, 8, 15));
        paymentTerms.setLumpSumAmount(new BigDecimal("1000.99"));
        paymentTerms.setPaymentTermsType(new LegacyPaymentTermsType(PaymentTermsTypeCode.I));
        return paymentTerms;
    }
}
