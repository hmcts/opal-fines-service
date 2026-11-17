package uk.gov.hmcts.opal.service.opal.till;

import static uk.gov.hmcts.opal.mapper.helper.JsonNullableMapperHelper.fromJsonNullable;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.opal.generated.model.TillsCreateAdditionalInformation;
import uk.gov.hmcts.opal.generated.model.TillsCreatePayerDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePayerType;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentIn;
import uk.gov.hmcts.opal.generated.model.TillsCreateRequest;

final class CreateTillValidator {

    private CreateTillValidator() {
    }

    static void validate(TillsCreateRequest request) {
        request.getPaymentsIn().forEach(CreateTillValidator::validatePayment);
    }

    private static void validatePayment(TillsCreatePaymentIn payment) {
        TillsCreateAdditionalInformation information = payment.getAdditionalInformation();
        require(information != null, "additional_information is required for each payment");
        require(information.getPaymentReceivedFrom() != null, "payment_received_from is required");
        validatePayerShape(information);
        TillsCreatePaymentDetails details = payment.getPaymentDetails();
        if (details.getMethod() == TillsCreatePaymentDetails.MethodEnum.CT) {
            require(information.getTransferSource() != null,
                "transfer_source is required for Credit Transfer payments");
        }
        if (details.getDestinationType() == TillsCreatePaymentDetails.DestinationTypeEnum.F) {
            validateFines(payment, information);
        } else {
            require(payment.getDefendantAccountId() == null,
                "defendant_account_id must not be supplied for Court Fee or Suspense payments");
            requireText(information.getSuspenseNote(), "suspense_note");
            if (details.getDestinationType() == TillsCreatePaymentDetails.DestinationTypeEnum.C) {
                validateCourtFee(information);
            } else {
                validateSuspense(information);
            }
        }
    }

    private static void validateFines(TillsCreatePaymentIn payment, TillsCreateAdditionalInformation information) {
        require(payment.getDefendantAccountId() != null, "defendant_account_id is required for fines payments");
        require(information.getPaymentReceivedFrom() != TillsCreateAdditionalInformation.PaymentReceivedFromEnum.A,
            "payment_received_from must be D or T for fines payments");
        require(information.getPayerType() == null && fromJsonNullable(information.getPayerDetails()) == null,
            "payer_type and payer_details must not be supplied for fines payments");
        rejectCourtFeeFields(information);
        require(information.getSuspenseReasonCode() == null && information.getSuspenseReasonDetail() == null
            && information.getSuspenseNote() == null, "Suspense fields must not be supplied for fines payments");
        if (information.getPaymentReceivedFrom() == TillsCreateAdditionalInformation.PaymentReceivedFromEnum.T) {
            requireText(information.getThirdPartyPayerName(), "third_party_payer_name");
        }
    }

    private static void validateCourtFee(TillsCreateAdditionalInformation information) {
        require(information.getPaymentReceivedFrom() != TillsCreateAdditionalInformation.PaymentReceivedFromEnum.D,
            "payment_received_from must be A or T for Court Fee payments");
        requireText(information.getCourtFeeCode(), "court_fee_code");
        require(information.getNumberOfItems() != null && information.getNumberOfItems() > 0,
            "number_of_items must be greater than zero for Court Fee payments");
        require(information.getSuspenseReasonCode() == null && information.getSuspenseReasonDetail() == null,
            "Suspense reason fields must not be supplied for Court Fee payments");
        requirePayer(information);
    }

    private static void validateSuspense(TillsCreateAdditionalInformation information) {
        rejectCourtFeeFields(information);
        require(information.getSuspenseReasonCode() != null, "suspense_reason_code is required for Suspense payments");
        if (information.getSuspenseReasonCode() != TillsCreateAdditionalInformation.SuspenseReasonCodeEnum.UN) {
            require(information.getSuspenseReasonDetail() == null,
                "suspense_reason_detail is only permitted for UN Suspense payments");
            requirePayer(information);
        }
    }

    private static void rejectCourtFeeFields(TillsCreateAdditionalInformation information) {
        require(information.getCourtFeeCode() == null && information.getNumberOfItems() == null,
            "court_fee_code and number_of_items are only permitted for Court Fee payments");
    }

    private static void validatePayerShape(TillsCreateAdditionalInformation information) {
        TillsCreatePayerType type = information.getPayerType();
        TillsCreatePayerDetails payer = fromJsonNullable(information.getPayerDetails());
        if (type == null && payer == null) {
            return;
        }
        require(type != null && payer != null, "payer_type and payer_details must be supplied together");
        if (type == TillsCreatePayerType.INDIVIDUAL) {
            require(payer.getIndividual() != null && payer.getOrganisation() == null,
                "Individual payer_details must contain only an individual name structure");
        } else {
            require(payer.getOrganisation() != null && payer.getIndividual() == null,
                "Organisation payer_details must contain only an organisation name structure");
        }
    }

    private static void requirePayer(TillsCreateAdditionalInformation information) {
        require(information.getPayerType() != null && fromJsonNullable(information.getPayerDetails()) != null,
            "payer_type and payer_details are required for this payment scenario");
    }

    private static void requireText(String value, String field) {
        require(value != null && !value.isBlank(), field + " is required and must not be blank");
    }

    private static void require(boolean valid, String message) {
        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
