package uk.gov.hmcts.opal.mapper.response;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.legacy.LegacyGetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.mapper.common.AddressMapper;
import uk.gov.hmcts.opal.mapper.common.DefendantMapper;
import uk.gov.hmcts.opal.mapper.common.PartyMapper;
import uk.gov.hmcts.opal.mapper.common.PaymentMapper;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {
        PartyMapper.class,
        AddressMapper.class,
        DefendantMapper.class,
        PaymentMapper.class
    }
)
public interface GetMinorCreditorAccountAtAGlanceResponseMapper {

    GetMinorCreditorAccountAtAGlanceResponse toDto(LegacyGetMinorCreditorAccountAtAGlanceResponse legacy);
}
