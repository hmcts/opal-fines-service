package uk.gov.hmcts.opal.mapper.till;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.opal.entity.AssociatedRecordType.SUSPENSE_ITEMS;
import static uk.gov.hmcts.opal.entity.DestinationType.C;
import static uk.gov.hmcts.opal.entity.DestinationType.S;
import static uk.gov.hmcts.opal.entity.PaymentMethod.CT;
import static uk.gov.hmcts.opal.mapper.till.GetTillMapperTestData.payment;

import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.generated.model.TillsPaymentIn;

@SpringJUnitConfig(GetTillMapperTestConfiguration.class)
class GetTillPaymentMapperTest {

    @Autowired
    private GetTillPaymentMapper mapper;

    @Nested
    class ToPaymentItem {

        @Test
        void whenIndividualPayerIsProvided_mapsPaymentAndPayer_happyPath() {
            PaymentInEntity payment = payment(2L, S, """
                {"payment_received_from":"T","payer_details":{"individual":{
                  "forenames":"Sam","surname":"Smith"}}}
                """);
            payment.setAssociatedRecordType(SUSPENSE_ITEMS);
            payment.setAssociatedRecordId("789");
            payment.setPaymentMethod(CT);
            payment.setThirdPartyPayerName("Third Party");
            payment.setReceipt(true);

            TillsPaymentIn mapped = mapper.toPaymentItem(payment, Map.of()).getPaymentIn();

            assertAll(
                () -> assertEquals(2L, mapped.getPaymentInId()),
                () -> assertEquals("789", mapped.getAssociatedRecordId().get()),
                () -> assertEquals(TillsPaymentIn.AssociatedRecordTypeEnum.SUSPENSE_ITEMS,
                    mapped.getAssociatedRecordType().get()),
                () -> assertEquals(TillsPaymentIn.PaymentReceivedFromEnum.T, mapped.getPaymentReceivedFrom()),
                () -> assertEquals("Sam", mapped.getPayerName().get().getForenames().get()),
                () -> assertEquals("Smith", mapped.getPayerName().get().getSurname().get()),
                () -> assertEquals("Third Party", mapped.getThirdPartyPayerName().get()),
                () -> assertEquals(CT.name(), mapped.getMethod().getValue()),
                () -> assertEquals(true, mapped.getReceipt()),
                () -> assertNull(mapped.getDefendantDetail().orElse(null))
            );
        }

        @Test
        void whenOrganisationPayerIsProvided_mapsOrganisationName_happyPath() {
            PaymentInEntity payment = payment(2L, C, """
                {"payment_received_from":"T","payer_details":{"organisation":{
                  "organisation_name":"Payer Limited"}}}
                """);

            TillsPaymentIn mapped = mapper.toPaymentItem(payment, Map.of()).getPaymentIn();

            assertEquals("Payer Limited", mapped.getPayerName().get().getOrganisationName().get());
        }

        @Test
        void whenAdditionalInformationIsInvalid_rejectsPersistedData_sadPath() {
            PaymentInEntity payment = payment(1L, S, "not-json");

            assertThatThrownBy(() -> mapper.toPaymentItem(payment, Map.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Payment 1 has invalid additional_information");
        }
    }
}
