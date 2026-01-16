package uk.gov.hmcts.opal.service.opal;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.DocumentEntity;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;
import uk.gov.hmcts.opal.entity.document.DocumentEntityStatus;
import uk.gov.hmcts.opal.repository.DocumentInstanceRepository;
import uk.gov.hmcts.opal.repository.DocumentRepository;
import uk.gov.hmcts.opal.service.iface.DocumentServiceInterface;
import uk.gov.hmcts.opal.util.DocumentIdConstants;
import uk.gov.hmcts.opal.util.RecordTypeConstants;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.DocumentService")
public class DocumentService implements DocumentServiceInterface {

    @Autowired
    private DocumentInstanceRepository documentInstanceRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private BusinessUnitService businessUnitService;

    @Override
    public void createDocumentInstance(Long defAccountId, short businessUnitId) {

        // Look up the document template
        DocumentEntity doc = documentRepository.findByDocumentId(DocumentIdConstants.TTPLET).orElseThrow();

        DocumentInstanceEntity documentInstanceEntity = DocumentInstanceEntity.builder()
            .document(doc)
            .businessUnit(businessUnitService.getBusinessUnit(businessUnitId))
            .generatedDate(LocalDateTime.now())
            .generatedBy("generatedby")
            .associatedRecordId(defAccountId)
            .associatedRecordType(RecordTypeConstants.DEFENDANT_ACCOUNTS)
            .status(DocumentEntityStatus.NEW)
            .build();

        documentInstanceRepository.save(documentInstanceEntity);

        log.debug(":createDocumentInstance: Created document instance for defAccountId={} BU={}",
            defAccountId, businessUnitId);
    }
}
