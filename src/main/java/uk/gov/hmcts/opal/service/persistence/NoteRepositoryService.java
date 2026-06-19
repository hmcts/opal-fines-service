package uk.gov.hmcts.opal.service.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.repository.NoteRepository;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.NoteRepositoryService")
public class NoteRepositoryService {

    private final NoteRepository noteRepository;

    @Transactional(readOnly = true)
    public List<NoteEntity> findAll(Specification<NoteEntity> specification) {
        return noteRepository.findAll(specification);
    }
}
