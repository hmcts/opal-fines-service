package uk.gov.hmcts.opal.documents;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.common.exceptions.standard.InternalServerErrorException;
import uk.gov.hmcts.common.exceptions.standard.NotFoundException;
import uk.gov.hmcts.opal.documents.docmosis.DocmosisClient;
import uk.gov.hmcts.opal.documents.docmosis.DocmosisRenderDto;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentTemplateRepository documentTemplateRepository;
    private final DocmosisClient docmosisClient;
    private final String accessKey;

    public DocumentServiceImpl(
        DocumentTemplateRepository documentTemplateRepository,
        DocmosisClient docmosisClient,
        @Value("${opal.docmosis.token}") String accessKey
    ) {
        this.documentTemplateRepository = documentTemplateRepository;
        this.docmosisClient = docmosisClient;
        this.accessKey = accessKey;
    }

    @Override
    public byte[] convertDataIntoPdf(DocumentTemplate.DocumentType documentType, Object data) {
        return convertDataIntoPdf(
            findActiveByDocumentType(documentType), data
        );
    }

    public byte[] convertDataIntoPdf(DocumentTemplate documentTemplate, Object data) {
        if (!data.getClass().getName().equals(documentTemplate.getDataMappingClass())) {
            throw new InternalServerErrorException(
                "Data Class Mismatch",
                "Provided data class: " + data.getClass().getName()
                    + " does not match expected data class: " + documentTemplate.getDataMappingClass()
            );
        }
        DocmosisRenderDto docmosisRenderDto = DocmosisRenderDto.builder()
            .templateName(documentTemplate.getTemplateName())
            .outputName(documentTemplate.getDocumentType().name() + ".pdf")
            .accessKey(this.accessKey)
            .data(data)
            .build();
        return docmosisClient.render(docmosisRenderDto);
    }

    @Override
    public DocumentTemplate findActiveByDocumentType(DocumentTemplate.DocumentType documentType) {
        List<DocumentTemplate> documentTemplates =
            documentTemplateRepository.findActiveDocumentTemplateFromType(documentType);

        if (documentTemplates.size() == 1) {
            return documentTemplates.getFirst();
        }

        if (documentTemplates.isEmpty()) {
            throw new NotFoundException(
                "No Active Document Template found",
                "No active document template found for document type: " + documentType);
        }
        throw new InternalServerErrorException(
            "Multiple Active Document Templates found",
            "Multiple active document templates found for document type: " + documentType
                + ". Expected only one active template. Document template Ids: "
                + documentTemplates.stream().map(DocumentTemplate::getId).toList());

    }
}
