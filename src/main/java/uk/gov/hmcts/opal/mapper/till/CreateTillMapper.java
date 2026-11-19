package uk.gov.hmcts.opal.mapper.till;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.AssociatedRecordType;
import uk.gov.hmcts.opal.entity.DestinationType;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.PaymentInEntity;
import uk.gov.hmcts.opal.entity.PaymentMethod;
import uk.gov.hmcts.opal.entity.TillEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.generated.model.TillsCreatePayerDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePayerType;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentDetails;
import uk.gov.hmcts.opal.generated.model.TillsCreatePaymentIn;

@Mapper(componentModel = "spring", imports = TillsCreatePayerType.class)
public interface CreateTillMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "organisation", expression = "java(type == TillsCreatePayerType.ORGANISATION)")
    @Mapping(target = "title", source = "payer.individual.title")
    @Mapping(target = "forenames", source = "payer.individual.forenames")
    @Mapping(target = "surname", source = "payer.individual.surname")
    @Mapping(target = "organisationName", source = "payer.organisation.organisationName")
    @Mapping(target = "addressLine1", source = "payer.addressLine1")
    @Mapping(target = "addressLine2", source = "payer.addressLine2")
    @Mapping(target = "addressLine3", source = "payer.addressLine3")
    @Mapping(target = "postcode", source = "payer.postcode")
    PartyEntity toParty(TillsCreatePayerDetails payer, TillsCreatePayerType type);

    @Mapping(target = "paymentInId", ignore = true)
    PaymentInEntity copyPayment(PaymentInEntity payment);

    PaymentMethod toPaymentMethod(TillsCreatePaymentDetails.MethodEnum method);

    DestinationType toDestinationType(TillsCreatePaymentDetails.DestinationTypeEnum destinationType);

    @Mapping(target = "tillId", ignore = true)
    @Mapping(target = "status", constant = "Created")
    @Mapping(target = "autoPayment", constant = "false")
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "interfaceFile", ignore = true)
    @Mapping(target = "paymentInEntities", ignore = true)
    TillEntity toTillEntity(BusinessUnitEntity businessUnit, String ownedBy, String ownedByName, Short tillNumber,
                            BigDecimal totalAmount, Short paymentsCount, LocalDateTime createdDate);

    @Mapping(target = "paymentInId", ignore = true)
    @Mapping(target = "additionalInformation", source = "additionalInformation")
    @Mapping(target = "tillEntity", source = "till")
    @Mapping(target = "paymentAmount", source = "requestPayment.paymentDetails.amount")
    @Mapping(target = "paymentMethod", source = "requestPayment.paymentDetails.method")
    @Mapping(target = "destinationType", source = "requestPayment.paymentDetails.destinationType")
    @Mapping(target = "allocationType", source = "requestPayment.paymentDetails.allocationType")
    @Mapping(target = "thirdPartyPayerName", source = "requestPayment.additionalInformation.thirdPartyPayerName")
    @Mapping(target = "receipt", ignore = true)
    @Mapping(target = "allocated", ignore = true)
    @Mapping(target = "autoPayment", constant = "false")
    PaymentInEntity toPaymentIn(TillsCreatePaymentIn requestPayment, TillEntity till, LocalDateTime paymentDate,
                                String additionalInformation, AssociatedRecordType associatedRecordType,
                                String associatedRecordId);
}
