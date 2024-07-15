package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "standard_letters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "standardLetterId")
public class StandardLetterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "standard_letter_id_seq_generator")
    @SequenceGenerator(name = "standard_letter_id_seq_generator", sequenceName = "standard_letter_id_seq",
        allocationSize = 1)
    @Column(name = "standard_letter_id", nullable = false)
    private Long standardLetterId;

    @ManyToOne
    @JoinColumn(name = "business_unit_id", nullable = false)
    private BusinessUnitEntity businessUnit;

    @Column(name = "standard_letter_code", length = 10, nullable = false)
    private String standardLetterCode;

    @Column(name = "standard_letter_name", length = 50, nullable = false)
    private String standardLetterName;

    @Column(name = "associated_record_type", length = 30, nullable = false)
    private String associatedRecordType;

    @Column(name = "user_entries")
    private String userEntries;

    @Column(name = "document_body", nullable = false)
    private String documentBody;

}
