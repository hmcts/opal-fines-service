package uk.gov.hmcts.opal.mapper.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.xml.bind.JAXBException;
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
import uk.gov.hmcts.opal.util.XmlUtil;

class DefendantAccountPaymentTermsLegacyResponseMapperTest {

    private final DefendantAccountPaymentTermsLegacyResponseMapper mapper =
        Mappers.getMapper(DefendantAccountPaymentTermsLegacyResponseMapper.class);

    @Test
    void shouldMapToResponse() {
        LegacyPaymentTerms paymentTerms = getLegacyPaymentTerms();

        LegacyGetDefendantAccountPaymentTermsResponse legacyGetDefendantAccountPaymentTermsResponse =
            LegacyGetDefendantAccountPaymentTermsResponse.builder()
                .version(new BigInteger("5"))
                .paymentTerms(paymentTerms)
                .paymentCardLastRequested(LocalDate.of(2026, 8, 15))
                .lastEnforcement("123456")
                .build();

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

    @Test
    void shouldReturnFortnightlyInstalmentPeriodDisplayName() {
        LegacyPaymentTerms paymentTerms = getLegacyPaymentTerms();
        paymentTerms.setInstalmentPeriod(new LegacyInstalmentPeriod(InstalmentPeriodCode.F));

        LegacyGetDefendantAccountPaymentTermsResponse legacyGetDefendantAccountPaymentTermsResponse =
            LegacyGetDefendantAccountPaymentTermsResponse.builder()
                .version(new BigInteger("5"))
                .paymentTerms(paymentTerms)
                .paymentCardLastRequested(LocalDate.of(2026, 8, 15))
                .lastEnforcement("123456")
                .build();

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
        assertEquals(InstalmentPeriodCodeEnum.F,
            defendantAccountPaymentTerms.getInstalmentPeriod().get().getInstalmentPeriodCode());
        assertEquals(InstalmentPeriodDisplayNameEnum.FORTNIGHTLY,
            defendantAccountPaymentTerms.getInstalmentPeriod().get().getInstalmentPeriodDisplayName());
        assertEquals(LocalDateTime.of(2026, 8, 20, 9, 0),
            defendantAccountPaymentTerms.getPostedDetails().get().getPostedDate());
        assertEquals("John Doe", defendantAccountPaymentTerms.getPostedDetails().get().getPostedBy().get());
        assertEquals("j.doe", defendantAccountPaymentTerms.getPostedDetails().get().getPostedByName().get());
    }

    @Test
    void shouldMapBritishInstalmentXmlResponse() throws JAXBException {
        var response = unmarshalXmlResponse("instalment_period", "instalment_period_code", "instalment_amount");

        assertMappedInstalmentPaymentTerms(response);
    }

    @Test
    void shouldMapAmericanInstalmentXmlResponseForBackwardCompatibility() throws JAXBException {
        var response = unmarshalXmlResponse("installment_period", "installment_period_code", "installment_amount");

        assertMappedInstalmentPaymentTerms(response);
    }

    private void assertMappedInstalmentPaymentTerms(LegacyGetDefendantAccountPaymentTermsResponse response) {
        var mappedResponse = mapper.toResponse(response);

        assertNotNull(mappedResponse);
        assertEquals(LocalDate.of(2026, 1, 15), mappedResponse.getPaymentCardLastRequested());

        var paymentTerms = mappedResponse.getPaymentTerms();
        assertNotNull(paymentTerms);
        assertEquals("Updated instalments", paymentTerms.getReasonForExtension().get());
        assertEquals(LocalDate.of(2026, 1, 15), paymentTerms.getEffectiveDate().get());
        assertEquals(PaymentTermsTypeCodeEnum.I,
            paymentTerms.getPaymentTermsType().getPaymentTermsTypeCode());
        assertEquals(new BigDecimal("45.00"), paymentTerms.getInstalmentAmount().get());
        assertEquals(InstalmentPeriodCodeEnum.M,
            paymentTerms.getInstalmentPeriod().get().getInstalmentPeriodCode());
        assertEquals(InstalmentPeriodDisplayNameEnum.MONTHLY,
            paymentTerms.getInstalmentPeriod().get().getInstalmentPeriodDisplayName());
    }

    private LegacyGetDefendantAccountPaymentTermsResponse unmarshalXmlResponse(
        String instalmentPeriodElement,
        String instalmentPeriodCodeElement,
        String instalmentAmountElement) throws JAXBException {
        return XmlUtil.unmarshalXmlString("""
            <response>
              <version>9223372036854775808</version>
              <payment_terms>
                <reason_for_extension>Updated instalments</reason_for_extension>
                <payment_terms_type>
                  <payment_terms_type_code>I</payment_terms_type_code>
                </payment_terms_type>
                <effective_date>2026-01-15</effective_date>
                <%s>
                  <%s>M</%s>
                </%s>
                <%s>45.00</%s>
              </payment_terms>
              <payment_card_last_requested>2026-01-15</payment_card_last_requested>
            </response>
            """.formatted(
            instalmentPeriodElement,
            instalmentPeriodCodeElement,
            instalmentPeriodCodeElement,
            instalmentPeriodElement,
            instalmentAmountElement,
            instalmentAmountElement), LegacyGetDefendantAccountPaymentTermsResponse.class);
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
