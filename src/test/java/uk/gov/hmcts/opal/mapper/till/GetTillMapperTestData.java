package uk.gov.hmcts.opal.mapper.till;

import static uk.gov.hmcts.opal.entity.defendantaccount.AssociationType.DEFENDANT;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.opal.entity.DestinationType;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;

final class GetTillMapperTestData {

    static final Long DEFENDANT_ACCOUNT_ID = 456L;
    static final LocalDateTime CREATED_DATE = LocalDateTime.of(2026, 9, 9, 10, 30);

    private GetTillMapperTestData() {
    }

    static TillEntity till() {
        return TillEntity.builder()
            .tillId(123L)
            .tillNumber((short) 42)
            .businessUnit(BusinessUnitEntity.builder().businessUnitId((short) 77).build())
            .ownedByName("Alex Cashier")
            .createdDate(CREATED_DATE)
            .build();
    }

    static PaymentInEntity payment(Long id, DestinationType destinationType, String additionalInformation) {
        return PaymentInEntity.builder()
            .paymentInId(id)
            .paymentAmount(new BigDecimal("12.34"))
            .paymentDate(LocalDateTime.of(2026, 9, 9, 11, 15))
            .destinationType(destinationType)
            .allocationType("FULL")
            .additionalInformation(additionalInformation)
            .build();
    }

    static DefendantAccountEntity defendantAccount(PartyEntity party) {
        return DefendantAccountEntity.builder()
            .defendantAccountId(DEFENDANT_ACCOUNT_ID)
            .accountNumber("ACC123")
            .parties(List.of(DefendantAccountPartiesEntity.builder()
                .associationType(DEFENDANT)
                .party(party)
                .build()))
            .build();
    }

    static PartyEntity individualParty() {
        return PartyEntity.builder()
            .organisation(false)
            .forenames("Jane")
            .surname("Doe")
            .build();
    }

    static PartyEntity organisationParty() {
        return PartyEntity.builder()
            .organisation(true)
            .organisationName("Defendant Limited")
            .build();
    }
}
