package uk.gov.hmcts.opal.service.filehandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.opal.common.exception.DownstreamServiceUnavailableException;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.AddInterfaceFileRequestMetadata;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.GetInterfaceFiles200Response;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileObject;
import uk.gov.hmcts.opal.service.filehandler.clients.FileHandlerClient;

@ExtendWith(MockitoExtension.class)
class FileHandlerAPIServiceTest {

    private static final Long INTERFACE_FILE_ID = 123L;

    @Mock
    private FileHandlerClient client;

    @Mock
    MultipartFile file;

    @Mock
    AddInterfaceFileRequestMetadata metadata;

    private FileHandlerSystemUserContext systemUserContext;
    private FileHandlerAPIService service;

    @BeforeEach
    void setUp() {
        systemUserContext = new FileHandlerSystemUserContext();
        service = new FileHandlerAPIService(client, systemUserContext);
    }

    @Test
    void getInterfaceFiles_returnsResponseBodyUsingSelectedSystemUser() {
        GetInterfaceFiles200Response expected = new GetInterfaceFiles200Response();

        when(client.getInterfaceFiles(null, null, null, null, null, null, null)).thenAnswer(invocation -> {
            assertThat(systemUserContext.getCurrentSystemUser()).contains(SystemUserEnum.OPAL_SYSTEM_USER);
            return ResponseEntity.ok(expected);
        });

        GetInterfaceFiles200Response actual = service.getInterfaceFiles(
            SystemUserEnum.OPAL_SYSTEM_USER, null, null, null, null, null, null, null);

        assertThat(actual).isSameAs(expected);
        assertThat(systemUserContext.getCurrentSystemUser()).isEmpty();
    }

    @Test
    void getInterfaceFile_returnsResponseBodyUsingSelectedSystemUser() {
        InterfaceFileObject expected = new InterfaceFileObject();
        when(client.getInterfaceFile(INTERFACE_FILE_ID)).thenAnswer(invocation -> {
            assertThat(systemUserContext.getCurrentSystemUser()).contains(SystemUserEnum.OPAL_SYSTEM_USER);
            return ResponseEntity.ok(expected);
        });

        InterfaceFileObject actual = service.getInterfaceFile(SystemUserEnum.OPAL_SYSTEM_USER, INTERFACE_FILE_ID);

        assertThat(actual).isSameAs(expected);
        assertThat(systemUserContext.getCurrentSystemUser()).isEmpty();
    }

    @Test
    void getInterfaceFileContent_returnsResponseBodyUsingSelectedSystemUser() {
        Resource expected = new ByteArrayResource("file-content".getBytes(StandardCharsets.UTF_8));
        when(client.getInterfaceFileContent(INTERFACE_FILE_ID)).thenAnswer(invocation -> {
            assertThat(systemUserContext.getCurrentSystemUser()).contains(SystemUserEnum.OPAL_SYSTEM_USER);
            return ResponseEntity.ok(expected);
        });

        Resource actual = service.getInterfaceFileContent(SystemUserEnum.OPAL_SYSTEM_USER, INTERFACE_FILE_ID);

        assertThat(actual).isSameAs(expected);
        assertThat(systemUserContext.getCurrentSystemUser()).isEmpty();
    }

    @Test
    void addInterfaceFile_returnsResponseBodyUsingSelectedSystemUser() {
        InterfaceFileObject expected = new InterfaceFileObject();
        when(client.addInterfaceFile(file, metadata)).thenAnswer(invocation -> {
            assertThat(systemUserContext.getCurrentSystemUser()).contains(SystemUserEnum.OPAL_SYSTEM_USER);
            return ResponseEntity.ok(expected);
        });

        InterfaceFileObject actual = service.addInterfaceFile(SystemUserEnum.OPAL_SYSTEM_USER, file, metadata);

        assertThat(actual).isSameAs(expected);
        assertThat(systemUserContext.getCurrentSystemUser()).isEmpty();
    }

    @Test
    void getInterfaceFiles_notFoundPropagatesFeignNotFoundAndClearsSelectedSystemUser() {
        FeignException.NotFound notFound = (FeignException.NotFound) feignException(404, "Not Found");
        when(client.getInterfaceFiles(null, null, null, null, null, null, null)).thenThrow(notFound);

        assertThatThrownBy(() -> service.getInterfaceFiles(
            SystemUserEnum.OPAL_SYSTEM_USER, null, null, null, null, null, null, null))
            .isSameAs(notFound);
        assertThat(systemUserContext.getCurrentSystemUser()).isEmpty();
    }

    @Test
    void getInterfaceFile_notFoundPropagatesFeignNotFoundAndClearsSelectedSystemUser() {
        FeignException.NotFound notFound = (FeignException.NotFound) feignException(404, "Not Found");
        when(client.getInterfaceFile(INTERFACE_FILE_ID)).thenThrow(notFound);

        assertThatThrownBy(() -> service.getInterfaceFile(SystemUserEnum.OPAL_SYSTEM_USER, INTERFACE_FILE_ID))
            .isSameAs(notFound);

        assertThat(systemUserContext.getCurrentSystemUser()).isEmpty();
    }

    @Test
    void getInterfaceFileContent_unexpectedResponseThrowsDownstreamServiceUnavailableException() {
        FeignException failure = feignException(500, "Internal Server Error");
        when(client.getInterfaceFileContent(INTERFACE_FILE_ID)).thenThrow(failure);

        assertThatThrownBy(() -> service.getInterfaceFileContent(SystemUserEnum.OPAL_SYSTEM_USER, INTERFACE_FILE_ID))
            .isInstanceOf(DownstreamServiceUnavailableException.class)
            .hasMessageContaining("file content")
            .hasCause(failure);
        assertThat(systemUserContext.getCurrentSystemUser()).isEmpty();
    }

    @Test
    void addInterfaceFile_unexepectedResponseThrowsDownstreamServiceUnavailableException() {
        FeignException failure = feignException(500, "Internal Server Error");
        when(client.addInterfaceFile(file, metadata)).thenThrow(failure);

        assertThatThrownBy(() -> service.addInterfaceFile(SystemUserEnum.OPAL_SYSTEM_USER, file, metadata))
            .isInstanceOf(DownstreamServiceUnavailableException.class)
            .hasCause(failure);

        assertThat(systemUserContext.getCurrentSystemUser()).isEmpty();
    }

    private FeignException feignException(int status, String reason) {
        Request request = Request.create(
            Request.HttpMethod.GET,
            "/interface-files/" + INTERFACE_FILE_ID + "/content",
            Map.of(),
            null,
            StandardCharsets.UTF_8,
            new RequestTemplate()
        );
        Response response = Response.builder()
            .status(status)
            .reason(reason)
            .request(request)
            .build();
        return FeignException.errorStatus("file-handler", response);
    }
}
