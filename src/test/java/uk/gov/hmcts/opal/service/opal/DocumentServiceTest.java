package uk.gov.hmcts.opal.service.opal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.DocumentEntity;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitFullEntity;
import uk.gov.hmcts.opal.entity.document.DocumentEntityStatus;
import uk.gov.hmcts.opal.repository.DocumentInstanceRepository;
import uk.gov.hmcts.opal.repository.DocumentRepository;
import uk.gov.hmcts.opal.util.RecordTypeConstants;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentInstanceRepository documentInstanceRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private BusinessUnitService businessUnitService;

    @Captor
    private ArgumentCaptor<DocumentInstanceEntity> instanceCaptor;

    @InjectMocks
    private DocumentService documentService;

    @Test
    void createDocumentInstance_success_savesDocumentInstanceWithExpectedValues() {
        long defAccountId = 123L;
        short businessUnitId = 5;
        DocumentEntity document = new DocumentEntity();
        BusinessUnitFullEntity buFullEntity = new BusinessUnitFullEntity();
        when(documentRepository.findByDocumentId(anyString())).thenReturn(Optional.of(document));
        when(businessUnitService.getBusinessUnit(businessUnitId)).thenReturn(buFullEntity);

        documentService.createDocumentInstance(defAccountId, businessUnitId);

        verify(documentInstanceRepository, times(1)).save(instanceCaptor.capture());
        DocumentInstanceEntity saved = instanceCaptor.getValue();

        assertSame(document, saved.getDocument());
        assertSame(buFullEntity, saved.getBusinessUnit());
        assertEquals(DocumentEntityStatus.NEW, saved.getStatus());
        assertEquals(defAccountId, saved.getAssociatedRecordId());
        assertEquals(RecordTypeConstants.DEFENDANT_ACCOUNTS, saved.getAssociatedRecordType());
    }

    @Test
    void createDocumentInstance_missingTemplate_throwsNoSuchElementException() {
        long defAccountId = 123L;
        short businessUnitId = 5;
        when(documentRepository.findByDocumentId(anyString())).thenReturn(Optional.empty());

        // Assert that NoSuchElementException is thrown
        assertThrows(java.util.NoSuchElementException.class, () ->
            documentService.createDocumentInstance(defAccountId, businessUnitId)
        );

        // Verify that save() was never called
        verify(documentInstanceRepository, never()).save(any());
    }
}
