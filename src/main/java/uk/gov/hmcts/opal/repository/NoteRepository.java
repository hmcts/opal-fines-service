package uk.gov.hmcts.opal.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.NoteEntity;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, Long>, JpaSpecificationExecutor<NoteEntity> {

    List<NoteEntity> findByAssociatedRecordIdAndNoteType(String associatedRecordId, String noteType);

    NoteEntity findTopByAssociatedRecordIdAndNoteTypeOrderByPostedDateDesc(
        String associatedRecordId, String noteType);

    @Query(value = """
        SELECT *
        FROM notes
        WHERE associated_record_type = 'defendant_accounts'::t_associated_record_type_enum
          AND associated_record_id = :associatedRecordId
          AND note_type = 'AA'::t_note_type_enum
          AND posted_date >= COALESCE(CAST(:dateFrom AS date), '-infinity'::date)
          AND posted_date < COALESCE(CAST(:dateTo AS date) + INTERVAL '1 day', 'infinity'::timestamp)
        """, nativeQuery = true)
    List<NoteEntity> findDefendantAccountHistoryNotes(
        @Param("associatedRecordId") String associatedRecordId,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo);

    void deleteByAssociatedRecordId(String defendantAccountId);
}
