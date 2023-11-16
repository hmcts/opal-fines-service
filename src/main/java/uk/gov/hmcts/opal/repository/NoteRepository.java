package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.NoteEntity;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, Long> {



}
