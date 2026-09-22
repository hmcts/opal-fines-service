package uk.gov.hmcts.opal.entity.interfacefile;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransformedInterfaceFileData extends InterfaceFileCommonDataExtract {
    private long totalAmount;

    public TransformedInterfaceFileData(InterfaceFileCommonDataExtract parent) {
        this.fileName = parent.getFileName();
        this.destinationDetails = parent.getDestinationDetails();
        this.paymentType = parent.getPaymentType();
        this.dwpCourtCode = parent.getDwpCourtCode();
        this.transactions = new ArrayList<>();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }
}

