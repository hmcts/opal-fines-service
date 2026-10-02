package uk.gov.hmcts.opal.service.interfacejob.json;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.service.interfacejob.json.fileHandler.InterfaceFileCommonDataExtract;
import uk.gov.hmcts.opal.service.interfacejob.json.fileHandler.Transaction;
import uk.gov.hmcts.opal.service.interfacejob.json.records.InterfaceJobRecord;

@Mapper(componentModel = "spring")
public interface InterfaceJobRecordMapper {

    @Mapping(target = "destinationSortCode", source = "extract.destinationDetails.bankDetails.sortCode")
    @Mapping(target = "destinationBankAccountNumber", source = "extract.destinationDetails.bankDetails.accountNumber")
    @Mapping(target = "destinationAccountType", source = "extract.destinationDetails.bankDetails.type")
    @Mapping(target = "destinationBeneficiaryName", source = "extract.destinationDetails.bankDetails.name")
    @Mapping(target = "transactionCode", source = "transaction.transactionCode")
    @Mapping(target = "originatorSortCode", source = "transaction.originatorDetails.bankDetails.sortCode")
    @Mapping(target = "originatorBankAccountNumber", source = "transaction.originatorDetails.bankDetails.accountNumber")
    @Mapping(target = "originatorName", source = "transaction.originatorDetails.name")
    @Mapping(target = "originatorReference", source = "transaction.originatorDetails.accountReference")
    @Mapping(target = "amountPence", source = "transaction.amount")
    InterfaceJobRecord mapToRecord(InterfaceFileCommonDataExtract extract, Transaction transaction);

    default InterfaceJobRecord[] mapToRecords(InterfaceFileCommonDataExtract extract) {
        return extract.getTransactions()
            .stream()
            .map(transaction -> mapToRecord(extract, transaction))
            .toArray(InterfaceJobRecord[]::new);
    }
}
