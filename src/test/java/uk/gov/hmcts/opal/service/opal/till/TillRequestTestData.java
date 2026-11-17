package uk.gov.hmcts.opal.service.opal.till;

import java.math.BigDecimal;
import java.util.List;
import uk.gov.hmcts.opal.generated.model.TillsCreateAdditionalInformation;
import uk.gov.hmcts.opal.generated.model.TillsCreateIndividualPayer;
import uk.gov.hmcts.opal.generated.model.TillsCreatePayerDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePayerType;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentIn;
import uk.gov.hmcts.opal.generated.model.TillsCreateRequest;

final class TillRequestTestData {
    private TillRequestTestData() {
    }

    static TillsCreateRequest request(TillsCreatePaymentIn... payments) {
        return TillsCreateRequest.builder().businessUnitId((short) 10).paymentsIn(List.of(payments)).build();
    }

    static TillsCreatePaymentIn payment(String destination, String reason) {
        TillsCreateAdditionalInformation information = TillsCreateAdditionalInformation.builder()
            .paymentReceivedFrom(TillsCreateAdditionalInformation.PaymentReceivedFromEnum.D).build();
        if (!destination.equals("F")) {
            information.setSuspenseNote("Payment note");
            if (destination.equals("C")) {
                information.setPaymentReceivedFrom(TillsCreateAdditionalInformation.PaymentReceivedFromEnum.A);
                information.setCourtFeeCode("FEE");
                information.setNumberOfItems((short) 2);
            } else {
                information.setSuspenseReasonCode(TillsCreateAdditionalInformation.SuspenseReasonCodeEnum
                    .fromValue(reason));
            }
            if (!"UN".equals(reason)) {
                information.setPayerType(TillsCreatePayerType.INDIVIDUAL);
                information.payerDetails(TillsCreatePayerDetails.builder()
                    .individual(TillsCreateIndividualPayer.builder().surname("Smith").build()).build());
            }
        }
        return TillsCreatePaymentIn.builder().defendantAccountId(destination.equals("F") ? 99L : null)
            .paymentDetails(TillsCreatePaymentDetails.builder().amount(new BigDecimal("12.50"))
                .method(TillsCreatePaymentDetails.MethodEnum.NC)
                .destinationType(TillsCreatePaymentDetails.DestinationTypeEnum.fromValue(destination))
                .allocationType(destination.equals("S") ? reason : "FULL").build())
            .additionalInformation(information).build();
    }
}
