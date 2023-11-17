package uk.gov.hmcts.opal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
@Data
@Builder
public class NoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "note_id_seq_generator")
    @SequenceGenerator(name = "note_id_seq_generator", sequenceName = "note_id_seq", allocationSize = 1)
    private Long noteId;

    @Column(name = "note_type", length = 2)
    private String noteType;

    @Column(name = "associated_record_type", length = 30)
    private String associatedRecordType;

    @Column(name = "associated_record_id", length = 30)
    private String associatedRecordId;

    @Column(name = "note_text", columnDefinition = "TEXT")
    private String noteText;

    @Column(name = "posted_date")
    private LocalDateTime postedDate;

    @Column(name = "posted_by", length = 20)
    private String postedBy;

}
