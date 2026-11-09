package uk.gov.hmcts.opal.service.report;

import static uk.gov.hmcts.opal.dto.ToJsonString.toJsonNode;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import uk.gov.hmcts.opal.entity.PaymentInEntity;

final class PaymentReportAdditionalInformation {

    private PaymentReportAdditionalInformation() {
    }

    static String displayText(PaymentInEntity payment) {
        String value = payment.getAdditionalInformation();
        if (!payment.isAutoPayment()) {
            return value;
        }
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            JsonNode information = toJsonNode(value);
            if (!information.isObject()) {
                throw invalidInformation(payment);
            }
            JsonNode interfaceName = information.path("interface_name");
            if (interfaceName.isMissingNode()) {
                return null;
            }
            if (!interfaceName.isString()) {
                throw invalidInformation(payment);
            }
            String name = interfaceName.asString();
            return name.isBlank() ? null : name;
        } catch (JacksonException exception) {
            throw invalidInformation(payment, exception);
        }
    }

    private static IllegalArgumentException invalidInformation(PaymentInEntity payment) {
        return invalidInformation(payment, null);
    }

    private static IllegalArgumentException invalidInformation(PaymentInEntity payment, Exception cause) {
        return new IllegalArgumentException(
            "Payment " + payment.getPaymentInId() + " has invalid auto-payment additional_information", cause);
    }
}
