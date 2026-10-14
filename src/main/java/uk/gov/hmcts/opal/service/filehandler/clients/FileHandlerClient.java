package uk.gov.hmcts.opal.service.filehandler.clients;

import org.springframework.cloud.openfeign.FeignClient;
import uk.gov.hmcts.opal.filehandler.generated.InterfaceFile.client.InterfaceFilesApi;

@FeignClient(name = "fileHandlerClient",
    url = "${file-handler.service.url}",
    configuration = FileHandlerFeignClientConfiguration.class
)
public interface FileHandlerClient extends InterfaceFilesApi {
}
