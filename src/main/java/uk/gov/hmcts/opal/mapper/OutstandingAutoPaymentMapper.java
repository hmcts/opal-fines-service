package uk.gov.hmcts.opal.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.opal.entity.businessunit.OutstandingAutoPaymentEntity;
import uk.gov.hmcts.opal.generated.model.BusinessUnitsOutstandingAutoPaymentItem;

@Mapper(componentModel = "spring")
public interface OutstandingAutoPaymentMapper {

    @Mapping(target = "fileCount", source = "filesToProcessCount")
    @Mapping(target = "tillCount", source = "tillsToAllocateCount")
    BusinessUnitsOutstandingAutoPaymentItem toItem(OutstandingAutoPaymentEntity entity);

    List<BusinessUnitsOutstandingAutoPaymentItem> toItems(List<OutstandingAutoPaymentEntity> entities);
}
