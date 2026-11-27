package uk.gov.hmcts.opal.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.hmcts.opal.entity.InterfaceFileEntity;
import uk.gov.hmcts.opal.entity.InterfaceJobProcessedFileSummaryEntity;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsMessageGroup;
import uk.gov.hmcts.opal.generated.model.InterfaceJobsProcessedFileSummaryResponse;

@Mapper(componentModel = "spring")
public interface InterfaceJobProcessedFileSummaryMapper {

    @Mapping(target = "fileName", source = "summary.interfaceFileName")
    @Mapping(target = "businessUnitName", source = "businessUnitName")
    @Mapping(target = "interfaceMessages", source = "messages")
    @Mapping(target = "totalAmount", source = "summary.totalAmount", qualifiedByName = "penceToPounds")
    InterfaceJobsProcessedFileSummaryResponse toResponse(
        InterfaceJobProcessedFileSummaryEntity summary,
        String businessUnitName,
        List<InterfaceJobsMessageGroup> messages);

    @Mapping(target = "fileName", source = "file.fileName")
    @Mapping(target = "source", source = "file.source")
    @Mapping(target = "totalAmount", source = "file.totalAmount", qualifiedByName = "penceToPounds")
    @Mapping(target = "totalRecords", source = "file.recordCount")
    @Mapping(target = "totalErrors", source = "totalErrors")
    @Mapping(target = "businessUnitName", source = "businessUnitName")
    @Mapping(target = "interfaceMessages", source = "messages")
    InterfaceJobsProcessedFileSummaryResponse toResponse(
        InterfaceFileEntity file,
        String businessUnitName,
        Long totalErrors,
        List<InterfaceJobsMessageGroup> messages);

    @Named("penceToPounds")
    default BigDecimal penceToPounds(BigDecimal amount) {
        return amount == null ? null : amount.movePointLeft(2);
    }
}
