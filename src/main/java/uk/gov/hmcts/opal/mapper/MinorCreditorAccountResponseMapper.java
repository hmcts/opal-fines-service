package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;

@Mapper(
    componentModel = "spring",
    uses = {AddressDetailsCommonMapper.class, PartyDetailsCommonMapper.class, MinorCreditorPaymentMapper.class},
    builder = @Builder(disableBuilder = true)
)
public interface MinorCreditorAccountResponseMapper {

    @Mapping(target = "creditorAccountId", source = "account.creditorAccountId")
    @Mapping(target = "partyDetails", source = "party")
    @Mapping(target = "address", source = "party")
    @Mapping(target = "payment", source = "account")
    MinorCreditorAccountResponse toMinorCreditorAccountResponse(CreditorAccountEntity.Lite account, PartyEntity party);
}
