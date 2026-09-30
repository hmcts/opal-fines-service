package uk.gov.hmcts.opal.service.filehandler;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileTypeEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.StatusEnum;

@Builder
@Getter
public class GetInterfaceFilesParams {

    private final InterfaceFileEnum source;
    private final InterfaceFileEnum target;
    private final InterfaceFileTypeEnum type;
    private final String domain;
    private final StatusEnum status;
    private final LocalDateTime fromDate;
    private final LocalDateTime toDate;

}
