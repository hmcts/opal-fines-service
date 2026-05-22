package uk.gov.hmcts.opal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorAccountHeaderEntity;
import uk.gov.hmcts.opal.mapper.common.BusinessUnitSummaryMapper;
import uk.gov.hmcts.opal.mapper.common.CreditorAccountTypeMapper;
import uk.gov.hmcts.opal.mapper.common.PartyMapper;

@Mapper(componentModel = "spring",
    uses = {
        BusinessUnitSummaryMapper.class,
        PartyMapper.class,
        CreditorAccountTypeMapper.class
    })
public interface MinorCreditorAccountHeaderEntityMapper {

    @Mapping(target = "creditor.accountNumber", source = "entity.creditorAccountNumber")
    @Mapping(target = "creditor.accountId", source = "entity.creditorAccountId")
    @Mapping(target = "creditor.accountType", source = "entity.creditorAccountType")
    @Mapping(target = "creditor.hasAssociatedDefendant", source = "entity.hasAssociatedDefendant")
    @Mapping(target = "version", source = "entity.versionNumber")
    @Mapping(target = "financials.awarded", source = "entity.awarded")
    @Mapping(target = "financials.paidOut", source = "entity.paidOut")
    @Mapping(target = "financials.awaitingPayout", source = "entity.awaitingPayment")
    @Mapping(target = "financials.outstanding", source = "entity.outstanding")
    @Mapping(target = "businessUnit", source = "entity")
    GetMinorCreditorAccountHeaderSummaryResponse toResponse(MinorCreditorAccountHeaderEntity entity, PartyEntity party);

}
