package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.legacy.CreditorAccountPaymentDetailsLegacy;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountResponseMinorCreditorPayment;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CreditorAccountPaymentDetailsLegacyMapper {

    MinorCreditorAccountResponseMinorCreditorPayment toOpal(CreditorAccountPaymentDetailsLegacy payment);
}
