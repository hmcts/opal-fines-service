package uk.gov.hmcts.opal.documents;

public interface DocumentService {

    byte[] convertDataIntoPdf(DocumentTemplate.DocumentType documentType, Object data);

    DocumentTemplate findActiveByDocumentType(DocumentTemplate.DocumentType documentType);
}
