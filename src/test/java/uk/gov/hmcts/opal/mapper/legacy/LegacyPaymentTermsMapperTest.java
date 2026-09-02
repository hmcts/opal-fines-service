package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

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
            () -> assertEquals(12, result.getDaysInDefault()),
            () -> assertEquals(LocalDate.of(2026, 8, 28), result.getDateDaysInDefaultImposed()),
            () -> assertEquals(true, result.getExtension()),
            () -> assertEquals("reason", result.getReasonForExtension()),
            () -> assertEquals("I", result.getPaymentTermsType().getPaymentTermsTypeCode().getValue()),
            () -> assertEquals(LocalDate.of(2026, 9, 1), result.getEffectiveDate()),
            () -> assertEquals("F", result.getInstalmentPeriod().orElseThrow().getInstalmentPeriodCode().getValue()),
            () -> assertEquals(new BigDecimal("10.50"), result.getLumpSumAmount()),
            () -> assertEquals(new BigDecimal("2.50"), result.getInstalmentAmount()),
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
            () -> assertNull(result.getDaysInDefault()),
            () -> assertNull(result.getDateDaysInDefaultImposed()),
            () -> assertNull(result.getReasonForExtension()),
            () -> assertNull(result.getPaymentTermsType()),
            () -> assertNull(result.getEffectiveDate()),
            () -> assertFalse(result.getInstalmentPeriod().isPresent()),
            () -> assertNull(result.getLumpSumAmount()),
            () -> assertNull(result.getInstalmentAmount()),
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
