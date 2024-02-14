package uk.gov.hmcts.opal.controllers;

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
import uk.gov.hmcts.opal.dto.search.DocumentSearchDto;
import uk.gov.hmcts.opal.entity.DocumentEntity;
import uk.gov.hmcts.opal.service.DocumentServiceInterface;

import java.util.List;

import static uk.gov.hmcts.opal.util.ResponseUtil.buildResponse;


@RestController
@RequestMapping("/api/documents")
@Slf4j(topic = "DocumentController")
@Tag(name = "Document Controller")
public class DocumentController {

    private final DocumentServiceInterface documentService;

    public DocumentController(@Qualifier("documentServiceProxy") DocumentServiceInterface documentService) {
        this.documentService = documentService;
    }

    @GetMapping(value = "/{documentId}")
    @Operation(summary = "Returns the Document for the given documentId.")
    public ResponseEntity<DocumentEntity> getDocumentById(@PathVariable String documentId) {

        log.info(":GET:getDocumentById: documentId: {}", documentId);

        DocumentEntity response = documentService.getDocument(documentId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Documents based upon criteria in request body")
    public ResponseEntity<List<DocumentEntity>> postDocumentsSearch(@RequestBody DocumentSearchDto criteria) {
        log.info(":POST:postDocumentsSearch: query: \n{}", criteria);

        List<DocumentEntity> response = documentService.searchDocuments(criteria);

        return buildResponse(response);
    }


}
