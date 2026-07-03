package uk.gov.hmcts.opal.service.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.DestinationType;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.SuspenseAccountEntity;
import uk.gov.hmcts.opal.entity.SuspenseItemEntity;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.AssociationType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountStatus;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountType;
import uk.gov.hmcts.opal.entity.PaymentMethod;

@ExtendWith(MockitoExtension.class)
class CashListReportAssemblerTest {

    private static final Long TILL_ID = 99000000010000L;
    private static final Long DEFENDANT_ACCOUNT_ID = 99000000020000L;
    private static final Long SUSPENSE_ITEM_ID = 99000000030000L;

    @Mock
    private CashListPaymentLinkService cashListPaymentLinkService;

    private CashListReportAssembler assembler;
    private BusinessUnitEntity businessUnit;
    private TillEntity till;

    @BeforeEach
    void setUp() {
        assembler = new CashListReportAssembler(cashListPaymentLinkService);
        businessUnit = BusinessUnitEntity.builder()
            .businessUnitId((short) 77)
            .businessUnitName("London Collection Unit")
            .businessUnitCode("LOND")
            .build();
        till = TillEntity.builder()
            .tillId(TILL_ID)
            .tillNumber((short) 9001)
            .ownedBy("L080JG")
            .businessUnit(businessUnit)
            .build();
    }

    @Test
    void toReportData_buildsTillPaymentAccountDefendantAndSuspenseDetails() {
        // Arrange
        PaymentInEntity accountPayment = payment(
            1L, DestinationType.F, AssociatedRecordType.DEFENDANT_ACCOUNTS,
            String.valueOf(DEFENDANT_ACCOUNT_ID),
            "Account payment");
        PaymentInEntity suspensePayment = payment(
            2L, DestinationType.S, AssociatedRecordType.SUSPENSE_ITEMS,
            String.valueOf(SUSPENSE_ITEM_ID), "Suspense payment");

        // Act
        when(cashListPaymentLinkService.getDefendantAccount(accountPayment)).thenReturn(defendantAccount());
        when(cashListPaymentLinkService.getSuspenseItem(suspensePayment)).thenReturn(suspenseItem());

        CashListReportData data = assembler.toReportData(till, businessUnit, List.of(accountPayment, suspensePayment));

        // Assert
        assertThat(data.getNumberOfRecords()).isEqualTo((short) 2);
        assertThat(data.getTillDetails().getTillId()).isEqualTo(TILL_ID);
        assertThat(data.getTillDetails().getBusinessUnitName()).isEqualTo("London Collection Unit");
        assertThat(data.getTotal()).isEqualByComparingTo("251.00");
        assertThat(data.getEntries()).hasSize(2);

        CashListReportData.CashListEntry firstEntry = data.getEntries().getFirst();
        assertThat(firstEntry.getEntry()).isEqualTo(1);
        assertThat(firstEntry.getType()).isEqualTo("FA");
        assertThat(firstEntry.getSuspense()).isNull();
        assertThat(firstEntry.getAccountNumber()).isEqualTo("ACC123");
        assertThat(firstEntry.getName()).isEqualTo("SMITH John");
        assertThat(firstEntry.getNameAdditionalInformation()).isNull();
        assertThat(firstEntry.getPaymentMethod()).isEqualTo("NC");
        assertThat(firstEntry.getAmount()).isEqualByComparingTo("125.50");

        CashListReportData.CashListEntry secondEntry = data.getEntries().getLast();
        assertThat(secondEntry.getEntry()).isEqualTo(2);
        assertThat(secondEntry.getType()).isEqualTo("SA");
        assertThat(secondEntry.getSuspense()).isEqualTo("PA");
        assertThat(secondEntry.getAccountNumber()).isEqualTo("Suspense Ref");
        assertThat(secondEntry.getName()).isEqualTo("1");
        assertThat(secondEntry.getNameAdditionalInformation()).isEqualTo("Manual - Suspense payment");
        assertThat(secondEntry.getPaymentMethod()).isEqualTo("NC");
        assertThat(secondEntry.getAmount()).isEqualByComparingTo("125.50");
    }

    @Test
    void toReportData_throwsWhenDefendantAccountDoesNotExist() {
        PaymentInEntity payment = payment(
            1L, DestinationType.F, AssociatedRecordType.DEFENDANT_ACCOUNTS,
            String.valueOf(DEFENDANT_ACCOUNT_ID),
            "Account payment");
        when(cashListPaymentLinkService.getDefendantAccount(payment))
            .thenThrow(new EntityNotFoundException(
                "Defendant account not found for associated_record_id: " + DEFENDANT_ACCOUNT_ID));

        assertThatThrownBy(() -> assembler.toReportData(till, businessUnit, List.of(payment)))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Defendant account not found");
    }

    @Test
    void toReportData_throwsWhenSuspenseItemDoesNotExist() {
        PaymentInEntity payment = payment(
            1L, DestinationType.S, AssociatedRecordType.SUSPENSE_ITEMS,
            String.valueOf(SUSPENSE_ITEM_ID), "Suspense payment");
        when(cashListPaymentLinkService.getSuspenseItem(payment))
            .thenThrow(new EntityNotFoundException(
                "Suspense item not found for associated_record_id: " + SUSPENSE_ITEM_ID));

        assertThatThrownBy(() -> assembler.toReportData(till, businessUnit, List.of(payment)))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Suspense item not found");
    }

    @Test
    void toReportData_usesDebtorPartyWhenDefendantAssociationIsMissing() {
        PaymentInEntity payment = payment(
            1L, DestinationType.F, AssociatedRecordType.DEFENDANT_ACCOUNTS,
            String.valueOf(DEFENDANT_ACCOUNT_ID),
            "Account payment");
        DefendantAccountEntity defendantAccount = defendantAccountWithParties(
            List.of(
                accountParty(party(1L, "First"), null, false),
                accountParty(party(2L, "Debtor"), null, true)
            )
        );
        when(cashListPaymentLinkService.getDefendantAccount(payment)).thenReturn(defendantAccount);

        CashListReportData data = assembler.toReportData(till, businessUnit, List.of(payment));

        assertThat(data.getEntries().getFirst().getName()).isEqualTo("SMITH Debtor");
    }

    @Test
    void toReportData_usesFirstPartyWhenDefendantAssociationAndDebtorAreMissing() {
        PaymentInEntity payment = payment(
            1L, DestinationType.F, AssociatedRecordType.DEFENDANT_ACCOUNTS,
            String.valueOf(DEFENDANT_ACCOUNT_ID),
            "Account payment");
        DefendantAccountEntity defendantAccount = defendantAccountWithParties(
            List.of(
                accountParty(party(1L, "First"), null, false),
                accountParty(party(2L, "Second"), null, false)
            )
        );
        when(cashListPaymentLinkService.getDefendantAccount(payment)).thenReturn(defendantAccount);

        CashListReportData data = assembler.toReportData(till, businessUnit, List.of(payment));

        assertThat(data.getEntries().getFirst().getName()).isEqualTo("SMITH First");
    }

    @Test
    void toReportData_throwsWhenDestinationTypeIsUnsupported() {
        PaymentInEntity payment = payment(
            1L, DestinationType.C, AssociatedRecordType.DEFENDANT_ACCOUNTS,
            String.valueOf(DEFENDANT_ACCOUNT_ID),
            "Court fee payment");

        assertThatThrownBy(() -> assembler.toReportData(till, businessUnit, List.of(payment)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Payment 1 has unsupported destination_type: C");
    }

    @Test
    void toReportData_throwsWhenDefendantPartyDoesNotExist() {
        PaymentInEntity payment = payment(
            1L, DestinationType.F, AssociatedRecordType.DEFENDANT_ACCOUNTS,
            String.valueOf(DEFENDANT_ACCOUNT_ID),
            "Account payment");
        DefendantAccountEntity defendantAccount = defendantAccountWithParties(List.of());
        when(cashListPaymentLinkService.getDefendantAccount(payment)).thenReturn(defendantAccount);

        assertThatThrownBy(() -> assembler.toReportData(till, businessUnit, List.of(payment)))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Defendant party not found for defendant_account_id: " + DEFENDANT_ACCOUNT_ID);
    }

    @Test
    void toReportData_setsNullPaymentMethodWhenPaymentMethodIsMissing() {
        PaymentInEntity payment = payment(
            1L, DestinationType.F, AssociatedRecordType.DEFENDANT_ACCOUNTS,
            String.valueOf(DEFENDANT_ACCOUNT_ID),
            "Account payment");
        payment.setPaymentMethod(null);
        when(cashListPaymentLinkService.getDefendantAccount(payment)).thenReturn(defendantAccount());

        CashListReportData data = assembler.toReportData(till, businessUnit, List.of(payment));

        assertThat(data.getEntries()).singleElement().satisfies(entry ->
            assertThat(entry.getPaymentMethod()).isNull());
    }

    private PaymentInEntity payment(
        Long paymentInId, DestinationType destinationType, AssociatedRecordType associatedRecordType,
        String associatedRecordId,
        String additionalInformation) {
        return PaymentInEntity.builder()
            .paymentInId(paymentInId)
            .tillEntity(till)
            .paymentAmount(new BigDecimal("125.50"))
            .paymentDate(LocalDateTime.of(2026, 5, 26, 14, 30))
            .paymentMethod(PaymentMethod.NC)
            .destinationType(destinationType)
            .allocationType("FULL")
            .associatedRecordType(associatedRecordType)
            .associatedRecordId(associatedRecordId)
            .thirdPartyPayerName("A Payer")
            .additionalInformation(additionalInformation)
            .receipt(true)
            .allocated(true)
            .build();
    }

    private DefendantAccountEntity defendantAccount() {
        PartyEntity party = PartyEntity.builder()
            .partyId(99000000040000L)
            .title("Mr")
            .forenames("John")
            .surname("Smith")
            .build();
        DefendantAccountEntity defendantAccount = defendantAccountWithParties(
            List.of(accountParty(party, AssociationType.DEFENDANT, true)));
        return defendantAccount;
    }

    private DefendantAccountEntity defendantAccountWithParties(List<DefendantAccountPartiesEntity> parties) {
        DefendantAccountEntity defendantAccount = DefendantAccountEntity.builder()
            .defendantAccountId(DEFENDANT_ACCOUNT_ID)
            .businessUnit(businessUnit)
            .accountNumber("ACC123")
            .amountImposed(new BigDecimal("250.00"))
            .amountPaid(new BigDecimal("125.50"))
            .accountBalance(new BigDecimal("124.50"))
            .accountStatus(DefendantAccountStatus.LIVE)
            .accountType(DefendantAccountType.FINES)
            .build();
        parties.forEach(party -> party.setDefendantAccount(defendantAccount));
        defendantAccount.setParties(parties);
        return defendantAccount;
    }

    private static DefendantAccountPartiesEntity accountParty(
        PartyEntity party, AssociationType associationType, Boolean debtor) {
        return DefendantAccountPartiesEntity.builder()
            .party(party)
            .associationType(associationType)
            .debtor(debtor)
            .build();
    }

    private static PartyEntity party(Long partyId, String forenames) {
        return PartyEntity.builder()
            .partyId(partyId)
            .forenames(forenames)
            .surname("Smith")
            .build();
    }

    private SuspenseItemEntity suspenseItem() {
        SuspenseAccountEntity suspenseAccount = SuspenseAccountEntity.builder()
            .suspenseAccountId(99000000031000L)
            .businessUnit(businessUnit)
            .accountNumber("SUSP123")
            .build();
        return SuspenseItemEntity.builder()
            .suspenseItemId(SUSPENSE_ITEM_ID)
            .suspenseAccount(suspenseAccount)
            .suspenseItemNumber((short) 1)
            .suspenseItemType("PA")
            .createdDate(LocalDateTime.of(2026, 5, 26, 15, 0))
            .paymentMethod("CA")
            .courtFeeId(99000000032000L)
            .build();
    }
}
