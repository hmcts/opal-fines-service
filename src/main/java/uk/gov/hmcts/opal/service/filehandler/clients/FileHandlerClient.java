package uk.gov.hmcts.opal.service.filehandler.clients;

import java.time.LocalDateTime;
import org.jspecify.annotations.Nullable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.GetInterfaceFiles200Response;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.InterfaceFileTypeEnum;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.model.StatusEnum;
import uk.gov.hmcts.opal.service.filehandler.FileHandlerFeignClientConfiguration;

@FeignClient(name = "fileHandlerClient",
    url = "${OPAL_FILE_HANDLER_URL:http://localhost:4075}",
    configuration = FileHandlerFeignClientConfiguration.class
)
public interface FileHandlerClient {

    @GetMapping("/interface-files")
    ResponseEntity<GetInterfaceFiles200Response> getInterfaceFiles(
        @Nullable InterfaceFileEnum source,
        @Nullable InterfaceFileEnum target,
        @Nullable InterfaceFileTypeEnum type,
        @Nullable String domain,
        @Nullable StatusEnum status,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Nullable LocalDateTime fromDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Nullable LocalDateTime toDate
    );

    @GetMapping("/interface-files/{id}/content")
    ResponseEntity<Resource> getInterfaceFileContent(@PathVariable Long id);

}
