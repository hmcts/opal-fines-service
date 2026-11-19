package uk.gov.hmcts.opal.service.opal.till;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.CourtFeeEntity;
import uk.gov.hmcts.opal.entity.DestinationType;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.PaymentMethod;
import uk.gov.hmcts.opal.generated.model.TillsCreateAdditionalInformation;
import uk.gov.hmcts.opal.mapper.till.CreateTillMapper;
import uk.gov.hmcts.opal.repository.CourtFeeRepository;

@ExtendWith(MockitoExtension.class)
class CreateTillCourtFeeServiceTest {

    @Mock private CourtFeeRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private CreateTillCourtFeeService service;
    private final TillsCreateAdditionalInformation information = TillsCreateAdditionalInformation.builder()
        .courtFeeCode("FEE").numberOfItems((short) 2).build();

    @BeforeEach
    void setUp() {
        service = new CreateTillCourtFeeService(repository, Mappers.getMapper(CreateTillMapper.class), objectMapper);
    }

    @Test
    void splitsExcessAndPreservesSharedAssociationAndMetadata() {
        givenFee("5.00");
        PaymentInEntity payment = payment("12.50");
        List<PaymentInEntity> payments = service.split(payment, information, (short) 10);

        assertThat(payments).hasSize(2).allSatisfy(part -> {
            assertThat(part.getAssociatedRecordType()).isEqualTo(AssociatedRecordType.MISCELLANEOUS_ACCOUNTS);
            assertThat(part.getAssociatedRecordId()).isEqualTo("123");
            assertThat(part.getPaymentMethod()).isEqualTo(PaymentMethod.CT);
            assertThat(part.getDestinationType()).isEqualTo(DestinationType.C);
            assertThat(part.getAllocationType()).isEqualTo("FULL");
            assertThat(part.getAdditionalInformation()).contains("NATWEST", "reference", "note");
        });
        assertThat(payments.get(0)).isNotSameAs(payments.get(1));
        assertThat(payments.get(0).getPaymentAmount()).isEqualByComparingTo("10.00");
        assertThat(payments.get(1).getPaymentAmount()).isEqualByComparingTo("2.50");
        assertThat(objectMapper.readTree(payments.get(0).getAdditionalInformation())
            .path("court_fee_overpayment").asBoolean()).isFalse();
        assertThat(objectMapper.readTree(payments.get(1).getAdditionalInformation())
            .path("court_fee_overpayment").asBoolean()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"7.50", "10.00"})
    void keepsOneRecordWithoutExcess(String amount) {
        givenFee("5.00");
        PaymentInEntity payment = payment(amount);
        assertThat(service.split(payment, information, (short) 10)).containsExactly(payment);
        assertThat(payment.getPaymentAmount()).isEqualByComparingTo(amount);
        assertThat(payment.getAdditionalInformation()).doesNotContain("court_fee_overpayment");
    }

    @Test
    void ignoresOtherDestinations() {
        PaymentInEntity payment = payment("12.50");
        payment.setDestinationType(DestinationType.F);
        assertThat(service.split(payment, information, (short) 10)).containsExactly(payment);
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsUnknownFeeInBusinessUnit() {
        assertThatThrownBy(() -> service.split(payment("12.50"), information, (short) 10))
            .isInstanceOf(ResponseStatusException.class).hasMessageContaining("court_fee_code", "business_unit_id 10");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.00", "-5.00"})
    void rejectsInvalidFeeAmount(String amount) {
        givenFee(amount);
        assertThatThrownBy(() -> service.split(payment("12.50"), information, (short) 10))
            .isInstanceOf(ResponseStatusException.class).hasMessageContaining("greater than zero");
    }

    private void givenFee(String amount) {
        when(repository.findByBusinessUnit_BusinessUnitIdAndCourtFeeCode((short) 10, "FEE"))
            .thenReturn(Optional.of(CourtFeeEntity.builder().amount(new BigDecimal(amount)).build()));
    }

    private static PaymentInEntity payment(String amount) {
        return PaymentInEntity.builder().paymentAmount(new BigDecimal(amount)).paymentMethod(PaymentMethod.CT)
            .destinationType(DestinationType.C).allocationType("FULL")
            .associatedRecordType(AssociatedRecordType.MISCELLANEOUS_ACCOUNTS).associatedRecordId("123")
            .additionalInformation("""
                {"payment_received_from":"A","payment_reference":"reference","transfer_source":"NATWEST",
                 "court_fee_code":"FEE","number_of_items":2,"suspense_note":"note"}
                """).build();
    }
}
