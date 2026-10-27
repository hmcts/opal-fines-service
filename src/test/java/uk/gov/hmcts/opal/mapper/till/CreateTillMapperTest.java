package uk.gov.hmcts.opal.mapper.till;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.DestinationType;
import uk.gov.hmcts.opal.entity.PaymentMethod;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.TillStatusEnum;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentIn;

class CreateTillMapperTest {

    private final CreateTillMapper mapper = Mappers.getMapper(CreateTillMapper.class);

    @Test
    void toTillEntity_setsManualTillDefaults() {
        BusinessUnitEntity businessUnit = BusinessUnitEntity.builder().businessUnitId((short) 10).build();
        LocalDateTime createdDate = LocalDateTime.of(2026, 9, 14, 10, 30);

        TillEntity till = mapper.toTillEntity(
            businessUnit,
            "7",
            "Test User",
            (short) 42,
            new BigDecimal("25.00"),
            (short) 2, createdDate);

        assertThat(till.getStatus()).isEqualTo(TillStatusEnum.Created);
        assertThat(till.isAutoPayment()).isFalse();
    }

    @Test
    void toPaymentIn_mapsRequestResolvedAssociationAndServerDefaults() {
        TillEntity till = TillEntity.builder().tillId(123L).build();
        LocalDateTime paymentDate = LocalDateTime.of(2026, 9, 14, 10, 30);

        TillsCreatePaymentIn requestPayment = TillsCreatePaymentIn.builder()
            .defendantAccountId(99L)
            .paymentDetails(TillsCreatePaymentDetails.builder()
                .amount(new BigDecimal("12.50"))
                .method(TillsCreatePaymentDetails.MethodEnum.NOTES_COINS)
                .destinationType(TillsCreatePaymentDetails.DestinationTypeEnum.FINES)
                .allocationType("FULL")
                .additionalInformation(TillsCreatePaymentDetails.AdditionalInformationEnum.DEFENDANT)
                .thirdPartyPayerName("Payer")
                .build())
            .build();

        PaymentInEntity payment = mapper.toPaymentIn(
            requestPayment,
            till,
            paymentDate,
            "{\"payment_received_from\":\"D\"}",
            AssociatedRecordType.DEFENDANT_ACCOUNTS,
            "99");

        assertThat(payment.getTillEntity()).isEqualTo(till);
        assertThat(payment.getPaymentAmount()).isEqualByComparingTo("12.50");
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.NC);
        assertThat(payment.getDestinationType()).isEqualTo(DestinationType.F);
        assertThat(payment.getAllocationType()).isEqualTo("FULL");
        assertThat(payment.getThirdPartyPayerName()).isEqualTo("Payer");
        assertThat(payment.isReceipt()).isFalse();
        assertThat(payment.isAllocated()).isFalse();
        assertThat(payment.isAutoPayment()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("paymentMethods")
    void toPaymentMethod_mapsPublicValueToStoredCode(TillsCreatePaymentDetails.MethodEnum source,
                                                      PaymentMethod expected) {
        assertThat(mapper.toPaymentMethod(source)).isEqualTo(expected);
    }

    private static Arguments[] paymentMethods() {
        return new Arguments[] {
            Arguments.of(TillsCreatePaymentDetails.MethodEnum.NOTES_COINS, PaymentMethod.NC),
            Arguments.of(TillsCreatePaymentDetails.MethodEnum.CHEQUE, PaymentMethod.CQ),
            Arguments.of(TillsCreatePaymentDetails.MethodEnum.CREDITOR_TRANSFER, PaymentMethod.CT),
            Arguments.of(TillsCreatePaymentDetails.MethodEnum.POSTAL_ORDER, PaymentMethod.PO)
        };
    }

    @ParameterizedTest
    @MethodSource("destinationTypes")
    void toDestinationType_mapsPublicValueToStoredCode(TillsCreatePaymentDetails.DestinationTypeEnum source,
                                                        DestinationType expected) {
        assertThat(mapper.toDestinationType(source)).isEqualTo(expected);
    }

    private static Arguments[] destinationTypes() {
        return new Arguments[] {
            Arguments.of(TillsCreatePaymentDetails.DestinationTypeEnum.FINES, DestinationType.F),
            Arguments.of(TillsCreatePaymentDetails.DestinationTypeEnum.SUSPENSE, DestinationType.S),
            Arguments.of(TillsCreatePaymentDetails.DestinationTypeEnum.COURT_FEE, DestinationType.C)
        };
    }

}
