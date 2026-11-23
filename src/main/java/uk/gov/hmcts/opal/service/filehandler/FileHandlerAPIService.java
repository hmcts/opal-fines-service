package uk.gov.hmcts.opal.service.filehandler;

import feign.FeignException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.opal.common.exception.DownstreamServiceUnavailableException;
import uk.gov.hmcts.opal.common.logging.SecurityEventLogging.ActionOutcome;
import uk.gov.hmcts.opal.common.logging.SecurityEventLogging.EventDataKey;
import uk.gov.hmcts.opal.common.logging.SecurityEventLogging.EventDataValue;
import uk.gov.hmcts.opal.common.logging.SecurityEventLogging.EventName;
import uk.gov.hmcts.opal.common.logging.SecurityEventLogging.EventOpType;
import uk.gov.hmcts.opal.common.logging.SecurityEventLoggingService;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.AddInterfaceFileRequestMetadata;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.GetInterfaceFiles200Response;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileObject;
import uk.gov.hmcts.opal.service.filehandler.clients.FileHandlerClient;

@Service
@Slf4j(topic = "opal.FileHandlerAPIService")
public class FileHandlerAPIService implements FileHandlerInterfaceFiles {

    private static final Map<String, Object> SECURITY_LOGGING_EVENT_DATA = Map.of(
        EventDataKey.AUTHENTICATING_SERVICE, EventDataValue.FILE_HANDLER_SERVICE_NAME,
        EventDataKey.TARGET_SERVICE, EventDataValue.FINES_SERVICE_NAME);

    private final FileHandlerClient client;
    private final FileHandlerSystemUserContext systemUserContext;
    private final SecurityEventLoggingService securityEventLoggingService;

    public FileHandlerAPIService(
        @Lazy FileHandlerClient client,
        FileHandlerSystemUserContext systemUserContext,
        SecurityEventLoggingService securityEventLoggingService
    ) {
        this.client = client;
        this.systemUserContext = systemUserContext;
        this.securityEventLoggingService = securityEventLoggingService;
    }

    @Override
    public GetInterfaceFiles200Response getInterfaceFiles(SystemUserEnum systemUser,
        GetInterfaceFilesParams params
    ) {
        try {
            GetInterfaceFiles200Response response = systemUserContext.executeAs(
                systemUser,
                () -> client.getInterfaceFiles(
                    params.getSource(),
                    params.getTarget(),
                    null,
                    params.getType(),
                    params.getDomain(),
                    params.getStatus(),
                    null,
                    null,
                    params.getFromDate(),
                    params.getToDate()
                ).getBody()
            );
            logSuccessfulAuthentication();
            return response;
        } catch (FeignException.NotFound exception) {
            throw exception;
        } catch (FeignException exception) {
            logIfUnsuccessfulAuthentication(exception);
            throw new DownstreamServiceUnavailableException(
                "Unable to retrieve interface file from file-handler service",
                exception
            );
        }
    }

    @Override
    public InterfaceFileObject getInterfaceFile(SystemUserEnum systemUser, Long interfaceFileId) {
        try {
            InterfaceFileObject interfaceFileObject = systemUserContext.executeAs(
                systemUser,
                () -> client.getInterfaceFile(interfaceFileId).getBody()
            );
            logSuccessfulAuthentication();
            return interfaceFileObject;
        } catch (FeignException.NotFound exception) {
            logSuccessfulAuthentication();
            throw exception;
        } catch (FeignException exception) {
            logIfUnsuccessfulAuthentication(exception);
            throw new DownstreamServiceUnavailableException(
                "Unable to retrieve interface file from file-handler-service",
                exception
            );
        }
    }

    @Override
    public Resource getInterfaceFileContent(SystemUserEnum systemUser, Long interfaceFileId) {
        try {
            Resource resource = systemUserContext.executeAs(
                systemUser,
                () -> client.getInterfaceFileContent(interfaceFileId).getBody()
            );
            logSuccessfulAuthentication();
            return resource;
        } catch (FeignException.NotFound exception) {
            logSuccessfulAuthentication();
            throw exception;
        } catch (FeignException exception) {
            logIfUnsuccessfulAuthentication(exception);
            throw new DownstreamServiceUnavailableException(
                "Unable to retrieve interface file content from file-handler service",
                exception
            );
        }
    }

    @Override
    public InterfaceFileObject addInterfaceFile(
        SystemUserEnum systemUser, MultipartFile file, AddInterfaceFileRequestMetadata metadata) {
        try {
            InterfaceFileObject interfaceFileObject = systemUserContext.executeAs(
                systemUser,
                () -> client.addInterfaceFile(file, metadata).getBody()
            );
            logSuccessfulAuthentication();
            return interfaceFileObject;
        } catch (FeignException exception) {
            logIfUnsuccessfulAuthentication(exception);
            throw new DownstreamServiceUnavailableException(
                "unable to add interface file",
                exception
            );
        }
    }

    private void logSuccessfulAuthentication() {
        logAuthentication(ActionOutcome.PASS);
    }

    private void logIfUnsuccessfulAuthentication(FeignException exception) {
        if (exception instanceof FeignException.Forbidden || exception instanceof FeignException.Unauthorized) {
            logAuthentication(ActionOutcome.FAIL);
        }
    }

    private void logAuthentication(String outcome) {
        securityEventLoggingService.logEvent(
            EventName.SERVICE_TO_SERVICE_AUTH,
            outcome,
            null,
            EventOpType.AUTHENTICATION,
            null, // the logging service populates this internally
            SECURITY_LOGGING_EVENT_DATA);
    }
}
