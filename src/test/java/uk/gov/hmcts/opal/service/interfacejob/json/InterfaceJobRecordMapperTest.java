package uk.gov.hmcts.opal.service.interfacejob.json;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.opal.service.interfacejob.json.filehandler.BankDetails;
import uk.gov.hmcts.opal.service.interfacejob.json.filehandler.DestinationDetails;
import uk.gov.hmcts.opal.service.interfacejob.json.filehandler.InterfaceFileCommonDataExtract;
import uk.gov.hmcts.opal.service.interfacejob.json.filehandler.OriginatorDetails;
import uk.gov.hmcts.opal.service.interfacejob.json.filehandler.Transaction;
import uk.gov.hmcts.opal.service.interfacejob.json.records.InterfaceJobRecord;

public class InterfaceJobRecordMapperTest {

    private InterfaceJobRecordMapper mapper = Mappers.getMapper(InterfaceJobRecordMapper.class);

    @Test
    void mapToRecords_mapsAllFields() {
        // Arrange
        InterfaceFileCommonDataExtract extract = createExtract();

        // Act
        InterfaceJobRecord[] records = mapper.mapToRecords(extract);

        // Assert
        assertThat(records).hasSize(2);

        assertThat(records[0].getDestinationAccountType()).isEqualTo("SAVINGS");
        assertThat(records[0].getDestinationBankAccountNumber()).isEqualTo("123098765");
        assertThat(records[0].getDestinationBeneficiaryName()).isEqualTo("John Beneficiary");
        assertThat(records[0].getDestinationSortCode()).isEqualTo("20-40-22");
        assertThat(records[0].getAmountPence()).isEqualTo(15000L);
        assertThat(records[0].getTransactionCode()).isEqualTo("01");
        assertThat(records[0].getOriginatorName()).isEqualTo("Sarah Originator");
        assertThat(records[0].getOriginatorReference()).isEqualTo("orig1");
        assertThat(records[0].getOriginatorSortCode()).isEqualTo("11-11-11");
        assertThat(records[0].getOriginatorBankAccountNumber()).isEqualTo("11111111");

        assertThat(records[1].getDestinationAccountType()).isEqualTo("SAVINGS");
        assertThat(records[1].getDestinationBankAccountNumber()).isEqualTo("123098765");
        assertThat(records[1].getDestinationBeneficiaryName()).isEqualTo("John Beneficiary");
        assertThat(records[1].getDestinationSortCode()).isEqualTo("20-40-22");
        assertThat(records[1].getAmountPence()).isEqualTo(2500L);
        assertThat(records[1].getTransactionCode()).isEqualTo("01");
        assertThat(records[1].getOriginatorName()).isEqualTo("Penny Originator");
        assertThat(records[1].getOriginatorReference()).isEqualTo("orig2");
        assertThat(records[1].getOriginatorSortCode()).isEqualTo("22-22-22");
        assertThat(records[1].getOriginatorBankAccountNumber()).isEqualTo("22222222");
    }

    private InterfaceFileCommonDataExtract createExtract() {
        BankDetails destinationBankDetails = new BankDetails("123098765",
            "20-40-22",
            "John Beneficiary",
            "SAVINGS");

        return new InterfaceFileCommonDataExtract("test.xml",
            new DestinationDetails(destinationBankDetails),
            "CHEQUE",
            createTranactions(),
            "DWP1");
    }

    private static List<Transaction> createTranactions() {
        // Transaction 1 originator details
        BankDetails orig1BankDetails = new BankDetails("11111111",
            "11-11-11",
            "S Originator",
            "CURRENT");
        OriginatorDetails orig1Details = new OriginatorDetails("Sarah Originator", "orig1", orig1BankDetails);

        // Transaction 2 originator details
        BankDetails orig2BankDetails = new BankDetails("22222222",
            "22-22-22",
            "P Originator",
            "CURRENT");
        OriginatorDetails orig2Details = new OriginatorDetails("Penny Originator", "orig2", orig2BankDetails);

        return List.of(
            new Transaction("01", orig1Details, 15000L, "20260101"),
            new Transaction("01", orig2Details, 2500L, "20260202")
        );
    }
}
