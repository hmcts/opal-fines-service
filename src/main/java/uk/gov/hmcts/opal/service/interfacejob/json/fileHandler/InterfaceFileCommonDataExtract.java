package uk.gov.hmcts.opal.service.interfacejob.json.fileHandler;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InterfaceFileCommonDataExtract {
    private String fileName;
    private DestinationDetails destinationDetails;
    private String paymentType;
    private List<Transaction> transactions;
    private String dwpCourtCode;
}

