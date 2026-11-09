package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.opal.entity.PaymentInEntity;

class PaymentReportAdditionalInformationTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "{}", "{\"interface_name\":\"\"}", "{\"interface_name\":\" \"}"})
    void displayText_returnsNoDetailWhenAutoPaymentInterfaceNameIsAbsent(String information) {
        PaymentInEntity payment = PaymentInEntity.builder()
            .paymentInId(1L)
            .autoPayment(true)
            .additionalInformation(information)
            .build();

        assertThat(PaymentReportAdditionalInformation.displayText(payment)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{", "PAYMENTS_IN", "null", "[]", "\"PAYMENTS_IN\"",
        "{\"interface_name\":123}", "{\"interface_name\":{}}", "{\"interface_name\":null}"
    })
    void displayText_rejectsInvalidAutoPaymentInformationWithPaymentId(String information) {
        PaymentInEntity payment = PaymentInEntity.builder()
            .paymentInId(42L)
            .autoPayment(true)
            .additionalInformation(information)
            .build();

        assertThatThrownBy(() -> PaymentReportAdditionalInformation.displayText(payment))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Payment 42 has invalid auto-payment additional_information");
    }
}
