package uk.gov.hmcts.opal.mapper.till;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.opal.entity.AssociatedRecordType.MISCELLANEOUS_ACCOUNTS;
import static uk.gov.hmcts.opal.entity.AssociatedRecordType.SUSPENSE_ITEMS;
import static uk.gov.hmcts.opal.entity.DestinationType.C;
import static uk.gov.hmcts.opal.entity.DestinationType.S;
import static uk.gov.hmcts.opal.entity.PaymentMethod.CT;
import static uk.gov.hmcts.opal.mapper.till.GetTillMapperTestData.payment;

import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.entity.DestinationType;
import uk.gov.hmcts.opal.entity.MiscellaneousAccountEntity;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.generated.model.TillsPaymentIn;

@SpringJUnitConfig(GetTillMapperTestConfiguration.class)
class GetTillPaymentMapperTest {

    @Autowired
    private GetTillPaymentMapper mapper;

    @Nested
    class ToPayment {

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

            TillsPaymentIn mapped = mapper.toPayment(payment, Map.of(), Map.of());

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

            TillsPaymentIn mapped = mapper.toPayment(payment, Map.of(), Map.of());

            assertEquals("Payer Limited", mapped.getPayerName().get().getOrganisationName().get());
        }

        @ParameterizedTest
        @EnumSource(value = DestinationType.class, names = {"C", "S"})
        void resolvesIndividualWithoutJsonPayer(DestinationType destination) {
            PaymentInEntity payment = durablePayment(destination, "{\"payment_received_from\":\"T\"}");
            payment.setThirdPartyPayerName("Third Party");
            PartyEntity party = PartyEntity.builder().forenames("Sam").surname("Smith")
                .organisationName("Unused organisation").build();

            TillsPaymentIn mapped = mapper.toPayment(payment, Map.of(), accounts(party));

            assertAll(
                () -> assertEquals("Sam", mapped.getPayerName().get().getForenames().get()),
                () -> assertEquals("Smith", mapped.getPayerName().get().getSurname().get()),
                () -> assertNull(mapped.getPayerName().get().getOrganisationName().orElse(null)),
                () -> assertEquals("Third Party", mapped.getThirdPartyPayerName().get())
            );
        }

        @Test
        void courtFeePairUsesOrganisationInsteadOfStaleJson() {
            String json = """
                {"payment_received_from":"T","payer_details":{"organisation":{"organisation_name":"Stale"}}}
                """;
            PartyEntity party = PartyEntity.builder().organisation(true).organisationName("Durable Ltd")
                .forenames("Unused").surname("Unused").build();
            TillsPaymentIn exact = mapper.toPayment(durablePayment(C, json), Map.of(), accounts(party));
            TillsPaymentIn overpayment = mapper.toPayment(durablePayment(S, json), Map.of(), accounts(party));

            assertAll(
                () -> assertEquals("Durable Ltd", exact.getPayerName().get().getOrganisationName().get()),
                () -> assertEquals(exact.getPayerName(), overpayment.getPayerName()),
                () -> assertNull(exact.getPayerName().get().getForenames().orElse(null)),
                () -> assertNull(exact.getPayerName().get().getSurname().orElse(null))
            );
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        void brokenDurableAssociationNeverUsesJson(boolean missingParty) {
            PaymentInEntity payment = durablePayment(C, """
                {"payment_received_from":"T","payer_details":{"individual":{"surname":"Stale"}}}
                """);
            Map<Long, MiscellaneousAccountEntity> accounts = missingParty ? accounts(null) : Map.of();

            assertThatThrownBy(() -> mapper.toPayment(payment, Map.of(), accounts))
                .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void legacyWithoutPayerDetailsReturnsNull() {
            assertNull(mapper.toPayment(payment(2L, S, "T"), Map.of(), Map.of()).getPayerName().orElse(null));
        }

        private PaymentInEntity durablePayment(DestinationType destination, String json) {
            PaymentInEntity payment = payment(2L, destination, json);
            payment.setAssociatedRecordType(MISCELLANEOUS_ACCOUNTS);
            payment.setAssociatedRecordId("789");
            return payment;
        }

        private Map<Long, MiscellaneousAccountEntity> accounts(PartyEntity party) {
            return Map.of(789L, MiscellaneousAccountEntity.builder().miscellaneousAccountId(789L).party(party).build());
        }

        @Test
        void whenAdditionalInformationIsInvalid_rejectsPersistedData_sadPath() {
            PaymentInEntity payment = payment(1L, S, "not-json");

            assertThatThrownBy(() -> mapper.toPayment(payment, Map.of(), Map.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Payment 1 has invalid additional_information");
        }
    }
}
