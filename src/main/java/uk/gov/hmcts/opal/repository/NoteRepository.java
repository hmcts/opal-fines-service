package uk.gov.hmcts.opal.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.entity.NoteEntity;
import uk.gov.hmcts.opal.repository.projection.MinorCreditorNoteHistoryProjection;

@Repository
public interface NoteRepository extends JpaRepository<NoteEntity, Long>, JpaSpecificationExecutor<NoteEntity> {

    List<NoteEntity> findByAssociatedRecordIdAndNoteType(String associatedRecordId, String noteType);

    NoteEntity findTopByAssociatedRecordIdAndNoteTypeOrderByPostedDateDesc(
        String associatedRecordId, String noteType);

    void deleteByAssociatedRecordId(String defendantAccountId);

    @Query(value = """
        SELECT n.note_id AS noteId,
               n.posted_date AS postedDate,
               n.posted_by AS postedBy,
               n.posted_by_name AS postedByName,
               n.note_text AS noteText
          FROM notes n
         WHERE n.associated_record_type = 'creditor_accounts'::public.t_associated_record_type_enum
           AND n.associated_record_id = :creditorAccountId
           AND n.note_type = 'AA'::public.t_note_type_enum
           AND n.posted_date IS NOT NULL
           AND n.posted_date >= :postedFromInclusive
           AND n.posted_date < :postedToExclusive
         ORDER BY n.posted_date DESC, n.note_id
        """, nativeQuery = true)
    List<MinorCreditorNoteHistoryProjection> findMinorCreditorHistory(
        @Param("creditorAccountId") String creditorAccountId,
        @Param("postedFromInclusive") LocalDateTime postedFromInclusive,
        @Param("postedToExclusive") LocalDateTime postedToExclusive);

}
