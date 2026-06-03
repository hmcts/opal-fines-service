package uk.gov.hmcts.opal.mapper.legacy;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.legacy.GetMinorCreditorAccountLegacyResponse;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true),
    uses = {
        PartyDetailsLegacyMapper.class,
        AddressDetailsLegacyMapper.class,
        CreditorAccountPaymentDetailsLegacyMapper.class
    }
)
public interface GetMinorCreditorAccountLegacyResponseMapper {

    @Mapping(target = "version", source = "accountVersion")
    MinorCreditorAccountResponse toOpal(GetMinorCreditorAccountLegacyResponse legacyResponse);
}
