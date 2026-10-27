package uk.gov.hmcts.opal.mapper.till;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.opal.entity.AssociatedRecordType.DEFENDANT_ACCOUNTS;
import static uk.gov.hmcts.opal.entity.DestinationType.F;
import static uk.gov.hmcts.opal.entity.DestinationType.S;
import static uk.gov.hmcts.opal.mapper.till.GetTillMapperTestData.DEFENDANT_ACCOUNT_ID;
import static uk.gov.hmcts.opal.mapper.till.GetTillMapperTestData.defendantAccount;
import static uk.gov.hmcts.opal.mapper.till.GetTillMapperTestData.individualParty;
import static uk.gov.hmcts.opal.mapper.till.GetTillMapperTestData.organisationParty;
import static uk.gov.hmcts.opal.mapper.till.GetTillMapperTestData.payment;

import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.generated.model.TillsDefendantDetail;

@SpringJUnitConfig(GetTillMapperTestConfiguration.class)
class GetTillDefendantMapperTest {

    @Autowired
    private GetTillDefendantMapper mapper;

    @Nested
    class ResolveDefendantDetail {

        @Test
        void whenDefendantIsAnIndividual_mapsIndividualNames_happyPath() {
            PaymentInEntity payment = finePayment();
            DefendantAccountEntity account = defendantAccount(individualParty());

            TillsDefendantDetail mapped = mapper.resolveDefendantDetail(
                payment, Map.of(DEFENDANT_ACCOUNT_ID, account)).get();

            assertAll(
                () -> assertEquals("ACC123", mapped.getDefendantAccountNumber()),
                () -> assertFalse(mapped.getPartyDetails().getOrganisationFlag()),
                () -> assertEquals("Jane", mapped.getPartyDetails().getIndividualNames().get().getForenames()),
                () -> assertNull(mapped.getPartyDetails().getOrganisationNames().orElse(null))
            );
        }

        @Test
        void whenDefendantIsAnOrganisation_mapsOrganisationName_happyPath() {
            PaymentInEntity payment = finePayment();
            DefendantAccountEntity account = defendantAccount(organisationParty());

            TillsDefendantDetail mapped = mapper.resolveDefendantDetail(
                payment, Map.of(DEFENDANT_ACCOUNT_ID, account)).get();

            assertAll(
                () -> assertTrue(mapped.getPartyDetails().getOrganisationFlag()),
                () -> assertEquals("Defendant Limited",
                    mapped.getPartyDetails().getOrganisationNames().get().getOrganisationName()),
                () -> assertNull(mapped.getPartyDetails().getIndividualNames().orElse(null))
            );
        }

        @Test
        void whenPaymentIsNotForAFine_returnsEmptyDetail_happyPath() {
            assertNull(mapper.resolveDefendantDetail(payment(1L, S, "D"), Map.of()).orElse(null));
        }

        @Test
        void whenDefendantAccountIsMissing_returnsNotFound_sadPath() {
            assertThatThrownBy(() -> mapper.resolveDefendantDetail(finePayment(), Map.of()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Defendant account not found for associated_record_id: " + DEFENDANT_ACCOUNT_ID);
        }
    }

    private PaymentInEntity finePayment() {
        PaymentInEntity payment = payment(1L, F, "D");
        payment.setAssociatedRecordType(DEFENDANT_ACCOUNTS);
        payment.setAssociatedRecordId(String.valueOf(DEFENDANT_ACCOUNT_ID));
        return payment;
    }
}
