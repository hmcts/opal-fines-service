package uk.gov.hmcts.opal.mapper.till;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ValueMapping;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.DestinationType;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.PaymentMethod;
import uk.gov.hmcts.opal.entity.PaymentReceivedFrom;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.TransferSource;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.TillsCreateAdditionalInformation;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentIn;

@Mapper(componentModel = "spring")
public interface CreateTillMapper {

    @ValueMapping(source = "NOTES_COINS", target = "NC")
    @ValueMapping(source = "CHEQUE", target = "CQ")
    @ValueMapping(source = "CREDITOR_TRANSFER", target = "CT")
    @ValueMapping(source = "POSTAL_ORDER", target = "PO")
    PaymentMethod toPaymentMethod(TillsCreatePaymentDetails.MethodEnum method);

    @ValueMapping(source = "FINES", target = "F")
    @ValueMapping(source = "SUSPENSE", target = "S")
    @ValueMapping(source = "COURT_FEE", target = "C")
    DestinationType toDestinationType(TillsCreatePaymentDetails.DestinationTypeEnum destinationType);

    PaymentReceivedFrom toPaymentReceivedFrom(
        TillsCreatePaymentDetails.AdditionalInformationEnum additionalInformation);

    TransferSource toTransferSource(TillsCreateAdditionalInformation.TransferSourceEnum transferSource);

    @Mapping(target = "tillId", ignore = true)
    @Mapping(target = "status", constant = "Created")
    @Mapping(target = "autoPayment", constant = "false")
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "interfaceFile", ignore = true)
    @Mapping(target = "paymentInEntities", ignore = true)
    TillEntity toTillEntity(BusinessUnitEntity businessUnit, String ownedBy, String ownedByName, Short tillNumber,
                            BigDecimal totalAmount, Short paymentsCount, LocalDateTime createdDate);

    @Mapping(target = "paymentInId", ignore = true)
    @Mapping(target = "tillEntity", source = "till")
    @Mapping(target = "paymentAmount", source = "requestPayment.paymentDetails.amount")
    @Mapping(target = "paymentMethod", source = "requestPayment.paymentDetails.method")
    @Mapping(target = "destinationType", source = "requestPayment.paymentDetails.destinationType")
    @Mapping(target = "allocationType", source = "requestPayment.paymentDetails.allocationType")
    @Mapping(target = "thirdPartyPayerName", source = "requestPayment.paymentDetails.thirdPartyPayerName")
    @Mapping(target = "receipt", constant = "false")
    @Mapping(target = "allocated", constant = "false")
    @Mapping(target = "autoPayment", constant = "false")
    PaymentInEntity toPaymentIn(TillsCreatePaymentIn requestPayment, TillEntity till, LocalDateTime paymentDate,
                                String additionalInformation, AssociatedRecordType associatedRecordType,
                                String associatedRecordId);
}
