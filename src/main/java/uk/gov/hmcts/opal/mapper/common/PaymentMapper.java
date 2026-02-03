package uk.gov.hmcts.opal.mapper.common;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.Payment;
import uk.gov.hmcts.opal.dto.legacy.common.LegacyPayment;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    default Payment toDto(LegacyPayment legacy) {
        if (legacy == null) {
            return null;
        }

        return Payment.builder()
            .isBacs(legacy.isBacs())
            .holdPayment(legacy.isHoldPayment())
            .build();
    }
}
