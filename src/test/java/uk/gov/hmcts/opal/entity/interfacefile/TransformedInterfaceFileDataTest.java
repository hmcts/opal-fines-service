package uk.gov.hmcts.opal.entity.interfacefile;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.BankDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.DestinationDetails;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.InterfaceFileCommonDataExtract;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.PaymentType;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.Transaction;
import uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model.TransformedInterfaceFileData;

public class TransformedInterfaceFileDataTest {

    private InterfaceFileCommonDataExtract withInterfaceFileCommonDataExtract() {
        return InterfaceFileCommonDataExtract.builder()
            .fileName("test-file.txt")
            .destinationDetails(DestinationDetails.builder()
                .bankDetails(BankDetails.builder()
                    .accountNumber("12341234")
                    .sortCode("01-01-01")
                    .name("name")
                    .type("personal")
                    .build())
                .build()
            )
            .paymentType(PaymentType.CASH)
            .transactions(List.of(
                Transaction.builder()
                    .transactionCode("11")
                    .amount(50L)
                    .build(),
                Transaction.builder()
                    .transactionCode("99")
                    .amount(75L)
                    .build()
            ))
            .dwpCourtCode("1234567dwp")
            .build();
    }

    private Transaction withTransaction() {
        return Transaction.builder()
            .transactionCode("99")
            .amount(75L)
            .build();
    }

    @Test
    void testConstructorCopiesParentCorrectly() {
        InterfaceFileCommonDataExtract parent = withInterfaceFileCommonDataExtract();

        TransformedInterfaceFileData transformed = new TransformedInterfaceFileData(parent);

        assertThat(transformed.getFileName()).isEqualTo(parent.getFileName());
        assertThat(transformed.getDestinationDetails()).isEqualTo(parent.getDestinationDetails());
        assertThat(transformed.getDwpCourtCode()).isEqualTo(parent.getDwpCourtCode());
        assertThat(transformed.getPaymentType()).isEqualTo(parent.getPaymentType());

        assertThat(transformed.getTransactions()).isEmpty();
    }

    @Test
    void testAddTransactionAddsTransactionCorrectly() {
        InterfaceFileCommonDataExtract parent = withInterfaceFileCommonDataExtract();
        Transaction transaction = withTransaction();

        TransformedInterfaceFileData transformed = new TransformedInterfaceFileData(parent);
        assertThat(transformed.getTransactions()).isEmpty();
        transformed.addTransaction(transaction);

        assertThat(transformed.getTransactions()).isNotEmpty();
        assertThat(transformed.getTransactions().getFirst()).isEqualTo(transaction);

    }
}
