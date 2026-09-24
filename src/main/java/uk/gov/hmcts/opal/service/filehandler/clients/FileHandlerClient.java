package uk.gov.hmcts.opal.service.filehandler.clients;

import org.springframework.cloud.openfeign.FeignClient;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.client.InterfaceFilesApi;

@FeignClient(name = "fileHandlerClient",
    url = "${OPAL_FILE_HANDLER_URL:http://localhost:4075}",
    configuration = FileHandlerFeignClientConfiguration.class
)
public interface FileHandlerClient extends InterfaceFilesApi {
}
