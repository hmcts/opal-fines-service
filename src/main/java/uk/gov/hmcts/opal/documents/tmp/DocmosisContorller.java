package uk.gov.hmcts.opal.documents.tmp;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.documents.DocumentService;
import uk.gov.hmcts.opal.documents.DocumentTemplate.DocumentType;


@RestController
@RequestMapping("/docmosis")
@AllArgsConstructor
public class DocmosisContorller {

    private final DocumentService documentService;

    @PostMapping(value = "/{documentType}", consumes = "application/json", produces = "application/pdf")
    public ResponseEntity<byte[]> tmp(
        @PathVariable DocumentType documentType,
        @RequestBody HMRCDocumentDto hmrcDocumentDto) {
        return ResponseEntity.ok(documentService.convertDataIntoPdf(documentType, hmrcDocumentDto));
    }
}
