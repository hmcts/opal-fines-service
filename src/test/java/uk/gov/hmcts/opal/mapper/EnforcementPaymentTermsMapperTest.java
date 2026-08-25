package uk.gov.hmcts.opal.mapper;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.opal.generated.model.EnforcementInstalmentPeriodCommonStrict;
import uk.gov.hmcts.opal.generated.model.EnforcementPaymentTermsCommonStrict;
import uk.gov.hmcts.opal.generated.model.EnforcementPaymentTermsTypeCommonStrict;
import uk.gov.hmcts.opal.generated.model.EnforcementPostedDetailsCommonStrict;
import uk.gov.hmcts.opal.generated.model.PaymentTermsDefendantAccount;

class EnforcementPaymentTermsMapperTest {

    private final EnforcementPaymentTermsMapper mapper = Mappers.getMapper(EnforcementPaymentTermsMapper.class);

    @Nested
    class ToPaymentTerms {

        @Test
        void whenSourceContainsValues_thenMapsNullableFieldsAndEnums() {
            EnforcementPaymentTermsCommonStrict source = new EnforcementPaymentTermsCommonStrict()
                .daysInDefault(7)
                .dateDaysInDefaultImposed(LocalDate.of(2026, 5, 28))
                .extension(false)
                .reasonForExtension("extension reason")
                .paymentTermsType(new EnforcementPaymentTermsTypeCommonStrict()
                    .paymentTermsTypeCode(EnforcementPaymentTermsTypeCommonStrict.PaymentTermsTypeCodeEnum.P))
                .effectiveDate(LocalDate.of(2026, 10, 30))
                .instalmentPeriod(new EnforcementInstalmentPeriodCommonStrict()
                    .instalmentPeriodCode(EnforcementInstalmentPeriodCommonStrict.InstalmentPeriodCodeEnum.M))
                .lumpSumAmount(new BigDecimal("500.00"))
                .instalmentAmount(new BigDecimal("10.50"))
                .postedDetails(new EnforcementPostedDetailsCommonStrict()
                    .postedDate(LocalDateTime.of(2026, 5, 28, 12, 30))
                    .postedBy("user-id")
                    .postedByName("User Name"));

            PaymentTermsDefendantAccount result = mapper.toPaymentTerms(source);

            assertAll(
                () -> assertEquals(7, result.getDaysInDefault().orElse(null)),
                () -> assertEquals(LocalDate.of(2026, 5, 28), result.getDateDaysInDefaultImposed().orElse(null)),
                () -> assertFalse(result.getExtension()),
                () -> assertEquals("extension reason", result.getReasonForExtension().orElse(null)),
                () -> assertEquals("P", result.getPaymentTermsType().getPaymentTermsTypeCode().getValue()),
                () -> assertEquals(LocalDate.of(2026, 10, 30), result.getEffectiveDate().orElse(null)),
                () -> assertEquals("M",
                    result.getInstalmentPeriod().orElseThrow().getInstalmentPeriodCode().getValue()),
                () -> assertEquals(new BigDecimal("500.00"), result.getLumpSumAmount().orElse(null)),
                () -> assertEquals(new BigDecimal("10.50"), result.getInstalmentAmount().orElse(null)),
                () -> assertEquals(LocalDateTime.of(2026, 5, 28, 12, 30),
                    result.getPostedDetails().orElseThrow().getPostedDate()),
                () -> assertEquals("user-id", result.getPostedDetails().orElseThrow().getPostedBy().orElse(null)),
                () -> assertEquals("User Name", result.getPostedDetails().orElseThrow().getPostedByName().orElse(null))
            );
        }

        @Test
        void whenNullableFieldsAreExplicitlyNull_thenMapsNullValues() {
            EnforcementPaymentTermsCommonStrict source = new EnforcementPaymentTermsCommonStrict()
                .extension(true)
                .paymentTermsType(null);
            source.setDaysInDefault(JsonNullable.of(null));
            source.setDateDaysInDefaultImposed(JsonNullable.of(null));
            source.setReasonForExtension(JsonNullable.of(null));
            source.setEffectiveDate(JsonNullable.of(null));
            source.setInstalmentPeriod(JsonNullable.of(null));
            source.setLumpSumAmount(JsonNullable.of(null));
            source.setInstalmentAmount(JsonNullable.of(null));
            source.setPostedDetails(JsonNullable.of(null));

            PaymentTermsDefendantAccount result = mapper.toPaymentTerms(source);

            assertAll(
                () -> assertNull(result.getDaysInDefault().orElse(null)),
                () -> assertNull(result.getDateDaysInDefaultImposed().orElse(null)),
                () -> assertTrue(result.getExtension()),
                () -> assertNull(result.getReasonForExtension().orElse(null)),
                () -> assertNull(result.getPaymentTermsType()),
                () -> assertNull(result.getEffectiveDate().orElse(null)),
                () -> assertNull(result.getInstalmentPeriod().orElse(null)),
                () -> assertNull(result.getLumpSumAmount().orElse(null)),
                () -> assertNull(result.getInstalmentAmount().orElse(null)),
                () -> assertNull(result.getPostedDetails().orElse(null))
            );
        }
    }
}
