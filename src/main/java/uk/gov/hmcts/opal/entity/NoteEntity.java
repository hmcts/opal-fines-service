package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.entity.converter.AssociatedRecordTypeConverter;
import uk.gov.hmcts.opal.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "note_id_seq_generator")
    @SequenceGenerator(name = "note_id_seq_generator", sequenceName = "note_id_seq", allocationSize = 1)
    @Column(name = "note_id")
    private Long noteId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "note_type", length = 2, columnDefinition = "t_note_type_enum")
    private NoteType noteType;

    @Convert(converter = AssociatedRecordTypeConverter.class)
    @ColumnTransformer(write = "?::t_associated_record_type_enum")
    @Column(name = "associated_record_type", length = 30, columnDefinition = "t_associated_record_type_enum")
    private AssociatedRecordType associatedRecordType;

    @Column(name = "associated_record_id", length = 30)
    private String associatedRecordId;

    @Column(name = "note_text", columnDefinition = "TEXT")
    private String noteText;

    @Column(name = "posted_date")
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    private LocalDateTime postedDate;

    @Column(name = "posted_by", length = 20)
    private String businessUnitUserId;

    @Column(name = "posted_by_name", length = 100)
    private String postedByUsername;

}
