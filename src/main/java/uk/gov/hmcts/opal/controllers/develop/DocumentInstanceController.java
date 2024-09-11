package uk.gov.hmcts.opal.controllers.develop;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.search.DocumentInstanceSearchDto;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;
import uk.gov.hmcts.opal.service.DocumentInstanceServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/dev/document-instances")
@Slf4j(topic = "DocumentInstanceController")
@Tag(name = "DocumentInstance Controller")
public class DocumentInstanceController {

    private final DocumentInstanceServiceInterface documentInstanceService;

    public DocumentInstanceController(
        @Qualifier("documentInstanceServiceProxy") DocumentInstanceServiceInterface documentInstanceService) {
        this.documentInstanceService = documentInstanceService;
    }

    @GetMapping(value = "/{documentInstanceId}")
    @Operation(summary = "Returns the DocumentInstance for the given documentInstanceId.")
    public ResponseEntity<DocumentInstanceEntity> getDocumentInstanceById(@PathVariable Long documentInstanceId) {

        log.info(":GET:getDocumentInstanceById: documentInstanceId: {}", documentInstanceId);

        DocumentInstanceEntity response = documentInstanceService.getDocumentInstance(documentInstanceId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches DocumentInstances based upon criteria in request body")
    public ResponseEntity<List<DocumentInstanceEntity>> postDocumentInstancesSearch(
        @RequestBody DocumentInstanceSearchDto criteria) {
        log.info(":POST:postDocumentInstancesSearch: query: \n{}", criteria);

        List<DocumentInstanceEntity> response = documentInstanceService.searchDocumentInstances(criteria);

        return buildResponse(response);
    }


}
