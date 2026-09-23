package uk.gov.hmcts.opal.service.filehandler;

import java.time.LocalDateTime;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.GetInterfaceFiles200Response;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileTypeEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.StatusEnum;

public interface FileHandlerInterfaceFiles {

    GetInterfaceFiles200Response getInterfaceFiles(SystemUserEnum systemUser,
        @Nullable InterfaceFileEnum source,
        @Nullable InterfaceFileEnum target,
        @Nullable InterfaceFileTypeEnum type,
        @Nullable String domain,
        @Nullable StatusEnum status,
        @Nullable LocalDateTime fromDate,
        @Nullable LocalDateTime toDate);

    Resource getInterfaceFileContent(SystemUserEnum systemUser, Long interfaceFileId);

}
