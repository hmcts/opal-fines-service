package uk.gov.hmcts.opal.mapper.legacy;

import java.math.BigInteger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.legacy.UpdateMinorCreditorAccountLegacyRequest;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        PartyDetailsCommonMapper.class,
        AddressDetailsCommonMapper.class,
        CreditorAccountPaymentDetailsCommonMapper.class
    }
)
public interface PatchMinorCreditorAccountRequestMapper {

    @Mappings({
        @Mapping(target = "creditorAccountId", source = "minorCreditorAccountId"),
        @Mapping(target = "accountVersion", source = "etag"),
        @Mapping(target = "partyDetails", source = "request.partyDetails"),
        @Mapping(target = "address", source = "request.address"),
        @Mapping(target = "payment", source = "request.payment")
    })
    UpdateMinorCreditorAccountLegacyRequest toLegacyRequest(
        Long minorCreditorAccountId,
        PatchMinorCreditorAccountRequest request,
        BigInteger etag,
        String businessUnitUserId,
        Short businessUnitId
    );
}
