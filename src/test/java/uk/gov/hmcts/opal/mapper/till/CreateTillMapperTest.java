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
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.PaymentMethod;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.TillStatusEnum;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.TillsCreateAdditionalInformation;
import uk.gov.hmcts.opal.generated.model.TillsCreateIndividualPayer;
import uk.gov.hmcts.opal.generated.model.TillsCreatePayerDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePayerType;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentIn;

class CreateTillMapperTest {

    private final CreateTillMapper mapper = Mappers.getMapper(CreateTillMapper.class);

    @Test
    void mapsIndividualNameAndOptionalAddressToParty() {
        PartyEntity party = mapper.toParty(TillsCreatePayerDetails.builder()
            .individual(TillsCreateIndividualPayer.builder().title("Ms").forenames("Jane").surname("Smith").build())
            .addressLine1("1 Street").addressLine2("Town").addressLine3("County").postcode("AB1 2CD").build(),
            TillsCreatePayerType.INDIVIDUAL);
        assertThat(party.isOrganisation()).isFalse();
        assertThat(party.getTitle()).isEqualTo("Ms");
        assertThat(party.getForenames()).isEqualTo("Jane");
        assertThat(party.getSurname()).isEqualTo("Smith");
        assertThat(party.getAddressLine1()).isEqualTo("1 Street");
        assertThat(party.getAddressLine2()).isEqualTo("Town");
        assertThat(party.getAddressLine3()).isEqualTo("County");
        assertThat(party.getPostcode()).isEqualTo("AB1 2CD");
        assertThat(party.getOrganisationName()).isNull();
    }

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
            .additionalInformation(TillsCreateAdditionalInformation.builder().thirdPartyPayerName("Payer").build())
            .paymentDetails(TillsCreatePaymentDetails.builder()
                .amount(new BigDecimal("12.50"))
                .method(TillsCreatePaymentDetails.MethodEnum.NC)
                .destinationType(TillsCreatePaymentDetails.DestinationTypeEnum.F)
                .allocationType("FULL")
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
            Arguments.of(TillsCreatePaymentDetails.MethodEnum.NC, PaymentMethod.NC),
            Arguments.of(TillsCreatePaymentDetails.MethodEnum.CQ, PaymentMethod.CQ),
            Arguments.of(TillsCreatePaymentDetails.MethodEnum.CT, PaymentMethod.CT),
            Arguments.of(TillsCreatePaymentDetails.MethodEnum.PO, PaymentMethod.PO)
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
            Arguments.of(TillsCreatePaymentDetails.DestinationTypeEnum.F, DestinationType.F),
            Arguments.of(TillsCreatePaymentDetails.DestinationTypeEnum.S, DestinationType.S),
            Arguments.of(TillsCreatePaymentDetails.DestinationTypeEnum.C, DestinationType.C)
        };
    }

}
