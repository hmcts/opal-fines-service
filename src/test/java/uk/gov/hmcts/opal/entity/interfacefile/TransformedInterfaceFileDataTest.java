package uk.gov.hmcts.opal.entity.interfacefile;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

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

        assertThat(transformed.fileName).isEqualTo(parent.fileName);
        assertThat(transformed.destinationDetails).isEqualTo(parent.destinationDetails);
        assertThat(transformed.dwpCourtCode).isEqualTo(parent.dwpCourtCode);
        assertThat(transformed.paymentType).isEqualTo(parent.paymentType);

        assertThat(transformed.transactions).isEmpty();
    }

    @Test
    void testAddTransactionAddsTransactionCorrectly() {
        InterfaceFileCommonDataExtract parent = withInterfaceFileCommonDataExtract();
        Transaction transaction = withTransaction();

        TransformedInterfaceFileData transformed = new TransformedInterfaceFileData(parent);
        assertThat(transformed.transactions).isEmpty();
        transformed.addTransaction(transaction);

        assertThat(transformed.transactions).isNotEmpty();
        assertThat(transformed.transactions.getFirst()).isEqualTo(transaction);

    }
}
