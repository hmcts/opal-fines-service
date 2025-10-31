package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.NoteEntity;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, Long>, JpaSpecificationExecutor<NoteEntity> {

    List<NoteEntity> findByAssociatedRecordIdAndNoteType(String associatedRecordId, String noteType);

    NoteEntity findTopByAssociatedRecordIdAndNoteTypeOrderByPostedDateDesc(
        String associatedRecordId, String noteType);

    void deleteByAssociatedRecordId(String defendantAccountId);


}
