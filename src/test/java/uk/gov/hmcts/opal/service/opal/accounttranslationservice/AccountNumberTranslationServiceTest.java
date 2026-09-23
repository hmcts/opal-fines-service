package uk.gov.hmcts.opal.service.opal.accounttranslationservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.alternatepaymentreference.AlternatePaymentReferenceEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitType;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.repository.AlternatePaymentReferenceRepository;
import uk.gov.hmcts.opal.repository.DefendantAccountRepository;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.AccountNumberTranslationService;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.BankDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.DestinationDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.InterfaceFileCommonDataExtract;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.OriginatorDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.PaymentType;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.Transaction;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.TransformedInterfaceFileData;

@ExtendWith(MockitoExtension.class)
public class AccountNumberTranslationServiceTest {

    private static final String VALID_ACCOUNT_REFERENCE = "12345678A";
    private static final String VALID_ACCOUNT_REFERENCE_2 = "09876543B";
    private static final String INVALID_ACCOUNT_REFERENCE = "A!G$HJ@£B&";

    @Mock
    private DefendantAccountRepository defendantAccountRepository;
    @Mock
    private AlternatePaymentReferenceRepository aprRepository;
    @InjectMocks
    private AccountNumberTranslationService service;

    @Mock
    private DefendantAccountEntity defendantAccount;
    @Mock
    private AlternatePaymentReferenceEntity alternatePaymentReference;
    @Mock
    private BusinessUnitEntity mockBusinessUnit;



    private InterfaceFileEntity withInterfaceFileEntity() {
        return InterfaceFileEntity.builder()
            .interfaceFileId(1L)
            .overrideInhibits(true)
            .fileName("file-name.dat")
            .source("OPAL")
            .build();
    }

    private BusinessUnitEntity withBusinessUnit() {
        return BusinessUnitEntity.builder()
            .businessUnitId((short)1005)
            .businessUnitName("Greater Manchester")
            .businessUnitCode("06")
            .businessUnitType(BusinessUnitType.AREA)
            .build();
    }

    private InterfaceFileCommonDataExtract withInterfaceFileCommonDataExtract(List<Transaction> transactions) {
        return InterfaceFileCommonDataExtract.builder()
            .fileName("file-name.dat")
            .destinationDetails(DestinationDetails.builder()
                .bankDetails(BankDetails.builder()
                    .accountNumber("12341234")
                    .sortCode("01-02-03")
                    .type("Personal")
                    .build())
                .build()
            )
            .transactions(transactions)
            .paymentType(PaymentType.CASH)
            .dwpCourtCode("dwp1234567")
            .build();
    }

    private Transaction withTransaction(String code, String originatorName, String originatorRef, Long amount) {
        return Transaction.builder()
            .transactionCode(code)
            .originatorDetails(OriginatorDetails.builder()
                .name(originatorName)
                .accountReference(originatorRef)
                .build())
            .amount(amount)
            .build();
    }

    @Test
    @DisplayName("A CHEQUE file is received and processed correctly")
    void allTransactionsAreCheques() {
        var businessUnit = withBusinessUnit();
        var interfaceFileEntity = withInterfaceFileEntity();
        var interfaceFileCommonDataExtract = withInterfaceFileCommonDataExtract(List.of(
            withTransaction("11", "", "", 500L),
            withTransaction("11", "", "", 500L)
        ));

        TransformedInterfaceFileData transformedInterfaceFileData = service.process(
            interfaceFileEntity,
            interfaceFileCommonDataExtract,
            businessUnit);

        verify(defendantAccountRepository, never()).findByAccountNumberAndBusinessUnit_BusinessUnitId(any(), any());
        verify(aprRepository, never()).findByAprTextAndBusinessUnitCode(any(), any());

        assertThat(transformedInterfaceFileData.getTransactions()).hasSize(2);
        assertThat(transformedInterfaceFileData.getTotalAmount()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("A CHEQUE file is received but contains invalid transactions")
    void containsChequesAndInvalidTransactions() {
        var businessUnit = withBusinessUnit();
        var interfaceFileEntity = withInterfaceFileEntity();
        var interfaceFileCommonDataExtract = withInterfaceFileCommonDataExtract(List.of(
            withTransaction("11", "", VALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("11", "", VALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("44", "", VALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("52", "", VALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("1543", "", VALID_ACCOUNT_REFERENCE, 500L)
        ));

        TransformedInterfaceFileData transformedInterfaceFileData = service.process(
            interfaceFileEntity,
            interfaceFileCommonDataExtract,
            businessUnit);

        verify(defendantAccountRepository, never()).findByAccountNumberAndBusinessUnit_BusinessUnitId(any(), any());
        verify(aprRepository, never()).findByAprTextAndBusinessUnitCode(any(), any());

        assertThat(transformedInterfaceFileData.getTransactions()).hasSize(2);
        assertThat(transformedInterfaceFileData.getTotalAmount()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("A CASH file is received and are all valid transaction codes")
    void allTransactionsAreCash() {
        var businessUnit = withBusinessUnit();
        var interfaceFileEntity = withInterfaceFileEntity();
        var interfaceFileCommonDataExtract = withInterfaceFileCommonDataExtract(List.of(
            withTransaction("00", "", VALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("15", "", VALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("68", "", VALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("93", "", VALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("99", "", VALID_ACCOUNT_REFERENCE, 500L)
        ));

        when(defendantAccountRepository.findByAccountNumberAndBusinessUnit_BusinessUnitId(
            eq(VALID_ACCOUNT_REFERENCE), eq((short)1005))
        ).thenReturn(Optional.of(defendantAccount));

        TransformedInterfaceFileData transformedInterfaceFileData = service.process(
            interfaceFileEntity,
            interfaceFileCommonDataExtract,
            businessUnit);

        verify(defendantAccountRepository, times(5))
            .findByAccountNumberAndBusinessUnit_BusinessUnitId(VALID_ACCOUNT_REFERENCE, (short)1005);
        verify(aprRepository, never()).findByAprTextAndBusinessUnitCode(any(), any());

        assertThat(transformedInterfaceFileData.getTransactions()).hasSize(5);
        assertThat(transformedInterfaceFileData.getTotalAmount()).isEqualTo(2500L);

        for (int i = 0; i < transformedInterfaceFileData.getTransactions().size(); i++) {
            Transaction transaction = transformedInterfaceFileData.getTransactions().get(i);
            assertThat(transaction.getAmount()).isEqualTo(500L);
            assertThat(transaction.getTransactionCode()).isIn("00", "68", "99");
            assertThat(transaction.getOriginatorDetails().getAccountReference()).isEqualTo(VALID_ACCOUNT_REFERENCE);
        }
    }

    @Test
    @DisplayName("A CASH file is received and and invalid transaction codes are ignored")
    void onlyCashAndChequeTransactionsAreProcessed() {
        var businessUnit = withBusinessUnit();
        var interfaceFileEntity = withInterfaceFileEntity();
        var interfaceFileCommonDataExtract = withInterfaceFileCommonDataExtract(List.of(
            withTransaction("00", "", VALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("15", "", VALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("68", "", VALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("44", "", VALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("55", "", VALID_ACCOUNT_REFERENCE, 500L)
        ));

        when(defendantAccountRepository.findByAccountNumberAndBusinessUnit_BusinessUnitId(
            eq(VALID_ACCOUNT_REFERENCE), eq((short)1005))
        ).thenReturn(Optional.of(defendantAccount));

        TransformedInterfaceFileData transformedInterfaceFileData = service.process(
            interfaceFileEntity,
            interfaceFileCommonDataExtract,
            businessUnit);

        verify(defendantAccountRepository, times(3))
            .findByAccountNumberAndBusinessUnit_BusinessUnitId(VALID_ACCOUNT_REFERENCE, (short)1005);
        verify(aprRepository, never()).findByAprTextAndBusinessUnitCode(any(), any());

        assertThat(transformedInterfaceFileData.getTransactions()).hasSize(3);
        assertThat(transformedInterfaceFileData.getTotalAmount()).isEqualTo(1500L);

        for (int i = 0; i < transformedInterfaceFileData.getTransactions().size(); i++) {
            Transaction transaction = transformedInterfaceFileData.getTransactions().get(i);
            assertThat(transaction.getAmount()).isEqualTo(500L);
            assertThat(transaction.getTransactionCode()).isIn("00", "68", "99");
            assertThat(transaction.getOriginatorDetails().getAccountReference()).isEqualTo(VALID_ACCOUNT_REFERENCE);
        }
    }

    @Test
    @DisplayName("A CASH file is received and all refs map but must be extracted from original reference")
    void onlyCashTransactionsDontMapDirectlyToAccount() {
        var businessUnit = withBusinessUnit();
        var interfaceFileEntity = withInterfaceFileEntity();
        var interfaceFileCommonDataExtract = withInterfaceFileCommonDataExtract(List.of(
            withTransaction("99", "", "pfx" + VALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("99", "", VALID_ACCOUNT_REFERENCE + "sfx", 500L),
            withTransaction("99", "", "pfx" + VALID_ACCOUNT_REFERENCE + "sfx", 500L),
            withTransaction("99", "", "pfx132" + VALID_ACCOUNT_REFERENCE + "432sfx", 500L),
            withTransaction("99", "", "pfx3" + VALID_ACCOUNT_REFERENCE + "sfx", 500L),
            withTransaction("99", "", "pfx" + VALID_ACCOUNT_REFERENCE + "2sfx", 500L),
            withTransaction("99", "", "123" + VALID_ACCOUNT_REFERENCE + "456", 500L),
            withTransaction("99", "", "$%^" + VALID_ACCOUNT_REFERENCE + "£$%", 500L)
        ));

        when(defendantAccountRepository.findByAccountNumberAndBusinessUnit_BusinessUnitId(
            eq(VALID_ACCOUNT_REFERENCE), eq((short)1005))
        ).thenReturn(Optional.of(defendantAccount));

        TransformedInterfaceFileData transformedInterfaceFileData = service.process(
            interfaceFileEntity,
            interfaceFileCommonDataExtract,
            businessUnit);

        verify(defendantAccountRepository, times(8))
            .findByAccountNumberAndBusinessUnit_BusinessUnitId(VALID_ACCOUNT_REFERENCE, (short)1005);
        verify(aprRepository, never()).findByAprTextAndBusinessUnitCode(any(), any());

        assertThat(transformedInterfaceFileData.getTransactions()).hasSize(8);
        assertThat(transformedInterfaceFileData.getTotalAmount()).isEqualTo(4000L);

        for (int i = 0; i < transformedInterfaceFileData.getTransactions().size(); i++) {
            Transaction transaction = transformedInterfaceFileData.getTransactions().get(i);
            assertThat(transaction.getAmount()).isEqualTo(500L);
            assertThat(transaction.getTransactionCode()).isEqualTo("99");
            assertThat(transaction.getOriginatorDetails().getAccountReference()).isEqualTo(VALID_ACCOUNT_REFERENCE);
        }
    }

    @Test
    @DisplayName("A CASH file is received and account refs map to an existing opal account through an APR")
    void onlyCashTransactionsWithAPR() {
        var businessUnit = withBusinessUnit();
        var interfaceFileEntity = withInterfaceFileEntity();
        var interfaceFileCommonDataExtract = withInterfaceFileCommonDataExtract(List.of(
            withTransaction("99", "", VALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("99", "", VALID_ACCOUNT_REFERENCE, 500L)
        ));

        when(defendantAccountRepository.findByAccountNumberAndBusinessUnit_BusinessUnitId(
            eq(VALID_ACCOUNT_REFERENCE), eq((short)1005))
        ).thenReturn(Optional.empty());
        when(aprRepository.findByAprTextAndBusinessUnitCode(eq(VALID_ACCOUNT_REFERENCE), eq("06")))
            .thenReturn(Optional.of(alternatePaymentReference));
        when(alternatePaymentReference.getDefendantAccount()).thenReturn(defendantAccount);
        when(defendantAccount.getBusinessUnit()).thenReturn(mockBusinessUnit);
        when(mockBusinessUnit.getBusinessUnitId()).thenReturn((short)1005);

        TransformedInterfaceFileData transformedInterfaceFileData = service.process(
            interfaceFileEntity,
            interfaceFileCommonDataExtract,
            businessUnit);

        verify(defendantAccountRepository, times(4))
            .findByAccountNumberAndBusinessUnit_BusinessUnitId(VALID_ACCOUNT_REFERENCE, (short)1005);
        verify(aprRepository, times(2))
            .findByAprTextAndBusinessUnitCode(VALID_ACCOUNT_REFERENCE, "06");

        assertThat(transformedInterfaceFileData.getTransactions()).hasSize(2);
        assertThat(transformedInterfaceFileData.getTotalAmount()).isEqualTo(1000L);

        for (int i = 0; i < transformedInterfaceFileData.getTransactions().size(); i++) {
            Transaction transaction = transformedInterfaceFileData.getTransactions().get(i);
            assertThat(transaction.getAmount()).isEqualTo(500L);
            assertThat(transaction.getTransactionCode()).isEqualTo("99");
            assertThat(transaction.getOriginatorDetails().getAccountReference()).isEqualTo("0");
        }
    }

    @Test
    @DisplayName("A CASH file is received and originator name map to an existing opal account through an APR")
    void onlyCashTransactionsWithAPRFromOriginatorName() {
        var businessUnit = withBusinessUnit();
        var interfaceFileEntity = withInterfaceFileEntity();
        var interfaceFileCommonDataExtract = withInterfaceFileCommonDataExtract(List.of(
            withTransaction("99", VALID_ACCOUNT_REFERENCE, INVALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("99", VALID_ACCOUNT_REFERENCE, INVALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("99", VALID_ACCOUNT_REFERENCE, VALID_ACCOUNT_REFERENCE_2, 500L)
        ));

        when(defendantAccountRepository.findByAccountNumberAndBusinessUnit_BusinessUnitId(
            eq(VALID_ACCOUNT_REFERENCE_2), eq((short)1005))
        ).thenReturn(Optional.empty());
        when(aprRepository.findByAprTextAndBusinessUnitCode(eq(INVALID_ACCOUNT_REFERENCE), eq("06")))
            .thenReturn(Optional.empty());
        when(aprRepository.findByAprTextAndBusinessUnitCode(eq(VALID_ACCOUNT_REFERENCE), eq("06")))
            .thenReturn(Optional.of(alternatePaymentReference));
        when(aprRepository.findByAprTextAndBusinessUnitCode(eq(VALID_ACCOUNT_REFERENCE_2), eq("06")))
            .thenReturn(Optional.empty());
        when(alternatePaymentReference.getDefendantAccount()).thenReturn(defendantAccount);
        when(defendantAccount.getBusinessUnit()).thenReturn(mockBusinessUnit);
        when(mockBusinessUnit.getBusinessUnitId()).thenReturn((short)1005);

        TransformedInterfaceFileData transformedInterfaceFileData = service.process(
            interfaceFileEntity,
            interfaceFileCommonDataExtract,
            businessUnit);

        verify(defendantAccountRepository, times(2))
            .findByAccountNumberAndBusinessUnit_BusinessUnitId(VALID_ACCOUNT_REFERENCE_2, (short)1005);
        verify(aprRepository, times(3))
            .findByAprTextAndBusinessUnitCode(VALID_ACCOUNT_REFERENCE, "06");

        assertThat(transformedInterfaceFileData.getTransactions()).hasSize(3);
        assertThat(transformedInterfaceFileData.getTotalAmount()).isEqualTo(1500L);

        for (int i = 0; i < transformedInterfaceFileData.getTransactions().size(); i++) {
            Transaction transaction = transformedInterfaceFileData.getTransactions().get(i);
            assertThat(transaction.getAmount()).isEqualTo(500L);
            assertThat(transaction.getTransactionCode()).isEqualTo("99");
            assertThat(transaction.getOriginatorDetails().getAccountReference()).isEqualTo("0");
        }
    }

    @Test
    @DisplayName("A CASH file is received and no transactions map to an existing opal account")
    void noAccountReferencesOrAPRsMapToAnExistingOpalAccount() {
        var businessUnit = withBusinessUnit();
        var interfaceFileEntity = withInterfaceFileEntity();
        var interfaceFileCommonDataExtract = withInterfaceFileCommonDataExtract(List.of(
            withTransaction("99", INVALID_ACCOUNT_REFERENCE, "", 500L),
            withTransaction("99", "", INVALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("99", "", "pfx" + INVALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("99", "", "pfx" + INVALID_ACCOUNT_REFERENCE + "sfx", 500L),
            withTransaction("99", "", INVALID_ACCOUNT_REFERENCE + "sfx", 500L)
        ));

        when(aprRepository.findByAprTextAndBusinessUnitCode(anyString(), anyString()))
            .thenReturn(Optional.empty());

        TransformedInterfaceFileData transformedInterfaceFileData = service.process(
            interfaceFileEntity,
            interfaceFileCommonDataExtract,
            businessUnit);

        verify(defendantAccountRepository, never())
            .findByAccountNumberAndBusinessUnit_BusinessUnitId(any(), any());
        verify(aprRepository, times(5))
            .findByAprTextAndBusinessUnitCode(contains(INVALID_ACCOUNT_REFERENCE), eq("06"));

        assertThat(transformedInterfaceFileData.getTransactions()).hasSize(0);
        assertThat(transformedInterfaceFileData.getTotalAmount()).isEqualTo(0);
    }

    @Test
    @DisplayName("A CASH file is received, and the BU of an apr is differs to the expected")
    void onlyCashTransactionsWithAPRAndDifferentBU() {
        var businessUnit = withBusinessUnit();
        var interfaceFileEntity = withInterfaceFileEntity();
        var interfaceFileCommonDataExtract = withInterfaceFileCommonDataExtract(List.of(
            withTransaction("99", "", VALID_ACCOUNT_REFERENCE, 500L),
            withTransaction("99", "", VALID_ACCOUNT_REFERENCE, 500L)
        ));

        when(defendantAccountRepository.findByAccountNumberAndBusinessUnit_BusinessUnitId(
            eq(VALID_ACCOUNT_REFERENCE), eq((short)1005))
        ).thenReturn(Optional.empty());
        when(aprRepository.findByAprTextAndBusinessUnitCode(eq(VALID_ACCOUNT_REFERENCE), eq("06")))
            .thenReturn(Optional.of(alternatePaymentReference));
        when(alternatePaymentReference.getDefendantAccount()).thenReturn(defendantAccount);
        when(defendantAccount.getBusinessUnit()).thenReturn(mockBusinessUnit);
        when(mockBusinessUnit.getBusinessUnitId()).thenReturn((short)0);

        TransformedInterfaceFileData transformedInterfaceFileData = service.process(
            interfaceFileEntity,
            interfaceFileCommonDataExtract,
            businessUnit);

        verify(defendantAccountRepository, times(4))
            .findByAccountNumberAndBusinessUnit_BusinessUnitId(VALID_ACCOUNT_REFERENCE, (short)1005);
        verify(aprRepository, times(2))
            .findByAprTextAndBusinessUnitCode(VALID_ACCOUNT_REFERENCE, "06");

        assertThat(transformedInterfaceFileData.getTransactions()).hasSize(2);
        assertThat(transformedInterfaceFileData.getTotalAmount()).isEqualTo(1000L);

        assertThat(transformedInterfaceFileData.getDestinationDetails().getBankDetails().getSortCode()).isNull();
        assertThat(transformedInterfaceFileData.getDestinationDetails().getBankDetails().getAccountNumber()).isNull();
    }

}
