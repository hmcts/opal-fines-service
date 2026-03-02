package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountResponseMinorCreditorPayment;

@Mapper(componentModel = "spring")
public interface MinorCreditorPaymentMapper {

    @Mapping(target = "accountName", source = "bankAccountName")
    @Mapping(target = "sortCode", source = "bankSortCode")
    @Mapping(target = "accountNumber", source = "bankAccountNumber")
    @Mapping(target = "accountReference", source = "bankAccountReference")
    @Mapping(target = "payByBacs", source = "payByBacs")
    @Mapping(target = "holdPayment", source = "holdPayout")
    MinorCreditorAccountResponseMinorCreditorPayment toMinorCreditorPayment(CreditorAccountEntity.Lite account);
}
