package uk.gov.hmcts.opal.service.opal;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.search.DocumentInstanceSearchDto;
import uk.gov.hmcts.opal.entity.DocumentInstanceEntity;
import uk.gov.hmcts.opal.repository.DocumentInstanceRepository;
import uk.gov.hmcts.opal.service.DocumentInstanceServiceInterface;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentInstanceService implements DocumentInstanceServiceInterface {

    private final DocumentInstanceRepository documentInstanceRepository;

    @Override
    public DocumentInstanceEntity getDocumentInstance(long documentInstanceId) {
        return documentInstanceRepository.getReferenceById(documentInstanceId);
    }

    @Override
    public List<DocumentInstanceEntity> searchDocumentInstances(DocumentInstanceSearchDto criteria) {
        return null;
    }

}
