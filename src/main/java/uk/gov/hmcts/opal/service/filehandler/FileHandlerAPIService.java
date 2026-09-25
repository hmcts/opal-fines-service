package uk.gov.hmcts.opal.service.filehandler;

import feign.FeignException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.common.exception.DownstreamServiceUnavailableException;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.GetInterfaceFiles200Response;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileObject;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileTypeEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.StatusEnum;
import uk.gov.hmcts.opal.service.filehandler.clients.FileHandlerClient;

@Service
@Slf4j(topic = "opal.FileHandlerAPIService")
public class FileHandlerAPIService implements FileHandlerInterfaceFiles {

    private final FileHandlerClient client;
    private final FileHandlerSystemUserContext systemUserContext;

    public FileHandlerAPIService(
        @Lazy FileHandlerClient client,
        FileHandlerSystemUserContext systemUserContext
    ) {
        this.client = client;
        this.systemUserContext = systemUserContext;
    }

    @Override
    public GetInterfaceFiles200Response getInterfaceFiles(SystemUserEnum systemUser,
        @Nullable InterfaceFileEnum source,
        @Nullable InterfaceFileEnum target,
        @Nullable InterfaceFileTypeEnum type,
        @Nullable String domain,
        @Nullable StatusEnum status,
        @Nullable LocalDateTime fromDate,
        @Nullable LocalDateTime toDate
    ) {
        try {
            return systemUserContext.executeAs(
                systemUser,
                () -> client.getInterfaceFiles(
                    source, target, type, domain, status, fromDate, toDate).getBody()
            );
        } catch (FeignException.NotFound exception) {
            throw exception;
        } catch (FeignException exception) {
            throw new DownstreamServiceUnavailableException(
                "Unable to retrieve interface file from file-handler service",
                exception
            );
        }
    }

    @Override
    public InterfaceFileObject getInterfaceFile(SystemUserEnum systemUser, Long interfaceFileId) {
        try {
            return systemUserContext.executeAs(
                systemUser,
                () -> client.getInterfaceFile(interfaceFileId).getBody()
            );
        } catch (FeignException.NotFound exception) {
            throw exception;
        } catch (FeignException exception) {
            throw new DownstreamServiceUnavailableException(
                "Unable to retrieve interface file from file-handler-service",
                exception
            );
        }
    }

    @Override
    public Resource getInterfaceFileContent(SystemUserEnum systemUser, Long interfaceFileId) {
        try {
            return systemUserContext.executeAs(
                systemUser,
                () -> client.getInterfaceFileContent(interfaceFileId).getBody()
            );
        } catch (FeignException exception) {
            throw new DownstreamServiceUnavailableException(
                "Unable to retrieve interface file content from file-handler service",
                exception
            );
        }
    }
}
