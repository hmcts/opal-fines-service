package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import uk.gov.hmcts.opal.generated.model.DefendantAccountInstalmentPeriodCommonStrict.InstalmentPeriodCodeEnum;
import uk.gov.hmcts.opal.generated.model.DefendantAccountInstalmentPeriodCommonStrict.InstalmentPeriodDisplayNameEnum;
import uk.gov.hmcts.opal.generated.model.DefendantAccountPaymentTermsTypeCommonStrict.PaymentTermsTypeCodeEnum;

class DefendantAccountPaymentTermsLegacyResponseMapperTest {

    private final DefendantAccountPaymentTermsLegacyResponseMapper mapper =
        Mappers.getMapper(DefendantAccountPaymentTermsLegacyResponseMapper.class);

    @Test
    void shouldMapToResponse() {
        LegacyPaymentTerms paymentTerms = getLegacyPaymentTerms();

        LegacyGetDefendantAccountPaymentTermsResponse legacyGetDefendantAccountPaymentTermsResponse =
            new LegacyGetDefendantAccountPaymentTermsResponse(new BigInteger("5"), paymentTerms,
                LocalDate.of(2026, 8, 15), "123456");

        var defendantAccountPaymentTerm = mapper.toResponse(legacyGetDefendantAccountPaymentTermsResponse);

        assertNotNull(defendantAccountPaymentTerm);
        assertEquals(5L, defendantAccountPaymentTerm.getVersion());
        assertEquals(LocalDate.of(2026, 8, 15), defendantAccountPaymentTerm.getPaymentCardLastRequested());
        assertEquals("123456", defendantAccountPaymentTerm.getLastEnforcement());

        var defendantAccountPaymentTerms = defendantAccountPaymentTerm.getPaymentTerms();
        assertNotNull(defendantAccountPaymentTerms);
        assertEquals(10, defendantAccountPaymentTerms.getDaysInDefault().get());
        assertEquals(LocalDate.of(2026, 8, 15), defendantAccountPaymentTerms.getDateDaysInDefaultImposed().get());
        assertEquals(BigDecimal.valueOf(1000.99), defendantAccountPaymentTerms.getLumpSumAmount().get());
        assertEquals(PaymentTermsTypeCodeEnum.I,
            defendantAccountPaymentTerms.getPaymentTermsType().getPaymentTermsTypeCode());
        assertEquals(InstalmentPeriodCodeEnum.M,
            defendantAccountPaymentTerms.getInstalmentPeriod().get().getInstalmentPeriodCode());
        assertEquals(InstalmentPeriodDisplayNameEnum.MONTHLY,
            defendantAccountPaymentTerms.getInstalmentPeriod().get().getInstalmentPeriodDisplayName());
        assertEquals(LocalDateTime.of(2026, 8, 20, 9, 0),
            defendantAccountPaymentTerms.getPostedDetails().get().getPostedDate());
        assertEquals("John Doe", defendantAccountPaymentTerms.getPostedDetails().get().getPostedBy().get());
        assertEquals("j.doe", defendantAccountPaymentTerms.getPostedDetails().get().getPostedByName().get());
    }

    @Test
    void shouldDefaultVersionToOne() {
        LegacyGetDefendantAccountPaymentTermsResponse legacy =
            LegacyGetDefendantAccountPaymentTermsResponse.builder().version(null).build();

        var result = mapper.toResponse(legacy);
        assertEquals(1L, result.getVersion());
    }

    private static @NonNull LegacyPaymentTerms getLegacyPaymentTerms() {
        LocalDateTime postedDate = LocalDateTime.of(2026, 8, 20, 9, 0, 0);

        LegacyPostedDetails postedDetails = new LegacyPostedDetails(postedDate, "John Doe", "j.doe");

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