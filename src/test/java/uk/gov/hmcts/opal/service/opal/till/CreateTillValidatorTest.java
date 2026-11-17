package uk.gov.hmcts.opal.service.opal.till;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.opal.service.opal.till.TillRequestTestData.payment;
import static uk.gov.hmcts.opal.service.opal.till.TillRequestTestData.request;

import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.generated.model.TillsCreateAdditionalInformation;
import uk.gov.hmcts.opal.generated.model.TillsCreateIndividualPayer;
import uk.gov.hmcts.opal.generated.model.TillsCreateOrganisationPayer;
import uk.gov.hmcts.opal.generated.model.TillsCreatePayerDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePayerType;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentIn;

class CreateTillValidatorTest {

    @ParameterizedTest
    @CsvSource({"F,", "C,", "S,OF", "S,FA", "S,MS", "S,UN"})
    void acceptsSupportedScenarios(String destination, String reason) {
        assertThatCode(() -> CreateTillValidator.validate(request(payment(destination, reason))))
            .doesNotThrowAnyException();
    }

    @Test
    void acceptsCriminalThirdPartyWithoutStructuredPayer() {
        TillsCreatePaymentIn payment = payment("F", null);
        payment.getAdditionalInformation().setPaymentReceivedFrom(
            TillsCreateAdditionalInformation.PaymentReceivedFromEnum.T);
        payment.getAdditionalInformation().setThirdPartyPayerName("Third party");
        assertThatCode(() -> CreateTillValidator.validate(request(payment))).doesNotThrowAnyException();
        payment.getAdditionalInformation().setThirdPartyPayerName(" ");
        rejects(payment, "third_party_payer_name");
    }

    @ParameterizedTest
    @CsvSource({"C,", "S,OF", "S,FA", "S,MS"})
    void requiresPayerForIdentifiedPayments(String destination, String reason) {
        TillsCreatePaymentIn payment = payment(destination, reason);
        payment.getAdditionalInformation().setPayerType(null);
        payment.getAdditionalInformation().setPayerDetails(null);
        rejects(payment, "payer_type and payer_details are required");
    }

    @ParameterizedTest
    @EnumSource(TillsCreatePaymentDetails.MethodEnum.class)
    void requiresSourceOnlyForCreditTransfer(TillsCreatePaymentDetails.MethodEnum method) {
        TillsCreatePaymentIn payment = payment("F", null);
        payment.getPaymentDetails().setMethod(method);
        if (method == TillsCreatePaymentDetails.MethodEnum.CT) {
            rejects(payment, "transfer_source");
        } else {
            assertThatCode(() -> CreateTillValidator.validate(request(payment))).doesNotThrowAnyException();
        }
        payment.getAdditionalInformation().setTransferSource(TillsCreateAdditionalInformation.TransferSourceEnum.OTHER);
        assertThatCode(() -> CreateTillValidator.validate(request(payment))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(TillsCreatePayerType.class)
    void acceptsMatchingNamesWithOptionalFields(TillsCreatePayerType type) {
        TillsCreatePaymentIn payment = payment("S", "UN");
        payment.getAdditionalInformation().setPayerType(type);
        TillsCreatePayerDetails details = TillsCreatePayerDetails.builder().build();
        if (type == TillsCreatePayerType.INDIVIDUAL) {
            details.setIndividual(TillsCreateIndividualPayer.builder().build());
        } else {
            details.setOrganisation(TillsCreateOrganisationPayer.builder().build());
        }
        payment.getAdditionalInformation().payerDetails(details);
        assertThatCode(() -> CreateTillValidator.validate(request(payment))).doesNotThrowAnyException();
        details.setIndividual(TillsCreateIndividualPayer.builder().build());
        details.setOrganisation(TillsCreateOrganisationPayer.builder().build());
        rejects(payment, "name structure");
    }

    @ParameterizedTest
    @MethodSource("invalidPayments")
    void rejectsInvalidScenario(String destination, String reason, Consumer<TillsCreatePaymentIn> change,
                                String message) {
        TillsCreatePaymentIn payment = payment(destination, reason);
        change.accept(payment);
        rejects(payment, message);
    }

    private static Stream<Arguments> invalidPayments() {
        return Stream.of(
            invalid("F", null, p -> p.setDefendantAccountId(null), "defendant_account_id"),
            invalid("C", null, p -> p.setDefendantAccountId(99L), "defendant_account_id"),
            invalid("S", "UN", p -> p.setDefendantAccountId(99L), "defendant_account_id"),
            invalid("F", null, p -> p.getAdditionalInformation().setPaymentReceivedFrom(
                TillsCreateAdditionalInformation.PaymentReceivedFromEnum.A), "payment_received_from"),
            invalid("C", null, p -> p.getAdditionalInformation().setPaymentReceivedFrom(
                TillsCreateAdditionalInformation.PaymentReceivedFromEnum.D), "payment_received_from"),
            invalid("F", null, p -> p.getAdditionalInformation().setPayerType(TillsCreatePayerType.INDIVIDUAL),
                "must be supplied together"),
            invalid("F", null, p -> p.getAdditionalInformation().setCourtFeeCode("FEE"), "only permitted"),
            invalid("F", null, p -> p.getAdditionalInformation().setSuspenseNote("note"), "Suspense fields"),
            invalid("C", null, p -> p.getAdditionalInformation().setCourtFeeCode(" "), "court_fee_code"),
            invalid("C", null, p -> p.getAdditionalInformation().setNumberOfItems((short) 0), "number_of_items"),
            invalid("C", null, p -> p.getAdditionalInformation().setSuspenseNote(null), "suspense_note"),
            invalid("S", "UN", p -> p.getAdditionalInformation().setSuspenseNote(" "), "suspense_note"),
            invalid("S", "UN", p -> p.getAdditionalInformation().setSuspenseReasonCode(null), "suspense_reason_code"),
            invalid("S", "OF", p -> p.getAdditionalInformation().setSuspenseReasonDetail("detail"), "only permitted"),
            invalid("S", "UN", p -> p.getAdditionalInformation().setNumberOfItems((short) 1), "only permitted"),
            invalid("C", null, p -> p.getAdditionalInformation().setSuspenseReasonCode(
                TillsCreateAdditionalInformation.SuspenseReasonCodeEnum.UN), "Suspense reason fields"));
    }

    private static Arguments invalid(String destination, String reason, Consumer<TillsCreatePaymentIn> change,
                                     String message) {
        return Arguments.of(destination, reason, change, message);
    }

    private static void rejects(TillsCreatePaymentIn payment, String message) {
        assertThatThrownBy(() -> CreateTillValidator.validate(request(payment)))
            .isInstanceOf(ResponseStatusException.class).hasMessageContaining(message);
    }
}
