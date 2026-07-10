package uk.gov.hmcts.opal.mapper.print;

import java.util.List;
import org.mapstruct.Mapper;
import uk.gov.hmcts.opal.dto.print.PrintJobDto;
import uk.gov.hmcts.opal.entity.print.PrintJobEntity;

@Mapper(componentModel = "spring")
public interface PrintJobMapper {

    PrintJobEntity toEntity(PrintJobDto printJobDto);

    List<PrintJobEntity> toEntities(List<PrintJobDto> printJobDtos);
}
