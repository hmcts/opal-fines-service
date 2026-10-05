package uk.gov.hmcts.opal.service.opal.accountnumbertranslation.model;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransformedInterfaceFileData extends InterfaceFileCommonDataExtract {
    private long totalAmount;

    public TransformedInterfaceFileData(InterfaceFileCommonDataExtract parent) {
        super(
            parent.getFileName(),
            parent.getDestinationDetails(),
            parent.getPaymentType(),
            new ArrayList<>(),
            parent.getDwpCourtCode()
        );
    }

    @Override
    public void addTransaction(Transaction transaction) {
        super.addTransaction(transaction);
        totalAmount += transaction.getAmount();
    }
}

