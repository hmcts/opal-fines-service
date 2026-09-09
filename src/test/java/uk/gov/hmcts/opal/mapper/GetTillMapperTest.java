package uk.gov.hmcts.opal.mapper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.opal.entity.AssociatedRecordType.DEFENDANT_ACCOUNTS;
import static uk.gov.hmcts.opal.entity.AssociatedRecordType.SUSPENSE_ITEMS;
import static uk.gov.hmcts.opal.entity.DestinationType.C;
import static uk.gov.hmcts.opal.entity.DestinationType.F;
import static uk.gov.hmcts.opal.entity.DestinationType.S;
import static uk.gov.hmcts.opal.entity.PaymentMethod.CT;
import static uk.gov.hmcts.opal.entity.PaymentMethod.NC;
import static uk.gov.hmcts.opal.entity.defendantaccount.AssociationType.DEFENDANT;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.opal.entity.DestinationType;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.generated.model.TillsGetResponse;
import uk.gov.hmcts.opal.generated.model.TillsPaymentIn;

class GetTillMapperTest {

    private static final Long DEFENDANT_ACCOUNT_ID = 456L;
    private static final LocalDateTime CREATED_DATE = LocalDateTime.of(2026, 9, 9, 10, 30);

    private GetTillMapper mapper;
    private TillEntity till;

    @BeforeEach
    void setUp() {
        mapper = new GetTillMapper(JsonMapper.builder().findAndAddModules().build());
        till = TillEntity.builder()
            .tillId(123L)
            .tillNumber((short) 42)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 77).build())
            .ownedByName("Alex Cashier")
            .createdDate(CREATED_DATE)
            .build();
    }

    @Nested
    class ToResponse {

        @Test
        void whenFineAndSuspensePayments_mapsNestedDetails_happyPath() {
            PaymentInEntity finePayment = payment(1L, F, "D");
            finePayment.setAssociatedRecordType(DEFENDANT_ACCOUNTS);
            finePayment.setAssociatedRecordId(String.valueOf(DEFENDANT_ACCOUNT_ID));
            finePayment.setPaymentMethod(NC);
            finePayment.setThirdPartyPayerName("Third Party");
            finePayment.setReceipt(true);

            PaymentInEntity suspensePayment = payment(2L, S, """
                {"payment_received_from":"T","payer_details":{"individual":{
                  "forenames":"Sam","surname":"Smith"}}}
                """);
            suspensePayment.setAssociatedRecordType(SUSPENSE_ITEMS);
            suspensePayment.setAssociatedRecordId("789");
            suspensePayment.setPaymentMethod(CT);
            DefendantAccountEntity account = defendantAccount(individualParty());

            TillsGetResponse response = mapper.toResponse(
                till, List.of(finePayment, suspensePayment), Map.of(DEFENDANT_ACCOUNT_ID, account));

            TillsPaymentIn fine = response.getPaymentsIn().getFirst().getPaymentIn();
            TillsPaymentIn suspense = response.getPaymentsIn().getLast().getPaymentIn();
            assertAll(
                () -> assertEquals((short) 42, response.getTillNumber()),
                () -> assertEquals((short) 77, response.getBusinessUnitId()),
                () -> assertEquals("Alex Cashier", response.getCreatedBy()),
                () -> assertEquals(CREATED_DATE, response.getCreatedDate()),
                () -> assertEquals(2, response.getPaymentsIn().size()),
                () -> assertEquals(TillsPaymentIn.PaymentReceivedFromEnum.D, fine.getPaymentReceivedFrom()),
                () -> assertEquals("ACC123", fine.getDefendantDetail().get().getDefendantAccountNumber()),
                () -> assertFalse(fine.getDefendantDetail().get().getPartyDetails().getOrganisationFlag()),
                () -> assertEquals("Jane", fine.getDefendantDetail().get().getPartyDetails()
                    .getIndividualNames().get().getForenames()),
                () -> assertNull(fine.getPayerName().orElse(null)),
                () -> assertEquals(TillsPaymentIn.PaymentReceivedFromEnum.T, suspense.getPaymentReceivedFrom()),
                () -> assertEquals("Sam", suspense.getPayerName().get().getForenames().get()),
                () -> assertEquals("Smith", suspense.getPayerName().get().getSurname().get()),
                () -> assertNull(suspense.getDefendantDetail().orElse(null))
            );
        }

        @Test
        void whenOrganisationAndCourtFee_mapsOrganisationNames_happyPath() {
            PaymentInEntity finePayment = payment(1L, F,
                "{\"additional_information\":{\"payment_received_from\":\"A\"}}");
            finePayment.setAssociatedRecordType(DEFENDANT_ACCOUNTS);
            finePayment.setAssociatedRecordId(String.valueOf(DEFENDANT_ACCOUNT_ID));
            PaymentInEntity courtFeePayment = payment(2L, C, """
                {"payment_received_from":"T","payer_details":{"organisation":{
                  "organisation_name":"Payer Limited"}}}
                """);
            DefendantAccountEntity account = defendantAccount(organisationParty());

            TillsGetResponse response = mapper.toResponse(
                till, List.of(finePayment, courtFeePayment), Map.of(DEFENDANT_ACCOUNT_ID, account));

            TillsPaymentIn fine = response.getPaymentsIn().getFirst().getPaymentIn();
            TillsPaymentIn courtFee = response.getPaymentsIn().getLast().getPaymentIn();
            assertAll(
                () -> assertTrue(fine.getDefendantDetail().get().getPartyDetails().getOrganisationFlag()),
                () -> assertEquals("Defendant Limited", fine.getDefendantDetail().get().getPartyDetails()
                    .getOrganisationNames().get().getOrganisationName()),
                () -> assertNull(fine.getDefendantDetail().get().getPartyDetails()
                    .getIndividualNames().orElse(null)),
                () -> assertEquals("Payer Limited", courtFee.getPayerName().get()
                    .getOrganisationName().get())
            );
        }

        @Test
        void whenAdditionalInformationIsInvalid_rejectsPersistedData_sadPath() {
            PaymentInEntity payment = payment(1L, S, "not-json");

            assertThatThrownBy(() -> mapper.toResponse(till, List.of(payment), Map.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Payment 1 has invalid additional_information");
        }

        @Test
        void whenDefendantAccountIsMissing_returnsNotFound_sadPath() {
            PaymentInEntity payment = payment(1L, F, "D");
            payment.setAssociatedRecordType(DEFENDANT_ACCOUNTS);
            payment.setAssociatedRecordId(String.valueOf(DEFENDANT_ACCOUNT_ID));

            assertThatThrownBy(() -> mapper.toResponse(till, List.of(payment), Map.of()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Defendant account not found for associated_record_id: " + DEFENDANT_ACCOUNT_ID);
        }
    }

    private PaymentInEntity payment(Long id, DestinationType destinationType,
                                    String additionalInformation) {

        return PaymentInEntity.builder()
            .paymentInId(id)
            .paymentAmount(new BigDecimal("12.34"))
            .paymentDate(LocalDateTime.of(2026, 9, 9, 11, 15))
            .destinationType(destinationType)
            .allocationType("FULL")
            .additionalInformation(additionalInformation)
            .build();
    }

    private DefendantAccountEntity defendantAccount(PartyEntity party) {
        return DefendantAccountEntity.builder()
            .defendantAccountId(DEFENDANT_ACCOUNT_ID)
            .accountNumber("ACC123")
            .parties(List.of(DefendantAccountPartiesEntity.builder()
                .associationType(DEFENDANT)
                .party(party)
                .build()))
            .build();
    }

    private PartyEntity individualParty() {
        return PartyEntity.builder()
            .organisation(false)
            .forenames("Jane")
            .surname("Doe")
            .build();
    }

    private PartyEntity organisationParty() {
        return PartyEntity.builder()
            .organisation(true)
            .organisationName("Defendant Limited")
            .build();
    }
}
