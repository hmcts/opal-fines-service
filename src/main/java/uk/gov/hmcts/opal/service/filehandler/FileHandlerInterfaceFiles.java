package uk.gov.hmcts.opal.service.filehandler;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.AddInterfaceFileRequestMetadata;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.GetInterfaceFiles200Response;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileObject;

public interface FileHandlerInterfaceFiles {

    GetInterfaceFiles200Response getInterfaceFiles(SystemUserEnum systemUser, GetInterfaceFilesParams params);

    InterfaceFileObject getInterfaceFile(SystemUserEnum systemUser, Long interfaceFileId);

    Resource getInterfaceFileContent(SystemUserEnum systemUser, Long interfaceFileId);

    InterfaceFileObject addInterfaceFile(
        SystemUserEnum systemUser, MultipartFile file, AddInterfaceFileRequestMetadata metadata);
}
