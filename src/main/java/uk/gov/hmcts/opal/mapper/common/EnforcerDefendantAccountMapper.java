package uk.gov.hmcts.opal.mapper.common;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.generated.model.EnforcerDefendantAccount;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EnforcerDefendantAccountMapper {

    EnforcerDefendantAccount toDto(EnforcerEntity entity);
}
