package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.dto.legacy.LegacyInstalmentPeriod;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTerms;
import uk.gov.hmcts.opal.dto.legacy.LegacyPaymentTermsType;
import uk.gov.hmcts.opal.dto.legacy.LegacyPostedDetails;
import uk.gov.hmcts.opal.generated.model.PaymentTermsDefendantAccount;

class LegacyPaymentTermsMapperTest {

    private final LegacyPaymentTermsMapper mapper = Mappers.getMapper(LegacyPaymentTermsMapper.class);

    @Test
    void toOpal_mapsAllFieldsAndNestedValues() {
        LegacyPaymentTerms source = LegacyPaymentTerms.builder()
            .daysInDefault(12)
            .dateDaysInDefaultImposed(LocalDate.of(2026, 8, 28))
            .extension(true)
            .reasonForExtension("reason")
            .paymentTermsType(new LegacyPaymentTermsType(LegacyPaymentTermsType.PaymentTermsTypeCode.I))
            .effectiveDate(LocalDate.of(2026, 9, 1))
            .instalmentPeriod(new LegacyInstalmentPeriod(LegacyInstalmentPeriod.InstalmentPeriodCode.F))
            .lumpSumAmount(new BigDecimal("10.50"))
            .instalmentAmount(new BigDecimal("2.50"))
            .postedDetails(new LegacyPostedDetails(
                LocalDateTime.of(2026, 8, 28, 10, 15), "user-1", "User One"))
            .build();

        PaymentTermsDefendantAccount result = mapper.toOpal(source);

        assertAll(
            () -> assertEquals(12, result.getDaysInDefault().orElse(null)),
            () -> assertEquals(LocalDate.of(2026, 8, 28), result.getDateDaysInDefaultImposed().orElse(null)),
            () -> assertTrue(result.getExtension()),
            () -> assertEquals("reason", result.getReasonForExtension().orElse(null)),
            () -> assertEquals("I", result.getPaymentTermsType().getPaymentTermsTypeCode().getValue()),
            () -> assertEquals(LocalDate.of(2026, 9, 1), result.getEffectiveDate().orElse(null)),
            () -> assertEquals("F", result.getInstalmentPeriod().orElseThrow().getInstalmentPeriodCode().getValue()),
            () -> assertEquals(new BigDecimal("10.50"), result.getLumpSumAmount().orElse(null)),
            () -> assertEquals(new BigDecimal("2.50"), result.getInstalmentAmount().orElse(null)),
            () -> assertEquals(LocalDateTime.of(2026, 8, 28, 10, 15),
                result.getPostedDetails().orElseThrow().getPostedDate()),
            () -> assertEquals("user-1", result.getPostedDetails().orElseThrow().getPostedBy().orElse(null)),
            () -> assertEquals("User One", result.getPostedDetails().orElseThrow().getPostedByName().orElse(null))
        );
    }

    @Test
    void toOpal_leavesNullableFieldsUndefinedWhenSourceValuesAreNull() {
        PaymentTermsDefendantAccount result = mapper.toOpal(LegacyPaymentTerms.builder().extension(false).build());

        assertAll(
            () -> assertNull(result.getDaysInDefault().orElse(null)),
            () -> assertNull(result.getDateDaysInDefaultImposed().orElse(null)),
            () -> assertNull(result.getReasonForExtension().orElse(null)),
            () -> assertNull(result.getPaymentTermsType()),
            () -> assertNull(result.getEffectiveDate().orElse(null)),
            () -> assertFalse(result.getInstalmentPeriod().isPresent()),
            () -> assertNull(result.getLumpSumAmount().orElse(null)),
            () -> assertNull(result.getInstalmentAmount().orElse(null)),
            () -> assertFalse(result.getPostedDetails().isPresent())
        );
    }

    @Test
    void toOpal_nullSource_returnsNull() {
        assertNull(mapper.toOpal(null));
    }

    @ParameterizedTest
    @EnumSource(LegacyPaymentTermsType.PaymentTermsTypeCode.class)
    void toOpal_mapsAllPaymentTermsTypeCodes(LegacyPaymentTermsType.PaymentTermsTypeCode code) {
        PaymentTermsDefendantAccount result = mapper.toOpal(LegacyPaymentTerms.builder()
            .paymentTermsType(new LegacyPaymentTermsType(code))
            .build());

        assertEquals(code.name(), result.getPaymentTermsType().getPaymentTermsTypeCode().getValue());
    }

    @ParameterizedTest
    @EnumSource(LegacyInstalmentPeriod.InstalmentPeriodCode.class)
    void toOpal_mapsAllInstalmentPeriodCodes(LegacyInstalmentPeriod.InstalmentPeriodCode code) {
        PaymentTermsDefendantAccount result = mapper.toOpal(LegacyPaymentTerms.builder()
            .instalmentPeriod(new LegacyInstalmentPeriod(code))
            .build());

        assertEquals(code.name(), result.getInstalmentPeriod().orElseThrow().getInstalmentPeriodCode().getValue());
    }
}
